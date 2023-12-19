package com.android.swaddle.fragments.dialog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.android.hilaron.adapter.CountryAdapter
import com.android.swaddle.R
import com.android.swaddle.databinding.CountrySelectionPopupBinding
import com.android.swaddle.listeners.CountryData
import com.android.swaddle.models.CountryDataModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CountryPickerDialogue : DialogFragment(), CountryData {
    private var countryDataListener: CountryData? = null
    private lateinit var mBinding: CountrySelectionPopupBinding
    private var countryData = ArrayList<CountryDataModel>()
    private var filteredCountriesList = ArrayList<CountryDataModel>()
    private lateinit var countryAdapter: CountryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_FRAME, R.style.ReportDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.country_selection_popup, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val text = resources.openRawResource(R.raw.country_codes)
            .bufferedReader().use { it.readText() }
        countryData = getCountryList(text) as ArrayList<CountryDataModel>
        filteredCountriesList.clear()
        filteredCountriesList.addAll(countryData)
        countryAdapter = CountryAdapter(filteredCountriesList, this)
        mBinding.rvSelectCountry.adapter = countryAdapter

        mBinding.etSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                filteredCountriesList.clear()
                if (s.toString().isNotEmpty())
                    filteredCountriesList.addAll(countryData.filter {
                        it.name.toLowerCase().contains(s.toString().toLowerCase())
                    })
                else
                    filteredCountriesList.addAll(countryData)
                countryAdapter.notifyDataSetChanged()
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {

            }
        })

    }

    fun setListener(countryData: CountryData) {
        this.countryDataListener = countryData
    }

    companion object {
        @JvmStatic
        fun newInstance(
        ) =
            CountryPickerDialogue().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun getSelectedCountryData(position: Int, countryCode: String) {
        dismiss()
        this.countryDataListener?.getSelectedCountryData(position, countryCode)
    }

    private fun getCountryList(jsonObject: String): List<CountryDataModel> {
        val gson = Gson()
        val type = object : TypeToken<List<CountryDataModel>>() {}.type
        return gson.fromJson(jsonObject, type)
    }
}

