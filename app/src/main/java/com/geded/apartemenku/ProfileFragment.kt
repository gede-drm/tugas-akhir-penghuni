package com.geded.apartemenku

import android.R
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class ProfileFragment : Fragment() {
    private lateinit var binding:FragmentProfileBinding
    private lateinit var adapterWMA: ArrayAdapter<String>
    var username = ""
    var unit_id = 0
    var wma = 0
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var shared: SharedPreferences = requireActivity().getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        username = shared.getString(LoginActivity.USERNAME, "").toString()
        unit_id = shared.getInt(LoginActivity.RESIDENTID, 0)
        token = shared.getString(LoginActivity.TOKEN, "").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.progressBarProfile.isVisible = true
        binding.txtUnitNoProfile.isVisible = false
        binding.txtViewONProfile.isVisible = false
        binding.txtOwnerNameProfile.isVisible = false
        binding.txtViewHNProfile.isVisible = false
        binding.txtHolderNameProfile.isVisible = false
        binding.txtViewPhProfile.isVisible = false
        binding.txtPhNumProfile.isVisible = false
        binding.txtViewRmProfile.isVisible = false
        binding.cardViewWMAProfile.isVisible = false

        getData()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtUnitNoProfile.isVisible = false
        binding.txtViewONProfile.isVisible = false
        binding.txtOwnerNameProfile.isVisible = false
        binding.txtViewHNProfile.isVisible = false
        binding.txtHolderNameProfile.isVisible = false
        binding.txtViewPhProfile.isVisible = false
        binding.txtPhNumProfile.isVisible = false
        binding.txtViewRmProfile.isVisible = false
        binding.cardViewWMAProfile.isVisible = false

        adapterWMA = ArrayAdapter(requireActivity(), R.layout.simple_list_item_1, arrayListOf("3", "5", "7"))
        adapterWMA.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerWMAPreference.adapter = adapterWMA

        binding.spinnerWMAPreference.onItemSelectedListener = object:OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(wma != parent?.getItemAtPosition(position).toString().toInt()){
                    changeWMA()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        binding.btnLogout.setOnClickListener {
            var shared: SharedPreferences = requireActivity().getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
            var editor: SharedPreferences.Editor = shared.edit()

            val q = Volley.newRequestQueue(activity)
            val url = Global.urlGeneralWS + "cleartoken"

            var stringRequest = object : StringRequest(
                Method.POST, url, Response.Listener {
                    val obj = JSONObject(it)
                    if(obj.getString("status")=="success") {
                        editor.putString(LoginActivity.USERNAME, "")
                        editor.putInt(LoginActivity.RESIDENTID, 0)
                        editor.putString(LoginActivity.UNITNO, "")
                        editor.putString(LoginActivity.HOLDERNAME, "")
                        editor.putString(ShoppingCartActivity.CART, "")
                        editor.putString(LoginActivity.TOKEN, "")
                        editor.apply()

                        activity?.let{ fragmentActivity ->
                            val intent = Intent(fragmentActivity, LoginActivity::class.java)
                            fragmentActivity.startActivity(intent)
                            fragmentActivity.finish()
                        }
                    }
                    else if(obj.getString("status")=="notauthenticated"){
                        Helper.logoutSystem(this.requireActivity())
                    }
                    else{
                        val builder = activity?.let { context -> MaterialAlertDialogBuilder(context) }
                        builder?.setCancelable(false)
                        builder?.setTitle("Terjadi Masalah")
                        builder?.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                        builder?.setPositiveButton("OK"){dialog, which->
                        }
                        builder?.create()?.show()
                    }
                },
                Response.ErrorListener {
                    val builder = activity?.let { context -> MaterialAlertDialogBuilder(context) }
                    builder?.setCancelable(false)
                    builder?.setTitle("Terjadi Masalah")
                    builder?.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder?.setPositiveButton("OK"){dialog, which->
                    }
                    builder?.create()?.show()
                }){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["username"] = username.toString()
                    params["token"] = token.toString()
                    return params
                }
            }
            stringRequest.setShouldCache(false)
            q.add(stringRequest)
        }
    }

    fun getData(){
        val q = Volley.newRequestQueue(activity)
        val url = Global.urlWS + "unitinfo"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val dataObj = obj.getJSONObject("data")
                    var wma_preference = dataObj.getInt("wma_preference")
                    binding.txtUnitNoProfile.text = "Unit " + dataObj.getString("unit_no")
                    binding.txtOwnerNameProfile.text = dataObj.getString("owner_name")
                    binding.txtHolderNameProfile.text = dataObj.getString("holder_name")
                    binding.txtPhNumProfile.text = dataObj.getString("holder_ph_number")

                    var spinnerPos = adapterWMA.getPosition(wma_preference.toString())
                    binding.spinnerWMAPreference.setSelection(spinnerPos)

                    binding.progressBarProfile.isVisible = false
                    binding.txtUnitNoProfile.isVisible = true
                    binding.txtViewONProfile.isVisible = true
                    binding.txtOwnerNameProfile.isVisible = true
                    binding.txtViewHNProfile.isVisible = true
                    binding.txtHolderNameProfile.isVisible = true
                    binding.txtViewPhProfile.isVisible = true
                    binding.txtPhNumProfile.isVisible = true
                    binding.txtViewRmProfile.isVisible = true
                    binding.cardViewWMAProfile.isVisible = true
                }
                else if(obj.getString("status")=="notauthenticated"){
                    binding.progressBarProfile.isVisible = false
                    Helper.logoutSystem(this.requireActivity())
                }
                else{
                    binding.progressBarProfile.isVisible = false

                    val builder = activity?.let { context -> MaterialAlertDialogBuilder(context) }
                    builder?.setCancelable(false)
                    builder?.setTitle("Terjadi Masalah")
                    builder?.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder?.setPositiveButton("OK"){dialog, which->
                    }
                    builder?.create()?.show()
                }
            },
            Response.ErrorListener {
                val builder = activity?.let { context -> MaterialAlertDialogBuilder(context) }
                builder?.setCancelable(false)
                builder?.setTitle("Terjadi Masalah")
                builder?.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder?.setPositiveButton("OK"){dialog, which->
                }
                builder?.create()?.show()
            }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["unit_id"] = unit_id.toString()
                params["token"] = token.toString()
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun changeWMA(){
        val q = Volley.newRequestQueue(activity)
        val url = Global.urlWS + "changewmapref"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    wma = binding.spinnerWMAPreference.selectedItem.toString().toInt()
                    Toast.makeText(context, "Preferensi Pengingat Berhasil diubah", Toast.LENGTH_SHORT).show()
                }
                else if(obj.getString("status") == "nothingchanged") {
                    Toast.makeText(context, "Tidak terdapat perubahan preferensi pengingat", Toast.LENGTH_SHORT).show()
                }
                else if(obj.getString("status")=="notauthenticated"){
                    Helper.logoutSystem(this.requireActivity())
                }
                else{
                    Toast.makeText(context, "Maaf, terjadi kesalahan! Silakan coba lagi nanti", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(context, "Maaf, terjadi kesalahan! Silakan coba lagi nanti", Toast.LENGTH_SHORT).show()
            }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["unit_id"] = unit_id.toString()
                params["wma_preference"] = binding.spinnerWMAPreference.selectedItem.toString()
                params["token"] = token.toString()
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
}