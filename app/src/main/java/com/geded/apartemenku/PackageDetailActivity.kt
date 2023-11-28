package com.geded.apartemenku

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityPackageDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import org.json.JSONObject

class PackageDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPackageDetailBinding
    var urlQR = ""
    var package_id = 0
    var token = ""
    companion object{
        val PACKAGE_ID = "PACKAGE_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        package_id = intent.getIntExtra(PACKAGE_ID, 0)
        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN, "").toString()

        binding.progressBarPkgDetail.isVisible = true
        binding.imgViewDetailPkg.isVisible = false
        binding.textView8.isVisible = false
        binding.textView9.isVisible = false
        binding.textView10.isVisible = false
        binding.txtRcvDateDetailPkg.isVisible = false
        binding.txtPickDateDetailPkg.isVisible = false
        binding.txtDescriptionDetailPkg.isVisible = false
        binding.btnShowQRDetailPkg.isVisible = false

        getData()

        binding.btnShowQRDetailPkg.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view =layoutInflater.inflate(R.layout.bottom_sheet_qr_pkg, null)
            dialog.setCancelable(false)
            dialog.setContentView(view)

            dialog.show()
            val imgQRPkg = view.findViewById<ImageView>(R.id.imgQRPkg)
            Picasso.get().load(urlQR).into(imgQRPkg)

            view.findViewById<Button>(R.id.btnCloseDialogQRPkg).setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    fun getData(){
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "package/detail"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    binding.progressBarPkgDetail.isVisible = false
                    binding.textView8.isVisible = true
                    binding.textView9.isVisible = true
                    binding.textView10.isVisible = true
                    binding.imgViewDetailPkg.isVisible = true
                    binding.txtRcvDateDetailPkg.isVisible = true
                    binding.txtPickDateDetailPkg.isVisible = true
                    binding.txtDescriptionDetailPkg.isVisible = true

                    val pkgObj = obj.getJSONObject("data")
                    val photo_url = pkgObj.getString("photo_url")
                    urlQR = pkgObj.getString("qr_url")
                    Picasso.get().load(photo_url).into(binding.imgViewDetailPkg)
                    binding.txtRcvDateDetailPkg.text = pkgObj.getString("receive_date") + " (Petugas: " + pkgObj.getString("rcv_officer") + ")"
                    binding.txtDescriptionDetailPkg.text = pkgObj.getString("description")
                    if(pkgObj.getString("pickup_date") != ""){
                        binding.txtPickDateDetailPkg.text = pkgObj.getString("pickup_date") + " (Petugas: " + pkgObj.getString("pick_officer") + ")"
                        binding.btnShowQRDetailPkg.isVisible = false
                    }
                    else{
                        binding.txtPickDateDetailPkg.setTextColor(ContextCompat.getColor(binding.txtPickDateDetailPkg.context, R.color.md_theme_dark_onError))
                        binding.txtPickDateDetailPkg.text = "Belum Diambil"
                        binding.btnShowQRDetailPkg.isVisible = true
                    }
                }
                else if(obj.getString("status")=="notauthenticated"){
                    Helper.logoutSystem(this)
                }
                else{
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK"){dialog, which->
                        finish()
                    }
                    builder.create().show()
                }
            },
            Response.ErrorListener {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK"){dialog, which->
                    finish()
                }
                builder.create().show()
            }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["package_id"] = package_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
}