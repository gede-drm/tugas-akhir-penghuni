package com.geded.apartemenku

import android.R
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityShoppingCartBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class ShoppingCartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShoppingCartBinding
    var cartList:ArrayList<Cart> = arrayListOf()
    var checkoutConfigs:ArrayList<ProCheckoutConfig> = arrayListOf()
    var unit_id = 0
    var token = ""
    companion object{
        val CART = "CART"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        unit_id = shared.getInt(LoginActivity.RESIDENTID, 0)
        token = shared.getString(LoginActivity.TOKEN, "").toString()

        binding.cardViewPayment.isVisible = false
        binding.recViewShoppingCart.isVisible = false
        binding.recViewCC.isVisible = false
        binding.txtCC.isVisible = false
        binding.progressBarSC.isVisible = true

        binding.btnCheckoutSC.setOnClickListener {
            checkout()
        }
    }

    override fun onResume() {
        super.onResume()
        getCart()
    }

    fun getCart(){
        cartList.clear()
        binding.cardViewPayment.isVisible = false
        binding.recViewShoppingCart.isVisible = false
        binding.recViewCC.isVisible = false
        binding.progressBarSC.isVisible = true

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        var cart = shared.getString(CART, "").toString()
        if(cart == "") {
            binding.cardViewPayment.isVisible = false
            binding.recViewShoppingCart.isVisible = false
            binding.txtEmptySC.isVisible = true
            binding.progressBarSC.isVisible = false
        }
        else{
            if(cart.isNotEmpty()) {
                val sType = object : TypeToken<List<TempCart>>() {}.type
                var itemCarts = Gson().fromJson(cart.toString(), sType) as ArrayList<TempCart>
                Log.d("SHOWCART", itemCarts.toString())

                val q = Volley.newRequestQueue(this)
                val url = Global.urlWS + "productcart"

                var stringRequest = object : StringRequest(
                    Method.POST, url, Response.Listener {
                        Log.d("VOLLEY", it)
                        val obj = JSONObject(it)
                        if (obj.getString("status") == "success") {
                            val emptyStatus = obj.getString("emptyStatus")
                            if (emptyStatus != "empty") {
                                val emptyIds = obj.getJSONArray("emptyids")
                                var itemRemove: ArrayList<TempCart> = arrayListOf()
                                for (i in 0 until emptyIds.length()) {
                                    itemCarts.forEach { ic ->
                                        if (ic.item_id.toString() == emptyIds[i]) {
                                            itemRemove.add(ic)
                                        }
                                    }
                                }

                                itemRemove.forEach { ir ->
                                    itemCarts.remove(ir)
                                }

                                var editor: SharedPreferences.Editor = shared.edit()
                                editor.putString(CART, Gson().toJson(itemCarts))
                                editor.apply()

                                val builder = MaterialAlertDialogBuilder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Yahh :(")
                                builder.setMessage("Mohon Maaf, Terdapat Beberapa Barang yang Kosong dan Telah dikeluarkan dari Keranjang Anda.")
                                builder.setPositiveButton("OK") { dialog, which ->
                                }
                                builder.create().show()
                            }

                            val cartData = obj.getJSONArray("data")
                            for (i in 0 until cartData.length()) {
                                val cartObj = cartData.getJSONObject(i)
                                val cartListItem = Cart(
                                    cartObj.getInt("id"),
                                    cartObj.getInt("qty"),
                                    cartObj.getString("name"),
                                    cartObj.getDouble("price"),
                                    cartObj.getDouble("subtotal"),
                                    cartObj.getInt("tenant_id"),
                                    cartObj.getString("tenant_name"),
                                    cartObj.getString("photo_url"),
                                    cartObj.getInt("cash")
                                )
                                cartList.add(cartListItem)
                            }

                            val total = Helper.formatter(obj.getDouble("total"))
                            binding.txtTotalCart.text = "Rp$total"
                            updateList()

                            val tenantData = obj.getJSONArray("delivery")
                            checkoutConfigs.clear()
                            for (i in 0 until tenantData.length()) {
                                val tData = tenantData.getJSONObject(i)
                                val tConfig = ProCheckoutConfig(
                                    tData.getInt("id"),
                                    tData.getString("tenant"),
                                    tData.getInt("cash"),
                                    tData.getInt("delivery"),
                                    tData.getString("open_hour"),
                                    tData.getString("close_hour"),
                                    null,
                                    null,
                                    null,
                                    null
                                )
                                checkoutConfigs.add(tConfig)
                            }
                            updateListConfig()
                        } else if(obj.getString("status") == "allempty") {
                            binding.cardViewPayment.isVisible = false
                            binding.recViewShoppingCart.isVisible = false
                            binding.txtEmptySC.isVisible = true
                            binding.progressBarSC.isVisible = false

                            val builder = MaterialAlertDialogBuilder(this)
                            builder.setCancelable(false)
                            builder.setTitle("Yahh :(")
                            builder.setMessage("Mohon Maaf, Semua Barang di Keranjang Anda Telah Habis Stoknya dan Telah dikeluarkan dari Keranjang Anda.")
                            builder.setPositiveButton("OK") { dialog, which ->
                            }
                            builder.create().show()
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
                    },
                    Response.ErrorListener {
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
                        for (i in 0 until itemCarts.size) {
                            params["product_ids[$i]"] = itemCarts[i].item_id.toString()
                            params["product_qtys[$i]"] = itemCarts[i].qty.toString()
                        }
                        params["token"] = token
                        return params
                    }
                }
                stringRequest.setShouldCache(false)
                q.add(stringRequest)
            }
            else{
                binding.cardViewPayment.isVisible = false
                binding.recViewShoppingCart.isVisible = false
                binding.txtEmptySC.isVisible = true
                binding.progressBarSC.isVisible = false
            }
        }
    }

    fun updateList() {
        val lm: LinearLayoutManager = LinearLayoutManager(this)
        var recyclerView = binding.recViewShoppingCart
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = ShoppingCartAdapter(cartList, this)
        recyclerView.isVisible = true
        binding.txtEmptySC.visibility = View.GONE

        binding.cardViewPayment.isVisible = true
        binding.recViewShoppingCart.isVisible = true
        binding.progressBarSC.isVisible = false
    }

    fun updateListConfig(){
        val lm:LinearLayoutManager = LinearLayoutManager(this)
        var recyclerView = binding.recViewCC
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = ProCheckoutConfigAdapter(checkoutConfigs, this)
        recyclerView.isVisible = true
        binding.txtCC.isVisible = true
    }

    fun deleteItem(item_id:Int, item_name:String){
        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        var cart = shared.getString(CART, "").toString()
        val sType = object : TypeToken<List<TempCart>>() { }.type
        var itemCarts = Gson().fromJson(cart.toString(), sType) as ArrayList<TempCart>
        var itemRemove:TempCart ?= null

        itemCarts.forEach { ic ->
            if(ic.item_id == item_id) {
                itemRemove = ic
            }
        }

        if(itemRemove != null){
            itemCarts.remove(itemRemove)
        }

        if(itemCarts.size == 0){
            var editor: SharedPreferences.Editor = shared.edit()
            editor.putString(CART, "")
            editor.apply()
        }
        else{
            var editor: SharedPreferences.Editor = shared.edit()
            editor.putString(CART, Gson().toJson(itemCarts))
            editor.apply()
        }

        Toast.makeText(this, "Item $item_name Berhasil dihapus!", Toast.LENGTH_SHORT).show()

        getCart()
    }

    fun checkout() {
        var configNull: ArrayList<String> = arrayListOf()
        checkoutConfigs.forEach { cc ->
            if (cc.date == null || cc.time == null || cc.payment_method == null || cc.delivery_method == null) {
                configNull.add(cc.tenant_name)
            }
        }
        if (configNull.size > 0) {
            val message = "Konfigurasi Checkout untuk Tenant " + configNull.joinToString(separator = ", ") + " Belum diisi"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } else {
            val q = Volley.newRequestQueue(this)
            val url = Global.urlWS + "tenant/productcheckout"

            var stringRequest = object : StringRequest(
                Method.POST, url, Response.Listener {
                    Log.d("VOLLEY", it)
                    val obj = JSONObject(it)
                    if (obj.getString("status") == "success") {
                        Toast.makeText(this, "Transaksi Berhasil!", Toast.LENGTH_SHORT).show()
                        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
                        var editor: SharedPreferences.Editor = shared.edit()
                        editor.putString(CART, "")
                        editor.apply()

//                        CEK ADA TRANSFER/TIDAK, KALAU ADA -> Transfer Page & FINISH else FINISH
                    } else if(obj.getString("status") == "failednostock") {
                        val builder = MaterialAlertDialogBuilder(this)
                        builder.setCancelable(false)
                        builder.setTitle("Gagal Membuat Transaksi")
                        builder.setMessage("Mohon maaf, terdapat barang yang tiba-tiba stoknya habis dan semua transaksi tidak dibuat.\nSilakan lakukan pembayaran ulang untuk barang lainnya.")
                        builder.setPositiveButton("OK") { dialog, which ->
                        }
                        builder.create().show()
                    }
                    else{
                        val builder = MaterialAlertDialogBuilder(this)
                        builder.setCancelable(false)
                        builder.setTitle("Gagal Membuat Transaksi")
                        builder.setMessage("Terdapat Masalah pada Server\nSilakan Coba Lagi Nanti.")
                        builder.setPositiveButton("OK") { dialog, which ->
                        }
                        builder.create().show()
                    }
                },
                Response.ErrorListener {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK") { dialog, which ->
                    }
                    builder.create().show()
                }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    for (i in 0 until cartList.size) {
                        params["product_ids[$i]"] = cartList[i].item_id.toString()
                        params["product_qtys[$i]"] = cartList[i].qty.toString()
                        params["product_prices[$i]"] = cartList[i].item_price.toString()
                        params["product_tenants[$i]"] = cartList[i].tenant_id.toString()
                    }
                    for (i in 0 until checkoutConfigs.size) {
                        params["tenant_ids[$i]"] = checkoutConfigs[i].tenant_id.toString()
                        params["tenant_deliveries[$i]"] = checkoutConfigs[i].delivery_method.toString()
                        params["tenant_datetimes[$i]"] =  checkoutConfigs[i].date.toString() + " " + checkoutConfigs[i].time.toString()
                        params["tenant_paymethods[$i]"] = checkoutConfigs[i].payment_method.toString()
                    }
                    params["unit_id"] = unit_id.toString()
                    params["token"] = token
                    return params
                }
            }
            val retryPolicy = DefaultRetryPolicy(7500, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            stringRequest.retryPolicy = retryPolicy;
            stringRequest.setShouldCache(false)
            q.add(stringRequest)
        }
    }
}