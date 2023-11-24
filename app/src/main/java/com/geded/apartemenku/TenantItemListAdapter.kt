package com.geded.apartemenku

import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutItemTenantBinding
import com.squareup.picasso.Picasso

class TenantItemListAdapter(val items:ArrayList<TenantItem>, val type:String, val context: FragmentActivity?):
    RecyclerView.Adapter<TenantItemListAdapter.TenantItemListViewHolder>() {
    class TenantItemListViewHolder(val binding: LayoutItemTenantBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantItemListViewHolder {
        val binding = LayoutItemTenantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TenantItemListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: TenantItemListViewHolder, position: Int) {
        val url = items[position].photo_url
        val price = Helper.formatter(items[position].price)
        with(holder.binding) {
            Picasso.get().load(url).into(imgTenItem)
            txtNameTenItem.text = items[position].name
            if(items[position].pricePer != ""){
                txtPriceTenItem.text = "Rp$price"
            }
            else{
                txtPriceTenItem.text = "Rp$price"
            }

            if(items[position].availability == 0){
                cardTenItem.foreground = null
                txtNotAvailableTenItem.isVisible = true
            }
            else{
                txtNotAvailableTenItem.isVisible = false
            }
            txtRatingTenItem.text = items[position].rating.toString()
            txtSoldTenItem.text = "Terjual " + items[position].sold.toString()
        }
        holder.binding.cardTenItem.setOnClickListener {
            if(items[position].availability != 0){
                val intent = Intent(this.context, ItemDetailActivity::class.java)
                intent.putExtra(ItemDetailActivity.ITEM_ID, items[position].id)
                if (type == "product") {
                    intent.putExtra(ItemDetailActivity.ITEM_TYPE, "product")
                } else {
                    intent.putExtra(ItemDetailActivity.ITEM_TYPE, "service")
                }
                context?.startActivity(intent)
            }
        }
    }
}