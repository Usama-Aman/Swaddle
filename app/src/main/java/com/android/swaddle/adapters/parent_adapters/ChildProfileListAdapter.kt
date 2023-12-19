package com.android.swaddle.adapters.parent_adapters

import android.annotation.SuppressLint
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.adapters.AllergiesAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ItemChildProfilelistParentBinding
import com.android.swaddle.helper.CustomTypefaceSpan
import com.android.swaddle.helper.Spanny
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.item_child_profilelist_parent.view.*
import org.json.JSONArray
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

class ChildProfileListAdapter(private val mContext: BaseActivity, var list: ArrayList<ChildInfo>) :
    RecyclerView.Adapter<ChildProfileListAdapter.ChildProfileViewHolder>() {
    private lateinit var clickListener: ItemClickListener
    public fun setItems(updatedList: ArrayList<ChildInfo>) {
        this.list = updatedList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildProfileViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemChildProfilelistParentBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_child_profilelist_parent,
            parent,
            false
        )
        return ChildProfileViewHolder(
            binding,
            this@ChildProfileListAdapter,
            mContext
        )
    }

    override fun onBindViewHolder(holder: ChildProfileViewHolder, position: Int) {
        holder.onBind(list[position], position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ChildProfileViewHolder(
        val binding: ItemChildProfilelistParentBinding,
        val adapter: ChildProfileListAdapter,
        val mContext: BaseActivity
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "NewApi")
        fun onBind(item: ChildInfo, position: Int) {

            itemView.setOnClickListener {
                adapter.clickListener.onItemClick(adapterPosition, item)
            }

            binding.ivMenu.setOnClickListener { adapter.clickListener.onOptionItemClicked(position) }
            binding.tvSelectClassRoom.setOnClickListener {
                if ((mContext.userManager.user?.type == "provider" || mContext.userManager.user?.type == "staff") && item.deletedAt == null) {
                    adapter.clickListener.onPromoteClick(position)
                }
            }

            Glide.with(mContext).load(Constants.IMG_BASE_PATH + item.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivImg)

            binding.tvName.text = item.firstName + " " + item.lastName

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
                monthsBetween % 12 < 1 && yearsBetween < 1 -> {
                    binding.tvAge.setText("Less than 1 month")
                }
                else -> {
                    binding.tvAge.setText("${yearsBetween}y, ${monthsBetween % 12}m")
                }
            }

            /* val pan = Spanny(
                 "Age: ",
                 ForegroundColorSpan(mContext.resources?.getColor(R.color.colorBlack) ?: 0)
             ).append(
                 DateConverter.TimeStampNew(item.createdAt ?: "") ?: "",
                 ForegroundColorSpan(
                     mContext.resources?.getColor(R.color.colorAccent) ?: 0
                 )
             )

             binding.tvGrade.text = pan*/
            try {
                binding.tvCustody.text = DateConverter.jDateFormatFour(item.dob ?: "")
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvCustody.text = item.dob ?: ""
            }

            try {
                if (item.foodAllergies != null || item.environmentalAllergies != null) {
                    var allAlgs = ArrayList<String>()
                    if (item.foodAllergies != null) {

                        var algs = JSONArray(item.foodAllergies ?: "")
                        for (i in 0 until algs.length()) {
                            allAlgs.add(algs[i].toString())
                        }
                    }
                    if (item.environmentalAllergies != null) {
                        var environmentalAllergies = JSONArray(item.environmentalAllergies ?: "")
                        for (i in 0 until environmentalAllergies.length()) {
                            allAlgs.add(environmentalAllergies[i].toString())
                        }
                    }
                    setAllergiesAdapter(binding.rvTags, allAlgs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (mContext.userManager.user?.type == "provider" || mContext.userManager.user?.type == "staff") {
//                binding.ivMenu.viewVisible()

                if (item.classroomDetails != null) {
                    val classRoom = Spanny(
                        "Promoted to: ",
                        ForegroundColorSpan(
                            mContext.resources?.getColor(R.color.colorDashBoardBlack) ?: 0
                        )
                    ).append(
                        item.classroomDetails?.name ?: "",
                        ForegroundColorSpan(
                            mContext.resources?.getColor(R.color.colorAccent) ?: 0
                        ),
                        CustomTypefaceSpan(mContext.fontBold!!)
                    )
                    binding.rlPromotedClassroom.viewVisible()
                    binding.tvClassRoom.text = classRoom
                    binding.tvClassRoom.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_tick,
                        0,
                        0,
                        0
                    )
                    binding.tvSelectClassRoom.viewGone()
                    binding.tvTitleClassRoom.viewGone()
                } else {
                    binding.rlPromotedClassroom.viewGone()
                    binding.tvSelectClassRoom.viewGone()
                    binding.tvTitleClassRoom.viewGone()
                }
            } else {
//                binding.ivMenu.viewGone()
                if (item.classroomDetails != null) {
                    val classRoom = Spanny(
                        "Classroom: ",
                        ForegroundColorSpan(
                            mContext.resources?.getColor(R.color.colorDashBoardBlack) ?: 0
                        )
                    ).append(
                        item.classroomDetails?.name ?: "",
                        ForegroundColorSpan(
                            mContext.resources?.getColor(R.color.colorAccent) ?: 0
                        ),
                        CustomTypefaceSpan(mContext.fontBold!!)
                    )
                    binding.rlPromotedClassroom.viewVisible()
                    binding.tvClassRoom.text = classRoom
                    binding.tvClassRoom.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    binding.tvSelectClassRoom.viewGone()
                    binding.tvTitleClassRoom.viewGone()
                } else {
                    binding.rlPromotedClassroom.viewGone()
                    binding.tvSelectClassRoom.viewGone()
                    binding.tvTitleClassRoom.viewGone()
                }
            }

            if (item.deletedAt != null) {
                binding.viewDeleted.viewVisible()
            } else {
                binding.viewDeleted.viewGone()
            }
        }

        private fun setAllergiesAdapter(rvTags: RecyclerView, data: ArrayList<String>) {
            var layoutManager = FlexboxLayoutManager(mContext)
            layoutManager.flexDirection = FlexDirection.ROW
            layoutManager.justifyContent = JustifyContent.FLEX_START
            rvTags.layoutManager = layoutManager
            var adapter =
                AllergiesAdapter(
                    mContext,
                    data
                )
            rvTags.adapter = adapter

            if (data.size > 0) {
                binding.tvNoAllergies.viewGone()
            } else {
                binding.tvNoAllergies.viewVisible()
            }
        }
    }

    fun setListener(listener: ItemClickListener) {
        clickListener = listener
    }

    interface ItemClickListener {
        fun onItemClick(position: Int, item: ChildInfo)
        fun onPromoteClick(position: Int)
        fun onOptionItemClicked(position: Int)
    }

}