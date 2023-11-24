package com.geded.apartemenku

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityTenantItemsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class TenantItemsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTenantItemsBinding
    var items:ArrayList<TenantItem> = arrayListOf()
    var tenant_id = 0
    var tenant_name = ""
    var tenant_type = ""
    var tenant_address = ""
    var token = ""
    companion object{
        val TENANT_ID = "TENANT_ID"
        val TENANT_NAME = "TENANT_NAME"
        val TENANT_TYPE = "TENANT_TYPE"
        val TENANT_ADDRESS = "TENANT_ADDRESS"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTenantItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tenant_id = intent.getIntExtra(TENANT_ID, 0)
        tenant_name = intent.getStringExtra(TENANT_NAME).toString()
        tenant_type = intent.getStringExtra(TENANT_TYPE).toString()
        tenant_address = intent.getStringExtra(TENANT_ADDRESS).toString()

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN, "").toString()

        binding.txtTenNameTItems.text = tenant_name
        binding.txtTenAddressTItems.text = tenant_address

        if(tenant_type == "service"){
            binding.btnItemCart.isVisible = false
        }

        binding.txtSearchTenItem.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                val search = binding.txtSearchTenItem.text.toString()
                getTenantItemsData(search)

                true
            }
            else {
                false
            }
        })

        binding.refreshLayoutTenantItems.setOnRefreshListener {
            val search = binding.txtSearchTenItem.text.toString()
            getTenantItemsData(search)
        }
    }

    override fun onResume() {
        super.onResume()
        val search = binding.txtSearchTenItem.text.toString()
        getTenantItemsData(search)
    }

    fun getTenantItemsData(search:String){
        items.clear();
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "tenant/tenantitems"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val data = obj.getJSONArray("data")
                    for (i in 0 until data.length()) {
                        var itemObj = data.getJSONObject(i)
                        var pricePer = itemObj.getString("pricePer")
                        if(pricePer != "") {
                            if (pricePer == "package") {
                                pricePer = "Paket"
                            } else {
                                pricePer = "Jam"
                            }
                        }
                        val item = TenantItem(itemObj.getInt("id"), itemObj.getString("name"), itemObj.getString("photo_url"), itemObj.getDouble("price"), pricePer, itemObj.getInt("availability"), itemObj.getDouble("rating"), itemObj.getInt("sold"))
                        items.add(item)
                    }
                    updateList()
                }
                else if(obj.getString("status")=="empty"){
                    binding.txtEmptyItemTenList.visibility = View.VISIBLE
                    binding.txtILSearchTenItem.visibility = View.INVISIBLE
                    binding.refreshLayoutTenantItems.isRefreshing = false
                    binding.recViewTenItems.visibility = View.INVISIBLE
                    binding.progressBarTItems.visibility = View.INVISIBLE
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
                params["token"] = token
                params["tenant_id"] = tenant_id.toString()
                params["search"] = search
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun updateList() {
        val glm: GridLayoutManager = GridLayoutManager(this, 2)
        var recyclerView = binding.recViewTenItems
        recyclerView.layoutManager = glm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = TenantItemListAdapter(items, tenant_type, this)
        recyclerView.isVisible = true
        binding.txtEmptyItemTenList.visibility = View.GONE
        binding.progressBarTItems.visibility = View.GONE
        binding.refreshLayoutTenantItems.isRefreshing = false
    }
}