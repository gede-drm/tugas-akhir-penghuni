package com.geded.apartemenku

data class Cart(val item_id:Int, val qty:Int, val item_name:String, val item_price: Double, val subtotal: Double, val tenant_id:Int, val tenant_name:String, val photo_url:String, val cash:Int)
