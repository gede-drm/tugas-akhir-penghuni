package com.geded.apartemenku

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

public class MyFirebaseMessagingService: FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        var editor: SharedPreferences.Editor = shared.edit()
        editor.putString(FIREBASE_TOKEN, token)
        editor.apply()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Handle data payload of FCM messages.
        if (message.data.isNotEmpty()) {
            // Handle the data message here.
        }

        // Handle notification payload of FCM messages.
        message.notification?.let {
            val channelId = "APARTEMENKU-NOTIFICATION"
            val notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
                setSmallIcon(R.mipmap.ic_launcher)
                setContentTitle(message.notification!!.title)
                setContentText(message.notification!!.body)
                priority = NotificationCompat.PRIORITY_DEFAULT
                setAutoCancel(true)
            }
            val notificationManager = NotificationManagerCompat.from(this.applicationContext)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val notifChannel:NotificationChannel = NotificationChannel(channelId, "APARTEMENKU-NOTIFICATION-CHANNEL", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(notifChannel)
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Aplikasi ApartemenKu Tidak Dapat Memunculkan Notifikasi", Toast.LENGTH_SHORT).show()
            }
            notificationManager.notify(1001, notificationBuilder.build())
        }
    }
    companion object {
        val FIREBASE_TOKEN = "FIREBASE_TOKEN"
        fun getToken(context: Context):String
        {
            var shared: SharedPreferences = context.getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
            val fcm_token = shared.getString(FIREBASE_TOKEN, "")
            return fcm_token.toString()
        }
    }
}