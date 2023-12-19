package com.android.swaddle.adapters.child_adpaters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemChildIncReportImagesBinding
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.slider.MediaSliderActivity
import com.bumptech.glide.Glide

class ChildIncRepImagesAdapter(
    private val context: com.android.swaddle.baseClasses.BaseActivity,
    private var list: ArrayList<String>

) : RecyclerView.Adapter<ChildIncRepImagesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemChildIncReportImagesBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_child_inc_report_images,
            parent,
            false
        )
        return ViewHolder(binding, context, this@ChildIncRepImagesAdapter)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.onBind(model, position)
    }

    override fun getItemCount(): Int {

        return list.size
    }

    class ViewHolder(
        val binding: ItemChildIncReportImagesBinding,
        val context: Context,
        val adapter: ChildIncRepImagesAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: String, position: Int) {
            Glide.with(context).load(Constants.IMG_BASE_PATH + item)
                .placeholder(R.drawable.ic_image_placeholder).into(binding.image)
            binding.image.setOnClickListener {
                val mMediaArray = ArrayList<MediaSliderActivity.MediaModel>()

                adapter.list.forEachIndexed { index, model ->
                    mMediaArray.add(
                        MediaSliderActivity.MediaModel(
                            Constants.IMG_BASE_PATH + model,
                            "image",
                            Constants.IMG_BASE_PATH
                        )
                    )
                }
                adapter.context.loadMediaSliderView(
                    mMediaArray,
                    position,
                    "Incident Report",
                    HIDE_DOTS = true
                )
            }
        }
    }


}