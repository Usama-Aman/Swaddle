package com.android.swaddle.adapters.provider_adapter

import android.content.Context
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemClassroomChildDetailBinding
import com.android.swaddle.helper.Spanny
import com.android.swaddle.models.ChildInfo
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.DateConverter
import com.bumptech.glide.Glide
import org.json.JSONArray
import java.lang.StringBuilder

class SingleClassRoomChildAdapter(
    private val context: Context,
    var list: ArrayList<ChildInfo>,
    var clickListener: ItemClickListener
) :
    RecyclerView.Adapter<SingleClassRoomChildAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        val binding: ItemClassroomChildDetailBinding = DataBindingUtil.inflate(
            inflator,
            R.layout.item_classroom_child_detail,
            parent,
            false
        )
        return MyViewHolder(binding, this@SingleClassRoomChildAdapter, context)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.onBind(list[position], position, clickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    class MyViewHolder(
        binding: ItemClassroomChildDetailBinding,
        singleClassRoomChildAdapter: SingleClassRoomChildAdapter,
        context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        private val binding: ItemClassroomChildDetailBinding = binding
        private val mAdapter: SingleClassRoomChildAdapter = singleClassRoomChildAdapter
        private val context = context

        fun onBind(item: ChildInfo, position: Int, clickListener: ItemClickListener) {
            if (item.foodAllergies != null) {
                var allAlgs = StringBuilder()
                var algs = JSONArray(item.foodAllergies ?: "")
                for (i in 0 until algs.length()) {
                    if (i == algs.length() - 1)
                        allAlgs.append(algs[i])
                    else
                        allAlgs.append(algs[i].toString() + ", ")
                }
                val disease = Spanny(
                    "Allergies: ",
                    ForegroundColorSpan(context.resources?.getColor(R.color.colorBlack) ?: 0)
                ).append(
                    allAlgs,
                    ForegroundColorSpan(
                        context.resources?.getColor(R.color.colorYellow) ?: 0
                    )
                )
                binding.tvDisease.text = disease
            } else {
                binding.tvDisease.text = "No Allergies"
            }

            binding.tvName.text = item.firstName + " " + item.lastName
            binding.tvDate.text = DateConverter.DateFormatFive(item.dob ?: "")

            Glide.with(context).load(Constants.IMG_BASE_PATH + item.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivImg)

            val duration = Spanny(
                "Most Recent Check Up: ",
                ForegroundColorSpan(context.resources?.getColor(R.color.colorBlack) ?: 0)
            ).append(
                "6 months",
                ForegroundColorSpan(
                    context.resources?.getColor(R.color.colroBlue) ?: 0
                )
            )

            binding.tvCheckUpDuration.text = duration

            val pan = Spanny(
                "Epi Pen: ",
                ForegroundColorSpan(context.resources?.getColor(R.color.colorBlack) ?: 0)
            ).append(
                item.iep ?: "",
                ForegroundColorSpan(
                    context.resources?.getColor(R.color.teal_400) ?: 0
                )
            )

            binding.tvEpiPen.text = pan

            this.itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }

            binding.ivMenu.setOnClickListener {
                clickListener.onOptionItemClicked(adapterPosition)
            }

        }

    }

    fun setItems(lis: ArrayList<ChildInfo>) {
        list = lis
    }


    interface ItemClickListener {
        fun onItemClick(position: Int)
        fun onOptionItemClicked(position: Int)
    }
}