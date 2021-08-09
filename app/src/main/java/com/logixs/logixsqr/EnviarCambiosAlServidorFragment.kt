package com.logixs.logixsqr
import android.util.Log
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.vulkansoft.sporter.Dialogs
import com.vulkansoft.sporter.HttpRequest
import kotlinx.android.synthetic.main.fragment_qr_entrega.*

public class EnviarCambiosAlServidorFragment : Fragment(){



    ///me explota en SharedPref.getPathUsuario(requireContext()), por eso pongo esta fun aca(cualquier lado)
    fun enviarNuevoTokenParaNotificaciones(token: String, idUsuario: String, pathUsuario: String) {

        val url =Configuracion.URL_LOGIXS+pathUsuario+ "/Notificaciones/UpdateTokenMensajesApp"



        // Genero la solicitud para enviar al backend
        val stringRequestBE = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { backendResponse ->

                // Si no hubo error en la solicitud proceso la respuesta
                // procesarRespuestaBackend(view, backendResponse, qrModel.id)
            },
            Response.ErrorListener {

                Log.d(this::class.java.simpleName, "Error al enviar la información al BE")


                Dialogs.mostrarErrorVolley(context, container)
            }

        ) {
            override fun getParams(): Map<String, String> {
                val gson = Gson()

                val params: MutableMap<String, String> =
                    HashMap()
                params["MensajeroId"] = idUsuario.toString()
                params["Path"] = pathUsuario.toString()
                params["Token_fcm"] = token

                return params
            }
        }
        with(HttpRequest) {
            getInstance(requireActivity().applicationContext).addToRequestQueue(
                stringRequestBE
            )
        }

    }
}