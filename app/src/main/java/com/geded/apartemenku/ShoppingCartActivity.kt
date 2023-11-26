package com.geded.apartemenku

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
    var token = ""
    companion object{
        val CART = "CART"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN, "").toString()

        binding.cardViewPayment.isVisible = false
        binding.recViewShoppingCart.isVisible = false
        binding.progressBarSC.isVisible = true

        getCart()
    }

    fun getCart(){
        cartList.clear()
        binding.cardViewPayment.isVisible = false
        binding.recViewShoppingCart.isVisible = false
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
                                    cartObj.getString("tenant_name"),
                                    cartObj.getString("photo_url")
                                )
                                cartList.add(cartListItem)
                            }

                            val total = Helper.formatter(obj.getDouble("total"))
                            binding.txtTotalCart.text = "Rp$total"
                            updateList()
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

    fun checkout(){

    }
}