package com.android.swaddle.adapters.provider_adapter

import android.content.Context
import android.text.format.DateFormat
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ItemRecIncReportBinding
import com.android.swaddle.helper.Spanny
import com.android.swaddle.helper.isVisible
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.CalendarModel
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class IncidentReportAdapter(
    private val context: BaseActivity,
    var listClass: ArrayList<ChildInfo>,
    var listener: ClickListener,
    var attendanceDate: String
) :
    RecyclerView.Adapter<IncidentReportAdapter.IncReportViewHolder>() {

    class IncReportViewHolder(
        itemView: ItemRecIncReportBinding,
        incidentReportAdapter: IncidentReportAdapter,
        var context: BaseActivity
    ) : RecyclerView.ViewHolder(itemView.root) {
        private val cc: Context = context
        private val adapter: IncidentReportAdapter = incidentReportAdapter
        private val binding: ItemRecIncReportBinding = itemView


        fun onBind(
            position: Int,
            listClass: ArrayList<ChildInfo>,
            listener: ClickListener
        ) {
            var item = listClass[position]

            val calendar = Calendar.getInstance()
            val today = Calendar.getInstance()
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar[Calendar.DAY_OF_WEEK] = Calendar.MONDAY

            var date = DateConverter.tempDate(adapter.attendanceDate)
            calendar.time = date

            Glide.with(context).load(Constants.IMG_BASE_PATH + item.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivImg)

            binding.tvName.text = item.firstName + " " + item.lastName

            val pan = Spanny(
                "Grade: ",
                ForegroundColorSpan(context.resources?.getColor(R.color.colorBlack) ?: 0)
            ).append(
                "N/A",
                ForegroundColorSpan(
                    context.resources?.getColor(R.color.colorYellow) ?: 0
                )
            )

            try {
                binding.tvCustody.text = DateConverter.DateFormatFive(item.dob ?: "")
                if(binding.dateLayout.isVisible()){
                    binding.tvCreatedDate.text = DateConverter.DateFormatFive(item.incidentReports?.createdAt ?: "")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvCustody.text = item.dob ?: ""
            }
            binding.btnAddIncident.setOnClickListener {
                listener.onAddIncidenceReportClick(position, item)
            }


//            if (calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                if (item.incidentReports == null) {
                    binding.btnAddIncident.viewVisible()
                    binding.lnrDraft.viewGone()
                    binding.tvSubmitted.viewGone()
                    binding.ivEdit.viewGone()
                    binding.ivView.viewGone()
                    binding.dateLayout.viewGone()

                } else {
                    if (context.userManager.user?.type == "provider" || context.userManager.user?.type == "provider") {
                        binding.ivEdit.viewVisible()
                        binding.ivView.viewVisible()
                    } else {
                        binding.ivEdit.viewGone()
                        binding.ivView.viewGone()
                    }
                    binding.btnAddIncident.viewGone()
                    binding.lnrDraft.viewGone()
                    binding.tvSubmitted.viewVisible()
                    binding.dateLayout.viewVisible()
                }
//            } else {
//                if (item.incidentReports == null) {
//                    binding.lnrDraft.viewGone()
//                    binding.tvSubmitted.viewGone()
//                    binding.ivView.viewGone()
//                    binding.ivEdit.viewGone()
//                } else {
//
//                    if (context.userManager.user?.type == "provider" || context.userManager.user?.type == "provider") {
//                        binding.ivView.viewVisible()
//                    } else {
//                        binding.ivView.viewGone()
//                    }
//
//                    binding.lnrDraft.viewGone()
//                    binding.tvSubmitted.viewVisible()
//                }
//                binding.ivEdit.viewGone()
//                binding.btnAddIncident.viewGone()
//            }

            binding.ivEdit.setOnClickListener {
                listener.onEditClick(adapterPosition, adapter.listClass[adapterPosition])
            }

            binding.ivView.setOnClickListener {
                listener.onViewClick(adapterPosition, adapter.listClass[adapterPosition])
            }
        }

    }

    fun setItems(list: ArrayList<ChildInfo>, attendanceDate: String) {
        listClass.clear()
        listClass.addAll(list)
        this.attendanceDate = attendanceDate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncReportViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemRecIncReportBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_rec_inc_report,
            parent,
            false
        )
        return IncReportViewHolder(binding, this@IncidentReportAdapter, context)
    }

    override fun onBindViewHolder(holder: IncReportViewHolder, position: Int) {
        holder.onBind(position, listClass, listener)
        holder.itemView.setOnClickListener {
            listener.onITemClick(position, listClass[position])
        }

    }

    override fun getItemCount(): Int {
        return listClass.size
    }


    interface ClickListener {
        fun onITemClick(position: Int, item: ChildInfo)
        fun onViewClick(position: Int, item: ChildInfo)
        fun onEditClick(position: Int, item: ChildInfo)
        fun onAddIncidenceReportClick(position: Int, item: ChildInfo)
    }

}