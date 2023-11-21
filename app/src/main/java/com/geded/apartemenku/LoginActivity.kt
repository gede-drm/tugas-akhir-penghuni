package com.geded.apartemenku

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityLoginBinding
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    companion object{
        val USERNAME = "USERNAME"
        val RESIDENTID = "RESIDENTID"
        val UNITNO = "UNITNO"
        val HOLDERNAME = "HOLDERNAME"
        val TOKEN = "TOKEN"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        var usernameSP = shared.getString(USERNAME, "")

        if(usernameSP != "")
        {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        binding.btnLogin.setOnClickListener {
            val inputUsername = binding.txtUsername.text
            val inputPassword = binding.txtPassword.text
            if ((inputUsername.toString() != "") && (inputPassword.toString() != "")) {
                var q = Volley.newRequestQueue(this)
                val url = Global.urlWS + "login"

                val stringRequest = object : StringRequest(
                    Method.POST, url,
                    Response.Listener {
                        Log.d("Success", it)
                        var obj = JSONObject(it)
                        var resultDb = obj.getString("status")
                        if (resultDb == "success") {
                            var array = obj.getJSONObject("data")
                            var username = inputUsername
                            var resident_id = array["resident_id"]
                            var unit_no = array["unit_no"]
                            var holder_name = array["holder_name"]
                            var token = array["token"]

                            var editor: SharedPreferences.Editor = shared.edit()
                            editor.putString(USERNAME, username.toString())
                            editor.putInt(RESIDENTID, resident_id.toString().toInt())
                            editor.putString(UNITNO, unit_no.toString())
                            editor.putString(HOLDERNAME, holder_name.toString())
                            editor.putString(TOKEN, token.toString())
                            editor.apply()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            this.finish()
                        }else if (resultDb == "notactive") {
                            Toast.makeText(this, "Unit Ini Sedang Berstatus Nonaktif!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Username atau Password Salah!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    Response.ErrorListener {
                        Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show()
                    }) {

                    override fun getParams(): MutableMap<String, String> {
                        val params = HashMap<String, String>()
                        params["username"] = inputUsername.toString()
                        params["password"] = inputPassword.toString()
                        return params
                    }
                }
                stringRequest.setShouldCache(false)
                q.add(stringRequest)
            } else{
                Toast.makeText(this, "Username atau Password Tidak Boleh Kosong!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}