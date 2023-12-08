package com.geded.apartemenku

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityMainBinding
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val fragments: ArrayList<Fragment> = ArrayList()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            Toast.makeText(this, "Aplikasi ApartemenKu Tidak Dapat Memunculkan Notifikasi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* BottomNav */
        fragments.add(HomeFragment())
        fragments.add(TransactionFragment())
        fragments.add(ProfileFragment())

        binding.viewPager.adapter = ViewPagerAdapter(this, fragments)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.bottomNavView.selectedItemId = binding.bottomNavView.menu.getItem(position).itemId
            }
        })
        binding.bottomNavView.setOnItemSelectedListener {
            binding.viewPager.currentItem = when(it.itemId){
                R.id.itemHome -> 0
                R.id.itemTransaction -> 1
                R.id.itemProfile -> 2
                else -> 0
            }
            true
        }
        /* End Bottom Nav */
        askNotificationPermission()

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        var unit_idSP = shared.getInt(LoginActivity.RESIDENTID, 0)
        var fcm_tokenSP = shared.getString(LoginActivity.FCMTOKENSP, "").toString()
        var fcm_tokenReal = MyFirebaseMessagingService.getToken(this)
        var token = shared.getString(LoginActivity.TOKEN, "").toString()
        if(fcm_tokenSP != fcm_tokenReal){
            var editor: SharedPreferences.Editor = shared.edit()
            editor.putString(LoginActivity.FCMTOKENSP, fcm_tokenReal)
            editor.apply()

            var q = Volley.newRequestQueue(this)
            val url = Global.urlWS + "registerfcm"

            val stringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener {
                    Log.d("Success", it)
                    var obj = JSONObject(it)
                    var resultDb = obj.getString("status")
                    if (resultDb == "success") {

                    } else {
                        Toast.makeText(this, "Terdapat Kesalahan, Silakan Muat Ulang Aplikasi!", Toast.LENGTH_SHORT).show()
                    } },
                Response.ErrorListener {
                }) {

                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["unit_id"] = unit_idSP.toString()
                    params["token"] = token
                    params["fcm_token"] = fcm_tokenReal
                    return params
                }
            }
            stringRequest.setShouldCache(false)
            q.add(stringRequest)
        }
    }
}