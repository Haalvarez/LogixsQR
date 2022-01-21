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
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.logixs.logixsqr.Configuracion
import com.logixs.logixsqr.ListEnviosActivity
import com.logixs.logixsqr.R
import com.logixs.logixsqr.SharedPref
import com.logixs.logixsqr.Utils.GPSUtils.getInstance
import com.logixs.logixsqr.ui.home.HomeFragment
import com.vulkansoft.sporter.Dialogs
import com.vulkansoft.sporter.HttpRequest
import kotlinx.android.synthetic.main.entrega.*
import kotlinx.android.synthetic.main.fragment_qr_entrega.*
import kotlinx.android.synthetic.main.fragment_qr_retiro.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class EntregaFragment : Fragment() {


    private val PERMISSION_ACCESS_FINE_LOCATION: Int = 100
    val operacionActual = "entrega"


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

        val root = inflater.inflate(R.layout.entrega, container, false)
        val LvPqParaEntregar=root.findViewById<ListView>(R.id.LvPqParaEntregar)
        val arrayAdapter:ArrayAdapter<*>
        var paquetes= mutableListOf("a","b","c")


        arrayAdapter= ArrayAdapter(requireContext() ,android.R.layout.simple_list_item_1 ,paquetes)
        LvPqParaEntregar.adapter=arrayAdapter



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
            ObtenerListaDePQPorEntregar()

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






    private fun ObtenerListaDePQPorEntregar() {

        val url =   Configuracion.URL_LOGIXS + SharedPref.getPathUsuario(requireContext()) + "/envioflex/PendientesDeEntrega"

        if (lat.isEmpty()) lat else "0"
        if (lng.isEmpty()) lng else "0"
        // Genero la solicitud para enviar al backend
        val stringRequestBE = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { backendResponse ->

                // Si no hubo error en la solicitud proceso la respuesta
                procesarRespuestaBackend(backendResponse)
            },
            Response.ErrorListener {

                Log.d(this::class.java.simpleName, "Error al enviar la información al BE")

                Dialogs.mostrarErrorVolley(context, container)
            }

        ) {
            override fun getParams(): Map<String, String> {
                val gson = Gson()
                //val stringQrModel = gson.toJson(qrModel)
                val params: MutableMap<String, String> =
                    HashMap()
                params["IdUsuario"] = SharedPref.getIdUsuario(requireContext())!!
                params["lat"] = lat
                params["lng"] = lng
                return params
            }
        }

        Log.d(this::class.java.simpleName, "Enviando la información al BE")

        // Agrego la solicitud a la cola de solicitudes
        HttpRequest.getInstance(requireActivity().applicationContext).addToRequestQueue(
            stringRequestBE
        )
    }



    private fun procesarRespuestaBackend(backendResponse: String?) {

        Log.d(this::class.java.simpleName, "Procesando la información del BE")
         try {
             val gson = Gson()

             // convierto el string a la clase Qr, que tiene el formato del json recibido
          //   val qrModel = gson.fromJson(backendResponse, PendientesDeEntregaXusuarioToListViewApp::class.java)


                    val jsonArray = JSONArray(backendResponse)
                    val length = jsonArray.length()
                    val listContents: MutableList<String> = ArrayList(length)
                    for (i in 0 until length) {


                        //listContents.add(jsonArray.getString(i))
                        val objects: JSONObject
                        objects = jsonArray.getJSONObject(i)
                        val AddressLine = objects["AddressLine"].toString()





                        listContents.add(AddressLine)
                    }





                    //val myListView = findViewById(R.id.LvPqParaEntregar) as ListView
             LvPqParaEntregar.adapter =
                        ArrayAdapter<String>(
                            requireContext() ,
                            android.R.layout.simple_list_item_1,
                            listContents
                        )
                } catch (e: java.lang.Exception) {
                    // this is just an example
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