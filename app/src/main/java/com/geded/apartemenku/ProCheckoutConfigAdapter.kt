package com.geded.apartemenku

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutCheckoutConfigBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.Date

class ProCheckoutConfigAdapter (val checkoutConfigs:ArrayList<ProCheckoutConfig>, val context: FragmentActivity?):
    RecyclerView.Adapter<ProCheckoutConfigAdapter.ProCheckoutConfigViewHolder>() {
    class ProCheckoutConfigViewHolder(val binding: LayoutCheckoutConfigBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProCheckoutConfigViewHolder {
        val binding = LayoutCheckoutConfigBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProCheckoutConfigViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return checkoutConfigs.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ProCheckoutConfigViewHolder, holderPosition: Int) {
        with(holder.binding) {
            txtTenNameCC.text = checkoutConfigs[holderPosition].tenant_name
            if(checkoutConfigs[holderPosition].cash == 0){
                val adapter =  context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, arrayListOf("Transfer Bank")) }
                adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPaymentCC.adapter = adapter
            }
            else{
                val adapter =  context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, arrayListOf("Tunai", "Transfer Bank")) }
                adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPaymentCC.adapter = adapter
            }

            if(checkoutConfigs[holderPosition].deliveryStatus == 0){
                val adapter =  context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, arrayListOf("Ambil")) }
                adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDeliveryCC.adapter = adapter
            }
            else{
                val adapter =  context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, arrayListOf("Kirim", "Ambil")) }
                adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDeliveryCC.adapter = adapter
            }

            if(checkoutConfigs[holderPosition].cash == 0 && checkoutConfigs[holderPosition].deliveryStatus == 0) {
                txtWarningCC.text = "Toko ini tidak menyediakan pembayaran tunai dan layanan kirim"
            }
            else if(checkoutConfigs[holderPosition].cash == 0){
                txtWarningCC.text = "Toko ini tidak menyediakan pembayaran tunai"
            }
            else if(checkoutConfigs[holderPosition].deliveryStatus == 0){
                txtWarningCC.text = "Toko ini tidak menyediakan layanan kirim"
            }
            else{
                txtWarningCC.isVisible = false
            }
        }
        holder.binding.txtDateCC.setOnClickListener {
            val today = MaterialDatePicker.todayInUtcMilliseconds()
            val oneweek = (today + 604800000L)
            val validator = listOf(DateValidatorPointForward.from(today), DateValidatorPointBackward.before(oneweek))
            val calendarConstraintBuilder = CalendarConstraints.Builder().setValidator(CompositeDateValidator.allOf(validator))
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Pilih Tanggal Kirim/Ambil").setSelection(MaterialDatePicker.todayInUtcMilliseconds()).setCalendarConstraints(calendarConstraintBuilder.build()).build()
            datePicker.addOnPositiveButtonClickListener {
                holder.binding.txtTimeCC.setText("")
                checkoutConfigs[holderPosition].time = null

                val txtDateFormatter = SimpleDateFormat("dd-MM-yyyy")
                val modelDateFormatter = SimpleDateFormat("yyyy-MM-dd")
                val txtDate = txtDateFormatter.format(Date(it))
                val modelDate = modelDateFormatter.format(Date(it))

                holder.binding.txtDateCC.setText(txtDate)
                checkoutConfigs[holderPosition].date = modelDate.toString()
            }
            context?.let { it1 -> datePicker.show(it1.supportFragmentManager, "DATE_PICKER") }
        }
        holder.binding.txtTimeCC.setOnClickListener {
            if(holder.binding.txtDateCC.text.toString() != "") {
                val timePicker = MaterialTimePicker.Builder().setTitleText("Pilih Jam Ambil/Kirim")
                    .setTimeFormat(TimeFormat.CLOCK_24H).build()
                timePicker.addOnPositiveButtonClickListener {
                    val time = timePicker.hour.toString().padStart(2, '0') + ":" + timePicker.minute.toString().padStart(2, '0')

                    val c = Calendar.getInstance();
                    val df = SimpleDateFormat("dd-MM-yyyy")
                    val tf = SimpleDateFormat("HH:mm");
                    val timeSelected = LocalTime.parse(time)
                    val timeNow = LocalTime.parse(tf.format(c.getTime()))
                    val openHour = LocalTime.parse(checkoutConfigs[holderPosition].open_hour)
                    val closeHour = LocalTime.parse(checkoutConfigs[holderPosition].close_hour)
                    if(df.format(c.getTime()) == holder.binding.txtDateCC.text.toString()){
                        if(timeSelected.isAfter(timeNow)){
                            if(timeSelected.isAfter(openHour) && timeSelected.isBefore(closeHour)){
                                holder.binding.txtTimeCC.setText(time)
                                checkoutConfigs[holderPosition].time = time
                            }
                            else{
                                Toast.makeText(context, "Waktu yang anda pilih berada di luar jam operasional toko!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else{
                            Toast.makeText(context, "Waktu harus lebih dari saat ini!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        if(timeSelected.isAfter(openHour) && timeSelected.isBefore(closeHour)){
                            holder.binding.txtTimeCC.setText(time)
                            checkoutConfigs[holderPosition].time = time
                        }
                        else{
                            Toast.makeText(context, "Waktu yang anda pilih berada di luar jam operasional toko!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                context?.let { it1 -> timePicker.show(it1.supportFragmentManager, "TIME_PICKER") }
            }
            else{
                Toast.makeText(context, "Silakan pilih tanggal kirim/ambil terlebih dahulu!", Toast.LENGTH_SHORT).show()
            }
        }
        holder.binding.spinnerDeliveryCC.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                if (holder.binding.spinnerDeliveryCC.selectedItem.toString() == "Ambil"){
                    checkoutConfigs[holderPosition].delivery_method = "pickup"
                }
                else{
                    checkoutConfigs[holderPosition].delivery_method = "delivery"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
        holder.binding.spinnerPaymentCC.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                if (holder.binding.spinnerPaymentCC.selectedItem.toString() == "Tunai"){
                    checkoutConfigs[holderPosition].payment_method = "cash"
                }
                else{
                    checkoutConfigs[holderPosition].payment_method = "transfer"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }
}