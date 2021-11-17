package com.logixs.logixsqr.ui.qr_entrega

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.gson.Gson
import com.logixs.logixsqr.*
import com.logixs.logixsqr.Utils.GPSUtils.getInstance
import com.logixs.logixsqr.database.AppDatabase
import com.logixs.logixsqr.database.Envio
import com.logixs.logixsqr.ui.home.HomeFragment
import com.vulkansoft.sporter.Dialogs
import com.vulkansoft.sporter.HttpRequest
import kotlinx.android.synthetic.main.fragment_qr_entrega.*
import kotlinx.android.synthetic.main.fragment_qr_retiro.*
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap


class QrEntregaFragment : Fragment() {

    private val PERMISSION_CAMARA_REQUEST_CODE: Int = 100
    private val PERMISSION_ACCESS_FINE_LOCATION: Int = 101
    val operacionActual = "entrega"




    lateinit var barcodeDetector: BarcodeDetector
    lateinit var cameraSource: CameraSource
    lateinit var cameraView: SurfaceView
    lateinit var fechaHoraUltimoScan: Calendar
    lateinit var txvIdVendedor: TextView
    lateinit var txvNicknameVendedor: TextView
    lateinit var txvEstado: TextView
    lateinit var imgEstado: ImageView
    lateinit var txvContador: TextView
    lateinit var txtDniRecibe: EditText
    lateinit var txtNombreRecibe: EditText
    lateinit var imgReset: ImageView
    lateinit var txtObs: EditText
    //lateinit var EstadoEntregaText: TextView
    lateinit var lat:String
    lateinit var lng:String
    private lateinit var db: AppDatabase
    var solicitudPreviaFinalizada = true
    var ultimoIdEnvio = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        try {
            container?.removeAllViews()
        } catch (e: Exception) {
        }

        val root = inflater.inflate(R.layout.fragment_qr_entrega, container, false)

        txtObs=root.findViewById(R.id.txt_obsEntrega)
        txvIdVendedor = root.findViewById(R.id.txv_id_vendedor)
        txvNicknameVendedor = root.findViewById(R.id.txv_nickname_vendedor)
        txvEstado = root.findViewById(R.id.txv_estado)
        txtDniRecibe = root.findViewById(R.id.txt_dni_recibe)
        txtNombreRecibe = root.findViewById(R.id.txt_nombre_recibe)
        imgEstado = root.findViewById(R.id.img_estado)
        imgEstado.setOnClickListener {
            completarTxv(txvEstado, resources.getString(R.string.lbl_vacio), R.color.colorTexto)
            completarTxv(
                txvIdVendedor,
                resources.getString(R.string.lbl_vacio),
                R.color.colorTexto
            )
            completarTxv(
                txvNicknameVendedor,
                resources.getString(R.string.lbl_vacio),
                R.color.colorTexto
            )
            imgEstado.visibility = ImageView.INVISIBLE
        }

        imgReset = root.findViewById(R.id.img_reset)
        configurarReset()

        txvContador = root.findViewById(R.id.txv_contador)
        configurarContador()

        db = AppDatabase.getInstance(requireContext().applicationContext)


