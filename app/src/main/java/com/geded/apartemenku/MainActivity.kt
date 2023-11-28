package com.geded.apartemenku

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.geded.apartemenku.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val fragments: ArrayList<Fragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /* BottomNav */
        fragments.add(HomeFragment())
        fragments.add(TransactionFragment())
        fragments.add(ProfileFragment())

        binding.viewPager.adapter = ViewPagerAdapter(this, fragments)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.bottomNavView.selectedItemId = binding.bottomNavView.menu.getItem(position).itemId
            }
        })
        binding.bottomNavView.setOnItemSelectedListener {
            binding.viewPager.currentItem = when(it.itemId){
                R.id.itemHome -> 0
                R.id.itemTransaction -> 1
                R.id.itemProfile -> 2
                else -> 0
            }
            true
        }
        /* End Bottom Nav */
    }
}