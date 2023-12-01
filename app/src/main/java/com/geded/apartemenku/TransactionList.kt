package com.geded.apartemenku

data class TransactionList(val id:Int, val transaction_date: String, val tenant_name:String, val item_name:String, val photo_url:String, val item_count:String, val remaining_item_count:Int, val total_payment:Double, val status:String)