        val spinner: Spinner = root.findViewById(R.id.spinner)

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext().applicationContext,
            R.array.estatoEntrega,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_selectable_list_item)
            // Apply the adapter to the spinner

            spinner.adapter = adapter
           //adapter.notifyDataSetChanged()



        spinner.setSelection(0)

         spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
               // spinner.setSelection(position)

               val item = spinner.getChildAt(0) as TextView
               item.setTextColor(Color.parseColor("#FFFFFF"))

            }
        }
        }
       return root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {

        Log.d(this::class.java.simpleName, "onViewCreated")

        try {

            ObtenerEnviosYActualizacionContador().execute()

            // creo el detector qr
            barcodeDetector = BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build()

            // creo la camara fuente
            cameraSource = CameraSource.Builder(context, barcodeDetector)
                .setRequestedPreviewSize(640, 640)
                .setRequestedFps(25f)
                .setAutoFocusEnabled(true).build()

            cameraView = v.findViewById(R.id.camera_view) as SurfaceView

            // listener de ciclo de vida de la camara
            cameraView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {

                    // si el usuario ya le dio los permisos para la camara
                    if (ContextCompat.checkSelfPermission(
                            context!!,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // inicio la camara
                        try {
                            cameraSource.start(cameraView.holder)
                        } catch (ie: IOException) {
                            Dialogs.mostrarSnackbarLargo(
                                container,
                                getString(R.string.mensaje_error_abrir_camara),
                                Color.RED
                            )
                        }
                    } else {

                        // Sino le indico que falta dar permisos
                        Dialogs.mostrarSnackbarButton(
                            container, getString(R.string.mensaje_error_permisos_camara), getString(
                                R.string.mensaje_aceptar
                            ), Color.YELLOW
                        )

                        // Pido los permisos
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            PERMISSION_CAMARA_REQUEST_CODE
                        )

                    }
                    //permisos fine location
                    if (ContextCompat.checkSelfPermission(
                            context!!,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // inicio el gps
                        try {
                            var gps =getInstance()
                            lat =gps.latitude
                            lng =gps.getLongitude()

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
                            container, getString(R.string.mensaje_error_permisos_ubicacion), getString(
                                R.string.mensaje_aceptar
                            ), Color.YELLOW
                        )

                        // Pido los permisos
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            PERMISSION_ACCESS_FINE_LOCATION
                        )
                    }
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    cameraSource.stop()
                }
            })

            //implement processor interface to catch the barcode scanner result
            barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
                override fun release() {
                }

                override fun receiveDetections(detections: Detector.Detections<Barcode>?) {

                    val barcode = detections?.detectedItems
                    if (barcode?.size() ?: 0 > 0) {
                        if (escaneoEstaHabilitado()) {

                            Log.d(this::class.java.simpleName, "Se procesa escaneo")

                            deshabilitarEscaneo()

                            // procedo con el analisis del qr y el envio al backend
                            procesarQR(v, barcode!!)
                        } else {
                            Log.d(this::class.java.simpleName, "Escaneo deshabilitado")
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Dialogs.mostrarSnackbarError(lyt_container,requireContext(),operacionActual,e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        // Cuando se solicitaron permisos y el usuario sigui una accion (cancelar rechazar o aprobar)
        when (requestCode) {

            // Cuando se trata de la respuesta a permisos de la carama
            PERMISSION_CAMARA_REQUEST_CODE -> {
                // Si la solicitud de permisos no fue cancelada por el usuario y los permisos fueron otorgados
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Recargo el fragment
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, QrEntregaFragment())
                        .commit();
                } else {
                    // Sino, muestro el mensaje indicando que los permisos son necesario y vuelvo al fragment home
                    Dialogs.mostrarSnackbarLargo(
                        container,
                        getString(R.string.mensaje_error_permisos_camara),
                        Color.YELLOW
                    )
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, HomeFragment())
                        .commit();
                }
                return
            }
            PERMISSION_ACCESS_FINE_LOCATION->{
                // Si la solicitud de permisos no fue cancelada por el usuario y los permisos fueron otorgados
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Recargo el fragment
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, QrEntregaFragment())
                        .commit();
                } else {
                    // Sino, muestro el mensaje indicando que los permisos son necesario y vuelvo al fragment home
                    Dialogs.mostrarSnackbarLargo(
                        container,
                        getString(R.string.mensaje_error_permisos_ubicacion),
                        Color.YELLOW
                    )
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, HomeFragment())
                        .commit();
                }
                return
            }




        }
    }

    private fun procesarQR(view: View, barcode: SparseArray<Barcode>) {

        try {
            // obtengo el string del QR
            val qrLeido = barcode.valueAt(0).displayValue.toString()
            val gson = Gson()

            // convierto el string a la clase Qr, que tiene el formato del json recibido
            val qrModel = gson.fromJson(qrLeido, QrModel::class.java)

            // Si se trata de un paquete NOML valido que esten completos los datos del que recibe
            if (qrModel.id.startsWith("NOML", true)) {
                if (txtDniRecibe.text.isEmpty() || txtNombreRecibe.text.isEmpty()) {

                    Dialogs.mostrarSnackbarLargo(
                        container,
                        getString(R.string.mensaje_campos_recibe_incompletos),
                        Color.RED
                    )

                    Log.d(
                        this::class.java.simpleName,
                        "El paquete es NOML y no se completaron los campos obligatorios"
                    )

                    habilitarEscaneo()
                    return
                }
            }
            // obtengo el usuario de mercado libre y sigo con el circuito
            obtenerInfoML(view, qrModel)
        }
        catch (e: Exception) {
            Dialogs.mostrarSnackbarError(lyt_container, requireActivity(), operacionActual,e)
        }
    }

    private fun obtenerInfoML(view: View, qrModel: QrModel) {

        var url=Configuracion.URL_ML + qrModel.sender_id
        if(qrModel.id.contains("NOML")){
            url="https://logixs.com.ar/"+SharedPref.getPathUsuario(requireContext())+"/usuarios/ConsultaUsuariosAppNOml?usuario="+qrModel.sender_id
        }

        // Genero la solicitud para obtener la info de ML
        val stringRequestML = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { mlResponse ->

                // Si no hubo error en la solicitud proceso la respuesta
                procesarRespuestaML(qrModel, mlResponse)
            },
            Response.ErrorListener {

                Log.d(this::class.java.simpleName, "Error al obtener información de ML")

                habilitarEscaneo()
                Dialogs.mostrarErrorVolley(context, container)
            }
        )

        Log.d(this::class.java.simpleName, "Obtengo la información de ML")

        // Agrego la solicitud a la cola de solicitudes
        HttpRequest.getInstance(requireActivity().applicationContext).addToRequestQueue(
            stringRequestML
        )
    }

    private fun procesarRespuestaML(qrModel: QrModel, mlResponse: String?) {

        try {
            if (ultimoIdEnvio == qrModel.id) {
                Log.d(this::class.java.simpleName, "Ya se procesó este envio")
                habilitarEscaneo()
                return
            }

            ultimoIdEnvio = qrModel.id

            Log.d(this::class.java.simpleName, "Procesando la información de ML")

            if (mlResponse != null) {
                try {
                    val gson = Gson()

                    // convierto la respuesta a la clase MLModel, que tiene el formato del json recibido
                    val mlModel = gson.fromJson(mlResponse, MLModel::class.java)

                    // muestro el nick en la vista
                    completarTxv(txvIdVendedor, qrModel.id, R.color.colorTexto)
                    completarTxv(txvNicknameVendedor, mlModel.nickname, R.color.colorTexto)

                    // guardo el nickname de ml en las shared
                    SharedPref.setNickNameMl(requireContext(), mlModel.nickname)

                    // Envio al backend la informacion del qr y de ml
                    enviarAlBackend(qrModel, mlModel)


                } catch (a: Exception) {

                    Log.d(this::class.java.simpleName, "Error al procesar la información de ML"+ a.message)

                    habilitarEscaneo()

                    // Si hubo un error al obtener la información
                    Dialogs.mostrarSnackbarLargo(
                        container,
                        getString(R.string.mensaje_error_mercado_libre),
                        Color.RED
                    )
                }
            }
        } catch (e: Exception) {
            Dialogs.mostrarSnackbarError(lyt_container, requireActivity(), operacionActual,e)
        }
    }

    private fun enviarAlBackend(qrModel: QrModel, mlModel: MLModel) {

        val url =
            Configuracion.URL_LOGIXS + SharedPref.getPathUsuario(requireContext()) + "/envioflex/RecibirScanQR"
//busco las coords
     //   requestPermissions(
     //   arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
     //       PERMISSION_CAMARA_REQUEST_CODE )
        if (lat.isNullOrEmpty()) lat else "0"
        if (lng.isNullOrEmpty()) lng else "0"


        // Genero la solicitud para enviar al backend
        val stringRequestBE = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { backendResponse ->

                // Si no hubo error en la solicitud proceso la respuesta
                procesarRespuestaBackend(backendResponse, qrModel.id)
            },
            Response.ErrorListener {

                Log.d(this::class.java.simpleName, "Error al enviar la información al BE")

                habilitarEscaneo()

                Dialogs.mostrarErrorVolley(context, container)
            }

        ) {
            override fun getParams(): Map<String, String> {
                val gson = Gson()
                val stringQrModel = gson.toJson(qrModel)
                val params: MutableMap<String, String> =
                    HashMap()
                params["MensajeroId"] = SharedPref.getIdUsuario(requireContext())!!
                params["EntregaOretiro"] = operacionActual
                params["Path"] = SharedPref.getPathUsuario(requireContext())!!
                params["Scan"] = stringQrModel
                params["IdML"] = qrModel.id
                params["Nickname"] = mlModel.nickname
                params["Sender_id"] = qrModel.sender_id.toString()
                params["recibeDNI"] = txtDniRecibe.text.toString()
                params["RecibeNombre"] = txtNombreRecibe.text.toString()
                params["lat"] = lat
                params["lng"] = lng
                params["obs"]=txtObs.text.toString()
                params["EstadoEntrega"] = spinner.getSelectedItem().toString()
                return params
            }
        }

        Log.d(this::class.java.simpleName, "Enviando la información al BE")

        // Agrego la solicitud a la cola de solicitudes
        HttpRequest.getInstance(requireActivity().applicationContext).addToRequestQueue(
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

                    insertarEnvioEnBase(idEnvio)

                    // Muestro el resultado correcto
                    completarTxv(txvEstado, postQrModel.mensaje, R.color.colorOk)
                    imgEstado.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.check))
                    reproducirSonido(R.raw.ok)

                    ObtenerEnviosYActualizacionContador().execute()

                    Log.d(this::class.java.simpleName, "Respuesta del BE: OK")

                } else {
                    // Sino, muestro el mensaje devuelto por el backend

                    // Muestro el resultado correcto
                    completarTxv(txvEstado, postQrModel.mensaje, R.color.colorError)
                    imgEstado.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.check))
                    reproducirSonido(R.raw.error)

                    Log.d(this::class.java.simpleName, "Respuesta del BE: Error")
                }

                imgEstado.visibility = ImageView.VISIBLE

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

        habilitarEscaneo()

    }

    private fun deshabilitarEscaneo() {
        solicitudPreviaFinalizada = false

        requireActivity().runOnUiThread(Runnable {
            txtNombreRecibe.isEnabled = false
            txtDniRecibe.isEnabled = false
        })

    }

    private fun habilitarEscaneo() {
        solicitudPreviaFinalizada = true

        requireActivity().runOnUiThread(Runnable {
            txtNombreRecibe.isEnabled = true
            txtDniRecibe.isEnabled = true
        })
    }

    private fun completarTxv(textView: TextView, texto: String, color: Int) {
        textView.text = texto
        textView.setTextColor(ContextCompat.getColor(requireActivity(), color))
    }

    private fun escaneoEstaHabilitado(): Boolean {

        if (!solicitudPreviaFinalizada) {

            Log.d(
                this::class.java.simpleName,
                "Se deniega el escaneo, no finalizó la solicitud anterior"
            )
            return false
        }

        val fechaHoraActual = Calendar.getInstance()

        // si todavia no hubo scan, actualizo la fecha hora y retorno true
        if (!this::fechaHoraUltimoScan.isInitialized) {

            Log.d(this::class.java.simpleName, "Se permite el escaneo, es el primero")

            fechaHoraUltimoScan = fechaHoraActual
            return true
        }

        var fechaHoraLimite = fechaHoraUltimoScan.clone() as Calendar

        // A la fecha hora limite le sumo la cantidad de segundos guardados en Configuracion
        fechaHoraLimite.add(Calendar.SECOND, Configuracion.TIEMPO_ENTRE_SCANS_QR)

        Log.d(
            this::class.java.simpleName,
            "fechaHoraActual " + fechaHoraActual.time.toString() + "--- fechaHoraLimite " + fechaHoraLimite.time.toString() + "--- fechaHoraUltimoScan " + fechaHoraUltimoScan.time.toString()
        )

        if (fechaHoraLimite < fechaHoraActual) {

            Log.d(
                this::class.java.simpleName,
                "Se permite el escaneo, pasó el tiempo entre escaneos"
            )

            fechaHoraUltimoScan = fechaHoraActual
            return true
        }

        Log.d(
            this::class.java.simpleName,
            "Se deniega el escaneo, no pasó el tiempo entre escaneos"
        )

        // si se llego hasta aca es porque no se puede escanear de vuelta
        return false;
    }

    private fun configurarReset() {
        imgReset.setOnLongClickListener {
            BorrarEnvios().execute()
            ObtenerEnviosYActualizacionContador().execute()
            true
        }
    }

    private fun configurarContador() {

        txvContador.setOnClickListener {
            abrirListadoEnvio()
        }
    }

    private fun abrirListadoEnvio() {
        val intent = Intent(requireActivity(), ListEnviosActivity::class.java)
        intent.putExtra("seccion", operacionActual)
        startActivity(intent)
    }

    private fun insertarEnvioEnBase(idEnvio: String) {

        val id = operacionActual + "_" + idEnvio + "_" + getRandomString()
        InsertarEnvio().execute(id,idEnvio,operacionActual)
    }

    private fun getRandomString(): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..5)
            .map { charset.random() }
            .joinToString("")
    }

    private fun reproducirSonido(sonido: Int) {
        val mp = MediaPlayer.create(requireContext(), sonido)
        mp.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        barcodeDetector.release()
        cameraSource.stop()
        cameraSource.release()
    }

    inner class ObtenerEnviosYActualizacionContador : AsyncTask<Unit, Unit, String>() {

        override fun doInBackground(vararg params: Unit): String {
            return db.EnvioDao().getAllBySeccion(operacionActual).count().toString()
        }
        override fun onPostExecute(result: String) {
            txvContador.text = result
        }
    }

    inner class InsertarEnvio : AsyncTask<String, Unit, Int>() {

        override fun doInBackground(vararg params: String): Int {

            var envio = Envio(
                params[0],
                params[1],
                params[2]
            )

            db.EnvioDao().insertAll(envio)
            return 0
        }
    }

    inner class BorrarEnvios : AsyncTask<Unit, Unit, Int>() {

        override fun doInBackground(vararg params: Unit): Int {
            db.EnvioDao().deleteBySeccion(operacionActual)
            return 0
        }
    }


}