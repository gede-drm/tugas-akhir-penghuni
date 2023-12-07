package com.geded.apartemenku

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.FragmentTransactionBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class TransactionFragment : Fragment() {
    private lateinit var binding:FragmentTransactionBinding
    var transactions:ArrayList<TransactionList> = arrayListOf()
    var unit_id = 0
    var token = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var shared: SharedPreferences = requireActivity().getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        unit_id = shared.getInt(LoginActivity.RESIDENTID, 0)
        token = shared.getString(LoginActivity.TOKEN, "").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTransactionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtSearchTrxTen.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                val search = binding.txtSearchTrxTen.text.toString()
                getData(search)

                true
            }
            else {
                false
            }
        })

        binding.swipeRefreshTrxList.setOnRefreshListener {
            binding.recViewTrxList.visibility = View.INVISIBLE
            val search = binding.txtSearchTrxTen.text.toString()
            getData(search)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.recViewTrxList.visibility = View.INVISIBLE
        binding.progressBarListTen.visibility = View.VISIBLE
        val search = binding.txtSearchTrxTen.text.toString()
        getData(search)
    }

    fun getData(search:String){
        transactions.clear();
        val q = Volley.newRequestQueue(this.context)
        val url = Global.urlWS + "transaction/list"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val data = obj.getJSONArray("data")
                    for (i in 0 until data.length()) {
                        var trxObj = data.getJSONObject(i)
                        var itemObj = trxObj.getJSONObject("item")
                        val trx = TransactionList(trxObj.getInt("id"), trxObj.getString("transaction_date"), trxObj.getString("tenant_name"), itemObj.getString("name"), itemObj.getString("image"),itemObj.getString("quantity"),trxObj.getInt("itemcount"), trxObj.getDouble("total_payment"), trxObj.getString("status"))
                        transactions.add(trx)
                    }
                    updateList()
                }
                else if(obj.getString("status")=="empty"){
                    binding.txtEmptyTrxList.visibility = View.VISIBLE
                    binding.swipeRefreshTrxList.isRefreshing = false
                    binding.recViewTrxList.visibility = View.INVISIBLE
                    binding.progressBarListTen.visibility = View.INVISIBLE
                }
                else if(obj.getString("status")=="notauthenticated"){
                    Helper.logoutSystem(requireActivity())
                }
                else{
                    val builder = MaterialAlertDialogBuilder(requireActivity())
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK"){dialog, which->
                    }
                    builder.create().show()
                }
            },
            Response.ErrorListener {
                val builder = MaterialAlertDialogBuilder(requireActivity())
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK"){dialog, which->
                }
                builder.create().show()
            }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["search"] = search
                params["unit_id"] = unit_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun updateList() {
        val lm: LinearLayoutManager = LinearLayoutManager(this.context)
        var recyclerView = binding.recViewTrxList
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = TransactionListAdapter(transactions, requireActivity())
        recyclerView.isVisible = true
        binding.txtEmptyTrxList.visibility = View.INVISIBLE
        binding.progressBarListTen.visibility = View.INVISIBLE
        binding.recViewTrxList.visibility = View.VISIBLE
        binding.swipeRefreshTrxList.isRefreshing = false
    }
}