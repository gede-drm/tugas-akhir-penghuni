package com.geded.apartemenku

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutItemRatingBinding

class ItemToRateAdapter(val itemToRates:ArrayList<ItemToRate>, val context: FragmentActivity?):
    RecyclerView.Adapter<ItemToRateAdapter.ItemToRateViewHolder>() {
    class ItemToRateViewHolder(val binding: LayoutItemRatingBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemToRateViewHolder {
        val binding = LayoutItemRatingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemToRateViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return itemToRates.size
    }

    override fun onBindViewHolder(holder: ItemToRateViewHolder, position: Int) {
        with(holder.binding) {
            txtItemNameRate.text = itemToRates[position].item_name
        }
        holder.binding.txtReviewRate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                val review = holder.binding.txtReviewRate.text.toString()
                itemToRates[position].review = review
            }
        }
        )

        holder.binding.btnStarOne.setOnClickListener {
            itemToRates[position].rating = 1
            holder.binding.btnStarOne.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarTwo.setImageResource(R.drawable.baseline_star_rate_grey_24)
            holder.binding.btnStarThree.setImageResource(R.drawable.baseline_star_rate_grey_24)
            holder.binding.btnStarFour.setImageResource(R.drawable.baseline_star_rate_grey_24)
            holder.binding.btnStarFive.setImageResource(R.drawable.baseline_star_rate_grey_24)
        }
        holder.binding.btnStarTwo.setOnClickListener {
            itemToRates[position].rating = 2
            holder.binding.btnStarOne.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarTwo.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarThree.setImageResource(R.drawable.baseline_star_rate_grey_24)
            holder.binding.btnStarFour.setImageResource(R.drawable.baseline_star_rate_grey_24)
            holder.binding.btnStarFive.setImageResource(R.drawable.baseline_star_rate_grey_24)
        }
        holder.binding.btnStarThree.setOnClickListener {
            itemToRates[position].rating = 3
            holder.binding.btnStarOne.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarTwo.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarThree.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarFour.setImageResource(R.drawable.baseline_star_rate_grey_24)
            holder.binding.btnStarFive.setImageResource(R.drawable.baseline_star_rate_grey_24)
        }
        holder.binding.btnStarFour.setOnClickListener {
            itemToRates[position].rating = 4
            holder.binding.btnStarOne.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarTwo.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarThree.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarFour.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarFive.setImageResource(R.drawable.baseline_star_rate_grey_24)
        }
        holder.binding.btnStarFive.setOnClickListener {
            itemToRates[position].rating = 5
            holder.binding.btnStarOne.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarTwo.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarThree.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarFour.setImageResource(R.drawable.baseline_star_rate_24)
            holder.binding.btnStarFive.setImageResource(R.drawable.baseline_star_rate_24)
        }
    }
}