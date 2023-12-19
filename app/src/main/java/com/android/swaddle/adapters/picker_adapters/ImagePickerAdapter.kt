package com.android.swaddle.adapters.picker_adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemImagesPickedBinding
import com.bumptech.glide.Glide


class ImagePickerAdapter(private val mContext: Context) :
    RecyclerView.Adapter<ImagePickerAdapter.ImagePickerViewHolder>() {
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
            this@ImagePickerAdapter,
            mContext
        )
    }

    override fun onBindViewHolder(holder: ImagePickerViewHolder, position: Int) {
        val uri = dataList[position]
        holder.onBind(uri)
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
        private val adapter: ImagePickerAdapter,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("UseCompatLoadingForColorStateLists")
        fun onBind(uri: String) {
            Glide.with(context).load(uri).into(binding.img)

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