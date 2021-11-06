package com.vulkansoft.sporter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.volley.VolleyError
import com.google.android.material.snackbar.Snackbar
import com.logixs.logixsqr.Configuracion
import com.logixs.logixsqr.ErrorActivity
import com.logixs.logixsqr.R
import java.io.PrintWriter
import java.io.StringWriter


object Dialogs {

    fun mostrarToastLargo(context: Context, mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
    }

    fun mostrarToastCorto(context: Context, mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }

    fun mostrarSnackbarLargo(view: View, textoMensaje: String, color: Int) {

        var snackbar = Snackbar.make(view, textoMensaje, Snackbar.LENGTH_LONG)
        snackbar.setTextColor(color)
        snackbar.show()

    }

    fun mostrarSnackbarError(view:View, context: Context, origen: String, exception: Exception){

        if(Configuracion.DEBUG) {

            var snackbar = Snackbar.make(view, "Modo debug - Presionar 'Ver detalle' para ver el error", Snackbar.LENGTH_SHORT)
                .setAction("Ver detalle", View.OnClickListener {

                    val stringWriter = StringWriter()
                    exception.printStackTrace(PrintWriter(stringWriter))

                    val intent = Intent(context, ErrorActivity::class.java)
                    intent.putExtra("origen", origen)
                    intent.putExtra("message", exception.message)
                    intent.putExtra("stackTrace", stringWriter.toString())
                    context.startActivity(intent)
                })

            snackbar.setTextColor(ContextCompat.getColor(context, R.color.colorError))
            snackbar.show()
        }
    }

    fun mostrarSnackbarButton(view:View, textoMensaje: String, textoBoton:String, colorTextoMensaje: Int){

        var snackbar =Snackbar.make(view, textoMensaje, Snackbar.LENGTH_LONG).setAction(textoBoton, View.OnClickListener {})
        snackbar.setTextColor(colorTextoMensaje)
        snackbar.show()

    }

    fun mostrarSnackbarCorto(view: View, mensaje: String) {
        Snackbar.make(view, mensaje, Snackbar.LENGTH_SHORT).show()
    }

    fun mostrarErrorVolley(context: Context?, view: View?) {
        if (context != null && view != null) {
            mostrarSnackbarLargo(view, context.getString(R.string.mensaje_error_conexion_servidor), Color.RED)
        }
    }

    fun mostrarErrorDeSolicitud(context: Context?, view: View?, volleyError: VolleyError) {
        if (context != null && view != null) {
            if (volleyError.networkResponse == null) {
                mostrarSnackbarLargo(view, context.getString(R.string.mensaje_error_conexion_servidor), Color.RED)
            } else {
                mostrarSnackbarLargo(view, context.getString(R.string.mensaje_error_generico_servidor), Color.RED)
            }
        }
    }

    fun mostrarExcepcion(view: View?, mensaje: String) {
        if (view != null) {
            mostrarSnackbarLargo(view, mensaje, Color.RED)
        }
    }
}
