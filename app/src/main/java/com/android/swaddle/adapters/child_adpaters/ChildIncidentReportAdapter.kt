package com.android.swaddle.adapters.child_adpaters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ItemRecChildIncBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.utils.DateConverter
import org.json.JSONArray

class ChildIncidentReportAdapter(
    private val context: BaseActivity,
    private var list: ArrayList<ChildInfo>

) : RecyclerView.Adapter<ChildIncidentReportAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemRecChildIncBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_rec_child_inc,
            parent,
            false
        )
        return ViewHolder(context, this@ChildIncidentReportAdapter, binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.onBind(list, position)
    }

    fun setItems(list: ArrayList<ChildInfo>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(
        val context: BaseActivity,
        val adapter: ChildIncidentReportAdapter,
        val binding: ItemRecChildIncBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(list: ArrayList<ChildInfo>, position: Int) {
            var item = list[position]
            binding.tvChildName.text = item.firstName + " " + item.lastName + " - Incident Report"
            binding.tvWhatHappenedDescription.text = item.incidentReports?.whatHappened
            binding.tvTreatmentGivenDescription.text = item.incidentReports?.treatmentGiven
            binding.tvTeacherCommentDescription.text = item.incidentReports?.teacherComment
            if (item.incidentReports != null)
                binding.tvDate.text =
                    DateConverter.convertGenericDate(
                        item.incidentReports?.createdAt ?: "",
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        "MMM dd, yyyy"
                    )
            if (item.incidentReports != null)
                binding.tvDay.text =
                    DateConverter.convertGenericDate(
                        item.incidentReports?.createdAt ?: "",
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        "EEEE"
                    )

            binding.ivDropDown.setOnClickListener {
                binding.expandLayout.toggle()
                if (binding.expandLayout.isExpanded) {
                    binding.ivDropDown.scaleY = -1f
                    binding.viewMiddle.viewGone()
                } else {
                    binding.ivDropDown.scaleY = 1f
                    binding.viewMiddle.viewVisible()
                }
            }

            val previousFiles: ArrayList<String> = ArrayList()
            if (item.incidentReports != null) {
                try {
                    if (item.incidentReports?.media != null) {

                        val array = JSONArray(item.incidentReports?.media ?: "")
                        for (i in 0 until array.length()) {
                            previousFiles.add(array[i].toString())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val secondAdapter = ChildIncRepImagesAdapter(context, previousFiles)
            binding.rvIncidentReportImg.adapter = secondAdapter
        }
    }
}