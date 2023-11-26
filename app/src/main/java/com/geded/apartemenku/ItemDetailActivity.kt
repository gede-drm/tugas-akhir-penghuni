package com.geded.apartemenku

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityItemDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import org.json.JSONArray
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
            binding.btnAddToCartItemDetail.text = "Tambahkan ke Keranjang"
            binding.btnSubtItemDetail.setOnClickListener {
                var count = binding.txtNumCartDetail.text.toString().toInt()
                if(count > 1){
                    count--
                    binding.txtNumCartDetail.setText(count.toString())
                }
                else{
                    Toast.makeText(this, "Jumlah Barang Tidak Dapat Kurang dari 0", Toast.LENGTH_SHORT).show()
                }
            }
            binding.btnPlusItemDetail.setOnClickListener {
                var count = binding.txtNumCartDetail.text.toString().toInt()
                if(count >= 1){
                    count++
                    binding.txtNumCartDetail.setText(count.toString())
                }
            }
            binding.btnAddToCartItemDetail.setOnClickListener {
                addToCart()
            }
            getProductDetail()
        }
        else {
            binding.btnSubtItemDetail.isVisible = false
            binding.btnPlusItemDetail.isVisible = false
            binding.txtNumCartDetail.isVisible = false
            binding.btnAddToCartItemDetail.text = "Beli Sekarang"
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
                else if(obj.getString("status")=="servicenull"){
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

    fun addToCart(){
        val qty = binding.txtNumCartDetail.text.toString().toInt()
        if(qty > 0){
            var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
            var cart = shared.getString(ShoppingCartActivity.CART, "").toString()
            if(cart == ""){
                var cartItems = arrayListOf<TempCart>()
                val tempCart = TempCart(item_id, qty)
                cartItems.add(tempCart)

                var editor: SharedPreferences.Editor = shared.edit()
                editor.putString(ShoppingCartActivity.CART, Gson().toJson(cartItems))
                editor.apply()

                Toast.makeText(this, "Item Berhasil  dimasukkan ke Keranjang", Toast.LENGTH_SHORT).show()
            }
            else{
                val sType = object : TypeToken<List<TempCart>>() { }.type
                var itemCarts = Gson().fromJson(cart.toString(), sType) as ArrayList<TempCart>
                var found = false
                itemCarts.forEach{
                    if(it.item_id == item_id) {
                        it.qty = it.qty + qty
                        found = true
                    }
                }
                if(found == false){
                    val tempCart = TempCart(item_id, qty)
                    itemCarts.add(tempCart)
                }

                var editor: SharedPreferences.Editor = shared.edit()
                editor.putString(ShoppingCartActivity.CART, Gson().toJson(itemCarts))
                editor.apply()

                Toast.makeText(this, "Item Berhasil  dimasukkan ke Keranjang", Toast.LENGTH_SHORT).show()
            }
        }
        else{
            Toast.makeText(this, "Jumlah Barang Tidak Dapat Kurang dari 0", Toast.LENGTH_SHORT).show()
        }
    }
}