package com.geded.apartemenku

import java.text.DecimalFormat

class Helper {
    companion object{
        fun formatter(n: Double): String {
            return DecimalFormat("#,###.00").format(n)
        }
        fun getShoppingCart():ArrayList<String>{
            return arrayListOf()
        }
    }
}