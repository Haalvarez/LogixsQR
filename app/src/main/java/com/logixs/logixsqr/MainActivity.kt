package com.logixs.logixsqr

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.logixs.logixsqr.Utils.GPSUtils.getInstance
import com.vulkansoft.sporter.HttpRequest
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //ubicacion
        var gps = getInstance()
        gps.findDeviceLocation(this)



        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        // Si no hay usuario logueado
        if (SharedPref.getIdUsuario(this) == "") {

            // Inicio la activity de logueo
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        } else {
            //
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.w(ContentValues.TAG,token.toString())
                Log.d(ContentValues.TAG,token.toString())
                val idUsuario=SharedPref.getIdUsuario(this)
                val path=SharedPref.getPathUsuario(this)

                val url =Configuracion.URL_LOGIXS+path+ "/Notificaciones/UpdateTokenMensajesApp"
                // Genero la solicitud para enviar al backend
                val stringRequestBE = object : StringRequest(
                    Method.POST, url,
                    Response.Listener<String> { backendResponse ->

                        // Si no hubo error en la solicitud proceso la respuesta
                        // procesarRespuestaBackend(view, backendResponse, qrModel.id)
                    },
                    Response.ErrorListener {

                        Log.d(this::class.java.simpleName, "Error al enviar la información al BE")
                    }

                ) {
                    override fun getParams(): Map<String, String> {
                        val gson = Gson()

                        val params: MutableMap<String, String> =
                            HashMap()
                        params["MensajeroId"] = idUsuario.toString()
                        params["Path"] = path.toString()
                        params["Token_fcm"] = token.toString()
                        params["AppVersion"] =  BuildConfig.VERSION_NAME;

                        return params
                    }
                }
                with(HttpRequest) {
                    getInstance(this@MainActivity.applicationContext).addToRequestQueue(
                        stringRequestBE
                    )
                }
            })
            //fcm
            // Sino, inicio la home del usuario
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        }
    }
}
