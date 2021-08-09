package com.logixs.logixsqr
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService:FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {

       Looper.prepare()
        Handler().post{

            Toast.makeText(baseContext,p0.notification?.title,Toast.LENGTH_LONG).show()
        }



         //   val intent = Intent(this, ::class.java)
          //  intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          //  startActivity(intent)
        Looper.loop()

       //}



    }
}