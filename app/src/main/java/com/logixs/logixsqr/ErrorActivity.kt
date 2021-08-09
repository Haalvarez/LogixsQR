package com.logixs.logixsqr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vulkansoft.sporter.Dialogs
import kotlinx.android.synthetic.main.activity_error.*

class ErrorActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        val className = intent.getStringExtra("origen").toString()
        val message = intent.getStringExtra("message").toString()
        val stackTrace = intent.getStringExtra("stackTrace").toString()

        tx_clase.text = className
        tx_message.text = message
        tx_stack_trace.text = stackTrace

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("error", stackTrace)
        clipboard.setPrimaryClip(clip)

        Dialogs.mostrarToastLargo(this,getString(R.string.mensaje_error_portapapeles))
    }
}