package com.rhea.onscan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.rhea.onscan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        supportFragmentManager.commit(true) {
            replace(R.id.activity_container, HomeFragment(), HomeFragment::class.java.simpleName)
        }
        setObservers()
    }

    private fun setObservers() {
        viewModel.qrType.observe(this) {
            addFragment(ScanFragment())
        }
        viewModel.scanResult.observe(this){
            addFragment(ResultFragment())
        }
    }

    private fun addFragment(fragment: Fragment){
        supportFragmentManager.commit(true){
            val tag =  fragment::class.java.simpleName
            add(R.id.activity_container, fragment, tag)
            addToBackStack(tag)
        }
    }
}