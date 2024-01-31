package com.geded.apartemenku

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityItemRatingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class ItemRatingActivity : AppCompatActivity() {
    private lateinit var binding:ActivityItemRatingBinding
    var itemToRates:ArrayList<ItemToRate> = arrayListOf()
    var serviceRate = 0
    var serviceReview = ""
    var tenant_type = ""
    var transaction_id = 0
    var token = ""
    companion object{
        val TRANSACTION_ID = "TRANSACTION_ID"
        val TENANT_TYPE = "TENANT_TYPE"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN, "").toString()
        transaction_id = intent.getIntExtra(TRANSACTION_ID, 0)
        tenant_type = intent.getStringExtra(TENANT_TYPE).toString()

        if(tenant_type == "product"){
            binding.txtItemRateTitle.text = "Selesaikan Transaksi"
            binding.btnRate.text = "Selesaikan Transaksi"
        }
        else{
            binding.txtItemRateTitle.text = "Berikan Rating"
            binding.btnRate.text = "Kirimkan Rating"
        }

        binding.btnSvcStarOne.setOnClickListener{
            serviceRate = 1
            binding.btnSvcStarOne.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarTwo.setImageResource(R.drawable.baseline_star_rate_grey_24)
            binding.btnSvcStarThree.setImageResource(R.drawable.baseline_star_rate_grey_24)
            binding.btnSvcStarFour.setImageResource(R.drawable.baseline_star_rate_grey_24)
            binding.btnSvcStarFive.setImageResource(R.drawable.baseline_star_rate_grey_24)
        }
        binding.btnSvcStarTwo.setOnClickListener{
            serviceRate = 2
            binding.btnSvcStarOne.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarTwo.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarThree.setImageResource(R.drawable.baseline_star_rate_grey_24)
            binding.btnSvcStarFour.setImageResource(R.drawable.baseline_star_rate_grey_24)
            binding.btnSvcStarFive.setImageResource(R.drawable.baseline_star_rate_grey_24)
        }
        binding.btnSvcStarThree.setOnClickListener{
            serviceRate = 3
            binding.btnSvcStarOne.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarTwo.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarThree.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarFour.setImageResource(R.drawable.baseline_star_rate_grey_24)
            binding.btnSvcStarFive.setImageResource(R.drawable.baseline_star_rate_grey_24)
        }
        binding.btnSvcStarFour.setOnClickListener{
            serviceRate = 4
            binding.btnSvcStarOne.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarTwo.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarThree.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarFour.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarFive.setImageResource(R.drawable.baseline_star_rate_grey_24)
        }
        binding.btnSvcStarFive.setOnClickListener{
            serviceRate = 5
            binding.btnSvcStarOne.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarTwo.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarThree.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarFour.setImageResource(R.drawable.baseline_star_rate_24)
            binding.btnSvcStarFive.setImageResource(R.drawable.baseline_star_rate_24)
        }

        binding.btnRate.setOnClickListener {
            var nullItemRates:ArrayList<Int> = arrayListOf()
            itemToRates.forEach {itr ->
                if(itr.rating == null || itr.review == null){
                    nullItemRates.add(itr.item_id)
                }
            }
            serviceReview = binding.txtSvcReviewRate.text.toString()
            if(nullItemRates.size == 0 && serviceRate > 0 && serviceReview != ""){
                finishTransaction()
            }
            else{
                Toast.makeText(this, "Mohon untuk mengisi semua rating dan review", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    fun getData(){
        itemToRates.clear()
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/getitemtorate"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val data = obj.getJSONArray("data")
                    for (i in 0 until data.length()) {
                        var itemObj = data.getJSONObject(i)
                        val itemToRate = ItemToRate(itemObj.getInt("id"), itemObj.getString("name"), null, null)
                        itemToRates.add(itemToRate)
                    }
                    updateList()
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
    fun updateList() {
        val lm: LinearLayoutManager = LinearLayoutManager(this)
        var recyclerView = binding.recViewItemToRate
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = ItemToRateAdapter(itemToRates, this)
        recyclerView.isVisible = true
    }
    fun finishTransaction(){
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/rateitem"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Terima Kasih atas Rating dan Transaksi Anda!", Toast.LENGTH_SHORT).show()
                    finish()
                } else if (obj.getString("status") == "notauthenticated") {
                    Helper.logoutSystem(this)
                } else {
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
                builder.setPositiveButton("OK"){dialog, which->
                }
                builder.create().show()
            }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["transaction_id"] = transaction_id.toString()
                itemToRates.forEachIndexed { idx, it ->
                    params["items_id[$idx]"] = it.item_id.toString()
                    params["items_rating[$idx]"] = it.rating.toString()
                    params["items_review[$idx]"] = it.review.toString()
                }
                params["service_rating"] = serviceRate.toString()
                params["service_review"] = serviceReview
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
}