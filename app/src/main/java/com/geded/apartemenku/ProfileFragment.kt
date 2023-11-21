package com.geded.apartemenku

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class ProfileFragment : Fragment() {
    private lateinit var binding:FragmentProfileBinding
    var username = ""
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var shared: SharedPreferences = requireActivity().getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        username = shared.getString(LoginActivity.USERNAME, "").toString()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                        editor.putString(LoginActivity.TOKEN, "")
                        editor.apply()

                        activity?.let{ fragmentActivity ->
                            val intent = Intent(fragmentActivity, LoginActivity::class.java)
                            fragmentActivity.startActivity(intent)
                            fragmentActivity.finish()
                        }
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

}