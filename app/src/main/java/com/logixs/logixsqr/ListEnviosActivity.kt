package com.logixs.logixsqr

import EnvioAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.logixs.logixsqr.database.AppDatabase
import com.logixs.logixsqr.database.Envio
import kotlin.concurrent.thread

class ListEnviosActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var seccion: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_envios)

        db = AppDatabase.getInstance(applicationContext)
        seccion = intent.getStringExtra("seccion").toString()
    }

    override fun onStart() {
        super.onStart()

        thread {
            val recListadoEnvios: RecyclerView = findViewById(R.id.recListadoEnvios)
            val linearLayoutManager = LinearLayoutManager(this)
            recListadoEnvios.layoutManager = linearLayoutManager
            recListadoEnvios.setHasFixedSize(true)
            //var envios =  mutableListOf<Envio>()

            var envios = db.EnvioDao().getAllBySeccion(seccion)
            if (envios.isNotEmpty()) {
                val enviosArray = ArrayList<Envio>()
                enviosArray.addAll(envios)
                recListadoEnvios.visibility = View.VISIBLE
                recListadoEnvios.adapter = EnvioAdapter(this, enviosArray)
            } else {
                recListadoEnvios.visibility = View.GONE
                this.runOnUiThread {
                    Toast.makeText(
                        this,
                        this.getString(R.string.mensaje_no_hay_envios),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
