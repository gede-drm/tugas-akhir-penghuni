package com.geded.apartemenku

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityServiceTenantBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class ServiceTenantActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServiceTenantBinding
    var tenants:ArrayList<TenantList> = arrayListOf()
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceTenantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN, "").toString()

        binding.txtSearchSTen.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                val search = binding.txtSearchSTen.text.toString()
                getTenantData(search)
                true
            }
            else {
                false
            }
        })

        binding.refreshLayoutSvcTen.setOnRefreshListener {
            val search = binding.txtSearchSTen.text.toString()
            getTenantData(search)
        }
    }

    override fun onResume() {
        super.onResume()
        val search = binding.txtSearchSTen.text.toString()
        getTenantData(search)
    }

    fun getTenantData(search:String){
        tenants.clear();
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "tenant/servicetenlist"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val data = obj.getJSONArray("data")
                    for (i in 0 until data.length()) {
                        var tenObj = data.getJSONObject(i)
                        val ten = TenantList(tenObj.getInt("id"), tenObj.getString("name"), tenObj.getString("address"), tenObj.getDouble("rating"), tenObj.getString("type"), tenObj.getString("service_hour_start"), tenObj.getString("service_hour_end"), tenObj.getInt("delivery"),  tenObj.getInt("cash"), tenObj.getString("status"))
                        tenants.add(ten)
                    }
                    updateList()
                }
                else if(obj.getString("status")=="empty"){
                    binding.txtEmptySTenList.visibility = View.VISIBLE
                    binding.txtILSearchSTen.visibility = View.INVISIBLE
                    binding.refreshLayoutSvcTen.isRefreshing = false
                    binding.recViewSTen.visibility = View.INVISIBLE
                    binding.progressBarSTen.visibility = View.INVISIBLE
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
                params["search"] = search
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun updateList() {
        val lm: LinearLayoutManager = LinearLayoutManager(this)
        var recyclerView = binding.recViewSTen
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = TenantListAdapter(tenants, this)
        recyclerView.isVisible = true
        binding.txtEmptySTenList.visibility = View.GONE
        binding.progressBarSTen.visibility = View.GONE
        binding.refreshLayoutSvcTen.isRefreshing = false
    }
}