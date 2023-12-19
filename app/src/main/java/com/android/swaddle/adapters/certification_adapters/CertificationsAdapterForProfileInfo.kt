package com.android.swaddle.adapters.certification_adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.ItemCertificationForProfileInfoBinding
import com.android.swaddle.databinding.ItemCertificationsBinding
import com.android.swaddle.models.get_certifications.CertificationsData
import com.android.swaddle.utils.DateConverter

class CertificationsAdapterForProfileInfo(
    private val context: Context
) : RecyclerView.Adapter<CertificationsAdapterForProfileInfo.ViewHolder>() {
        var certificationsList: ArrayList<CertificationsData> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemCertificationForProfileInfoBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_certification_for_profile_info,
            parent,
            false
        )
        return ViewHolder(context, this@CertificationsAdapterForProfileInfo, binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = certificationsList[position]
        holder.onBind(model)
    }

    override fun getItemCount(): Int {
        return certificationsList.size
    }

    class ViewHolder(
        val context: Context,
        val adapter: CertificationsAdapterForProfileInfo,
        val binding: ItemCertificationForProfileInfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(model: CertificationsData) {
            binding.tvCertificate.text = model.name
            binding.tvDate.text = DateConverter.convertGenericDate(model.date ?: "", "yyyy-MM-dd", "MM / dd / yyyy")
        }
    }

    fun updateCertificationsAdapter(arrayList: ArrayList<CertificationsData>?) {
        this.certificationsList = arrayList!!
        notifyDataSetChanged()
    }

}