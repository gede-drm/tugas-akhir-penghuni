package com.geded.apartemenku

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutTrxStatusBinding

class TrxStatusAdapter(val trxStatuses:ArrayList<TrxStatus>, val context: FragmentActivity?):
    RecyclerView.Adapter<TrxStatusAdapter.TrxStatusViewHolder>() {
    class TrxStatusViewHolder(val binding: LayoutTrxStatusBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrxStatusViewHolder {
        val binding = LayoutTrxStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrxStatusViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return trxStatuses.size
    }

    override fun onBindViewHolder(holder: TrxStatusViewHolder, position: Int) {
        with(holder.binding) {
            txtStatusDate.text = trxStatuses[position].date
            txtStatusDesc.text = trxStatuses[position].description
        }
    }
}