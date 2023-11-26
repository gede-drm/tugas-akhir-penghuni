package com.geded.apartemenku

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutShoppingCartItemBinding
import com.squareup.picasso.Picasso

class ShoppingCartAdapter(val cartItems:ArrayList<Cart>, val context: FragmentActivity?):
    RecyclerView.Adapter<ShoppingCartAdapter.ShoppingCartViewHolder>() {
    class ShoppingCartViewHolder(val binding: LayoutShoppingCartItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingCartViewHolder {
        val binding = LayoutShoppingCartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShoppingCartViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    override fun onBindViewHolder(holder: ShoppingCartViewHolder, position: Int) {
        val url = cartItems[position].photo_url
        val price = Helper.formatter(cartItems[position].item_price)
        val subtotal = Helper.formatter(cartItems[position].subtotal)
        with(holder.binding) {
            Picasso.get().load(url).into(imgViewItemSCI)
            txtTenNameSCI.text = cartItems[position].tenant_name
            txtItemNameSCI.text = cartItems[position].item_name
            txtItemPriceSCI.text = "Rp$price/pc"
            txtItemQtySCI.text = "x" + cartItems[position].qty.toString()
            txtItemSubTotalSCI.text = "Rp$subtotal"
        }
        holder.binding.btnDeleteItemSCI.setOnClickListener {
            (context as ShoppingCartActivity).deleteItem(cartItems[position].item_id, cartItems[position].item_name)
        }
    }
}