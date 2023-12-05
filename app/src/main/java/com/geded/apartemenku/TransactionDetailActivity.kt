package com.geded.apartemenku

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityTransactionDetailBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import org.json.JSONObject

class TransactionDetailActivity : AppCompatActivity() {
    private lateinit var binding:ActivityTransactionDetailBinding
    var trxItems:ArrayList<TransactionDetailItem> = arrayListOf()
    var trxStatuses:ArrayList<TrxStatus> =  arrayListOf()
    var tenant_type = ""
    var transferNotPaid = false
    var paymentproofurl = ""
    var transaction_id = 0
    var token = ""
    companion object{
        val TRANSACTION_ID = "TRANSACTION_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN, "").toString()
        transaction_id = intent.getIntExtra(TRANSACTION_ID, 0)

        binding.btnTFProofDT.setOnClickListener {
            if(transferNotPaid == false){
                if(paymentproofurl != ""){
                    val dialog = BottomSheetDialog(this)
                    val view =layoutInflater.inflate(R.layout.bottom_sheet_transfer_proof, null)
                    dialog.setCancelable(false)
                    dialog.setContentView(view)

                    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

                    dialog.show()
                    val imgTFProofDT = view.findViewById<ImageView>(R.id.imgViewTFProofDT)
                    Picasso.get().load(paymentproofurl).into(imgTFProofDT)

                    view.findViewById<Button>(R.id.btnCloseDialogTFProof).setOnClickListener {
                        dialog.dismiss()
                    }
                }
            }
            else{
                if(tenant_type == "product") {
                    val intent = Intent(this, TransferPaymentActivity::class.java)
                    startActivity(intent)
                }
                else{
                    val intent = Intent(this, ServiceTransferPaymentActivity::class.java)
                    intent.putExtra(ServiceTransferPaymentActivity.TRX_ID, transaction_id)
                    startActivity(intent)
                }
            }
        }
        binding.btnSeeStatusDT.setOnClickListener {
            if(trxStatuses.size > 0) {
                val dialog = BottomSheetDialog(this)
                val view =layoutInflater.inflate(R.layout.bottom_sheet_trx_status, null)
                dialog.setCancelable(false)
                dialog.setContentView(view)

                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

                dialog.show()

                val lm: LinearLayoutManager = LinearLayoutManager(this)
                var recyclerView = view.findViewById<RecyclerView>(R.id.recViewTrxStatus)
                recyclerView.layoutManager = lm
                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = TrxStatusAdapter(trxStatuses, this)
                recyclerView.isVisible = true

                view.findViewById<Button>(R.id.btnCloseDialogTFStatus).setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    fun getData(){
        trxItems.clear()
        trxStatuses.clear()

        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/detail"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val dataObj = obj.getJSONObject("data")

                    binding.txtTenNameDT.text = dataObj.getString("tenant_name")
                    binding.txtTrxDateDT.text = dataObj.getString("transaction_date")
                    binding.txtFinishDateDT.text = dataObj.getString("finish_date")
                    if(dataObj.getString("pickup_date") != ""){
                        binding.txtPickupDateDT.text = dataObj.getString("pickup_date")
                    }
                    else{
                        binding.txtViewTrxPickupDate.isVisible = false
                        binding.txtPickupDateDT.isVisible = false
                    }
                    if(dataObj.getString("delivery") == "delivery"){
                        binding.txtViewTrxDelivery.text = "Tanggal Kirim"
                        binding.txtDeliveryDT.text = "Kirim"
                    }
                    else{
                        if(dataObj.getString("svc_type") == "laundry"){
                            binding.txtDeliveryDT.text = "Taruh-Ambil Sendiri"
                        }
                        else{
                            if(dataObj.getString("tenant_type") == "product") {
                                binding.txtViewTrxDelivery.text = "Tanggal Ambil"
                            }
                            else{
                                binding.txtViewTrxDelivery.text = "Tanggal Pengerjaan"
                            }
                            binding.txtDeliveryDT.text = "Ambil"
                        }
                    }

                    if(dataObj.getString("payment") == "transfer"){
                        binding.txtPayMethodDT.text = "Transfer"
                        if(dataObj.getString("status") == "notransferproof"){
//                            if(dataObj.getString("tenant_type")=="service"){
//                                if(dataObj.getInt("permission_need")==1) {
//                                    binding.btnTFProofDT.isVisible =
//                                        !(dataObj.getString("permission_status") == "notproposed" || dataObj.getString(
//                                            "permission_status"
//                                        ) == "reject")
//                                }
//                                else {
//                                    transferNotPaid = true
//                                    binding.btnTFProofDT.setText("Unggah Bukti Transfer")
//                                }
//                            }
//                            else {
//                                transferNotPaid = true
//                                binding.btnTFProofDT.setText("Unggah Bukti Transfer")
//                            }
                            transferNotPaid = true
                            binding.btnTFProofDT.setText("Unggah Bukti Transfer")
                        }
                        else{
                            paymentproofurl = dataObj.getString("payment_proof_url")
                        }
                    }
                    else{
                        binding.txtPayMethodDT.text = "Tunai"
                        binding.btnTFProofDT.isVisible = false
                    }

                    val total_payment = Helper.formatter(dataObj.getDouble("total_payment"))
                    binding.txtTotalPaymentDT.text = "Rp$total_payment"

                    val itemArr = dataObj.getJSONArray("items")
                    for (i in 0 until itemArr.length()) {
                        val itemObj = itemArr.getJSONObject(i)
                        val item = TransactionDetailItem(itemObj.getInt("id"), itemObj.getString("name"), itemObj.getString("photo_url"), itemObj.getDouble("price"), itemObj.getInt("quantity"), itemObj.getString("pricePer"), itemObj.getDouble("subtotal"))
                        trxItems.add(item)
                    }
                    updateListItems()

                    val statusArr = dataObj.getJSONArray("statuses")
                    for (i in 0 until statusArr.length()){
                        val statusObj = statusArr.getJSONObject(i)
                        val status = TrxStatus(statusObj.getString("date"), statusObj.getString("description"))
                        trxStatuses.add(status)
                    }

                    tenant_type = dataObj.getString("tenant_type")
                    transaction_id = dataObj.getInt("id")
                } else if (obj.getString("status") == "notauthenticated") {
                    Helper.logoutSystem(this)
                } else {
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
                builder.setPositiveButton("OK"){dialog, which->
                    finish()
                }
                builder.create().show()
            }){
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

    fun updateListItems(){
        val lm: LinearLayoutManager = LinearLayoutManager(this)
        var recyclerView = binding.recViewItemsDT
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = TrxDetailItemsAdapter(trxItems, this)
        recyclerView.isVisible = true
    }
}