package com.android.hilaron.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.swaddle.R
import com.android.swaddle.databinding.SingleCountryBinding
import com.android.swaddle.listeners.CountryData
import com.android.swaddle.models.CountryDataModel

class CountryAdapter(
    private var countryData: ArrayList<CountryDataModel>,
    private val itemClickListener: CountryData
) : RecyclerView.Adapter<CountryAdapter.CountryVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding: SingleCountryBinding =
            DataBindingUtil.inflate(inflater, R.layout.single_country, parent, false)
        return CountryVH(binding, this)
    }

    override fun getItemCount(): Int {
        return countryData.size
    }

    override fun onBindViewHolder(holder: CountryVH, position: Int) {
        holder.bind(country = countryData[position], position = position)
    }

    fun onClicked(position: Int) {
        itemClickListener.getSelectedCountryData(
            position = position,
            countryCode = countryData[position].dial_code
        )
    }


    class CountryVH(binding: SingleCountryBinding, adapter: CountryAdapter) :
        RecyclerView.ViewHolder(binding.root) {
        private var mBinding: SingleCountryBinding = binding
        private var mAdapter: CountryAdapter = adapter

        fun bind(country : CountryDataModel, position: Int) {
            mBinding.adapter = mAdapter
            mBinding.model = country
            mBinding.position = position
        }
    }
}