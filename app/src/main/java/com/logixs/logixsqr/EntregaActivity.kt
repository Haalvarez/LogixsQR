package com.logixs.logixsqr

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.logixs.logixsqr.Utils.GPSUtils
import com.vulkansoft.sporter.Dialogs
import com.vulkansoft.sporter.HttpRequest
import kotlinx.android.synthetic.main.activity_entrega.*
import java.io.*


class EntregaActivity : AppCompatActivity() {

    private lateinit var entrega: EntregasPaquetesModel
    private val PERMISSION_CODE_READ = 1001
    private val PERMISSION_ACCESS_FINE_LOCATION: Int = 100
    private val IMAGE_PICK_CODE = 100
    private var images: ArrayList<String> = ArrayList()
    private var image: String = ""
    val operacionActual = "entrega"
    var lat: String = "0"
    var lng: String = "0"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrega)

        configurarActivity()

        inicioGPS()
    }

    private fun inicioGPS() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            try {
                var gps = GPSUtils.getInstance()
                if (gps.latitude != null && gps.longitude != null) {
                    lat = gps.latitude
                    lng = gps.longitude
                }

            } catch (ie: IOException) {
                Dialogs.mostrarSnackbarLargo(
                    container,
                    getString(R.string.mensaje_error_permisos_ubicacion),
                    Color.RED
                )
            }
        } else {

            // Sino le indico que falta dar permisos
            Dialogs.mostrarSnackbarButton(
                container,
                getString(R.string.mensaje_error_permisos_ubicacion),
                getString(R.string.mensaje_aceptar),
                Color.YELLOW
            )

            // Pido los permisos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_ACCESS_FINE_LOCATION
            )
        }
    }

    private fun configurarActivity() {

        entrega = intent.getSerializableExtra("entrega") as EntregasPaquetesModel
        txv_id_envio.text = entrega.IdEnvio
        txv_direccion.text = entrega.AddressLine
        txv_telefono.text = entrega.telefonoRecibe

        img_llamar.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:" + entrega.telefonoRecibe)
            startActivity(dialIntent)
        }

        img_atras.setOnClickListener {
            finish()
        }

        img_estado2.setOnClickListener {
            finish()
        }

        img_enviar.setOnClickListener {

            // Si se trata de un paquete NOML valido que esten completos los datos del que recibe
            if (entrega.IdEnvio.startsWith("NOML", true) && spinner.selectedItem.toString() == ("Entregado")) {
                if (txt_dni_recibe.text.isEmpty() || txt_nombre_recibe.text.isEmpty()) {

                    Dialogs.mostrarSnackbarLargo(
                        container,
                        getString(R.string.mensaje_campos_recibe_incompletos),
                        Color.RED
                    )
                }
                else{
                    enviarABackend()
                }
            }
            else{
                enviarABackend()
            }

        }

        img_cargar_imagenes.setOnClickListener {
            cargarImagenes()
        }

        llenarSpinnerEntregado()
    }

    private fun enviarABackend() {

        mostrarProgressBar()

        val context = this

        val url =
            Configuracion.URL_LOGIXS + SharedPref.getPathUsuario(this) + "/envioflex/RecibirScanQR"

        val stringRequestBE = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { backendResponse ->

                // Si no hubo error en la solicitud proceso la respuesta
                procesarRespuestaBackend(backendResponse, entrega.IdEnvio)

                mostrarActivity()
            },
            Response.ErrorListener {

                Log.d(this::class.java.simpleName, "Error al enviar la información al BE")

                mostrarActivity()

                Dialogs.mostrarErrorVolley(this, container)
            }

        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["MensajeroId"] = SharedPref.getIdUsuario(context)!!
                params["EntregaOretiro"] = operacionActual
                params["Path"] = SharedPref.getPathUsuario(context)!!
                params["Scan"] = "-"
                params["IdML"] = entrega.IdEnvio
                params["Nickname"] = entrega.NicknameVendedor
                params["Sender_id"] = entrega.Sender_id
                params["recibeDNI"] = txt_dni_recibe.text.toString()
                params["RecibeNombre"] = txt_nombre_recibe.text.toString()
                params["lat"] = lat
                params["lng"] = lng
                params["obs"] = txt_observaciones.text.toString()
                params["EstadoEntrega"] = spinner.selectedItem.toString()

                // Una Foto
                params["Foto"] = if (images.isEmpty()) "" else images.first()

                // Multiples Fotos
                //for (i in 0..images.size-1) {
                //    params["Foto$i"] = images.get(i)
                //}
                return params
            }
        }

        Log.d(this::class.java.simpleName, "Enviando la información al BE")

        // Agrego la solicitud a la cola de solicitudes
        HttpRequest.getInstance(applicationContext).addToRequestQueue(
            stringRequestBE
        )

    }

    private fun procesarRespuestaBackend(backendResponse: String?, idEnvio: String) {

        Log.d(this::class.java.simpleName, "Procesando la información del BE")

        if (backendResponse != null) {

            try {

                val gson = Gson()

                // convierto la respuesta a la clase PostQrModel, que tiene el formato del json recibido
                val postQrModel = gson.fromJson(backendResponse, PostQrModel::class.java)

                // Si se registro el qr correctamente
                if (postQrModel.resultado == 0) {

                    img_estado2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.check))
                    reproducirSonido(R.raw.ok)
                    img_enviar.visibility = ImageView.GONE

                    Log.d(this::class.java.simpleName, "Respuesta del BE: OK")

                } else {

                    // resultado incorrecto
                    img_estado2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.close))
                    reproducirSonido(R.raw.error)

                    Dialogs.mostrarSnackbarLargo(
                        container,
                        postQrModel.mensaje,
                        Color.RED
                    )

                    Log.d(this::class.java.simpleName, "Respuesta del BE: Error")
                }

                img_estado2.visibility = ImageView.VISIBLE

            } catch (a: Exception) {

                Log.d(this::class.java.simpleName, "Error al procesar la información del BE")

                // Sino hubo un error al procesar la respuesta del backend muestro error
                Dialogs.mostrarSnackbarLargo(
                    container,
                    getString(R.string.mensaje_error_registro_qr),
                    Color.RED
                )
            }
        }
    }

    private fun reproducirSonido(sonido: Int) {
        val mp = MediaPlayer.create(this, sonido)
        mp.start()
    }


    private fun llenarSpinnerEntregado() {
        /*
        ArrayAdapter.createFromResource(
            this,
            R.array.estatoEntrega,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item)

            // Apply the adapter to the spinner
            spinner.adapter = adapter
            //adapter.notifyDataSetChanged()

            spinner.setSelection(0)
        }
         */
        mostrarProgressBar()

        val url =
            Configuracion.URL_LOGIXS + SharedPref.getPathUsuario(this) + "/envioflex/EstadosDeEntrega"

        // Genero la solicitud para obtener la info de ML
        val stringRequestML = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->

                // Si no hubo error en la solicitud proceso la respuesta
                procesarRespuestaEstadosEntrega(response)

                mostrarActivity()
            },
            Response.ErrorListener {

                Log.d(this::class.java.simpleName, "Error al obtener información de ML")

                mostrarActivity()

                Dialogs.mostrarErrorVolley(this, container)
            }
        )

        Log.d(this::class.java.simpleName, "Obtengo los estados de entrega")

        // Agrego la solicitud a la cola de solicitudes
        HttpRequest.getInstance(applicationContext).addToRequestQueue(stringRequestML)

    }

    private fun procesarRespuestaEstadosEntrega(response: String) {
        val array = Gson().fromJson(response, mutableListOf<String>().javaClass)
        val adapter = ArrayAdapter(this, android.R.layout.simple_selectable_list_item, array)
        spinner.adapter = adapter
        spinner.setSelection(0)

    }

    private fun cargarImagenes() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

                ActivityCompat.requestPermissions(
                    this,
                    permission,
                    PERMISSION_CODE_READ
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
            } else {
                seleccionarImagenes()
            }
        }
    }

    private fun seleccionarImagenes() {

        val intent = Intent(Intent.ACTION_PICK)

        // Multiples Fotos
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        intent.type = "image/*"
        startActivityForResult(
            intent,
            IMAGE_PICK_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            if (data?.clipData != null) {

                images.clear()

                // agrego las imagenes al array de imagenes a enviar al backend y al
                // label que tiene los nombres
                var count = data!!.clipData!!.itemCount
                for (i in 0 until count) {

                    var imageUri: Uri = data!!.clipData!!.getItemAt(i).uri
                    images.add(codificarBase64(imageUri))
                }

                // Unica Foto
                txv_imagenes.text = getString(R.string.lbl_imagen_selecciona)

                // Multiples Fotos
                // txv_imagenes.text = getString(R.string.lbl_imagenes_seleccionadas, images.size.toString())
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        when (requestCode) {

            PERMISSION_ACCESS_FINE_LOCATION -> {

                // Si la solicitud de permisos no fue cancelada por el usuario y los permisos fueron otorgados
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // actualizo la activity
                    this.recreate()
                } else {

                    // Sino, muestro el mensaje indicando que los permisos son necesario y vuelvo al listado
                    Dialogs.mostrarSnackbarLargo(
                        container,
                        getString(R.string.mensaje_error_permisos_ubicacion),
                        Color.YELLOW
                    )
                    finish()
                }
                return
            }


        }
    }

    fun mostrarProgressBar(){
        progress.visibility = ProgressBar.VISIBLE
        container.visibility = ConstraintLayout.INVISIBLE
    }

    fun mostrarActivity(){
        progress.visibility = ProgressBar.INVISIBLE
        container.visibility = ConstraintLayout.VISIBLE
    }

    fun codificarBase64(imageUri: Uri): String {

        var imageStream: InputStream? = null
        try {
            imageStream = this.contentResolver.openInputStream(imageUri)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        var yourSelectedImage = BitmapFactory.decodeStream(imageStream)

        val anchoFinal: Int  = 1024
        val scale: Float = anchoFinal.toFloat() / yourSelectedImage.width
        var altoFinal:  Int  = (yourSelectedImage.height * scale).toInt()
        yourSelectedImage = Bitmap.createScaledBitmap(yourSelectedImage, anchoFinal, altoFinal, false)

        val baos = ByteArrayOutputStream()
        yourSelectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        val imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)

        return imageEncoded

    }

}