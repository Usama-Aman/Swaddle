package com.android.swaddle.adapters.certification_adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemCertificationsBinding
import com.android.swaddle.models.get_certifications.CertificationsData
import com.android.swaddle.utils.DateConverter

class CertificationsAdapter(
    private val context: Context,
    var clickListener: CertificationsAdapterListener

) : RecyclerView.Adapter<CertificationsAdapter.ViewHolder>() {
    private var certificationsList: ArrayList<CertificationsData> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemCertificationsBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_certifications,
            parent,
            false
        )
        return ViewHolder(context, binding, this@CertificationsAdapter)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = certificationsList[position]
        holder.onBind(model)

        val bind = holder.binding
        bind.ivClose.setOnClickListener {
            clickListener.onDeleteClick(
                holder.adapterPosition, certificationsList.get(holder.adapterPosition)
            )
        }
    }

    override fun getItemCount(): Int {
        return certificationsList.size
    }

    class ViewHolder(
        val context: Context,
        val binding: ItemCertificationsBinding,
        val adapter: CertificationsAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(model: CertificationsData) {
            binding.tvCertificate.text = model.name
//            binding.tvDate.text = DateConverter.ConvertDateFormat4(model.date ?: "")
            binding.tvDate.text =
                DateConverter.convertGenericDate(model.date ?: "", "yyyy-MM-dd", "MM / dd / yyyy")

        }
    }

    fun updateCertificationsAdapter(arrayList: ArrayList<CertificationsData>?) {
        this.certificationsList = arrayList!!
        notifyDataSetChanged()
    }

    interface CertificationsAdapterListener {
        fun onDeleteClick(position: Int, item: CertificationsData)
    }
}