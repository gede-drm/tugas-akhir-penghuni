package com.geded.apartemenku

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityPackageListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class PackageListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPackageListBinding
    var packages:ArrayList<PackageList> = arrayListOf()
    var unit_id = 0
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackageListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        unit_id = shared.getInt(LoginActivity.RESIDENTID, 0)
        token = shared.getString(LoginActivity.TOKEN, "").toString()

        getData()

        binding.refreshLayoutPkgList.setOnRefreshListener {
            getData()
        }
    }

    fun getData(){
        packages.clear();
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "package/list"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val data = obj.getJSONArray("data")
                    for (i in 0 until data.length()) {
                        var pkgObj = data.getJSONObject(i)
                        val pkg = PackageList(pkgObj.getInt("id"), pkgObj.getString("receive_date"), pkgObj.getString("pickup_date"), pkgObj.getString("detail"), pkgObj.getString("photo_url"))
                        packages.add(pkg)
                    }
                    updateList()
                }
                else if(obj.getString("status")=="empty"){
                    binding.txtEmptyPkgList.visibility = View.VISIBLE
                    binding.refreshLayoutPkgList.isRefreshing = false
                    binding.recViewPkgList.visibility = View.INVISIBLE
                    binding.progressBarPkgList.visibility = View.INVISIBLE
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
                params["unit_id"] = unit_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun updateList() {
        val lm: LinearLayoutManager = LinearLayoutManager(this)
        var recyclerView = binding.recViewPkgList
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = PackageListAdapter(packages, this)
        recyclerView.isVisible = true
        binding.txtEmptyPkgList.visibility = View.GONE
        binding.progressBarPkgList.visibility = View.GONE
        binding.refreshLayoutPkgList.isRefreshing = false
    }
}