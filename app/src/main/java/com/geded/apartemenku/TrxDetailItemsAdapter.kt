package com.geded.apartemenku

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutItemTransactionDetailBinding
import com.squareup.picasso.Picasso

class TrxDetailItemsAdapter (val trxDTItems:ArrayList<TransactionDetailItem>, val context: FragmentActivity?):
    RecyclerView.Adapter<TrxDetailItemsAdapter.TrxDetailItemsViewHolder>() {
    class TrxDetailItemsViewHolder(val binding: LayoutItemTransactionDetailBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrxDetailItemsViewHolder {
        val binding = LayoutItemTransactionDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrxDetailItemsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return trxDTItems.size
    }

    override fun onBindViewHolder(holder: TrxDetailItemsViewHolder, position: Int) {
        val url = trxDTItems[position].photo_url
        val price = Helper.formatter(trxDTItems[position].price)
        val subtotal = Helper.formatter(trxDTItems[position].subtotal)
        val pricePer = trxDTItems[position].pricePer
        with(holder.binding) {
            Picasso.get().load(url).into(imgItemDT)
            txtItemNameDT.text = trxDTItems[position].name
            if (pricePer != ""){
                txtItemPriceDT.text = "Rp$price/$pricePer"
            }
            else{
                txtItemPriceDT.text = "Rp$price"
            }
            txtItemQtyDT.text = "x" + trxDTItems[position].quantity
            txtItemSubTotalDT.text = "Rp$subtotal"
        }
    }
}