package com.geded.apartemenku

data class ProCheckoutConfig(val tenant_id: Int, val tenant_name:String, val cash:Int, val deliveryStatus:Int, var open_hour:String, var close_hour:String, var date:String?, var time:String?, var payment_method:String?, var delivery_method:String?)
