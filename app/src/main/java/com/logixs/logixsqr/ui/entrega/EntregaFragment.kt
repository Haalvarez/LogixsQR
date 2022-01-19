package com.logixs.logixsqr.ui.entrega

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.logixs.logixsqr.*
import com.logixs.logixsqr.Utils.GPSUtils.getInstance
import com.logixs.logixsqr.ui.home.HomeFragment
import com.vulkansoft.sporter.Dialogs
import com.vulkansoft.sporter.HttpRequest
import kotlinx.android.synthetic.main.fragment_qr_entrega.*
import kotlinx.android.synthetic.main.fragment_qr_retiro.*
import org.json.JSONObject
import java.io.IOException


class EntregaFragment : Fragment() {


    private val PERMISSION_ACCESS_FINE_LOCATION: Int = 100
    val operacionActual = "entrega"

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
    lateinit var lat: String
    lateinit var lng: String

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

        txtObs = root.findViewById(R.id.txt_obsEntrega)
        txvIdVendedor = root.findViewById(R.id.txv_id_vendedor)
        txvNicknameVendedor = root.findViewById(R.id.txv_nickname_vendedor)
        txvEstado = root.findViewById(R.id.txv_estado)
        txtDniRecibe = root.findViewById(R.id.txt_dni_recibe)
        txtNombreRecibe = root.findViewById(R.id.txt_nombre_recibe)
        imgEstado = root.findViewById(R.id.img_estado)

        return root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {

        Log.d(this::class.java.simpleName, "onViewCreated")

        try {
//permisos fine location
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // inicio el gps
                try {
                    var gps = getInstance()
                    lat = gps.latitude
                    lng = gps.getLongitude()

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


        } catch (e: Exception) {
            Dialogs.mostrarSnackbarError(lyt_container, requireContext(), operacionActual, e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        // Cuando se solicitaron permisos y el usuario sigui una accion (cancelar rechazar o aprobar)
        when (requestCode) {


            PERMISSION_ACCESS_FINE_LOCATION->{
                // Si la solicitud de permisos no fue cancelada por el usuario y los permisos fueron otorgados
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Recargo el fragment
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, EntregaFragment())
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



    private fun obtenerlistaPQCargados(view: View, qrModel: QrModel) {

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
                val fakeResponse = JSONObject("""{"nickname":""}""")

                procesarRespuestaML(qrModel, fakeResponse.toString())
                Log.d(this::class.java.simpleName, "Error al obtener información de ML")

                //  habilitarEscaneo()

                //  Dialogs.mostrarErrorVolley(context, container)
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


                    // Muestro el resultado correcto
                    completarTxv(txvEstado, postQrModel.mensaje, R.color.colorOk)
                    imgEstado.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.check))
                    reproducirSonido(R.raw.ok)
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
    }



    private fun completarTxv(textView: TextView, texto: String, color: Int) {
        textView.text = texto
        textView.setTextColor(ContextCompat.getColor(requireActivity(), color))
    }

    private fun abrirListadoEnvio() {
        val intent = Intent(requireActivity(), ListEnviosActivity::class.java)
        intent.putExtra("seccion", operacionActual)
        startActivity(intent)
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
        /*barcodeDetector.release()
        cameraSource.stop()
        cameraSource.release()*/
    }

}