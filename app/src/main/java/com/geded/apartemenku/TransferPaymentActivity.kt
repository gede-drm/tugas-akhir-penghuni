package com.geded.apartemenku

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityTransferPaymentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class TransferPaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransferPaymentBinding
    var transferTransactions:ArrayList<TransferTransaction> = arrayListOf()
    var tfTrxPosition = -1
    var tfIdsString = ""
    var tfIds:List<String> = listOf()
    val REQUEST_GALLERY = 2
    var unit_id = 0
    var token = ""

    var tempUriBase64 = ""
    var tempImageExtras:Uri ?= null

    companion object{
        val TRANSACTION_IDS = "TRANSACTION_IDS"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        unit_id = shared.getInt(LoginActivity.RESIDENTID, 0)
        token = shared.getString(LoginActivity.TOKEN, "").toString()

        tfIdsString = intent.getStringExtra(TRANSACTION_IDS).toString()

        tfIds = tfIdsString.split(';')

        getData()
    }

    fun pickPicture(position:Int){
        tfTrxPosition = position

        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_PICK
        startActivityForResult(i, REQUEST_GALLERY)
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
                    pickPicture(tfTrxPosition)
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
                tempUriBase64 = getImageUriFromBitmap(imageBitmap)

                if(tfTrxPosition != -1){
                    transferTransactions[tfTrxPosition].extrasImage = extras
                    transferTransactions[tfTrxPosition].base64Image = tempUriBase64

                    tfTrxPosition = -1

                    updateList()
                }
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

    fun getData() {
        transferTransactions.clear()
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/getunpaidprotransactions"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val data = obj.getJSONArray("data")
                    for (i in 0 until data.length()) {
                        var tfObj = data.getJSONObject(i)
                        val tf = TransferTransaction(tfObj.getInt("id"), tfObj.getString("transaction_date"), tfObj.getDouble("total_payment"), tfObj.getString("finish_date"), tfObj.getString("tenant_name"), tfObj.getString("bank_name"),tfObj.getString("account_holder"), tfObj.getString("account_number"), null, null)
                        transferTransactions.add(tf)
                    }
                    updateList()
                } else if (obj.getString("status") == "empty") {
                    binding.progressBarTF.visibility = View.GONE
                    Toast.makeText(this, "Seluruh Transaksi Transfer Telah dibayarkan", Toast.LENGTH_SHORT).show()
                    finish()
                } else if (obj.getString("status") == "notauthenticated") {
                    binding.progressBarTF.visibility = View.GONE
                    Helper.logoutSystem(this)
                }
                else{
                    binding.progressBarTF.visibility = View.GONE
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
                params["unit_id"] = unit_id.toString()
                tfIds.forEachIndexed { idx, tI ->
                    params["transaction_ids[$idx]"] = tI
                }
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun updateList(){
        val lm: LinearLayoutManager = LinearLayoutManager(this)
        val adapter = TransferTransactionListAdapter(transferTransactions, this)
        var recyclerView = binding.recViewTFCheckout
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        adapter.notifyDataSetChanged()
        recyclerView.adapter = adapter
        recyclerView.isVisible = true
        binding.progressBarTF.visibility = View.GONE
    }

    fun uploadProof(position: Int){
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/uploadtransferproof"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val message = "Bukti Transfer untuk Transaksi " + transferTransactions[position].tenant_name + " (Tgl Transaksi: " + transferTransactions[position].transaction_date +") Berhasil diunggah!"
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    getData()
                } else if (obj.getString("status") == "notauthenticated") {
                    binding.progressBarTF.visibility = View.GONE
                    Helper.logoutSystem(this)
                }
                else{
                    binding.progressBarTF.visibility = View.GONE
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
                params["transaction_id"] = transferTransactions[position].id.toString()
                params["proof_image"] = transferTransactions[position].base64Image.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
}