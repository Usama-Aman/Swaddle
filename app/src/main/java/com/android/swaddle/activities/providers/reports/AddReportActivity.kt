package com.android.swaddle.activities.providers.reports

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.android.swaddle.R
import com.android.swaddle.adapters.spinneradapter.BowelSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityAddReportBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.models.*
import com.android.swaddle.response.ResponseTypes
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.CustomToasts
import com.android.swaddle.utils.DateConverter
import com.android.swaddle.utils.apiController.ApiCallBack2
import com.android.swaddle.utils.apiController.ApiController2
import com.bumptech.glide.Glide
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.android.synthetic.main.activity_add_report.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.json.JSONArray
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class AddReportActivity : BaseActivity(), ApiCallBack2 {

    private lateinit var binding: ActivityAddReportBinding
    private var spinnerAdapter: BowelSpinnerAdapter? = null
    private var selectedBowl = "1"
    private var bowlList = ArrayList<String>()

    var selectedChild: ChildInfo? = null
    var dateToSave = ""
    var draft: DailyReport? = null
    private var breakFastType = ""
    private var lunchType = ""
    private var snacksType = ""
    private var bowlMovementType = ""
    private var mCalendar: Calendar = Calendar.getInstance()
    private var tb1String: String? = null
    private var tb2Long: Long? = null
    private var tb2String: String? = ""


    private var tb3Long: Long? = null
    private var tb3String: String? = ""

    private var tb4Long: Long? = null
    private var tb4String: String? = ""

    private var tb5Long: Long? = null
    private var tb5String: String? = ""

    private var tb6Long: Long? = null
    private var tb6String: String? = ""

    private var napTime1StartLong: Long? = null
    private var napTime1StartString: String? = ""

    private var napTime1EndLong: Long? = null
    private var napTime1EndString: String? = ""

    private var napTime2StartLong: Long? = null
    private var napTime2StartString: String? = ""

    private var napTime2EndLong: Long? = null
    private var napTime2EndString: String? = ""

    private var napTime3StartLong: Long? = null
    private var napTime3StartString: String? = ""

    private var napTime3EndLong: Long? = null
    private var napTime3EndString: String? = ""

    private var babyThings: ArrayList<String> = ArrayList()

    var oz1 = ""
    var oz2 = ""
    var oz3 = ""
    var oz4 = ""
    var oz5 = ""
    var oz6 = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        listener()

    }

    private fun setClassSpinner() {
        if (spinnerAdapter == null) {
            spinnerAdapter = BowelSpinnerAdapter(this@AddReportActivity, bowlList)
            binding.spBowel.adapter = spinnerAdapter
        } else {
            spinnerAdapter?.setItems(bowlList)
            spinnerAdapter?.notifyDataSetChanged()
        }
        binding.spBowel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedBowl = bowlList[position]
                Log.e("selectedBowl", bowlList[position])
                //   showSuccessToast(this@AddActivity,"Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun populateDraft() {
        binding.etBreakfast.setText(draft?.breakfastDescription ?: "")
        binding.etLunch.setText(draft?.lunchDescription ?: "")
        binding.etSnack.setText(draft?.snackDescription ?: "")
// set nap times
        try {
            var naps = JSONArray(draft?.napTimes)
            when (naps.length()) {
                1 -> {
                    binding.tvNapTime1Start.text = ""
                }
                2 -> {

                }
                3 -> {

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        when (draft?.breakfastPortion ?: "") {
            "All" -> {
                binding.rbBreakFastAll.isChecked = true
                breakFastType = "All"
            }
            "Some" -> {
                binding.rbBreakFastSome.isChecked = true
                breakFastType = "Some"
            }
            "None" -> {
                binding.rbBreakFastNone.isChecked = true
                breakFastType = "None"
            }
        }

        when (draft?.lunchPortion ?: "") {
            "All" -> {
                binding.rbLunchAll.isChecked = true
                lunchType = "All"
            }
            "Some" -> {
                binding.rbLunchSome.isChecked = true
                lunchType = "Some"
            }
            "None" -> {
                binding.rbLunchNone.isChecked = true
                lunchType = "None"
            }
        }

        when (draft?.snackPortion ?: "") {
            "All" -> {
                binding.rbSnackAll.isChecked = true
                snacksType = "All"
            }
            "Some" -> {
                binding.rbSnackSome.isChecked = true
                snacksType = "Some"
            }
            "None" -> {
                binding.rbSnackNone.isChecked = true
                snacksType = "None"
            }
        }

        try {
            binding.spBowel.setSelection((draft?.bowelMovementsCount ?: "0").toInt() - 1)
            selectedBowl = draft?.bowelMovementsCount ?: "0"
            when (draft?.bowelMovementsCondition ?: "") {
                "Normal" -> {
                    binding.rbBowelNormal.isChecked = true
                    bowlMovementType = "Normal"
                }
                "Hard" -> {
                    binding.rbHard.isChecked = true
                    bowlMovementType = "Hard"
                }
                "Loose" -> {
                    binding.rbLoose.isChecked = true
                    bowlMovementType = "Loose"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (draft?.thingsToBringTomorrow != null) {
            if (draft?.thingsToBringTomorrow != "null") {
                var array = JSONArray(draft?.thingsToBringTomorrow ?: "")
                for (i in 0 until array.length()) {
                    when (array[i].toString()) {
                        "Diapers" -> {
                            binding.cbDiaper.isChecked = true
                            babyThings.add(binding.cbDiaper.text.toString())
                        }
                        "Wipes" -> {
                            binding.cbWipes.isChecked = true
                            babyThings.add(binding.cbWipes.text.toString())
                        }
                        "Spare Clothing" -> {
                            binding.cbSpareCloth.isChecked = true
                            babyThings.add(binding.cbSpareCloth.text.toString())
                        }
                        "Ointment" -> {
                            binding.cbOinment.isChecked = true
                            babyThings.add(binding.cbOinment.text.toString())
                        }
                        "Formula" -> {
                            binding.cbFormula.isChecked = true
                            babyThings.add(binding.cbFormula.text.toString())
                        }
                        "Baby Food" -> {
                            binding.cbfood.isChecked = true
                            babyThings.add(binding.cbfood.text.toString())
                        }
                        else -> {
                            binding.cbOther.isChecked = true
                            binding.etType.setText(array[i].toString())
                        }
                    }
                }
            }
        }
        if (draft?.napTimes != null) {
            val array = draft?.napTimes!!.split("[")
            if (!draft?.napTimes!!.contains("[[")) {
                array.forEachIndexed { index, item ->
                    if (index >= 1) {
                        if (index == 1) {
                            val date = item.split("]")[0]
                            val dateStart = date.split(",")[0]
                            val dateEnd = date.split(",")[1]
                            binding.tvNapTime1Start.text = if (dateStart.replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else dateStart.replace("\"", "")
                            binding.tvNapTime1End.text = if (dateEnd.replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else dateEnd.replace("\"", "")
                            napTime1StartString = binding.tvNapTime1Start.text.toString()
                            napTime1EndString = binding.tvNapTime1End.text.toString()
                        } else if (index == 2) {
                            val date = item.split("]")[0]
                            val dateStart = date.split(",")[0]
                            val dateEnd = date.split(",")[1]
                            binding.tvNapTime2Start.text = if (dateStart.replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else dateStart.replace("\"", "")
                            binding.tvNapTime2End.text = if (dateEnd.replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else dateEnd.replace("\"", "")
                            napTime2StartString = binding.tvNapTime2Start.text.toString()
                            napTime2EndString = binding.tvNapTime2End.text.toString()
                        } else if (index == 3) {
                            val date = item.split("]")[0]
                            val dateStart = date.split(",")[0]
                            val dateEnd = date.split(",")[1]
                            binding.tvNapTime3Start.text = if (dateStart.replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else dateStart.replace("\"", "")
                            binding.tvNapTime3End.text = if (dateEnd.replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else dateEnd.replace("\"", "")
                            napTime3StartString = binding.tvNapTime3Start.text.toString()
                            napTime3EndString = binding.tvNapTime3End.text.toString()
                        }
                    }
                }
            }
            else {
                array.forEachIndexed { index, item ->
                    if (index >= 2) {
                        if (index == 2) {
                            val date = item.split("]")[0]
                            val dateStart = date.split(",")[0]
                            val dateEnd = date.split(",")[1]
                            binding.tvNapTime1Start.text = if (dateStart.replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else dateStart.replace("\"", "")
                            binding.tvNapTime1End.text = if (dateEnd.replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else dateEnd.replace("\"", "")
                            napTime1StartString = binding.tvNapTime1Start.text.toString()
                            napTime1EndString = binding.tvNapTime1End.text.toString()
                        } else if (index == 3) {
                            val date = item.split("]")[0]
                            val dateStart = date.split(",")[0]
                            val dateEnd = date.split(",")[1]
                            binding.tvNapTime2Start.text = if (dateStart.replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else dateStart.replace("\"", "")
                            binding.tvNapTime2End.text = if (dateEnd.replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else dateEnd.replace("\"", "")
                            napTime2StartString = binding.tvNapTime2Start.text.toString()
                            napTime2EndString = binding.tvNapTime2End.text.toString()
                        } else if (index == 4) {
                            val date = item.split("]")[0]
                            val dateStart = date.split(",")[0]
                            val dateEnd = date.split(",")[1]
                            binding.tvNapTime3Start.text = if (dateStart.replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else dateStart.replace("\"", "")
                            binding.tvNapTime3End.text = if (dateEnd.replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else dateEnd.replace("\"", "")
                            napTime3StartString = binding.tvNapTime3Start.text.toString()
                            napTime3EndString = binding.tvNapTime3End.text.toString()
                        }
                    }
                }
            }

        }
        if (draft?.bottlesDrankToday != null) {
            val array = draft?.bottlesDrankToday!!.split("[")

            if (!draft?.bottlesDrankToday!!.contains("[[")) {
                array.forEachIndexed { index, item ->
                    if (index >= 1) {
                        if (index == 1) {
                            val date = item.split("]")[0]
                            binding.tvBottleTime1.text = if (date.split(",")[0].replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else date.split(",")[0].replace("\"", "")
                            if (date.contains(","))
                                binding.etQty1.setText(
                                    if (date.split(",")[1].replace(
                                            "\"",
                                            ""
                                        ) == "null"
                                    ) "" else date.split(",")[1].replace("\"", "")
                                )
                            oz1 = binding.etQty1.text.toString()
                            tb1String = binding.tvBottleTime1.text.toString()
                        } else if (index == 2) {
                            val date = item.split("]")[0]
                            binding.tvBottleTime2.text = if (date.split(",")[0].replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else date.split(",")[0].replace("\"", "")
                            if (date.contains(","))
                                binding.etQty2.setText(
                                    if (date.split(",")[1].replace(
                                            "\"",
                                            ""
                                        ) == "null"
                                    ) "" else date.split(",")[1].replace("\"", "")
                                )
                            oz2 = binding.etQty2.text.toString()
                            tb2String = binding.tvBottleTime2.text.toString()
                        } else if (index == 3) {
                            val date = item.split("]")[0]
                            binding.tvBottleTime3.text = if (date.split(",")[0].replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else date.split(",")[0].replace("\"", "")
                            if (date.contains(","))
                                binding.etQty3.setText(
                                    if (date.split(",")[1].replace(
                                            "\"",
                                            ""
                                        ) == "null"
                                    ) "" else date.split(",")[1].replace("\"", "")
                                )
                            oz3 = binding.etQty3.text.toString()
                            tb3String = binding.tvBottleTime3.text.toString()
                        } else if (index == 4) {
                            val date = item.split("]")[0]
                            binding.tvBottleTime4.text = if (date.split(",")[0].replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else date.split(",")[0].replace("\"", "")
                            if (date.contains(","))
                                binding.etQty4.setText(
                                    if (date.split(",")[1].replace(
                                            "\"",
                                            ""
                                        ) == "null"
                                    ) "" else date.split(",")[1].replace("\"", "")
                                )
                            oz4 = binding.etQty4.text.toString()
                            tb4String = binding.tvBottleTime4.text.toString()
                        } else if (index == 5) {
                            val date = item.split("]")[0]
                            binding.tvBottleTime5.text = if (date.split(",")[0].replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else date.split(",")[0].replace("\"", "")
                            if (date.contains(","))
                                binding.etQty5.setText(
                                    if (date.split(",")[1].replace(
                                            "\"",
                                            ""
                                        ) == "null"
                                    ) "" else date.split(",")[1].replace("\"", "")
                                )
                            oz5 = binding.etQty5.text.toString()
                            tb5String = binding.tvBottleTime5.text.toString()
                        } else if (index == 6) {
                            val date = item.split("]")[0]
                            binding.tvBottleTime6.text = if (date.split(",")[0].replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else date.split(",")[0].replace("\"", "")
                            if (date.contains(","))
                                binding.etQty6.setText(
                                    if (date.split(",")[1].replace(
                                            "\"",
                                            ""
                                        ) == "null"
                                    ) "" else date.split(",")[1].replace("\"", "")
                                )
                            oz6 = binding.etQty6.text.toString()
                            tb6String = binding.tvBottleTime6.text.toString()
                        }


                    }
                }
            }
            else {
                array.forEachIndexed { index, item ->
                    if (index >= 2) {
                        if (index == 2) {
                            val date = item.split("]")[0]
                            binding.tvBottleTime1.text = if (date.split(",")[0].replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else date.split(",")[0].replace("\"", "")
                            if (date.contains(","))
                                binding.etQty1.setText(
                                    if (date.split(",")[1].replace(
                                            "\"",
                                            ""
                                        ) == "null"
                                    ) "" else date.split(",")[1].replace("\"", "")
                                )
                            oz1 = binding.etQty1.text.toString()
                            tb1String = binding.tvBottleTime1.text.toString()
                        } else if (index == 3) {
                            val date = item.split("]")[0]
                            binding.tvBottleTime2.text = if (date.split(",")[0].replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else date.split(",")[0].replace("\"", "")
                            if (date.contains(","))
                                binding.etQty2.setText(
                                    if (date.split(",")[1].replace(
                                            "\"",
                                            ""
                                        ) == "null"
                                    ) "" else date.split(",")[1].replace("\"", "")
                                )
                            oz2 = binding.etQty2.text.toString()
                            tb2String = binding.tvBottleTime2.text.toString()
                        } else if (index == 4) {
                            val date = item.split("]")[0]
                            binding.tvBottleTime3.text = if (date.split(",")[0].replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else date.split(",")[0].replace("\"", "")
                            if (date.contains(","))
                                binding.etQty3.setText(
                                    if (date.split(",")[1].replace(
                                            "\"",
                                            ""
                                        ) == "null"
                                    ) "" else date.split(",")[1].replace("\"", "")
                                )
                            oz3 = binding.etQty3.text.toString()
                            tb3String = binding.tvBottleTime3.text.toString()
                        } else if (index == 5) {
                            val date = item.split("]")[0]
                            binding.tvBottleTime4.text = if (date.split(",")[0].replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else date.split(",")[0].replace("\"", "")
                            if (date.contains(","))
                                binding.etQty4.setText(
                                    if (date.split(",")[1].replace(
                                            "\"",
                                            ""
                                        ) == "null"
                                    ) "" else date.split(",")[1].replace("\"", "")
                                )
                            oz4 = binding.etQty4.text.toString()
                            tb4String = binding.tvBottleTime4.text.toString()
                        } else if (index == 6) {
                            val date = item.split("]")[0]
                            binding.tvBottleTime5.text = if (date.split(",")[0].replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else date.split(",")[0].replace("\"", "")
                            if (date.contains(","))
                                binding.etQty5.setText(
                                    if (date.split(",")[1].replace(
                                            "\"",
                                            ""
                                        ) == "null"
                                    ) "" else date.split(",")[1].replace("\"", "")
                                )
                            oz5 = binding.etQty5.text.toString()
                            tb5String = binding.tvBottleTime5.text.toString()
                        } else if (index == 7) {
                            val date = item.split("]")[0]
                            binding.tvBottleTime6.text = if (date.split(",")[0].replace(
                                    "\"",
                                    ""
                                ) == "null"
                            ) "" else date.split(",")[0].replace("\"", "")
                            if (date.contains(","))
                                binding.etQty6.setText(
                                    if (date.split(",")[1].replace(
                                            "\"",
                                            ""
                                        ) == "null"
                                    ) "" else date.split(",")[1].replace("\"", "")
                                )
                            oz6 = binding.etQty6.text.toString()
                            tb6String = binding.tvBottleTime6.text.toString()
                        }


                    }
                }
            }
        }
        //binding.tvBottleTime1.text = draft?.bottlesDrankToday


        binding.etDesc.setText(draft?.comment ?: "")
    }

    private fun listener() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.rgBreakfast.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            breakFastType = radioButton.text.toString()
        }

        binding.rgLunch.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            lunchType = radioButton.text.toString()
        }

        binding.rgSnack.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            snacksType = radioButton.text.toString()
        }


        binding.rgBowl.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            bowlMovementType = radioButton.text.toString()
        }


        binding.cbDiaper.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                babyThings.add(binding.cbDiaper.text.toString())
            } else {
                babyThings.remove(binding.cbDiaper.text.toString())
            }
        }


        binding.cbWipes.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                babyThings.add(binding.cbWipes.text.toString())
            } else {
                babyThings.remove(binding.cbWipes.text.toString())
            }
        }


        binding.cbSpareCloth.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                babyThings.add(binding.cbSpareCloth.text.toString())
            } else {
                babyThings.remove(binding.cbSpareCloth.text.toString())
            }
        }


        binding.cbOinment.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                babyThings.add(binding.cbOinment.text.toString())
            } else {
                babyThings.remove(binding.cbOinment.text.toString())
            }
        }


        binding.cbFormula.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                babyThings.add(binding.cbFormula.text.toString())
            } else {
                babyThings.remove(binding.cbFormula.text.toString())
            }
        }


        binding.cbfood.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                babyThings.add(binding.cbfood.text.toString())
            } else {
                babyThings.remove(binding.cbfood.text.toString())
            }
        }

//        binding.cbOther.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                babyThings.add(binding.etType.text.toString().trim())
//            } else {
//                babyThings.remove(binding.etType.text.toString().trim())
//            }
//        }

        binding.tvBottleTime1.setOnClickListener {
            timePickerDialog("", binding.tvBottleTime1.text.toString()) {
                binding.tvBottleTime1.text = it
                tb1String = it
                binding.etQty1.isEnabled = true
            }
        }

        binding.tvBottleTime2.setOnClickListener {
            timePickerDialog("", binding.tvBottleTime2.text.toString()) {
                binding.tvBottleTime2.text = it
                tb2String = it
                binding.etQty2.isEnabled = true
            }
        }

        binding.tvBottleTime3.setOnClickListener {
            timePickerDialog("", binding.tvBottleTime3.text.toString()) {
                binding.tvBottleTime3.text = it
                tb3String = it
                binding.etQty3.isEnabled = true
            }
        }


        binding.tvBottleTime4.setOnClickListener {
            timePickerDialog("", binding.tvBottleTime4.text.toString()) {
                binding.tvBottleTime4.text = it
                tb4String = it
                binding.etQty4.isEnabled = true
            }
        }



        binding.tvBottleTime5.setOnClickListener {
            timePickerDialog("", binding.tvBottleTime5.text.toString()) {
                binding.tvBottleTime5.text = it
                tb5String = it
                binding.etQty5.isEnabled = true
            }
        }


        binding.tvBottleTime6.setOnClickListener {
            timePickerDialog("", binding.tvBottleTime6.text.toString()) {
                binding.tvBottleTime6.text = it
                tb6String = it
                binding.etQty6.isEnabled = true
            }
        }


        binding.tvNapTime1Start.setOnClickListener {
            timePickerDialog("", binding.tvNapTime1Start.text.toString()) {
                binding.tvNapTime1Start.text = it
                napTime1StartString = it

                binding.tvNapTime1End.text = ""
                napTime1EndString = ""
            }
        }
        binding.tvNapTime1End.setOnClickListener {
            if (validateTimeSelection(tvNapTime1Start))
                timePickerDialog(
                    setMinTime = binding.tvNapTime1Start.text.toString(),
                    selectedTime = binding.tvNapTime1End.text.trim().toString()
                ) {
                    if (validNapTime(binding.tvNapTime1Start.text.toString(), it)) {
                        binding.tvNapTime1End.text = it
                        napTime1EndString = it
                    } else {
                        binding.tvNapTime1End.text = ""
                        napTime1EndString = ""
                        showErrorToast(this, "Nap To time should be greater than From time.")
                    }

                }
        }


        binding.tvNapTime2Start.setOnClickListener {
            timePickerDialog("", binding.tvNapTime2Start.text.toString()) {
                binding.tvNapTime2Start.text = it
                napTime2StartString = it

                binding.tvNapTime2End.text = ""
                napTime2EndString = ""
            }
        }
        binding.tvNapTime2End.setOnClickListener {
            if (validateTimeSelection(tvNapTime2Start))
                timePickerDialog(
                    setMinTime = binding.tvNapTime2Start.text.toString(),
                    selectedTime = binding.tvNapTime2End.text.trim().toString()
                ) {
                    if (validNapTime(binding.tvNapTime2Start.text.toString(), it)) {
                        binding.tvNapTime2End.text = it
                        napTime2EndString = it
                    } else {
                        binding.tvNapTime2End.text = ""
                        napTime2EndString = ""
                        showErrorToast(this, "Nap To time should be greater than From time.")
                    }
                }
        }

        binding.tvNapTime3Start.setOnClickListener {
            timePickerDialog("", binding.tvNapTime3Start.text.toString()) {
                binding.tvNapTime3Start.text = it
                napTime3StartString = it

                binding.tvNapTime3End.text = ""
                napTime3EndString = ""
            }
        }

        binding.tvNapTime3End.setOnClickListener {
            if (validateTimeSelection(tvNapTime3Start))
                timePickerDialog(
                    setMinTime = binding.tvNapTime3Start.text.toString(),
                    selectedTime = binding.tvNapTime3End.text.trim().toString()
                ) {
                    if (validNapTime(binding.tvNapTime3Start.text.toString(), it)) {
                        binding.tvNapTime3End.text = it
                        napTime3EndString = it
                    } else {
                        binding.tvNapTime3End.text = ""
                        napTime3EndString = ""
                        showErrorToast(this, "Nap To time should be greater than From time.")
                    }
                }
        }

        binding.tvSubmit.setOnClickListener {
            if (validation()) {
                callAPItoSubmitReport("0")
            }
        }

        binding.tvDraft.setOnClickListener {

            val breakFast = binding.etBreakfast.text.toString()
            val lunch = binding.etLunch.text.toString()
            val snack = binding.etSnack.text.toString()

            if (breakFast.isEmpty() && breakFastType.isEmpty()
                && lunch.isEmpty() && lunchType.isEmpty()
                && snack.isEmpty() && snacksType.isEmpty()
                && !binding.cbOther.isChecked
            ) {
                showErrorToast(this, "Fill any field and would be able to save as draft.")
                return@setOnClickListener
            }
            oz1 = binding.etQty1.text.toString()
            oz2 = binding.etQty2.text.toString()
            oz3 = binding.etQty3.text.toString()
            oz4 = binding.etQty4.text.toString()
            oz5 = binding.etQty5.text.toString()
            oz6 = binding.etQty6.text.toString()
            callAPItoSubmitReport("1")
        }
    }

    private fun init() {
        selectedChild = intent.getSerializableExtra("item") as ChildInfo?
        dateToSave = intent.getStringExtra("dateToSave")!!
        draft = intent.getSerializableExtra("draft") as DailyReport?

        binding.ivChild.let {
            Glide.with(this@AddReportActivity)
                .load(Constants.IMG_BASE_PATH + selectedChild?.profilePicture)
                .placeholder(R.drawable.ic_user_placeholder_new).into(it)
        }
        binding.tvName.text = selectedChild?.firstName + " " + selectedChild?.lastName
        binding.tvCustody.text = DateConverter.DateFormatFive(selectedChild?.dob ?: "")

        mCalendar.set(Calendar.DAY_OF_YEAR, 1)
        mCalendar.set(Calendar.YEAR, 2020)

        bowlList.add("1")
        bowlList.add("2")
        bowlList.add("3")
        bowlList.add("4")
        bowlList.add("5")
        bowlList.add("6")
        bowlList.add("7")
        bowlList.add("8")
        bowlList.add("9")
        bowlList.add("10")
        setClassSpinner()
        if (draft != null) {
            populateDraft()
        }
    }

    var timesCounts1 = ArrayList<String>()
    var timesCounts2 = ArrayList<String>()
    var timesCounts3 = ArrayList<String>()
    var timesCounts4 = ArrayList<String>()
    var timesCounts5 = ArrayList<String>()
    var timesCounts6 = ArrayList<String>()

    lateinit var bottleTimeCounts: Array<ArrayList<String>>

    var reportDraft = "0"

    private fun callAPItoSubmitReport(
        draft: String
    ) {

        showLoading()

        reportDraft = draft

        if (binding.cbOther.isChecked) {
            babyThings.add(binding.etType.text.toString().trim())
        } else {
            babyThings.remove(binding.etType.text.toString().trim())
        }

        var w1 = ArrayList<String>()
        var w2 = ArrayList<String>()
        var w3 = ArrayList<String>()
        var w4 = ArrayList<String>()
        var w5 = ArrayList<String>()
        var w6 = ArrayList<String>()

        var time = ArrayList<String>()
        var quantity = ArrayList<String>()


        if (tb1String != "") {
            timesCounts1.add(tb1String ?: "")
            time.add(tb1String ?: "")
            w1.add(tb1String ?: "")
        }
        if (oz1 != "") {
            timesCounts1.add(oz1)
            quantity.add(oz1)
            w1.add(oz1)
        }
        if (tb2String != "") {
            timesCounts2.add(tb2String ?: "")

            time.add(tb2String ?: "")
            w2.add(tb2String ?: "")
        }
        if (oz2 != "") {
            timesCounts2.add(oz2)
            quantity.add(oz2)
            w2.add(oz2)
        }
        if (tb3String != "") {
            timesCounts3.add(tb3String ?: "")
            time.add(tb1String ?: "")
            w3.add(tb1String ?: "")
        }
        if (oz3 != "") {
            timesCounts3.add(oz3)
            quantity.add(oz3)
            w3.add(oz3)
        }

        if (tb4String != "") {
            timesCounts4.add(tb4String ?: "")
            time.add(tb4String ?: "")
            w4.add(tb4String ?: "")
        }
        if (oz4 != "") {
            timesCounts4.add(oz4)
            quantity.add(oz4)
            w4.add(oz4)
        }
        if (tb5String != "") {
            timesCounts5.add(tb5String ?: "")
            time.add(tb5String ?: "")
            w5.add(tb5String ?: "")
        }
        if (oz5 != "") {
            timesCounts5.add(oz5)
            quantity.add(oz5)
            w5.add(oz5)
        }

        if (tb6String != "") {
            timesCounts6.add(tb6String ?: "")
            time.add(tb6String ?: "")
            w6.add(tb6String ?: "")
        }
        if (oz6 != "") {
            timesCounts6.add(oz6)
            quantity.add(oz6)
            w6.add(oz6)
        }

        var nap1 = ArrayList<String>()
        var nap2 = ArrayList<String>()
        var nap3 = ArrayList<String>()


        if (napTime1StartString != "") {
            nap1.add(napTime1StartString ?: "")
        }

        if (napTime1EndString != "") {
            nap1.add(napTime1EndString ?: "")
        }



        if (napTime2StartString != "") {
            nap2.add(napTime2StartString ?: "")
        }
        if (napTime2EndString != "") {
            nap2.add(napTime2EndString ?: "")
        }

        if (napTime3StartString != "") {
            nap3.add(napTime3StartString ?: "")
        }

        if (napTime3EndString != "") {
            nap3.add(napTime3EndString ?: "")
        }

        var napTimes = ArrayList<ArrayList<String>>()
        var waterTimes = ArrayList<ArrayList<String>>()

        napTimes.add(nap1)
        napTimes.add(nap2)
        napTimes.add(nap3)

        waterTimes.add(timesCounts1)
        waterTimes.add(timesCounts2)
        waterTimes.add(timesCounts3)
        waterTimes.add(timesCounts4)
        waterTimes.add(timesCounts5)
        waterTimes.add(timesCounts6)
//        waterTimes.add(w1)
//        waterTimes.add(w2)
//        waterTimes.add(w3)
//        waterTimes.add(w4)
//        waterTimes.add(w5)
//        waterTimes.add(w6)

//        val builder = MultipartBody.Builder()
//        builder.setType(MultipartBody.FORM)
//        builder.addFormDataPart("search_type", "product")
//        if(user_id!=null){builder.addFormDataPart("user_id", user_id.toString())}
//        if(location!=null){builder.addFormDataPart("location",location!!)}
//        builder.addFormDataPart("page_no",page_no.toString())
//        if(gender!=null){builder.addFormDataPart("gender",gender.toString())}
//        if(category_id!=null){builder.addFormDataPart("category_id",category_id.toString())}
//        if(product_id!=null){builder.addFormDataPart("product_id",product_id.toString())}
//        if(amount!=null || amount!! > 0){builder.addFormDataPart("price",amount.toString())}
//
//        val body = builder.build()


        Log.i("Jchild_id", selectedChild?.id.toString())

        val builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)
        builder.addFormDataPart("child_id", "${selectedChild?.id ?: 0}")
        builder.addFormDataPart("breakfast_portion", breakFastType)
        builder.addFormDataPart("breakfast_description", binding.etBreakfast.text.toString().trim())
        builder.addFormDataPart("lunch_portion", lunchType)
        builder.addFormDataPart("lunch_description", binding.etLunch.text.toString().trim())
        builder.addFormDataPart("snack_portion", snacksType)
        builder.addFormDataPart("snack_description", binding.etSnack.text.toString().trim())
        builder.addFormDataPart("bowel_movements_count", selectedBowl)
        builder.addFormDataPart("bowel_movements_condition", bowlMovementType)
        builder.addFormDataPart("comment", binding.etDesc.text.toString().trim())
        builder.addFormDataPart("draft", draft)
        builder.addFormDataPart("created_at", dateToSave)



        napTimes.forEachIndexed { index, arrayList ->

            var cIndex = index

            arrayList.forEachIndexed { index, s ->

                Log.i("jNapTime[${cIndex}][${index}]", s)
                builder.addFormDataPart("nap_times[${cIndex}][${index}]", s)

            }
        }

        waterTimes.forEachIndexed { index, arrayList ->

            var cIndex = index

            arrayList.forEachIndexed { index, s ->

                Log.i("jWaterTimes[${cIndex}][${index}]", s)

                builder.addFormDataPart("bottles_drank_today[${cIndex}][${index}]", s)

            }

        }


        babyThings.forEachIndexed { index, s ->
            builder.addFormDataPart("things_to_bring_tomorrow[${index}]", s)

        }


        GlobalScope.launch {
            ApiController2.getInstance(this@AddReportActivity).subDailyReportNew(
                userManager.accessToken ?: "", this@AddReportActivity,
                ResponseTypes.ADD_UPDATE_CHILD_DAILY_REPORT, builder.build()
            )
        }


//        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
//            .subDailyReport(
//                "Bearer " + userManager.accessToken ?: "",
//                "${selectedChild?.id ?: 0}",
//                breakFastType,
//                binding.etBreakfast.text.toString().trim(),
//                lunchType,
//                binding.etLunch.text.toString().trim(),
//                snacksType,
//                binding.etSnack.text.toString().trim(),
//                waterTimes,
//                selectedBowl,
//                bowlMovementType,
//                napTimes,
//                babyThings,
//                binding.etDesc.text.toString().trim(),
//                draft
//            )
//        call.enqueue(object : Callback<LoginResponse> {
//            override fun onResponse(
//                call: Call<LoginResponse>,
//                response: Response<LoginResponse>
//            ) {
//                if (response.body()?.status == true) {
//                    showSuccessToast(this@AddReportActivity, response.body()?.message ?: "")
//                    if (draft == "0") {
//                        finish()
//                    }
//                } else {
//                    showErrorToast(this@AddReportActivity, response.body()?.message ?: "")
//                }
//                hideLoading()
//            }
//
//            override fun onFailure(
//                call: Call<LoginResponse>,
//                t: Throwable
//            ) {
//                showErrorToast(this@AddReportActivity, "Can't Connect to Server!")
//                hideLoading()
//                t.printStackTrace()
//            }
//        })
    }

    private fun validation(): Boolean {
        val breakFast = binding.etBreakfast.text.toString()
        val lunch = binding.etLunch.text.toString()
        val snack = binding.etSnack.text.toString()

        oz1 = binding.etQty1.text.toString()
        oz2 = binding.etQty2.text.toString()
        oz3 = binding.etQty3.text.toString()
        oz4 = binding.etQty4.text.toString()
        oz5 = binding.etQty5.text.toString()
        oz6 = binding.etQty6.text.toString()

        if (breakFast.isEmpty()) {
            showErrorToast(this, "Breakfast field cannot be Empty!")
            return false
        }
        if (breakFastType.isEmpty()) {
            showErrorToast(this, "Please select Breakfast Portion!")
            return false
        }

        if (lunch.isEmpty()) {
            showErrorToast(this, "lunch field cannot be empty")
            return false
        }
        if (lunchType.isEmpty()) {
            showErrorToast(this, "Please select Lunch Portion!")
            return false
        }

        if (snack.isEmpty()) {
            showErrorToast(this, "Snack field cannot be empty")
            return false
        }

        if (snacksType.isEmpty()) {
            showErrorToast(this, "Please select Snacks Portion!")
            return false
        }


        if (binding.cbOther.isChecked && binding.etType.text.toString().isEmpty()) {
            showErrorToast(this, "Please mention other item to bring tomorrow!")
            return false
        }

        if (oz1.isNotEmpty())
            if ((binding.tvBottleTime1.text.toString().isEmpty())) {
                showErrorToast(this, "Please enter time for drinking water")
                return false
            }

        if (oz2.isNotEmpty())
            if ((binding.tvBottleTime2.text.toString().isEmpty())) {
                showErrorToast(this, "Please enter time for drinking water")
                return false
            }

        if (oz3.isNotEmpty())
            if ((binding.tvBottleTime3.text.toString().isEmpty())) {
                showErrorToast(this, "Please enter time for drinking water")
                return false
            }

        if (oz4.isNotEmpty())
            if ((binding.tvBottleTime4.text.toString().isEmpty())) {
                showErrorToast(this, "Please enter time for drinking water")
                return false
            }

        if (oz5.isNotEmpty())
            if ((binding.tvBottleTime5.text.toString().isEmpty())) {
                showErrorToast(this, "Please enter time for drinking water")
                return false
            }

        if (oz6.isNotEmpty())
            if ((binding.tvBottleTime6.text.toString().isEmpty())) {
                showErrorToast(this, "Please enter time for drinking water")
                return false
            }

        //
        if (binding.tvBottleTime1.text.toString().isNotEmpty())
            if (oz1.isEmpty()) {
                showErrorToast(this, "Please fill water amount")
                return false
            }

        if (binding.tvBottleTime2.text.toString().isNotEmpty())
            if (oz2.isEmpty()) {
                showErrorToast(this, "Please fill water amount")
                return false
            }

        if (binding.tvBottleTime3.text.toString().isNotEmpty())
            if (oz3.isEmpty()) {
                showErrorToast(this, "Please fill water amountr")
                return false
            }
        if (binding.tvBottleTime4.text.toString().isNotEmpty())
            if (oz4.isEmpty()) {
                showErrorToast(this, "Please fill water amount")
                return false
            }
        if (binding.tvBottleTime5.text.toString().isNotEmpty())
            if (oz5.isEmpty()) {
                showErrorToast(this, "Please fill water amount")
                return false
            }
        if (binding.tvBottleTime6.text.toString().isNotEmpty())
            if (oz6.isEmpty()) {
                showErrorToast(this, "Please fill water amount")
                return false
            }
//        if (oz1.isEmpty() || oz2.isEmpty() || oz3.isEmpty() || oz4.isEmpty() || oz5.isEmpty() || oz6.isEmpty()) {
//            showErrorToast(this, "Please fill all water amount")
//            return false
//        }
//
//
//        if (napTime1EndString!!.isEmpty() || napTime1EndString!!.isEmpty() || napTime2StartString!!.isEmpty() || napTime2EndString!!.isEmpty()
//            || napTime3StartString!!.isEmpty() || napTime3EndString!!.isEmpty()
//        ) {
//            showErrorToast(this, "Please provide all nap times")
//            return false
//        }


        return true

    }

    private fun showClock(
        setMinTime: String = "",
        selectedTime: String,
        callback: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        var miniTimeCalendar = Calendar.getInstance()

        if (setMinTime != "") {
            miniTimeCalendar.time = DateConverter.convertTimeForTimePicker(setMinTime)
        }
        if (selectedTime != "") {
            calendar.time = DateConverter.convertTimeForTimePicker(selectedTime)
        }
        val dpd = TimePickerDialog.newInstance(
            { _, hourOfDay, minute, second ->
                val time = "$hourOfDay:$minute:$second"
                callback(DateConverter.convertToFromTimePicker(time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            calendar.get(Calendar.SECOND),
            false
        )
        if (setMinTime != "") {
            dpd.setMinTime(
                miniTimeCalendar.get(Calendar.HOUR_OF_DAY),
                miniTimeCalendar.get(Calendar.MINUTE),
                miniTimeCalendar.get(Calendar.SECOND)
            )
        }
        dpd.version = TimePickerDialog.Version.VERSION_1
//        dpd.setTimeInterval(1)
        dpd.show(supportFragmentManager, "Datepickerdialog")
    }

    private fun validateTimeSelection(tvOpeningTime: TextView): Boolean {
        if (tvOpeningTime.text.toString().trim().isEmpty()) {
            CustomToasts.failureToast(
                this@AddReportActivity,
                "Please select opening time first!"
            )
            return false
        }
        return true
    }

    private fun timePickerDialog(
        setMinTime: String = "",
        selectedTime: String, callback: (String) -> Unit
    ) {
        mCalendar = Calendar.getInstance()
        var miniTimeCalendar = Calendar.getInstance()
        if (setMinTime != "") {
            miniTimeCalendar.time = DateConverter.convertTimeForTimePicker(setMinTime)
        }
        if (selectedTime != "") {
            mCalendar.time = DateConverter.convertTimeForTimePicker(selectedTime)
        }
        val picker = android.app.TimePickerDialog(
            this, R.style.MySpinnerTimePickerStyle,
            { _, hourOfDay, minute ->
                var AM_PM = " AM"
                var mm_precede = ""
                var hourr = hourOfDay
                if (hourOfDay >= 12) {
                    AM_PM = " PM"
                    if (hourOfDay >= 13 && hourOfDay < 24) {
                        hourr -= 12
                    } else {
                        hourr = 12
                    }
                } else if (hourOfDay === 0) {
                    hourr = 12
                }
                if (minute < 10) {
                    mm_precede = "0"
                }
                val time: String = "" + hourr + ":" + mm_precede + minute + AM_PM
                callback(time)

            }, mCalendar.get(Calendar.HOUR_OF_DAY),
            mCalendar.get(Calendar.MINUTE),
            false
        )

        picker.show()
        /*val dpd = TimePickerDialog.newInstance(
            { _, hourOfDay, minute, second ->
                var AM_PM = " AM"
                var mm_precede = ""
                var hourr = hourOfDay
                if (hourOfDay >= 12) {
                    AM_PM = " PM"
                    if (hourOfDay >= 13 && hourOfDay < 24) {
                        hourr -= 12
                    } else {
                        hourr = 12
                    }
                } else if (hourOfDay === 0) {
                    hourr = 12
                }
                if (minute < 10) {
                    mm_precede = "0"
                }
                val time: String = "" + hourr + ":" + mm_precede + minute + AM_PM
                callback(time)

            }, mCalendar.get(Calendar.HOUR_OF_DAY),
            mCalendar.get(Calendar.MINUTE),
            mCalendar.get(Calendar.SECOND),
            false
        )
        dpd.setStyle(DialogFragment.STYLE_NORMAL,R.style.MySpinnerTimePickerStyle)
        if (setMinTime != "") {
            dpd.setMinTime(
                miniTimeCalendar.get(Calendar.HOUR_OF_DAY),
                miniTimeCalendar.get(Calendar.MINUTE),
                miniTimeCalendar.get(Calendar.SECOND)
            )
        }

        dpd.version = TimePickerDialog.Version.VERSION_1
        //dpd.setTimeInterval(1)
        dpd.show(supportFragmentManager, "Datepickerdialog")*/
    }

    override fun onStatusTrue(call: Any?, response: Any, responseType: String) {

        var body = (response as Response<LoginResponse>).body()!!


        if (body?.status == true) {
            showSuccessToast(this@AddReportActivity, body.message ?: "")
            //if (reportDraft == "1") {
            finish()
            //}
            hideLoading()
        } else {
            showErrorToast(this@AddReportActivity, body.message ?: "")
            hideLoading()
        }

    }

    override fun onStatusFalse(call: Any?, response: Any, responseType: String) {
        showErrorToast(
            this@AddReportActivity,
            (response as Response<LoginResponse>).body()?.message ?: ""
        )
        hideLoading()
    }

    override fun onResponseFailed(call: Any?, t: Throwable, responseType: String) {
        showErrorToast(this@AddReportActivity, "Can't Connect to Server!")
        hideLoading()
        t.printStackTrace()
    }

    private fun validNapTime(start: String, end: String): Boolean {
        val startHour = start.split(":")[0]
        val startMinute = start.split(":")[1].split(" ")[0]
        val startAmPM = start.split(" ")[1]

        val endHour = end.split(":")[0]
        val endMinute = end.split(":")[1].split(" ")[0]
        val endAmPM = end.split(" ")[1]
        if (startHour.toInt() == endHour.toInt()) {
            if (startMinute.toInt() <= endMinute.toInt()) {
                if (startAmPM == "AM" && endAmPM == "AM") {
                    return true
                }
                if (startAmPM == "PM" && endAmPM == "PM") {
                    return true
                }
            }
        }
        if (startHour.toInt() > endHour.toInt()) {
            if (startAmPM == "AM" && endAmPM == "PM") {
                return true
            }
            if (startHour.toInt() == 12 && startAmPM == "PM" && endAmPM == "PM") {
                return true
            }
        }
        if (startHour.toInt() < endHour.toInt()) {
            if (startAmPM == "AM" && endAmPM == "AM") {
                return true
            }
            if (startAmPM == "PM" && endAmPM == "PM") {
                return true
            }
            if (startAmPM == "AM" && endAmPM == "PM") {
                return true
            }
        }

        return false
    }
}