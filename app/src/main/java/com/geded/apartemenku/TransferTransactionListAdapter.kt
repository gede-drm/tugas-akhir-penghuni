package com.geded.apartemenku

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutItemTransactionTransferBinding

class TransferTransactionListAdapter (val tfTransactions:ArrayList<TransferTransaction>, val context: FragmentActivity?):
    RecyclerView.Adapter<TransferTransactionListAdapter.TransferTransactionListViewHolder>() {
    class TransferTransactionListViewHolder(val binding: LayoutItemTransactionTransferBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransferTransactionListViewHolder {
        val binding = LayoutItemTransactionTransferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransferTransactionListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return tfTransactions.size
    }

    override fun onBindViewHolder(holder: TransferTransactionListViewHolder, position: Int) {
        val total_payment = Helper.formatter(tfTransactions[position].total_payment)
        with(holder.binding) {
            txtTrxDateTF.text = tfTransactions[position].transaction_date
            txtTenNameTF.text = tfTransactions[position].tenant_name
            txtFinishDateTF.text = tfTransactions[position].finish_date
            txtTotalTrxTF.text = "Rp$total_payment"
            txtBankNameTF.text = tfTransactions[position].bank_name + " a.n. " + tfTransactions[position].account_holder
            txtBAccNumTF.text = tfTransactions[position].account_number
        }

        holder.binding.btnCopyBankAccTF.setOnClickListener {
            val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("account_number", tfTransactions[position].account_number)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(context, "Nomor Rekening telah disalin", Toast.LENGTH_SHORT).show()
        }
        holder.binding.btnPickProofTF.setOnClickListener {  }
        holder.binding.btnUploadProofTF.setOnClickListener {  }
    }
}