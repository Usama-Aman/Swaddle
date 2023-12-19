package com.android.swaddle.adapters.ethnicity_bottom_sheet_adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.adapters.parents_bottom_sheet_adapter.ParentsBottomSheetAdapter
import com.android.swaddle.databinding.ItemEthnicityBottomSheetBinding
import com.android.swaddle.models.User

class EthnicityBottomSheetAdapter(
    private val context: Context,
    private var list: ArrayList<EthnicityList>,
    var clickListener: ItemClickListener

) : RecyclerView.Adapter<EthnicityBottomSheetAdapter.ViewHolder>() {
    var listener: ItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemEthnicityBottomSheetBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_ethnicity_bottom_sheet,
            parent,
            false
        )
        return ViewHolder(binding, context, this@EthnicityBottomSheetAdapter)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(list[position], position, clickListener, list)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItems(lis: ArrayList<EthnicityList>) {
        list = lis
    }

    class ViewHolder(
        val binding: ItemEthnicityBottomSheetBinding,
        val context: Context,
        val adapter: EthnicityBottomSheetAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(
            item: EthnicityList,
            position: Int,
            clickListener: ItemClickListener,
            list: ArrayList<EthnicityList>
        ) {
            binding.tvEthnicity.text = item.text

            this.itemView.setOnClickListener {
                clickListener.onItemChecked(adapterPosition, item)
            }
            if (item.isChecked) {
                binding.iv.setImageDrawable(context.getDrawable(R.drawable.ic_check_new))
                binding.tvEthnicity.setTextColor(context.resources.getColor(R.color.colroBlue))
            } else {
                binding.tvEthnicity.setTextColor(context.resources.getColor(R.color.black))
                binding.iv.setImageDrawable(context.getDrawable(R.drawable.ic_box_new))
            }

            binding.iv.setOnClickListener {
                clickListener.onItemChecked(position, item)
            }
        }
    }


    interface ItemClickListener {
        fun onItemChecked(position: Int, item: EthnicityList)
    }
}

data class EthnicityList(val text: String, var isChecked: Boolean = false)
