package com.geded.apartemenku

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutItemWmaLogBinding

class WMALogsAdapter(val wmalogs:ArrayList<WMALog>, val context: FragmentActivity?):
    RecyclerView.Adapter<WMALogsAdapter.WMALogsViewHolder>() {
    class WMALogsViewHolder(val binding: LayoutItemWmaLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WMALogsViewHolder {
        val binding = LayoutItemWmaLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WMALogsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return wmalogs.size
    }

    override fun onBindViewHolder(holder: WMALogsViewHolder, position: Int) {
        with(holder.binding) {
            txtDateWMA.text = "Untuk " + wmalogs[position].date
            txtDescWMA.text = wmalogs[position].description
        }
    }
}