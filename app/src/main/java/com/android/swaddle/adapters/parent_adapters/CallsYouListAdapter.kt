package com.android.swaddle.adapters.parent_adapters

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ItemWhatChildCallsYouBinding
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.utils.Constants
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

class CallsYouListAdapter(
    private val mContext: BaseActivity,
    private var list: ArrayList<ChildInfo>,
    var clickListener: ItemClickListener
) :
    RecyclerView.Adapter<CallsYouListAdapter.ClassRoomViewHolder>() {
    var listener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassRoomViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemWhatChildCallsYouBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_what_child_calls_you,
            parent,
            false
        )
        return ClassRoomViewHolder(binding, this@CallsYouListAdapter, mContext)
    }

    override fun onBindViewHolder(holder: ClassRoomViewHolder, position: Int) {
        holder.onBind(list[position], position, clickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItems(lis: ArrayList<ChildInfo>) {
        list = lis
    }

    class ClassRoomViewHolder(
        val mBinding: ItemWhatChildCallsYouBinding,
        private val messageAdapter: CallsYouListAdapter,
        val contex: BaseActivity
    ) : RecyclerView.ViewHolder(mBinding.root) {

        fun onBind(
            item: ChildInfo,
            position: Int,
            clickListener: ItemClickListener
        ) {
            this.itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition, item)
            }

            Glide.with(contex).load(Constants.IMG_BASE_PATH + item.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder).into(mBinding.ivProfile)

            mBinding.tvProfileName.text = item.firstName + " " + item.lastName

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                setDOB(mBinding, item)
            }
            try {
                var obj = JSONArray(item.authorizedAdultChildCall)
                for (i in 0 until obj.length()) {
                    if (JSONObject(
                            obj.get(i).toString()
                        ).getInt("id") == contex.userManager.user?.id ?: 0
                    ) {
                        mBinding.etCallsYou.setText(
                            JSONObject(
                                obj.get(i).toString()
                            ).getString("calls_you")
                        )
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            mBinding.etCallsYou.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    item.callsYou = s.toString()
                    Log.e("callsYou", item.callsYou ?: "")
                }
            })
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun setDOB(binding: ItemWhatChildCallsYouBinding, item: ChildInfo) {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val month2 = if ((month + 1) < 10) {
                "0${month + 1}"
            } else {
                "${month + 1}"
            }
            val day2 = if ((day) < 10) {
                "0${day}"
            } else {
                "${day}"
            }
            val year2 = if ((year) < 10) {
                "${year}"
            } else {
                "${year}"
            }


            val myDate = "${year}-${month2}-${day2}"
            val monthsBetween: Long = ChronoUnit.MONTHS.between(
                LocalDate.parse(item.dob).withDayOfMonth(1),
                LocalDate.parse(myDate).withDayOfMonth(1)
            )
            val yearsBetween: Long = ChronoUnit.YEARS.between(
                LocalDate.parse(item.dob).withDayOfMonth(1),
                LocalDate.now(ZoneId.systemDefault())
//            LocalDate.parse(year2).withDayOfMonth(1)
            )

            when {
                yearsBetween < 1 -> {
                    binding.tvAge.setText("${monthsBetween % 12}m")
                }
                monthsBetween % 12 < 1 -> {
                    binding.tvAge.setText("${yearsBetween}y")
                }
                else -> {
                    binding.tvAge.setText("${yearsBetween}y, ${monthsBetween % 12}m")
                }
            }


        }
    }


    interface ItemClickListener {
        fun onItemClick(position: Int, item: ChildInfo)
        fun onSubmitClick(position: Int, item: ChildInfo)
    }
}