package com.geded.apartemenku

import android.content.Context
import android.content.SharedPreferences
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
            // Handle the notification message here.
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