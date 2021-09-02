package com.logixs.logixsqr

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.logixs.logixsqr.ui.WebView.WebViewFragment


class MyFirebaseMessagingService:FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        abrir()
        val noti=remoteMessage.getNotification()
        val data: Map<String, String> = remoteMessage.getData()
       Looper.prepare()
        Handler().post{

            Toast.makeText(baseContext,remoteMessage.notification?.title,Toast.LENGTH_LONG).show()
        }
        Looper.loop()
        val myCustomKey = data["dato1"]


        var notification = NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_menu_camera)
            .setContentTitle("setContentTitle")
            .setContentText(myCustomKey)
            //.setLargeIcon(R.drawable.circular)
            .setStyle(NotificationCompat.BigTextStyle())
                //.bigText(emailObject.getSubjectAndSnippet()))
            .build()







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