package com.android.swaddle.adapters.picker_adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemImagesPickedBinding
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.utils.Constants
import com.bumptech.glide.Glide


class IncidentReportImagesPickerAdapter(
    private val mContext: Context,
    var canUpdate: Boolean,
    var canUpdate2: Boolean
) :
    RecyclerView.Adapter<IncidentReportImagesPickerAdapter.ImagePickerViewHolder>() {
    private var clickListener: CLickListener? = null
    private var dataList: ArrayList<String> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePickerViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemImagesPickedBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_images_picked,
            parent,
            false
        )
        return ImagePickerViewHolder(
            binding,
            this@IncidentReportImagesPickerAdapter,
            mContext
        )
    }

    override fun onBindViewHolder(holder: ImagePickerViewHolder, position: Int) {
        val uri = dataList[position]
        holder.onBind(uri, canUpdate, canUpdate2)
        holder.clickListener()

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setData(list: ArrayList<String>) {
        dataList = list
        notifyDataSetChanged()
    }


    class ImagePickerViewHolder(
        private val binding: ItemImagesPickedBinding,
        private val adapter: IncidentReportImagesPickerAdapter,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("UseCompatLoadingForColorStateLists")
        fun onBind(uri: String, canUpdate: Boolean, canUpdate2: Boolean) {
            if (uri.contains("storage/childs/incident_report")) {
                Glide.with(context).load(Constants.IMG_BASE_PATH + uri)
                    .placeholder(R.drawable.ic_user_placeholder_new)
                    .into(binding.img)

            } else {
                Glide.with(context).load(uri)
                    .placeholder(R.drawable.ic_user_placeholder_new)
                    .into(binding.img)

            }

            if (canUpdate2) {
                binding.ivCross.viewVisible()
            } else if (canUpdate) {
                binding.ivCross.viewGone()
            }
        }

        fun clickListener() {
            binding.ivCross.setOnClickListener {
                adapter.clickListener?.onRemoveClick(adapterPosition)
            }
        }
    }


    fun setListener(listener: CLickListener) {
        clickListener = listener
    }


    interface CLickListener {
        fun onItemClick(pos: Int)
        fun onRemoveClick(pos: Int)
    }
}