package com.geded.apartemenku

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import java.text.DecimalFormat

class Helper {
    companion object{
        fun formatter(n: Double): String {
            return DecimalFormat("#,###.00").format(n)
        }
        fun logoutSystem(activity: Activity) {
            var shared: SharedPreferences =  activity.getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
            var editor: SharedPreferences.Editor = shared.edit()
            editor.putString(LoginActivity.USERNAME, "")
            editor.putInt(LoginActivity.RESIDENTID, 0)
            editor.putString(LoginActivity.UNITNO, "")
            editor.putString(LoginActivity.HOLDERNAME, "")
            editor.putString(ShoppingCartActivity.CART, "")
            editor.putString(LoginActivity.TOKEN, "")
            editor.apply()

            var alertMessage = "Anda Tercatat Login di Perangkat Lain."

            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(false)
            builder.setTitle("Logout Otomatis")
            builder.setMessage(alertMessage)
            builder.setPositiveButton("OK") { dialog, which ->
                val intent = Intent(activity, LoginActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            }
            builder.create().show()
        }
    }
}