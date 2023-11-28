package com.geded.apartemenku

data class TransferTransaction(val id: Int, val transaction_date:String, val total_payment:Double, val finish_date:String, val tenant_name:String, val bank_name:String, val account_holder:String, val account_number:String, var base64Image:String?)
