package com.geded.apartemenku

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityServiceTransferPaymentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class ServiceTransferPaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServiceTransferPaymentBinding
    val REQUEST_GALLERY = 2
    var transaction_id = 0
    var token = ""

    var uriBase64 = ""
    companion object{
        val TRX_ID = "TRX_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceTransferPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN, "").toString()

        transaction_id = intent.getIntExtra(TRX_ID, 0)
        binding.cardViewTFS.isVisible = false

        binding.btnCopyBankAccTFS.setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("account_number", binding.txtBAccNumTFS.text.toString())
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this,"Nomor Rekening telah disalin", Toast.LENGTH_SHORT).show()
        }

        binding.btnPickProofTFS.setOnClickListener {
            pickPicture()
        }

        binding.btnUploadProofTFS.setOnClickListener {
            if(uriBase64 != ""){
                uploadProof()
            }
            else{
                Toast.makeText(this,"Silakan Memasukkan Bukti Transfer Telebih Dahulu!", Toast.LENGTH_SHORT).show()
            }
        }

        getData()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickPicture()
                } else {
                    Toast.makeText(this, "Anda harus memperbolehkan akses aplikasi ke penyimpanan", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == REQUEST_GALLERY){
                val extras = data?.data
                val imageBitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, extras)

                binding.imgProofTFS.setImageURI(extras)
                uriBase64 = getImageUriFromBitmap(imageBitmap)
            }
        }
    }

    fun getImageUriFromBitmap(bitmap: Bitmap): String{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes)

        val byteImagePhoto = bytes.toByteArray()
        val encodedImage = "data:image/jpeg;base64," + Base64.encodeToString(byteImagePhoto, Base64.DEFAULT)
        return encodedImage
    }

    fun pickPicture(){
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_PICK
        startActivityForResult(i, REQUEST_GALLERY)
    }

    fun getData(){
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/getunpaidsvctransaction"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    binding.cardViewTFS.isVisible = true
                    val dataObj = obj.getJSONObject("data")
                    val total = Helper.formatter(dataObj.getDouble("total_payment"))
                    binding.txtTenNameTFS.text = dataObj.getString("tenant_name")
                    binding.txtTrxDateTFS.text = "Tanggal Transaksi: " + dataObj.getString("transaction_date")
                    binding.txtFinishDateTFS.text = "Tanggal Pengerjaan: " + dataObj.getString("finish_date")
                    binding.txtTotalTrxTFS.text = "Rp$total"
                    binding.txtBankNameTFS.text = dataObj.getString("bank_name") + " a.n. " + dataObj.getString("account_holder")
                    binding.txtBAccNumTFS.text = dataObj.getString("account_number")
                } else if (obj.getString("status") == "empty") {
                    binding.cardViewTFS.isVisible = false
                    binding.progressBarTFS.visibility = View.GONE
                    Toast.makeText(this, "Seluruh Transaksi Transfer Telah dibayarkan", Toast.LENGTH_SHORT).show()
                    finish()
                } else if (obj.getString("status") == "notauthenticated") {
                    binding.cardViewTFS.isVisible = false
                    binding.progressBarTFS.visibility = View.GONE
                    Helper.logoutSystem(this)
                }
                else{
                    binding.cardViewTFS.isVisible = false
                    binding.progressBarTFS.visibility = View.GONE
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK") { dialog, which ->
                        finish()
                    }
                    builder.create().show()
                }
            }, Response.ErrorListener {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK") { dialog, which ->
                    finish()
                }
                builder.create().show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["transaction_id"] = transaction_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun uploadProof(){
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/uploadtransferproof"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val message = "Bukti Transfer untuk Berhasil diunggah!"
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    finish()
                } else if (obj.getString("status") == "notauthenticated") {
                    Helper.logoutSystem(this)
                }
                else{
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK") { dialog, which ->
                    }
                    builder.create().show()
                }
            }, Response.ErrorListener {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK") { dialog, which ->
                    finish()
                }
                builder.create().show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["transaction_id"] = transaction_id.toString()
                params["proof_image"] = uriBase64
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
}
