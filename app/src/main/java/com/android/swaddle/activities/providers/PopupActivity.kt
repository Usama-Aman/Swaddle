package com.android.swaddle.activities.providers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.swaddle.databinding.ActivityPopupBinding
import org.jetbrains.anko.startActivity

class PopupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPopupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener{
            startActivity<LoginActivity>()
            finish()
        }
        binding.tvGoBack.setOnClickListener{
            startActivity<LoginActivity>()
            finish()
        }
    }
}