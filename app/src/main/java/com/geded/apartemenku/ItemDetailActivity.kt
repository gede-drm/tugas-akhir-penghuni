package com.geded.apartemenku

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityItemDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import org.json.JSONObject

class ItemDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemDetailBinding
    var reviews:ArrayList<Review> = arrayListOf()
    var item_id = 0
    var token = ""
    var item_type = ""
    companion object{
        val ITEM_ID = "ITEM_ID"
        val ITEM_TYPE = "ITEM_TYPE"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        item_id = intent.getIntExtra(ITEM_ID, 0)
        item_type = intent.getStringExtra(ITEM_TYPE).toString()

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN, "").toString()

        binding.scrollViewItemDetail.isVisible = false

        if(item_type == "product"){
            binding.btnCartItemDetail.text = "Tambahkan ke Keranjang"
            getProductDetail()
        }
        else{
            binding.btnCartItemDetail.text = "Beli Sekarang"
            getServiceDetail()
        }
    }

    fun getProductDetail(){
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "tenant/productdetail"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val itemObj = obj.getJSONObject("data")
                    val stock = itemObj.getInt("stock")
                    if(stock == 0){
                        val builder = MaterialAlertDialogBuilder(this)
                        builder.setCancelable(false)
                        builder.setTitle("Stok Habis")
                        builder.setMessage("Mohon Maaf, Stok Barang Ini Baru Saja Habis!")
                        builder.setPositiveButton("OK"){dialog, which->
                            finish()
                        }
                        builder.create().show()
                    }
                    else {
                        val statusReview = itemObj.getString("reviewsStatus")

                        val photo_url = itemObj.getString("photo_url")
                        val price = Helper.formatter(itemObj.getDouble("price"))

                        Picasso.get().load(photo_url).into(binding.imgViewItemDetail)
                        binding.txtNameItemDetail.text = itemObj.getString("name")
                        binding.txtPriceItemDetail.text = "Rp$price"
                        binding.txtSoldItemDetail.text =
                            "Terjual " + itemObj.getInt("sold").toString()
                        binding.txtRatingDetailItem.text = itemObj.getDouble("rating").toString()
                        binding.txtDescItemDetail.text = itemObj.getString("description")

                        if (statusReview != "empty") {
                            val arrReview = itemObj.getJSONArray("reviews")
                            for (i in 0 until arrReview.length()) {
                                val reviewObj = arrReview.getJSONObject(i)
                                val review = Review(
                                    reviewObj.getString("unit_no"),
                                    reviewObj.getInt("rating"),
                                    reviewObj.getString("review")
                                )
                                reviews.add(review)
                            }
                            updateListReview()
                        } else {
                            binding.recViewItemReviews.isVisible = false
                            binding.txtEmptyItemReview.isVisible = true
                        }

                        binding.progressBarItemDetail.isVisible = false
                        binding.scrollViewItemDetail.isVisible = true
                    }
                }
                else if(obj.getString("status")=="productnull"){
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Barang Tidak Tersedia")
                    builder.setMessage("Mohon Maaf, Barang ini Baru Saja ditiadakan oleh Tenant!")
                    builder.setPositiveButton("OK"){dialog, which->
                        finish()
                    }
                    builder.create().show()
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
                params["product_id"] = item_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun getServiceDetail(){
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "tenant/servicedetail"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val itemObj = obj.getJSONObject("data")
                    val availability = itemObj.getInt("availability")
                    if(availability == 0){
                        val builder = MaterialAlertDialogBuilder(this)
                        builder.setCancelable(false)
                        builder.setTitle("Jas Tidak Tersedia")
                        builder.setMessage("Mohon Maaf, Jasa Ini Tidak Tersedia untuk Sementara Waktu!")
                        builder.setPositiveButton("OK"){dialog, which->
                            finish()
                        }
                        builder.create().show()
                    }
                    else {
                        val statusReview = itemObj.getString("reviewsStatus")

                        val photo_url = itemObj.getString("photo_url")
                        val price = Helper.formatter(itemObj.getDouble("price"))
                        var pricePer = itemObj.getString("pricePer")
                        if (pricePer == "hour") {
                            pricePer = "Jam"
                        } else {
                            pricePer = "Paket"
                        }

                        Picasso.get().load(photo_url).into(binding.imgViewItemDetail)
                        binding.txtNameItemDetail.text = itemObj.getString("name")
                        binding.txtPriceItemDetail.text = "Rp$price/$pricePer"
                        binding.txtSoldItemDetail.text =
                            "Terjual " + itemObj.getInt("sold").toString()
                        binding.txtRatingDetailItem.text = itemObj.getDouble("rating").toString()
                        binding.txtDescItemDetail.text = itemObj.getString("description")

                        if (statusReview != "empty") {
                            val arrReview = itemObj.getJSONArray("reviews")
                            for (i in 0 until arrReview.length()) {
                                val reviewObj = arrReview.getJSONObject(i)
                                val review = Review(
                                    reviewObj.getString("unit_no"),
                                    reviewObj.getInt("rating"),
                                    reviewObj.getString("review")
                                )
                                reviews.add(review)
                            }
                            updateListReview()
                        } else {
                            binding.recViewItemReviews.isVisible = false
                            binding.txtEmptyItemReview.isVisible = true
                        }

                        binding.progressBarItemDetail.isVisible = false
                        binding.scrollViewItemDetail.isVisible = true
                    }
                }
                else if(obj.getString("status")=="productnull"){
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Jasa Tidak Tersedia")
                    builder.setMessage("Mohon Maaf, Jasa ini Baru Saja ditiadakan oleh Tenant!")
                    builder.setPositiveButton("OK"){dialog, which->
                        finish()
                    }
                    builder.create().show()
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
                params["service_id"] = item_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun updateListReview() {
        val lm:LinearLayoutManager = LinearLayoutManager(this)
        var recyclerView = binding.recViewItemReviews
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = ItemReviewAdapter(reviews, this)
        recyclerView.isVisible = true
        binding.txtEmptyItemReview.isVisible = false
    }
}