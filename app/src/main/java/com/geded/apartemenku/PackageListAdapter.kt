package com.geded.apartemenku

import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutItemPackageListBinding
import com.squareup.picasso.Picasso

class PackageListAdapter(val packages:ArrayList<PackageList>, val context: FragmentActivity?):
    RecyclerView.Adapter<PackageListAdapter.PackageListViewHolder>() {
    class PackageListViewHolder(val binding:LayoutItemPackageListBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageListViewHolder {
        val binding = LayoutItemPackageListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PackageListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return packages.size
    }

    override fun onBindViewHolder(holder: PackageListViewHolder, position: Int) {
        val url = packages[position].photo_url
        with(holder.binding) {
            Picasso.get().load(url).resize(200, 134).into(imgPkgList)
            txtReceiveDateList.text = "Masuk pada:\n" + packages[position].receive_date
        }
        if(packages[position].pickup_date != ""){
            holder.binding.txtPkgDetailList.text = packages[position].detail
            holder.binding.txtPickDateList.text = "Diambil pada:\n" + packages[position].pickup_date
            holder.binding.txtNoPickupPkgList.setTextColor(ContextCompat.getColor(holder.binding.txtNoPickupPkgList.context, R.color.md_theme_light_secondary))
            holder.binding.txtNoPickupPkgList.text = "Sudah\nDiambil"
        }
        else{
            holder.binding.txtPkgDetailList.text = packages[position].detail
            holder.binding.txtPickDateList.isVisible = false
            holder.binding.txtNoPickupPkgList.setTextColor(ContextCompat.getColor(holder.binding.txtNoPickupPkgList.context, R.color.md_theme_dark_onError))
            holder.binding.txtNoPickupPkgList.text = "Belum\nDiambil"
        }
        holder.binding.btnDetail.setOnClickListener {
            val intent = Intent(this.context, PackageDetailActivity::class.java)
            intent.putExtra(PackageDetailActivity.PACKAGE_ID, packages[position].id)
            context?.startActivity(intent)
        }
    }
}