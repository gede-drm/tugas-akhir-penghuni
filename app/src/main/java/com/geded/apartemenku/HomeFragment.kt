package com.geded.apartemenku

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.geded.apartemenku.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    var holder_name = ""
    var unit_no = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var shared: SharedPreferences = this.requireActivity().getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        holder_name = shared.getString(LoginActivity.HOLDERNAME,"").toString()
        unit_no = shared.getString(LoginActivity.UNITNO,"").toString()
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
    }
}