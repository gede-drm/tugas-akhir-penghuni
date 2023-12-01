package com.geded.apartemenku

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutTransactionListBinding
import com.squareup.picasso.Picasso

class TransactionListAdapter(val transactions:ArrayList<TransactionList>, val context: FragmentActivity?):
    RecyclerView.Adapter<TransactionListAdapter.TransactionListViewHolder>() {
    class TransactionListViewHolder(val binding: LayoutTransactionListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionListViewHolder {
        val binding = LayoutTransactionListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: TransactionListViewHolder, position: Int) {
        val url = transactions[position].photo_url
        Picasso.get().load(url).into(holder.binding.imgProductTL)
        val total_payment = Helper.formatter(transactions[position].total_payment)
        with(holder.binding) {
            txtDateTL.text = transactions[position].transaction_date
            txtStatusTL.text = transactions[position].status
            txtTenNameTL.text = transactions[position].tenant_name
            txtProNameTL.text = transactions[position].item_name
            txtProQtyTL.text = transactions[position].item_count
            if(transactions[position].remaining_item_count == 0){
                txtRemainingQtyTL.isVisible = false
            }
            txtRemainingQtyTL.text = "+" + transactions[position].remaining_item_count + " Item lainnya"
            txtTotalPriceTL.text = "Rp$total_payment"
        }
        holder.binding.btnDetailTL.setOnClickListener {
            val intent = Intent(this.context, TransactionDetailActivity::class.java)
            intent.putExtra(TransactionDetailActivity.TRANSACTION_ID, transactions[position].id)
            context?.startActivity(intent)
        }
    }
}