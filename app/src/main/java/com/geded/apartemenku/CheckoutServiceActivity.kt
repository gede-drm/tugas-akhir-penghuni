package com.geded.apartemenku

import android.R
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenku.databinding.ActivityCheckoutServiceBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Date

class CheckoutServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutServiceBinding
    var service_id = 0
    var service_qty = 0
    var service_price = 0
    var service_delivery = ""
    var paymethod = ""
    var tenant_openhour = ""
    var tenant_closehour = ""
    var date_chose = ""
    var time_chose = ""
    var tenant_type = ""
    var unit_id = 0
    var token = ""
    companion object{
        val SERVICE_ID = "SERVICE_ID"
        val SERVICE_QTY = "SERVICE_QTY"
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        unit_id = shared.getInt(LoginActivity.RESIDENTID, 0)
        token = shared.getString(LoginActivity.TOKEN, "").toString()

        service_id = intent.getIntExtra(SERVICE_ID, 0)
        service_qty = intent.getIntExtra(SERVICE_QTY, 0)

        binding.cardViewItemCS.isVisible = false
        binding.cardViewConfSvc.isVisible = false
        binding.cardViewPaymentCS.isVisible = false

        binding.txtDateCS.setOnClickListener {
            val today = MaterialDatePicker.todayInUtcMilliseconds()
            val validator = listOf(DateValidatorPointForward.from(today))
            val calendarConstraintBuilder = CalendarConstraints.Builder().setValidator(
                CompositeDateValidator.allOf(validator))
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Pilih Tanggal Pengerjaan").setSelection(
                MaterialDatePicker.todayInUtcMilliseconds()).setCalendarConstraints(calendarConstraintBuilder.build()).build()
            datePicker.addOnPositiveButtonClickListener {
                binding.txtTimeCS.setText("")
                time_chose = ""

                val txtDateFormatter = SimpleDateFormat("dd-MM-yyyy")
                val modelDateFormatter = SimpleDateFormat("yyyy-MM-dd")
                val txtDate = txtDateFormatter.format(Date(it))
                val modelDate = modelDateFormatter.format(Date(it))

                binding.txtDateCS.setText(txtDate)
                date_chose = modelDate.toString()
            }
            datePicker.show(this.supportFragmentManager, "DATE_PICKER_SERVICE")
        }
        binding.txtTimeCS.setOnClickListener {
            if(binding.txtDateCS.text.toString() != "") {
                val timePicker = MaterialTimePicker.Builder().setTitleText("Pilih Jam Ambil/Kirim")
                    .setTimeFormat(TimeFormat.CLOCK_24H).build()
                timePicker.addOnPositiveButtonClickListener {
                    val time = timePicker.hour.toString().padStart(2, '0') + ":" + timePicker.minute.toString().padStart(2, '0')

                    val c = Calendar.getInstance();
                    val df = SimpleDateFormat("dd-MM-yyyy")
                    val tf = SimpleDateFormat("HH:mm");
                    val timeSelected = LocalTime.parse(time)
                    val timeNow = LocalTime.parse(tf.format(c.getTime()))
                    val openHour = LocalTime.parse(tenant_openhour)
                    val closeHour = LocalTime.parse(tenant_closehour)
                    if(df.format(c.getTime()) == binding.txtDateCS.text.toString()){
                        if(timeSelected.isAfter(timeNow)){
                            if(timeSelected.isAfter(openHour) && timeSelected.isBefore(closeHour)){
                                binding.txtTimeCS.setText(time)
                                time_chose = time
                            }
                            else{
                                Toast.makeText(this, "Waktu yang anda pilih berada di luar jam operasional tenant!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else{
                            Toast.makeText(this, "Waktu harus lebih dari saat ini!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        if(timeSelected.isAfter(openHour) && timeSelected.isBefore(closeHour)){
                            binding.txtTimeCS.setText(time)
                            time_chose = time
                        }
                        else{
                            Toast.makeText(this, "Waktu yang anda pilih berada di luar jam operasional tenant!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                timePicker.show(this.supportFragmentManager, "TIME_PICKER_SERVICE")
            }
            else{
                Toast.makeText(this, "Silakan pilih tanggal kirim/ambil terlebih dahulu!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.spinnerDeliveryCS.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                if (binding.spinnerDeliveryCS.selectedItem.toString() == "Taruh-Ambil Sendiri") {
                    service_delivery = "pickup"
                } else {
                    service_delivery = "delivery"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        binding.spinnerPaymentCS.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                if (binding.spinnerPaymentCS.selectedItem.toString() == "Tunai") {
                    paymethod = "cash"
                } else {
                    paymethod = "transfer"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        binding.btnCheckoutCS.setOnClickListener {
            if(service_id != 0 && service_qty != 0 && service_price != 0){
                if(paymethod != "" && date_chose != "" && time_chose != ""){
                    if(tenant_type == "other"){
                        service_delivery = "delivery"
                        checkout()
                    }
                    else{
                        if(service_delivery != null){
                            checkout()
                        }
                        else{
                            Toast.makeText(this, "Silakan pilih kirim/ambil untuk pengerjaan terlebih dahulu!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else{
                    Toast.makeText(this, "Silakan pilih metode pembayaran, tanggal dan waktu kirim/ambil terlebih dahulu!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        getData()
    }

    fun getData(){
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "servicecheckoutlist"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    binding.progressBarCS.visibility = View.GONE
                    binding.cardViewItemCS.isVisible = true
                    binding.cardViewConfSvc.isVisible = true
                    binding.cardViewPaymentCS.isVisible = true

                    val dataObj = obj.getJSONObject("data")
                    val url = dataObj.getString("photo_url")
                    Picasso.get().load(url).into(binding.imgViewItemCS)

                    service_price = dataObj.getDouble("price").toInt()
                    val price = Helper.formatter(dataObj.getDouble("price"))
                    var pricePer = dataObj.getString("pricePer")
                    if(pricePer == "hour"){
                        pricePer = "Jam"
                    }
                    else{
                        pricePer = "Paket"
                    }

                    val permit_need = dataObj.getInt("permit_need")
                    binding.txtPermitNeedCS.isVisible = permit_need == 1
                    binding.txtItemNameCS.text = dataObj.getString("name")
                    binding.txtItemPriceCS.text = "Rp$price/$pricePer"
                    binding.txtItemQtyCS.text = "x" + dataObj.getInt("quantity").toString()

                    val tenantObj = obj.getJSONObject("tenant")
                    tenant_type  = tenantObj.getString("tenant_type")
                    if(tenant_type == "laundry"){
                        binding.txtDeliveryCS.isVisible = true
                        binding.spinnerDeliveryCS.isVisible = true

                        if(tenantObj.getInt("delivery_status") == 0){
                            val adapter =  ArrayAdapter(this, R.layout.simple_list_item_1, arrayListOf("Taruh-Ambil Sendiri"))
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            binding.spinnerDeliveryCS.adapter = adapter
                        }
                        else{
                            val adapter =  ArrayAdapter(this, R.layout.simple_list_item_1, arrayListOf("Taruh-Ambil Sendiri", "Kirim"))
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            binding.spinnerDeliveryCS.adapter = adapter
                        }
                    }
                    else{
                        service_delivery = "delivery"
                        binding.txtDeliveryCS.isVisible = false
                        binding.spinnerDeliveryCS.isVisible = false
                    }
                    if(tenantObj.getInt("cash_status") == 0){
                        binding.txtNoCashCS.isVisible = true
                        val adapter =  ArrayAdapter(this, R.layout.simple_list_item_1, arrayListOf("Transfer Bank"))
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerPaymentCS.adapter = adapter
                    }
                    else{
                        binding.txtNoCashCS.isVisible = false
                        val adapter =  ArrayAdapter(this, R.layout.simple_list_item_1, arrayListOf("Tunai", "Transfer Bank"))
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerPaymentCS.adapter = adapter
                    }
                    binding.txtTenNameCS.text = tenantObj.getString("name")
                    tenant_openhour = tenantObj.getString("open_hour")
                    tenant_closehour = tenantObj.getString("close_hour")
                    binding.txtTotalCS.text = "Rp" + Helper.formatter(obj.getDouble("total_payment"))

                } else if (obj.getString("status") == "empty") {
                    binding.progressBarCS.visibility = View.GONE
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Layanan Jasa Tidak Tersedia")
                    builder.setMessage("Mohon Maaf, Layanan Jasa yang Ingin Anda Beli Tiba-Tiba diubah Menjadi Tidak Tersedia oleh Tenant")
                    builder.setPositiveButton("OK") { dialog, which ->
                        finish()
                    }
                    builder.create().show()
                } else if (obj.getString("status") == "notauthenticated") {
                    binding.progressBarCS.visibility = View.GONE
                    Helper.logoutSystem(this)
                }
                else{
                    binding.progressBarCS.visibility = View.GONE
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK") { dialog, which ->
                        finish()
                    }
                    builder.create().show()
                }
            }, Response.ErrorListener {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK") { dialog, which ->
                    finish()
                }
                builder.create().show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["service_id"] = service_id.toString()
                params["service_qty"] = service_qty.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
    fun checkout() {
        binding.btnCheckoutCS.isEnabled = false
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/servicecheckout"
        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val transaction_id = obj.getInt("id")
                    Toast.makeText(this, "Transaksi Berhasil!", Toast.LENGTH_SHORT).show()
                    finish()
                } else if (obj.getString("status") == "failednotavailable") {
                    binding.btnCheckoutCS.isEnabled = true
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Gagal Membuat Transaksi")
                    builder.setMessage("Mohon maaf, terdapat jasa yang tiba-tiba tidak tersedia dan transaksi tidak dibuat.")
                    builder.setPositiveButton("OK") { dialog, which ->
                    }
                    builder.create().show()
                } else if (obj.getString("status") == "notauthenticated") {
                    Helper.logoutSystem(this)
                } else {
                    binding.btnCheckoutCS.isEnabled = true
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Gagal Membuat Transaksi")
                    builder.setMessage("Terdapat Masalah pada Server\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK") { dialog, which ->
                    }
                    builder.create().show()
                }
            },
            Response.ErrorListener {
                binding.btnCheckoutCS.isEnabled = true
                val builder = MaterialAlertDialogBuilder(this)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK") { dialog, which ->
                }
                builder.create().show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["unit_id"] = unit_id.toString()
                params["service_id"] = service_id.toString()
                params["service_qty"] = service_qty.toString()
                params["service_price"] = service_price.toString()
                params["delivery"] = service_delivery
                params["datetime"] = "$date_chose $time_chose"
                params["paymethod"] = paymethod
                params["token"] = token
                return params
            }
        }
        val retryPolicy = DefaultRetryPolicy(7500, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.retryPolicy = retryPolicy;
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
}