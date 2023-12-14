package com.geded.apartemenku

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import org.json.JSONObject

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    var wmalogs:ArrayList<WMALog> = arrayListOf()
    var announcements: ArrayList<Announcement> = arrayListOf()
    var ann_title = ""
    var ann_summary = ""
    var ann_date = ""
    var holder_name = ""
    var unit_no = ""
    var unit_id = 0
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var shared: SharedPreferences = this.requireActivity().getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        holder_name = shared.getString(LoginActivity.HOLDERNAME,"").toString()
        unit_no = shared.getString(LoginActivity.UNITNO,"").toString()
        unit_id = shared.getInt(LoginActivity.RESIDENTID,0)
        token = shared.getString(LoginActivity.TOKEN, "").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtHolderNameHome.text = "$holder_name!"
        binding.txtUnitNoHome.text = "Unit $unit_no"
        binding.btnPkgListHome.setOnClickListener {
            activity?.let {
                val intent = Intent(it, PackageListActivity::class.java)
                startActivity(intent)
            }
        }
        binding.btnBuyStuffHome.setOnClickListener {
            activity?.let {
                val intent = Intent(it, ProductTenantActivity::class.java)
                startActivity(intent)
            }
        }
        binding.btnBuyServiceHome.setOnClickListener {
            activity?.let {
                val intent = Intent(it, ServiceTenantActivity::class.java)
                startActivity(intent)
            }
        }
        binding.btnMoreAnnouncement.setOnClickListener {
            if(ann_title != "" && ann_summary != "" && ann_date != ""){
                val dialog = BottomSheetDialog(requireActivity())
                val view =layoutInflater.inflate(R.layout.bottom_sheet_announcement, null)
                dialog.setCancelable(false)
                dialog.setContentView(view)
                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                dialog.show()

                val lm: LinearLayoutManager = LinearLayoutManager(requireActivity())
                var recyclerView = view.findViewById<RecyclerView>(R.id.recViewAnnouncement)
                recyclerView.layoutManager = lm
                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = AnnouncementsAdapter(announcements, requireActivity())
                recyclerView.isVisible = true

                view.findViewById<Button>(R.id.btnCloseDialogAnn).setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.txtAnnTitle.isVisible = false
        binding.txtAnnSummary.isVisible = false
        binding.btnMoreAnnouncement.isVisible = false
        binding.txtNoAnn.isVisible = true

        binding.recViewWMALogs.isVisible = false
        binding.txtWMAEmpty.isVisible = true

        getAnnouncement()
        getOneWeekAnnouncements()
        getWMALogs()
    }

    fun getAnnouncement(){
        val q = Volley.newRequestQueue(requireActivity())
        val url = Global.urlWS + "announcement/get"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val data = obj.getJSONObject("data")
                    ann_title = data.getString("title")
                    ann_summary = data.getString("description")
                    ann_date = data.getString("date")

                    binding.txtAnnTitle.text = ann_title
                    binding.txtAnnSummary.text = ann_summary

                    binding.txtAnnTitle.isVisible = true
                    binding.txtAnnSummary.isVisible = true
                    binding.btnMoreAnnouncement.isVisible = true
                    binding.txtNoAnn.isVisible = false
                } else if (obj.getString("status") == "empty") {
                    binding.txtAnnTitle.isVisible = false
                    binding.txtAnnSummary.isVisible = false
                    binding.btnMoreAnnouncement.isVisible = false
                    binding.txtNoAnn.isVisible = true
                } else if (obj.getString("status") == "notauthenticated") {
                    Helper.logoutSystem(requireActivity())
                }
    }, Response.ErrorListener {

    }) {
        override fun getParams(): MutableMap<String, String> {
            val params = HashMap<String, String>()
            params["token"] = token
            return params
        }
    }
    stringRequest.setShouldCache(false)
    q.add(stringRequest)
    }

    fun getOneWeekAnnouncements(){
        val q = Volley.newRequestQueue(requireActivity())
        val url = Global.urlWS + "announcement/getoneweek"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val dataArr = obj.getJSONArray("data")
                    for (i in 0 until dataArr.length()) {
                        var dataObj = dataArr.getJSONObject(i)

                        val announcement = Announcement(dataObj.getString("title"), dataObj.getString("date"), dataObj.getString("description"))
                        announcements.add(announcement)
                    }

                } else if (obj.getString("status") == "empty") {
                    binding.txtAnnTitle.isVisible = false
                    binding.txtAnnSummary.isVisible = false
                    binding.btnMoreAnnouncement.isVisible = false
                    binding.txtNoAnn.isVisible = true
                } else if (obj.getString("status") == "notauthenticated") {
                    Helper.logoutSystem(requireActivity())
                }
            }, Response.ErrorListener {

            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun getWMALogs(){
        wmalogs.clear()

        val q = Volley.newRequestQueue(requireActivity())
        val url = Global.urlWS + "getwmalogs"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val data = obj.getJSONArray("data")
                    for (i in 0 until data.length()){
                        val dataObj = data.getJSONObject(i)
                        val wmaLog = WMALog(dataObj.getString("send_date"), dataObj.getString("description"))
                        wmalogs.add(wmaLog)
                    }
                    updateListWMA()
                } else if (obj.getString("status") == "empty") {
                    binding.recViewWMALogs.isVisible = false
                    binding.txtWMAEmpty.isVisible = true
                } else if (obj.getString("status") == "notauthenticated") {
                    Helper.logoutSystem(requireActivity())
                }
            }, Response.ErrorListener {

            }) {
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

    fun updateListWMA(){
        val lm: LinearLayoutManager = LinearLayoutManager(requireActivity())
        var recyclerView = binding.recViewWMALogs
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = WMALogsAdapter(wmalogs, requireActivity())

        binding.recViewWMALogs.isVisible = true
        binding.txtWMAEmpty.isVisible = false
    }
}