package com.geded.apartemenku

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutTenantListBinding

class TenantListAdapter(val tenants:ArrayList<TenantList>, val context: FragmentActivity?):
    RecyclerView.Adapter<TenantListAdapter.TenantListViewHolder>() {
    class TenantListViewHolder(val binding: LayoutTenantListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantListViewHolder {
        val binding = LayoutTenantListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  TenantListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return tenants.size
    }

    override fun onBindViewHolder(holder: TenantListViewHolder, position: Int) {
        with(holder.binding) {
            txtTenNameTList.text = tenants[position].name
            txtTenAddressTList.text = tenants[position].address
            txtTenRatingTList.text = tenants[position].rating.toString()
            txtTenSvcHourTList.text = "Jam Buka: " + tenants[position].start_hour + "-" + tenants[position].end_hour
            
            if(tenants[position].status == "open"){
                txtTenStatusTList.text = "Buka"
            }
            else{
                cardViewTenList.setCardBackgroundColor(ContextCompat.getColor(cardViewTenList.context, R.color.md_theme_dark_onSurface))
                cardViewTenList.foreground = null
                txtTenStatusTList.setTextColor(ContextCompat.getColor(txtTenStatusTList.context, R.color.md_theme_dark_onError))
                txtTenStatusTList.text = "Tutup"
            }
            if(tenants[position].delivery == 1){
                txtTenDeliveryTList.text = "Menyediakan Pesan Antar"
            }
            else{
                txtTenDeliveryTList.text = "Tidak Menyediakan Pesan Antar"
            }
        }
        holder.binding.cardViewTenList.setOnClickListener {
            if(tenants[position].status == "open"){
                val intent = Intent(this.context, TenantItemsActivity::class.java)
                intent.putExtra(TenantItemsActivity.TENANT_ID, tenants[position].id)
                intent.putExtra(TenantItemsActivity.TENANT_NAME, tenants[position].name)
                intent.putExtra(TenantItemsActivity.TENANT_TYPE, tenants[position].tenant_type)
                intent.putExtra(TenantItemsActivity.TENANT_ADDRESS, tenants[position].address)
                context?.startActivity(intent)
            }
        }
    }
}