package com.logixs.logixsqr

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.vulkansoft.sporter.Dialogs
import com.vulkansoft.sporter.HttpRequest
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Al hacer clic en el boton de login
        login.setOnClickListener {

            // Oculto el teclado
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(container.windowToken, 0)

            // Envio las credenciales al backend para validarlas
            login(this)
        }
    }

    private fun login(context: Context) {

        val url = Configuracion.URL_LOGIXS + "login/loginMobile"

        // Genero la solicitud para hacer el login
        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                // Si no hubo error en la solicitud proceso la respuesta
                procesarRespuesta(context, response)
            },
            Response.ErrorListener {
                Dialogs.mostrarErrorVolley(context, container)
            }

        ) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> =
                    HashMap()
                params["ID"] = tx_usuario.text.toString()
                params["pass"] = tx_password.text.toString()
                return params
            }

        }

        // Agrego la solicitud a la cola de solicitudes
        HttpRequest.getInstance(applicationContext).addToRequestQueue(stringRequest)
    }

    private fun procesarRespuesta(context: Context, response: String?) {

        if (response != null) {
            try {

                // Convierto el string a JSON
                val jsonResponse = JSONObject(response)

                val idUsuario = jsonResponse.get("idUsuario").toString()

                // Si la autenticacion fue correcta
                if (idUsuario != "0") {

                    // Guardo la información en las shared preferences
                    SharedPref.setIdUsuario(context, formatear(jsonResponse,"idUsuario"))
                    SharedPref.setNombreUsuario(context, formatear(jsonResponse,"Nombre"))
                    SharedPref.setApellidoUsuario(context, formatear(jsonResponse,"Apellido"))
                    SharedPref.setApodoUsuario(context, formatear(jsonResponse,"Apodo"))
                    SharedPref.setEmpresaUsuario(context, formatear(jsonResponse,"Empresa"))
                    SharedPref.setPathUsuario(context, formatear(jsonResponse,"Path"))
                    SharedPref.setPerfilUsuario(context, formatear(jsonResponse,"Perfil"))

                    // Abro la home
                    abrirHome(context)
                }
                else{
                    // Sino fue correcta muestro el error
                    Dialogs.mostrarSnackbarLargo(container, getString(R.string.mensaje_error_usuario_contrasenia), Color.RED)
                }

            } catch (a: Exception) {

                // Sino fue correcta muestro el error
                Dialogs.mostrarSnackbarLargo(container, getString(R.string.mensaje_error_autenticacion), Color.RED)
            }
        }

    }

    private fun formatear(jsonResponse: JSONObject, clave: String): String {

        if(jsonResponse.has(clave)){

            val valor = jsonResponse.get(clave).toString()
            if(valor.toUpperCase() != "NULL")
                return  valor;
        }

        return "";
    }

    private fun abrirHome(context:Context) {
        val intent = Intent(context, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}