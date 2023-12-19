package com.android.swaddle.baseClasses

import android.content.Context
import androidx.fragment.app.Fragment
import com.android.swaddle.prefrences.UserData


open class BaseFragment : Fragment() {
    lateinit var mContext: Context
    lateinit var mActivity: BaseActivity
//    val userManager: User
//        get() {
//            return UserData.user(mContext)
//        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as BaseActivity
    }

    var isViewCreated = false

    fun Any.toNotNullString(): String {
        //    Log.d("toString",this.toString())
        return if (this == "null") {
            this.toString().replace("null", "")
        } else {
            this.toString()
        }
    }


    protected open fun onBecameVisibleToUser() {

    }


    protected open fun onRecycle() {

    }

    override fun onDestroy() {
        onRecycle()
        super.onDestroy()
    }

    protected open fun onBecameInvisibleToUser() {

    }

    final override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            onBecameVisibleToUser()
        } else {
            onBecameInvisibleToUser()
        }
    }
}