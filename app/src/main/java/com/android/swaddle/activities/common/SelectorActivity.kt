package com.android.swaddle.activities.common

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.swaddle.R
import com.android.swaddle.activities.providers.LoginActivity
import com.android.swaddle.activities.providers.SignupActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivitySelectorBinding
import com.android.swaddle.enums.UserType
import com.android.swaddle.prefrences.UserData
import org.jetbrains.anko.startActivity

class SelectorActivity : BaseActivity() {
    private lateinit var binding: ActivitySelectorBinding
    private lateinit var userType: UserType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVar()
        listener()
    }

    private fun listener() {
        binding.btnProvider.setOnClickListener {
            userType = UserType.PROVIDER
            selection(binding.btnProvider, binding.tvProvider)
//            UserData.saveUserType(UserType.PROVIDER.type, this)
            startActivity<SignupActivity>("type" to UserType.PROVIDER)
        }

        binding.btnParent.setOnClickListener {
            userType = UserType.PARENT
            selection(binding.btnParent, binding.tvParent)
            UserData.saveUserType(UserType.PARENT.type, this)
//            startActivity(Intent(this, SignupActivity::class.java))
            startActivity<SignupActivity>("type" to UserType.PARENT)
        }

        binding.btnStaff.setOnClickListener {
            userType = UserType.STAFF
            selection(binding.btnStaff, binding.tvStaff)
            UserData.saveUserType(UserType.STAFF.type, this)
//            startActivity(Intent(this, SignupActivity::class.java))
            startActivity<SignupActivity>("type" to UserType.STAFF)
        }

        binding.layViewBack.setOnClickListener {
            finish()
        }

        binding.lnrSigninLabel.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        }


    }

    private fun initVar() {

    }

    private fun selection(rel: RelativeLayout, tv: TextView) {
        binding.btnStaff.isSelected = false
        binding.btnParent.isSelected = false
        binding.btnProvider.isSelected = false

        //change text color
        binding.tvStaff.setTextColor(ContextCompat.getColor(this, R.color.colorLoginBlack))
        binding.tvParent.setTextColor(ContextCompat.getColor(this, R.color.colorLoginBlack))
        binding.tvProvider.setTextColor(ContextCompat.getColor(this, R.color.colorLoginBlack))

        rel.isSelected = true
        tv.setTextColor(ContextCompat.getColor(this, R.color.colorWhite))
    }

}