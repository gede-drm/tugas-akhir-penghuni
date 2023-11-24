package com.geded.apartemenku

import android.text.TextUtils.substring
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutItemReviewBinding

class ItemReviewAdapter(val reviews:ArrayList<Review>, val context: FragmentActivity?):
    RecyclerView.Adapter<ItemReviewAdapter.ItemReviewViewHolder>() {
    class ItemReviewViewHolder(val binding: LayoutItemReviewBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemReviewViewHolder {
        val binding = LayoutItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemReviewViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    override fun onBindViewHolder(holder: ItemReviewViewHolder, position: Int) {
        with(holder.binding) {
            val unit_no = reviews[position].unit_no.substring(0, reviews[position].unit_no.length-2) + "**"
            txtReviewUnit.text = unit_no
            txtItemReview.text = reviews[position].review
            txtRatingReview.text = reviews[position].rating.toString()
        }
    }
}