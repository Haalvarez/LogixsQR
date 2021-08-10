package com.logixs.logixsqr

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.logixs.logixsqr.ui.WebView.WebViewFragment


class MyFirebaseMessagingService:FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        abrir()
        val noti=p0.getNotification()
        val data=p0.getData()
       Looper.prepare()
        Handler().post{

            Toast.makeText(baseContext,p0.notification?.title,Toast.LENGTH_LONG).show()
        }




        Looper.loop()

       //}



    }

fun abrir(){


    val abrirFragment = HomeActivity().abrirFragment(
        WebViewFragment(
            SharedPref.getPathUsuario(HomeActivity()),
            SharedPref.getIdUsuario(HomeActivity())
        ), "Mis Viajes"
    )

}
}