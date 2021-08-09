package com.vulkansoft.sporter

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.logixs.logixsqr.Configuracion

class HttpRequest constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: HttpRequest? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: HttpRequest(context).also {
                    INSTANCE = it
                }
            }
    }

    val requestQueue: RequestQueue by lazy {

        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(req: Request<T>) {

        Log.d(this::class.java.simpleName, "Se agrega una solicitud a " + req.url)

        req.setRetryPolicy(DefaultRetryPolicy(Configuracion.VOLLEY_TIMEOUT,Configuracion.VOLLEY_NUM_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
        requestQueue.add(req)
    }
}
