package com.android.swaddle.activities.common

import android.os.Bundle
import com.android.swaddle.activities.parent.AddChildActivity
import com.android.swaddle.activities.providers.ProviderMainActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityRegistrationSuccessfulBinding
import org.jetbrains.anko.startActivity

class RegistrationSuccessfulActivity : BaseActivity() {
    private lateinit var binding: ActivityRegistrationSuccessfulBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationSuccessfulBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvFinish.setOnClickListener {
            finishAffinity()
            startActivity<ProviderMainActivity>()
        }

        binding.tvEnrollAnotherChild.setOnClickListener {
            startActivity<AddChildActivity>("fromAddAnother" to true)
        }
    }
}