package com.geded.apartemenku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.geded.apartemenku.databinding.ActivityShoppingCartBinding

class ShoppingCartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShoppingCartBinding
    companion object{
        val CART = "CART"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}