package com.logixs.logixsqr.ui.entrega_listado

import CargadosAdapter
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.logixs.logixsqr.*
import com.logixs.logixsqr.Utils.GPSUtils.getInstance
import com.logixs.logixsqr.ui.home.HomeFragment
import com.vulkansoft.sporter.Dialogs
import com.vulkansoft.sporter.HttpRequest
import kotlinx.android.synthetic.main.fragment_entrega_listado.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class EntregaListadoFragment : Fragment() {


    private val PERMISSION_ACCESS_FINE_LOCATION: Int = 100
    val operacionActual = "entrega"

    lateinit var recListadoCargados: RecyclerView
    lateinit var imgCoordenadas: ImageView

    var lat: String = "0"
    var lng: String = "0"
    val url = "https://www.google.com/maps/dir"
    var urlPaquetes = ""
    var paquetesArray = ArrayList<EntregasPaquetesModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        cont: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        try {
            container?.removeAllViews()
        } catch (e: Exception) {
        }

        val root = inflater.inflate(R.layout.fragment_entrega_listado, container, false)
        recListadoCargados = root.findViewById<RecyclerView>(R.id.recListadoCargados)
        imgCoordenadas = root.findViewById<ImageButton>(R.id.img_coordenadas)

        imgCoordenadas.setOnClickListener {

            if(lat == "0"){
                Dialogs.mostrarSnackbarLargo(container,getString(R.string.mensaje_coordenadas_no_obtenidas),Color.YELLOW)
            }
            else {

                urlPaquetes = url

                urlPaquetes += "/ " + lat + "," + lng
                for(paquete in paquetesArray)
                    urlPaquetes += "/" + paquete.Lat + "," + paquete.Lng

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlPaquetes))
                requireContext().startActivity(intent)
            }
        }
        return root
    }


    override fun onResume() {

        super.onResume()

        Log.d(this::class.java.simpleName, "onResume")

        try {

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // inicio el gps
                try {
                    var gps = getInstance()
                    if (gps.latitude != null && gps.longitude != null) {
                        lat = gps.latitude
                        lng = gps.longitude
                    }
                    obtenerCargados()

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
            Dialogs.mostrarSnackbarError(container, requireContext(), operacionActual, e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        // Cuando se solicitaron permisos y el usuario sigui una accion (cancelar rechazar o aprobar)
        when (requestCode) {


            PERMISSION_ACCESS_FINE_LOCATION -> {
                // Si la solicitud de permisos no fue cancelada por el usuario y los permisos fueron otorgados
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Recargo el fragment
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, EntregaListadoFragment())
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

    private fun obtenerCargados() {
        val context = this
        val url =
            Configuracion.URL_LOGIXS + SharedPref.getPathUsuario(requireActivity()) + "/envioflex/ListaPaquetesParaEntregarApp"

        // Genero la solicitud para enviar al backend
        val stringRequestBE = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { backendResponse ->

                // Si no hubo error en la solicitud proceso la respuesta
                procesarRespuestaBackend(backendResponse)
                Log.d(this::class.java.simpleName, backendResponse.toString())
            },
            Response.ErrorListener {

                Log.d(this::class.java.simpleName, "Error al enviar la información al BE")

                Dialogs.mostrarErrorVolley(requireActivity(), container)
            }

        ) {
            override fun getParams(): Map<String, String> {

                val params: MutableMap<String, String> =
                    HashMap()
                params["IdUsuario"] = SharedPref.getIdUsuario(requireActivity())!!
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

    private fun procesarRespuestaBackend(mlResponse: String?) {

        try {

            Log.d(this::class.java.simpleName, "Procesando la respuesta del BE")

            if (mlResponse != null) {
                try {
                    val gson = Gson()

                    // convierto la respuesta a la clase MLModel, que tiene el formato del json recibido


                    val paquetesList: List<EntregasPaquetesModel> =
                        gson.fromJson(mlResponse, Array<EntregasPaquetesModel>::class.java).toList()

                    if (paquetesList.isEmpty()) {
                        recListadoCargados.visibility = RecyclerView.GONE
                        txv_no_hay_entregas.visibility = TextView.VISIBLE
                    }
                    else{
                        recListadoCargados.visibility = RecyclerView.VISIBLE
                        txv_no_hay_entregas.visibility = TextView.GONE

                        paquetesArray.clear()
                        paquetesArray.addAll(paquetesList)

                        val linearLayoutManager = LinearLayoutManager(requireActivity())
                        recListadoCargados.layoutManager = linearLayoutManager
                        recListadoCargados.setHasFixedSize(true)

                        recListadoCargados.visibility = View.VISIBLE
                        recListadoCargados.adapter =
                            CargadosAdapter(requireActivity(), paquetesArray)



                    }
                } catch (a: Exception) {

                    Log.d(
                        this::class.java.simpleName,
                        "Error al procesar la información de ML" + a.message
                    )

                    // Si hubo un error al obtener la información
                    Dialogs.mostrarSnackbarLargo(
                        container,
                        getString(R.string.mensaje_error_generico_servidor),
                        Color.RED
                    )
                }
            }
        } catch (e: Exception) {
            Dialogs.mostrarSnackbarError(container, requireActivity(), "PaquetesAEntregar", e)
        }
    }


}