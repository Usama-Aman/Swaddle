package com.android.swaddle.adapters.provider_adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ItemMedicalReportBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.DailyReport
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide

class MedicalReportAdapter(
    private val mContext: BaseActivity,
    val list: ArrayList<DailyReport>,
    var clickListener: ItemClickListener
) :
    RecyclerView.Adapter<MedicalReportAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemMedicalReportBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_medical_report,
            parent,
            false
        )
        return ReportViewHolder(binding, this@MedicalReportAdapter, mContext)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.onBind(list[position], clickListener, position, mContext)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItems(mList: ArrayList<DailyReport>) {
        list.clear()
        list.addAll(mList)
    }

    class ReportViewHolder(
        itemView: ItemMedicalReportBinding,
        dailyReportAdapter: MedicalReportAdapter,
        contex: BaseActivity
    ) : RecyclerView.ViewHolder(itemView.root) {
        private val binding: ItemMedicalReportBinding = itemView


        @SuppressLint("SetTextI18n")
        fun onBind(
            item: DailyReport,
            clickListener: ItemClickListener,
            position: Int,
            mContext: BaseActivity
        ) {
            try {

                binding.imgDownload.setOnClickListener {
                    clickListener.onDownloadClick(item.id!!)
                }
                binding.imgDelete.setOnClickListener {
                    clickListener.onDeleteClick(position)
                }
                binding.imgPerview.setOnClickListener {
                    clickListener.onViewDocClick(item.id!!)
                }
                /* itemView.setOnClickListener {
                     if (item.draft == 1) {
                         clickListener.onOpenDraftClick(position)
                     } else {
                         clickListener.onDownloadClick(position)
                     }
                 }*/
                if (item.draft == 1) {
                    binding.imgDownload.viewGone()
                    binding.tvFileName.text = "Drafted Report"
                    binding.tvDate.text = "Available for 24 hours"
                    binding.tvDate.setTextColor(Color.parseColor("#ff0000"))
                } else {
                    binding.tvDate.setTextColor(Color.parseColor("#636872"))
                    binding.tvDate.text = DateConverter.TimeStampNew(item.createdAt ?: "")
                    binding.tvFileName.text = (item.filePath ?: "").split("/").last()
                    binding.imgDownload.viewVisible()
                }
                if ((item.filePath ?: "").split("/").last().split(".").last() == "txt") {
                    Glide.with(mContext).load(R.drawable.ic_txt).into(binding.imgPdf)
                } else if ((item.filePath ?: "").split("/").last().split(".").last() == "xls") {
                    Glide.with(mContext).load(R.drawable.ic_xls).into(binding.imgPdf)
                } else if ((item.filePath ?: "").split("/").last().split(".").last() == "png") {
                    Glide.with(mContext).load(R.drawable.ic_png).into(binding.imgPdf)
                } else if ((item.filePath ?: "").split("/").last().split(".").last() == "pdf") {
                    Glide.with(mContext).load(R.drawable.ic_pdf).into(binding.imgPdf)
                } else if ((item.filePath ?: "").split("/").last().split(".").last() == "jpg"
                    || (item.filePath ?: "").split("/").last().split(".").last() == "jpeg"
                ) {
                    Glide.with(mContext).load(R.drawable.ic_jpg).into(binding.imgPdf)
                } else if ((item.filePath ?: "").split("/").last().split(".").last() == "docx"
                    || (item.filePath ?: "").split("/").last().split(".").last() == "doc"
                ) {
                    Glide.with(mContext).load(R.drawable.ic_doc).into(binding.imgPdf)
                } else {
                    Glide.with(mContext).load(R.drawable.ic_pdf).into(binding.imgPdf)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    interface ItemClickListener {
        fun onItemClick(position: Int)
        fun onDownloadClick(id: Int)
        fun onOpenDraftClick(position: Int)
        fun onViewDocClick(id: Int)
        fun onDeleteClick(position: Int)
    }
}