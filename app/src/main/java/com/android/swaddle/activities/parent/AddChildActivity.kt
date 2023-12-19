package com.android.swaddle.activities.parent

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.android.swaddle.R
import com.android.swaddle.activities.common.RegistrationSuccessfulActivity
import com.android.swaddle.activities.providers.ProviderMainActivity
import com.android.swaddle.adapters.CustodyDocumentAdapter
import com.android.swaddle.adapters.ethnicity_bottom_sheet_adapter.EthnicityList
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityAddChildBinding
import com.android.swaddle.fragments.bottomSheetFragment.EthnicityBottomSheet
import com.android.swaddle.fragments.bottomSheetFragment.ParentsBottomSheet
import com.android.swaddle.fragments.dialog.EpiPenAcknowledgementPopup
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.*
import com.android.swaddle.networkManager.NetworkURLs
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.networkManager.RetrofitClassNew
import com.android.swaddle.payment_screens.AddPaymentMethodsActivity
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.CustomToasts
import com.android.swaddle.utils.DateConverter
import com.android.swaddle.utils.maskeditor.getIdPlaceData
import com.android.swaddle.utils.maskeditor.getZipCode
import com.bumptech.glide.Glide
import com.codekidlabs.storagechooser.StorageChooser
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.irozon.alertview.AlertActionStyle
import com.irozon.alertview.AlertStyle
import com.irozon.alertview.AlertView
import com.irozon.alertview.objects.AlertAction
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.theartofdev.edmodo.cropper.CropImage
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.startActivity
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

@Suppress("USELESS_ELVIS_RIGHT_IS_NULL")
class AddChildActivity : BaseActivity(), (Place?, Exception?) -> Unit {

    private lateinit var binding: ActivityAddChildBinding
    private var mCalendar: Calendar = Calendar.getInstance()
    var custodyConsideration = "No"
    private var picker: DatePickerDialog? = null
    var startDate = ""
    var startDay: Int? = null
    var startMonth: Int? = null
    var startYear: Int? = null
    var gender = ""
    var dob = ""
    var iep = "No"
    var allergy = "No"
    var epiPen1 = "No"
    var epiPen1Expiration = ""
    var time = "Full Time"
    var fileUri: Uri? = null
    var filePath = ""
    var newFilesSelected = false

    var ethnicityList = ArrayList<EthnicityList>()

    var authorizedAdultsList = ArrayList<User>()
    var selectedParent: User? = null

    var foodAllergies = arrayListOf<String>("")
    var environmentalAllergies = arrayListOf<String>("")
    var fromList = false
    var fromAddAnother = false
    private var filesAdapter: CustodyDocumentAdapter? = null

    //    var user: User? = null
    var childInfo: ChildInfo? = null

    private var builder = StorageChooser.Builder()
    private var chooser: StorageChooser? = null

    private var childType: Int = Constants.update_child
    private var documents: ArrayList<String> = ArrayList()

    var selectedEthnicity: EthnicityList? = null
    var selectedEthnicityOtherText = ""

    var startTimeToSetAllWeek = ""
    var endTimeToSetAllWeek = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChildBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fromList = intent.getBooleanExtra("fromList", false)
        fromAddAnother = intent.getBooleanExtra("fromAddAnother", false)
//        user = intent.getSerializableExtra("item") as User?
        childInfo = intent.getSerializableExtra("childitem") as ChildInfo?
        childType = intent.getIntExtra(Constants.child_type, Constants.add_child)
        initVar()
        listener()

        setUpCustodyDocuments()
        Log.i("childId", childInfo?.id.toString())

        if (fromList) {
            binding.imgBack.viewVisible()
            binding.tvHeading.text = "Add Child"
        } else {
            binding.tvHeading.text = "Registration"
            binding.imgBack.viewGone()
        }
        if (childInfo != null)
            setupUpdatingData()
        else {
            initCheckBoxes()
        }

        if (userManager.user?.type == "provider" || userManager.user?.type == "staff") {
            binding.titleSelectParent.viewVisible()
            binding.cvSpinnerSelectParent.viewVisible()
        } else {
            binding.titleSelectParent.viewGone()
            binding.cvSpinnerSelectParent.viewGone()
        }
        callAPItoGetAuthorizedAdults()
    }

    private fun callAPItoGetAuthorizedAdults() {
        val call: Call<AuthorizedAdultsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getParentsOfProvider("Bearer " + userManager.accessToken ?: "")
        call.enqueue(object : Callback<AuthorizedAdultsResponse> {
            override fun onResponse(
                call: Call<AuthorizedAdultsResponse>,
                response: Response<AuthorizedAdultsResponse>
            ) {
//                    showSuccessToast(this@ClassListActivity, "Classroom Created Successfully")
                hideLoading()
                authorizedAdultsList = response.body()?.users ?: ArrayList()
                runOnUiThread {
                    authorizedAdultsList.forEach {
                        if (it.relation == "Mother" || it.relation == "Father") {
                            binding.tvParentName.text = "${it.firstName} ${it.lastName}"
                            Glide.with(this@AddChildActivity)
                                .load(Constants.IMG_BASE_PATH + it.profilePicture)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .into(binding.ivChildParent)
                        }
                    }

                }
            }

            override fun onFailure(
                call: Call<AuthorizedAdultsResponse>,
                t: Throwable
            ) {
                showErrorToast(this@AddChildActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun initCheckBoxes() {

        binding.rbEgg.setOnCheckedChangeListener { _, _ ->
            updateFoodAllergiesList()
        }
        binding.rbMilk.setOnCheckedChangeListener { _, _ ->
            updateFoodAllergiesList()
        }

        binding.rbTreeNuts.setOnCheckedChangeListener { _, _ ->
            updateFoodAllergiesList()
        }
        binding.rbSoy.setOnCheckedChangeListener { _, _ ->
            updateFoodAllergiesList()
        }
        binding.rbWheat.setOnCheckedChangeListener { _, _ ->
            updateFoodAllergiesList()
        }

        binding.rbFish.setOnCheckedChangeListener { _, _ ->
            updateFoodAllergiesList()
        }

        binding.rbShellfish.setOnCheckedChangeListener { _, _ ->
            updateFoodAllergiesList()
        }
        binding.rbOther.setOnCheckedChangeListener { _, isChecked ->
            updateFoodAllergiesList()
            if (isChecked) {
                binding.etTypeAnythingForFoodAllergy.viewVisible()
            } else {
                binding.etTypeAnythingForFoodAllergy.viewGone()
                binding.etTypeAnythingForFoodAllergy.setText("")
            }
        }

        binding.rbDog.setOnCheckedChangeListener { _, _ ->
            updateEnvironmentalAllergiesList()
        }

        binding.rbMold.setOnCheckedChangeListener { _, _ ->
            updateEnvironmentalAllergiesList()
        }

        binding.rbCat.setOnCheckedChangeListener { _, _ ->
            updateEnvironmentalAllergiesList()
        }

        binding.rbDustMites.setOnCheckedChangeListener { _, _ ->
            updateEnvironmentalAllergiesList()
        }

        binding.rbOtherEnvironmentAllergy.setOnCheckedChangeListener { _, isChecked ->
            updateEnvironmentalAllergiesList()
            if (isChecked) {
                binding.etTypeAnythingForEnvironmentAllergy.viewVisible()
            } else {
                binding.etTypeAnythingForEnvironmentAllergy.viewGone()
                binding.etTypeAnythingForEnvironmentAllergy.setText("")
            }
        }
    }

    private fun setUpCustodyDocuments() {
        if (filesAdapter == null) {
            filesAdapter = CustodyDocumentAdapter(this, documents, false)
            binding.rvFiles.adapter = filesAdapter

            filesAdapter?.setListener(object : CustodyDocumentAdapter.CLickListener {
                override fun onItemClick(pos: Int) {

                }

                override fun onRemoveClick(pos: Int) {
                    if (documents[pos].contains("/childs/custody_document")) {
                        val alert =
                            AlertView(
                                "Remove?",
                                "Are you sure you want to Remove file?",
                                AlertStyle.DIALOG
                            )
                        alert.addAction(AlertAction("Yes", AlertActionStyle.NEGATIVE) {

                            callAPItoRemoveMedia(documents[pos], "${childInfo?.id}", pos)
                        })
                        alert.addAction(AlertAction("No", AlertActionStyle.DEFAULT) {
                        })
                        alert.show(this@AddChildActivity)

                    } else {
                        documents.removeAt(pos)
                        filesAdapter?.setData(documents)
                    }
                }

                override fun onDownloadClick(position: Int) {
                }
            })
        } else {
            filesAdapter?.concatinateData(documents)
        }
    }

    private fun callAPItoRemoveMedia(media: String, id: String, pos: Int) {
        showLoading()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .deleteCustodyDocument("Bearer " + userManager.accessToken ?: "", id, media)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                hideLoading()
                if (response.body()?.status == true) {

                    showSuccessToast(
                        this@AddChildActivity,
                        response.body()?.message ?: ""
                    )
                    documents.removeAt(pos)
                    filesAdapter?.setData(documents)

                    newFilesSelected = documents.any { !it.contains("/childs/custody_document") }
//                    callAPItoActivities()
                } else {
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(
                            this@AddChildActivity,
                            jObjError.getString("message")
                        )
                    } catch (e: Exception) {
                        showErrorToast(
                            this@AddChildActivity,
                            response.body()?.message ?: ""
                        )
                    }
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showErrorToast(this@AddChildActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun setupUpdatingData() {
        binding.imgBack.viewVisible()
        binding.tvHeading.text = "Edit Child Profile"

        binding.tvEthnicityText.text = childInfo?.ethnicity
        selectedEthnicity = ethnicityList.find { it.text == childInfo?.ethnicity }

        if (childInfo?.ethnicityOthers != null) {
            binding.tvEthnicityText.text = childInfo?.ethnicity
            selectedEthnicity = ethnicityList.find { it.text == "Other" }
            binding.tvEthnicityOther.viewVisible()
            binding.cvEthnicityOther.viewVisible()
            binding.tvEthnicityTextOther.text = childInfo?.ethnicityOthers ?: ""
            selectedEthnicityOtherText = childInfo?.ethnicityOthers ?: ""
        }

        binding.etFirstname.setText(childInfo?.firstName ?: "")
        binding.etLastName.setText(childInfo?.lastName ?: "")
        binding.etMiddleName.setText(childInfo?.middleName ?: "")


        binding.etAddress.setText(childInfo?.primaryAddress ?: "")
        binding.etAprOrSuiteNo.setText(childInfo?.apt ?: "")
        binding.etState.setText(childInfo?.state ?: "")
        binding.etCity.setText(childInfo?.city ?: "")
        binding.etZipCode.setText(childInfo?.zipCode ?: "")
        binding.etAddress2.setText(childInfo?.secondaryAddress ?: "")
        binding.etAprOrSuiteNo2.setText(childInfo?.sApt ?: "")
        binding.etState2.setText(childInfo?.sState ?: "")
        binding.etCity2.setText(childInfo?.sCity ?: "")
        binding.etZipCode2.setText(childInfo?.sZipCode ?: "")

        binding.etNickName.setText(childInfo?.nickName ?: "")
        binding.tvDob.setText(childInfo?.dob ?: "")
        binding.etDesc.setText(childInfo?.custodyDescription)
        binding.etSpecialNeedsConsideration.setText(childInfo?.specialNeedConsiderations)


        try {
            var obj = JSONArray(childInfo?.authorizedAdultChildCall)
            for (i in 0 until obj.length()) {
                if (JSONObject(
                        obj.get(i).toString()
                    ).getInt("id") == userManager.user?.id ?: 0
                ) {
                    binding.etCallYou.setText(
                        JSONObject(
                            obj.get(i).toString()
                        ).getString("calls_you")
                    )
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }


        binding.etCallYou.setText(childInfo?.childCallYou ?: "")
//        binding.etSpecialNeedsConsideration.setText( childInfo?.custodyConsideration?:"")
//        binding.etDesc.text = childInfo?.custodyDescription?:""
//        binding.tvIEPDetail.text = childInfo?.iepDiagnosedBy?:""

//        if(childInfo?.epiPenExpirationDate?.length!!>0){
//
//            var a = childInfo?.epiPenExpirationDate?.split("-")
//
//            myCalendar.set(Calendar.YEAR,a?.get(0)!!.toInt())
//            myCalendar.set(Calendar.MONTH,a?.get(1)!!.toInt())
//            myCalendar.set(Calendar.DAY_OF_MONTH,a?.get(2)!!.toInt())
//
//            epiPen1Expiration = "${a[0]}-${a[1]}-${a[2]}"
//
//            binding.tvEPIPenExpiration.text = "${a[2]} / ${a[1]} / ${a[0]}"
//
//        }

        if (childInfo?.epiPenExpirationDate != null) {
            var a = childInfo?.epiPenExpirationDate?.split("-")

            myExpireCalendar.set(Calendar.YEAR, a?.get(0)!!.toInt())
            myExpireCalendar.set(Calendar.MONTH, a?.get(1)!!.toInt() - 1)
            myExpireCalendar.set(Calendar.DAY_OF_MONTH, a?.get(2).split(" ").first()!!.toInt())

            epiPen1Expiration = "${a[0]}-${a[1]}-${a?.get(2).split(" ").first()}"

            binding.tvEPIPenExpiration.text = "${a[1]} / ${a?.get(2).split(" ").first()} / ${a[0]}"
        }

        if (childInfo?.dob?.length != null) {

            var a = childInfo?.dob?.split("-")

            myCalendar.set(Calendar.YEAR, a?.get(0)!!.toInt())
            myCalendar.set(Calendar.MONTH, a?.get(1)!!.toInt() - 1)
            myCalendar.set(Calendar.DAY_OF_MONTH, a?.get(2)!!.toInt())

            dob = "${a[0]}-${a[1]}-${a[2]}"

            binding.tvDob.text = "${a[1]} / ${a[2]} / ${a[0]}"

        }

        Glide.with(this@AddChildActivity)
            .load(Constants.IMG_BASE_PATH + childInfo?.profilePicture)
            .placeholder(R.drawable.ic_user_placeholder).into(binding.ivProfile)

        Log.i("profilePath", "J:${Constants.IMG_BASE_PATH} " + childInfo?.profilePicture.toString())


        when (childInfo?.doesChildRequireEpiPin ?: "") {

            "Yes" -> {
                binding.rbEpiPen1Yes.isChecked = true
                epiPen1 = "Yes"
            }

            "No" -> {
                binding.rbEpiPen1NO.isChecked = true
                epiPen1 = "No"
            }
        }

        when (childInfo?.custodyConsideration ?: "") {

            "Yes" -> {
                binding.rbCustodyYes.isChecked = true
                custodyConsideration = "Yes"
                binding.tvDesc.visibility = View.VISIBLE
                binding.etDesc.visibility = View.VISIBLE
            }

            "No" -> {
                binding.rbCustodyNo.isChecked = true
                custodyConsideration = "No"
                binding.tvDesc.visibility = View.GONE
                binding.etDesc.visibility = View.GONE
            }
        }

        when (childInfo?.custody ?: "") {
            "Full Time" -> {
                binding.rbFullTime.isChecked = true
                time = "Full Time"
                binding.tvAddress2.visibility = View.GONE
                binding.etAddress2.visibility = View.GONE
                binding.tvAprOrSuiteNo2.visibility = View.GONE
                binding.etAprOrSuiteNo2.visibility = View.GONE
                binding.tvCity2.visibility = View.GONE
                binding.etCity2.visibility = View.GONE
                binding.tvZipCode2.visibility = View.GONE
                binding.etZipCode2.visibility = View.GONE
                binding.tvState2.visibility = View.GONE
                binding.etState2.visibility = View.GONE
            }
            "Part Time" -> {
                binding.rbPartTime.isChecked = true
                time = "Part Time"
                binding.tvAddress2.visibility = View.VISIBLE
                binding.etAddress2.visibility = View.VISIBLE
                binding.tvAprOrSuiteNo2.visibility = View.VISIBLE
                binding.etAprOrSuiteNo2.visibility = View.VISIBLE
                binding.tvCity2.visibility = View.VISIBLE
                binding.etCity2.visibility = View.VISIBLE
                binding.tvZipCode2.visibility = View.VISIBLE
                binding.etZipCode2.visibility = View.VISIBLE
                binding.tvState2.visibility = View.VISIBLE
                binding.etState2.visibility = View.VISIBLE
            }
        }

        when (childInfo?.gender ?: "") {
            "Female" -> {
                binding.rbFemal.isChecked = true
                gender = "Female"
            }
            "Unspecified" -> {
                binding.rbUnSpecified.isChecked = true
                gender = "Unspecified"
            }
            "Male" -> {
                binding.rbMale.isChecked = true
                gender = "Male"
            }
        }

        when (childInfo?.iep ?: "") {
            "Yes" -> {
                binding.rbYes.isChecked = true
                iep = "Yes"
            }
            "No" -> {
                binding.rbNo.isChecked = true
                iep = "No"
            }
        }
        binding.etAddress2.setText(childInfo?.secondaryAddress ?: "")
        binding.etIepDesc.setText(childInfo?.comment ?: "")
        binding.etIEpNote.setText(childInfo?.iepDiagnosedBy ?: "")
        when (childInfo?.doesHaveAnyAllergies ?: "") {
            "Yes" -> {
                binding.rbAllergyYes.isChecked = true
                allergy = "Yes"
                binding.lnrCheckBox.visibility = View.VISIBLE
                binding.llEnvironmentalAllergies.visibility = View.VISIBLE
            }
            "No" -> {
                binding.rbAllergyNo.isChecked = true
                allergy = "No"
                binding.lnrCheckBox.visibility = View.GONE
                binding.llEnvironmentalAllergies.visibility = View.GONE
            }
        }

        Log.i("childallergy", Gson().toJson(childInfo?.foodAllergies).toString())

        //user
//        binding.etDesc.setText(childInfo?.custodyDescription ?: "")
        var previousFiles: ArrayList<String> = ArrayList()
        if (childInfo?.custodyDocument != null) {
            var array = JSONArray(childInfo?.custodyDocument ?: "")
            for (i in 0 until array.length()) {
                previousFiles.add(array[i].toString())
            }
//            binding.tvSelectedFileName.text = childInfo?.custodyDocument?.split("/")?.last()
            documents = previousFiles
            setUpCustodyDocuments()
        }
        when (childInfo?.custodyConsideration ?: "") {
            "Yes" -> {
                binding.rbCustodyYes.isChecked = true
                custodyConsideration = "Yes"
                binding.tvDesc.viewVisible()
                binding.etDesc.viewVisible()
                binding.lnrUpload.viewVisible()
            }
            "No" -> {
                binding.rbCustodyNo.isChecked = true
                custodyConsideration = "No"
                binding.tvDesc.viewGone()
                binding.etDesc.viewGone()
                binding.lnrUpload.viewGone()
            }
        }

        try {
            var array = JSONArray(childInfo?.custodyDocument ?: "")
            documents = ArrayList()
            for (i in 0 until array.length()) {
                documents.add(array[i].toString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

//        adsf
        updateDayTimes()
        upodateAllergiesCheck()
    }

    private fun upodateAllergiesCheck() {
        try {

            foodAllergies.clear()
            environmentalAllergies.clear()

            foodAllergies = ArrayList()
            environmentalAllergies = ArrayList()

            if ((childInfo?.doesHaveAnyAllergies ?: "") == "Yes") {

                if (childInfo?.foodAllergies != null) {

                    var allAlgs = StringBuilder()
                    var algs = JSONArray(childInfo?.foodAllergies ?: "")
                    for (i in 0 until algs.length()) {
                        if (i == algs.length() - 1)
                            allAlgs.append(algs[i])
                        else
                            allAlgs.append(algs[i].toString() + ", ")

                        foodAllergies.add(algs[i].toString())

//                        Log.i("junidoc",algs[i].toString()+"   ${foodAllergies.size}")
                    }
                }

                if (childInfo?.environmentalAllergies != null) {

                    var allAlgs = StringBuilder()
                    var algs = JSONArray(childInfo?.environmentalAllergies ?: "")
                    for (i in 0 until algs.length()) {
                        if (i == algs.length() - 1)
                            allAlgs.append(algs[i])
                        else
                            allAlgs.append(algs[i].toString() + ", ")

                        environmentalAllergies.add(algs[i].toString())

//                            Log.i("junidoc",algs[i].toString()+"   ${environmentalAllergies.size}")
                    }
                }
            }

            binding.rbEgg.isChecked = foodAllergies.any { it == "eggs" }
            binding.rbMilk.isChecked = foodAllergies.any { it == "milk" }

            binding.rbTreeNuts.isChecked = foodAllergies.any { it == "tree" }
            binding.rbSoy.isChecked = foodAllergies.any { it == "soy" }
            binding.rbWheat.isChecked = foodAllergies.any { it == "wheat" }
            binding.rbFish.isChecked = foodAllergies.any { it == "fish" }
            binding.rbShellfish.isChecked = foodAllergies.any { it == "shelfish" }

            binding.rbOther.isChecked = foodAllergies.any {
                ((it.toLowerCase() != "eggs") && (it.toLowerCase() != "milk") && (it.toLowerCase() != "peanuts") &&
                        (it.toLowerCase() != "tree") && (it.toLowerCase() != "soy") && (it.toLowerCase() != "wheat") &&
                        (it.toLowerCase() != "fish") && (it.toLowerCase() != "shelfish"))
            }

            if (foodAllergies.any {
                    ((it.toLowerCase() != "eggs") && (it.toLowerCase() != "milk") && (it.toLowerCase() != "peanuts") &&
                            (it.toLowerCase() != "tree") && (it.toLowerCase() != "soy") && (it.toLowerCase() != "wheat") &&
                            (it.toLowerCase() != "fish") && (it.toLowerCase() != "shelfish"))
                }) {
                binding.etTypeAnythingForFoodAllergy.viewVisible()
                binding.etTypeAnythingForFoodAllergy.setText(foodAllergies.find {
                    ((it.toLowerCase() != "eggs") && (it.toLowerCase() != "milk") && (it.toLowerCase() != "peanuts") &&
                            (it.toLowerCase() != "tree") && (it.toLowerCase() != "soy") && (it.toLowerCase() != "wheat") &&
                            (it.toLowerCase() != "fish") && (it.toLowerCase() != "shelfish"))
                })
            } else {
                binding.etTypeAnythingForFoodAllergy.viewGone()
            }

            binding.rbDog.isChecked = environmentalAllergies.any { it == "dog" }
            binding.rbMold.isChecked = environmentalAllergies.any { it == "mold" }
            binding.rbCat.isChecked = environmentalAllergies.any { it == "cat" }
            binding.rbDustMites.isChecked = environmentalAllergies.any { it == "dust" }


            binding.rbOtherEnvironmentAllergy.isChecked = environmentalAllergies.any {
                ((it.toLowerCase() != "dog") && (it.toLowerCase() != "mold") && (it.toLowerCase() != "cat") &&
                        (it.toLowerCase() != "dust"))
            }

            if (environmentalAllergies.any {
                    ((it.toLowerCase() != "dog") && (it.toLowerCase() != "mold") && (it.toLowerCase() != "cat") &&
                            (it.toLowerCase() != "dust"))
                }) {
                binding.etTypeAnythingForEnvironmentAllergy.viewVisible()
                binding.etTypeAnythingForEnvironmentAllergy.setText(environmentalAllergies.find {
                    ((it.toLowerCase() != "dog") && (it.toLowerCase() != "mold") && (it.toLowerCase() != "cat") &&
                            (it.toLowerCase() != "dust"))
                })
            } else {
                binding.etTypeAnythingForEnvironmentAllergy.viewGone()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("jexception", e.message.toString())
        }

        initCheckBoxes()
    }


    private fun updateDayTimes() {

        if (childInfo?.monday == "[null,null]") {
            binding.tvOpeningTimeMon.text = ""
            binding.tvClosingTimeMon.text = ""
            binding.btnSetTimeToAll.viewGone()
        } else {
            var mon = JSONArray(childInfo?.monday)
            binding.tvOpeningTimeMon.text = mon[0].toString()
            binding.tvClosingTimeMon.text = mon[1].toString()

            startTimeToSetAllWeek = mon[0].toString()
            endTimeToSetAllWeek = mon[1].toString()
            binding.btnSetTimeToAll.viewVisible()
        }
        if (childInfo?.tuesday == "[null,null]") {
            binding.tvOpeningTimeTue.text = ""
            binding.tvClosingTimeTue.text = ""
        } else {
            var tue = JSONArray(childInfo?.tuesday)
            binding.tvOpeningTimeTue.text = tue[0].toString()
            binding.tvClosingTimeTue.text = tue[1].toString()
        }
        if (childInfo?.wednesday == "[null,null]") {
            binding.tvOpeningTimeWed.text = ""
            binding.tvClosingTimeWed.text = ""
        } else {
            var wed = JSONArray(childInfo?.wednesday)
            binding.tvOpeningTimeWed.text = wed[0].toString()
            binding.tvClosingTimeWed.text = wed[1].toString()
        }
        if (childInfo?.thursday == "[null,null]") {
            binding.tvOpeningTimeThu.text = ""
            binding.tvClosingTimeThu.text = ""
        } else {
            var thu = JSONArray(childInfo?.thursday)
            binding.tvOpeningTimeThu.text = thu[0].toString()
            binding.tvClosingTimeThu.text = thu[1].toString()
        }
        if (childInfo?.friday == "[null,null]") {
            binding.tvOpeningTimeFri.text = ""
            binding.tvClosingTimeFri.text = ""
        } else {
            var fri = JSONArray(childInfo?.friday)
            binding.tvOpeningTimeFri.text = fri[0].toString()
            binding.tvClosingTimeFri.text = fri[1].toString()
        }

    }

    private fun resetTimeSelection(tvStart: TextView, tvEnd: TextView) {
        tvStart.text = ""
        tvEnd.text = ""
    }

    var myCalendar = Calendar.getInstance()

    var myExpireCalendar = Calendar.getInstance()

    private fun setUpParent(selectedParents: User?, iv: CircleImageView, tv: TextView) {
        tv.text = selectedParents?.firstName + " " + selectedParents?.firstName
        iv.let {
            Glide.with(this@AddChildActivity)
                .load(Constants.IMG_BASE_PATH + selectedParents?.profilePicture ?: "")
                .placeholder(R.drawable.ic_user_placeholder).into(it)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun listener() {


        binding.btnSetTimeToAll.setOnClickListener {
            if (startTimeToSetAllWeek != "" && endTimeToSetAllWeek != "") {
                binding.tvOpeningTimeMon.text = startTimeToSetAllWeek
                binding.tvOpeningTimeTue.text = startTimeToSetAllWeek
                binding.tvOpeningTimeWed.text = startTimeToSetAllWeek
                binding.tvOpeningTimeThu.text = startTimeToSetAllWeek
                binding.tvOpeningTimeFri.text = startTimeToSetAllWeek

                binding.tvClosingTimeMon.text = endTimeToSetAllWeek
                binding.tvClosingTimeTue.text = endTimeToSetAllWeek
                binding.tvClosingTimeWed.text = endTimeToSetAllWeek
                binding.tvClosingTimeThu.text = endTimeToSetAllWeek
                binding.tvClosingTimeFri.text = endTimeToSetAllWeek
            }
        }


        binding.rbEpiPen1Yes.setOnClickListener {
            var dialog = EpiPenAcknowledgementPopup.getInstance(fromList)
            dialog.baseActivity = this@AddChildActivity
            dialog.clickListeners = object : EpiPenAcknowledgementPopup.ClickListener {

                override fun onIAgreeClicked(dialogFragment: DialogFragment?) {
                    dialog.dismiss()
                }

                override fun onCancelClicked() {
                }

            }
            dialog.show(supportFragmentManager, "EpiPenAcknowledgementPopup")

        }

        binding.tvEthnicityText.setOnClickListener {
            ethnicityList.forEachIndexed { index, item ->
                if (item.text == binding.tvEthnicityText.text) {
                    ethnicityList[index].isChecked = true
                } else ethnicityList[index].isChecked =
                    item.text == "Other" && selectedEthnicityOtherText != ""
            }
            var sheet = EthnicityBottomSheet(
                this@AddChildActivity,
                ethnicityList,
                selectedEthnicityOtherText
            )
            sheet.setListener(object : EthnicityBottomSheet.ClickListener {
                override fun onChooseClicked(selectedEthn: EthnicityList?, otherText: String) {
                    selectedEthnicity = selectedEthn
                    binding.tvEthnicityText.text = selectedEthn?.text
                    if (selectedEthn?.text == "Other") {
                        selectedEthnicityOtherText = otherText
                        binding.tvEthnicityOther.viewVisible()
                        binding.cvEthnicityOther.viewVisible()
                        binding.tvEthnicityTextOther.text = otherText

                    } else {
                        selectedEthnicityOtherText = ""
                        binding.tvEthnicityOther.viewGone()
                        binding.cvEthnicityOther.viewGone()
                        binding.tvEthnicityTextOther.text = ""
                    }
                }

                override fun onCancelClicked() {
                }
            })
            sheet.show(supportFragmentManager, "EthnicityBottomSheet")
        }

        binding.cvSpinnerSelectParent.setOnClickListener {
            if (selectedParent != null) {
                authorizedAdultsList.forEachIndexed { index, user ->
                    authorizedAdultsList[index].isUserSelected = selectedParent?.id == user.id
                }
            }
            var sheet =
                ParentsBottomSheet(this@AddChildActivity, "Select Parent", authorizedAdultsList)
            sheet.setListener(object : ParentsBottomSheet.ClickListener {
                override fun onSelectNowClicked(selectedParents: User?) {
                    selectedParent = selectedParents
                    Log.e("selectedPickUp", selectedParents?.id.toString())
                    setUpParent(selectedParents, binding.ivChildParent, binding.tvParentName)
                }

                override fun onCancelClicked() {

                }
            })
            sheet.show(supportFragmentManager, "ParentsBottomSheet")
        }




        binding.radioGroupCustody.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == binding.rbCustodyYes.id) {
                custodyConsideration = "Yes"
                binding.tvDesc.viewVisible()
                binding.etDesc.viewVisible()
                binding.lnrUpload.viewVisible()
            }
            if (checkedId == binding.rbCustodyNo.id) {
                custodyConsideration = "No"
                binding.tvDesc.viewGone()
                binding.etDesc.viewGone()
                binding.lnrUpload.viewGone()
            }
        }
        binding.tvUpload.setOnClickListener {
            Dexter.withContext(this)
                .withPermissions(
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) { /* ... */
                        chooser?.show()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest?>?,
                        token: PermissionToken?
                    ) { /* ... */
                        token?.continuePermissionRequest()
                    }
                }).check()
        }

        binding.tvHaveAnotherChild.setOnClickListener {
            startActivity<ChildProfileListActivity>()
        }

        binding.llDob.setOnClickListener {

            val dpd = DatePickerDialog(
                this@AddChildActivity, R.style.MySpinnerDatePickerStyle,
                { _, year, month, dayOfMonth ->
                    myCalendar.set(Calendar.YEAR, year)
                    myCalendar.set(Calendar.MONTH, month)
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    Log.i("DateOfBirth", "${month}")

                    dob = "$year-${month + 1}-$dayOfMonth"
                    binding.tvDob.text = "${month + 1} / $dayOfMonth / $year"
                }, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )

            val cal1 = Calendar.getInstance()
            cal1.add(Calendar.YEAR, -21)
            dpd.datePicker.maxDate = Calendar.getInstance().timeInMillis
            dpd.datePicker.minDate = cal1.timeInMillis
            dpd.show()
        }

        binding.llEPIPenExpiration.setOnClickListener {
//            var myCalendar = Calendar.getInstance()
            var dpd = DatePickerDialog(
                this@AddChildActivity, R.style.MySpinnerDatePickerStyle,
                { _, year, month, dayOfMonth ->
                    myExpireCalendar.set(Calendar.YEAR, year)
                    myExpireCalendar.set(Calendar.MONTH, month)
                    myExpireCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    epiPen1Expiration = "$year-${month + 1}-$dayOfMonth"
                    binding.tvEPIPenExpiration.text = "${month + 1} / $dayOfMonth / $year"

                }, myExpireCalendar
                    .get(Calendar.YEAR), myExpireCalendar.get(Calendar.MONTH),
                myExpireCalendar.get(Calendar.DAY_OF_MONTH)
            )
            dpd.datePicker.minDate = Calendar.getInstance().timeInMillis
            dpd.show()
        }

        binding.tbResetMon.setOnClickListener {
            resetTimeSelection(binding.tvOpeningTimeMon, binding.tvClosingTimeMon)
        }
        binding.tbResetTue.setOnClickListener {
            resetTimeSelection(binding.tvOpeningTimeTue, binding.tvClosingTimeTue)
        }
        binding.tbResetWed.setOnClickListener {
            resetTimeSelection(binding.tvOpeningTimeWed, binding.tvClosingTimeWed)
        }
        binding.tbResetThu.setOnClickListener {
            resetTimeSelection(binding.tvOpeningTimeThu, binding.tvClosingTimeThu)
        }
        binding.tbResetFri.setOnClickListener {
            resetTimeSelection(binding.tvOpeningTimeFri, binding.tvClosingTimeFri)
        }


        binding.btnSubmit.setOnClickListener {
            if (validate()) {
//            jCall()
//                withoutiFiles()
//                setFocus(binding.btnSubmit)
//                setFocus(binding.tvSubmit)
                sendToServer(filePath)
            }
        }

        binding.radioGroupGender.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == binding.rbFemal.id) {
                gender = "Female"
            }

            if (checkedId == binding.rbUnSpecified.id) {
                gender = "Unspecified"
            }

            if (checkedId == binding.rbMale.id) {
                gender = "Male"
            }
        }

        binding.radioGroupAddress.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == binding.rbFullTime.id) {
                time = "Full Time"
                binding.tvAddress2.visibility = View.GONE
                binding.etAddress2.visibility = View.GONE
                binding.tvAprOrSuiteNo2.visibility = View.GONE
                binding.etAprOrSuiteNo2.visibility = View.GONE
                binding.tvCity2.visibility = View.GONE
                binding.etCity2.visibility = View.GONE
                binding.tvZipCode2.visibility = View.GONE
                binding.etZipCode2.visibility = View.GONE
                binding.tvState2.visibility = View.GONE
                binding.etState2.visibility = View.GONE
            }

            if (checkedId == binding.rbPartTime.id) {
                time = "Part Time"
                binding.tvAddress2.visibility = View.VISIBLE
                binding.etAddress2.visibility = View.VISIBLE
                binding.tvAprOrSuiteNo2.visibility = View.VISIBLE
                binding.etAprOrSuiteNo2.visibility = View.VISIBLE
                binding.tvCity2.visibility = View.VISIBLE
                binding.etCity2.visibility = View.VISIBLE
                binding.tvZipCode2.visibility = View.VISIBLE
                binding.etZipCode2.visibility = View.VISIBLE
                binding.tvState2.visibility = View.VISIBLE
                binding.etState2.visibility = View.VISIBLE
            }
        }

        binding.radioGroupIEP.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == binding.rbYes.id) {
                iep = "Yes"
                binding.etIepDesc.visibility = View.VISIBLE
                binding.tvIEpNote.visibility = View.VISIBLE
                binding.etIEpNote.visibility = View.VISIBLE
            }

            if (checkedId == binding.rbNo.id) {
                iep = "No"
                binding.etIepDesc.visibility = View.GONE
                binding.tvIEpNote.visibility = View.GONE
                binding.etIEpNote.visibility = View.GONE


            }
        }

        binding.radioGroupAllergiNote.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == binding.rbAllergyYes.id) {
                allergy = "Yes"
                binding.lnrCheckBox.visibility = View.VISIBLE
                binding.llEnvironmentalAllergies.visibility = View.VISIBLE
            }

            if (checkedId == binding.rbAllergyNo.id) {
                allergy = "No"
                binding.lnrCheckBox.visibility = View.GONE
                binding.llEnvironmentalAllergies.visibility = View.GONE
            }
        }

        binding.radioGroupEpiPen1.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == binding.rbEpiPen1Yes.id) {
                epiPen1 = "Yes"
                binding.tvEPIPenExpiration.viewVisible()
                binding.titleEPIPenExpiration.viewVisible()
                binding.llEPIPenExpiration.viewVisible()
            }

            if (checkedId == binding.rbEpiPen1NO.id) {
                epiPen1 = "No"
                binding.tvEPIPenExpiration.viewGone()
                binding.titleEPIPenExpiration.viewGone()
                binding.llEPIPenExpiration.viewGone()
            }
        }

//        binding.lnrMondayStart.setOnClickListener {
//            showTimerClock {
//                binding.tvMondayStart.text = it
//            }
//        }
//
//
//        binding.lnrMondayEnd.setOnClickListener {
//            showTimerClock {
//                binding.tvMondayEnd.text = it
//            }
//        }
//
//        binding.lnrTuesdayStart.setOnClickListener {
//            showTimerClock {
//                binding.tvTuesdayStart.text = it
//            }
//        }
//
//        binding.lnrTuesdayEnd.setOnClickListener {
//            showTimerClock {
//                binding.tvTuesdayEnd.text = it
//            }
//        }
//
//        binding.lnrWedStart.setOnClickListener {
//            showTimerClock {
//                binding.tvWedStart.text = it
//            }
//        }
//
//        binding.lnrWedEnd.setOnClickListener {
//            showTimerClock {
//                binding.tvWedEnd.text = it
//            }
//        }


//        binding.lnrThursdayStart.setOnClickListener {
//            showTimerClock {
//                binding.tvThurStart.text = it
//            }
//        }
//
//        binding.lnrThursdayEnd.setOnClickListener {
//            showTimerClock {
//                binding.tvThurEnd.text = it
//            }
//        }
//
//
//        binding.lnrFridayStart.setOnClickListener {
//            showTimerClock {
//                binding.tvFridayStart.text = it
//            }
//        }
//
//        binding.lnrFridayEnd.setOnClickListener {
//            showTimerClock {
//                binding.tvFridayEnd.text = it
//            }
//        }

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.ivChangeProfilePic.setOnClickListener {
            selectImageWithCop()
        }

        binding.tvOpeningTimeMon.setOnClickListener {
            showClock(selectedTime = binding.tvOpeningTimeMon.text.trim().toString()) {
                binding.tvOpeningTimeMon.text = it
                binding.tvClosingTimeMon.text = ""

                startTimeToSetAllWeek = it
            }
        }

        binding.tvClosingTimeMon.setOnClickListener {
            if (validateTimeSelection(binding.tvOpeningTimeMon))
                showClock(
                    setMinTime = binding.tvOpeningTimeMon.text.toString(),
                    selectedTime = binding.tvClosingTimeMon.text.trim().toString()
                ) {
//                    binding.tvClosingTimeMon.text = it
//                    endTimeToSetAllWeek = it
                    if (validTime(binding.tvOpeningTimeMon.text.toString(), it)) {
                        binding.tvClosingTimeMon.text = it
                        endTimeToSetAllWeek = it
                    } else {
                        binding.tvClosingTimeMon.text = ""
                        endTimeToSetAllWeek = ""
                        showErrorToast(this, "To time should be greater than From time.")
                    }
                    if (startTimeToSetAllWeek != "" && endTimeToSetAllWeek != "") {
                        binding.btnSetTimeToAll.viewVisible()
                    }
                }
        }

        binding.tvOpeningTimeTue.setOnClickListener {
            showClock(selectedTime = binding.tvOpeningTimeTue.text.trim().toString()) {
                binding.tvOpeningTimeTue.text = it
                binding.tvClosingTimeTue.text = ""
            }
        }

        binding.tvClosingTimeTue.setOnClickListener {
            if (validateTimeSelection(binding.tvOpeningTimeTue))
                showClock(
                    binding.tvOpeningTimeTue.text.toString(),
                    selectedTime = binding.tvClosingTimeTue.text.trim().toString()
                ) {
//                    binding.tvClosingTimeTue.text = it
                    if (validTime(binding.tvOpeningTimeTue.text.toString(), it)) {
                        binding.tvClosingTimeTue.text = it
                    } else {
                        binding.tvClosingTimeTue.text = ""
                        showErrorToast(this, "To time should be greater than From time.")
                    }
                }
        }

        binding.tvOpeningTimeWed.setOnClickListener {
            showClock(selectedTime = binding.tvOpeningTimeWed.text.trim().toString()) {
                binding.tvOpeningTimeWed.text = it
                binding.tvClosingTimeWed.text = ""
            }
        }

        binding.tvClosingTimeWed.setOnClickListener {
            if (validateTimeSelection(binding.tvOpeningTimeWed))
                showClock(
                    binding.tvOpeningTimeWed.text.toString(),
                    selectedTime = binding.tvClosingTimeWed.text.trim().toString()
                ) {
                    if (validTime(binding.tvOpeningTimeWed.text.toString(), it)) {
                        binding.tvClosingTimeWed.text = it
                    } else {
                        binding.tvClosingTimeWed.text = ""
                        showErrorToast(this, "To time should be greater than From time.")
                    }
                }
        }

        binding.tvOpeningTimeThu.setOnClickListener {
            showClock(selectedTime = binding.tvOpeningTimeThu.text.trim().toString()) {
                binding.tvOpeningTimeThu.text = it
                binding.tvClosingTimeThu.text = ""
            }
        }

        binding.tvClosingTimeThu.setOnClickListener {
            if (validateTimeSelection(binding.tvOpeningTimeThu))
                showClock(
                    binding.tvOpeningTimeThu.text.toString(),
                    selectedTime = binding.tvClosingTimeThu.text.trim().toString()
                ) {
                    if (validTime(binding.tvOpeningTimeThu.text.toString(), it)) {
                        binding.tvClosingTimeThu.text = it
                    } else {
                        binding.tvClosingTimeThu.text = ""
                        showErrorToast(this, "To time should be greater than From time.")
                    }
                }
        }

        binding.tvOpeningTimeFri.setOnClickListener {
            showClock(selectedTime = binding.tvOpeningTimeFri.text.trim().toString()) {
                binding.tvOpeningTimeFri.text = it
                binding.tvClosingTimeFri.text = ""
            }
        }

        binding.tvClosingTimeFri.setOnClickListener {
            if (validateTimeSelection(binding.tvOpeningTimeFri))
                showClock(
                    binding.tvOpeningTimeFri.text.toString(),
                    selectedTime = binding.tvClosingTimeFri.text.trim().toString()
                ) {
                    if (validTime(binding.tvOpeningTimeFri.text.toString(), it)) {
                        binding.tvClosingTimeFri.text = it
                    } else {
                        binding.tvClosingTimeFri.text = ""
                        showErrorToast(this, "To time should be greater than From time.")
                    }
                }
        }

        binding.etAddress.setOnClickListener {
            startSearchActivity(1)
        }

        binding.etAddress2.setOnClickListener {
            startSearchActivity(2)
        }


        binding.etAddress.setOnFocusChangeListener { view, b ->

            if (b && alreadyFoccused) {
//                binding.etAddress.performClick()
                alreadyFoccused = true
            } else {

            }
        }
    }

    var alreadyFoccused = false

    private fun initVar() {
        ethnicityList.add(EthnicityList("Caucasian"))
        ethnicityList.add(EthnicityList("African American"))
        ethnicityList.add(EthnicityList("Native American"))
        ethnicityList.add(EthnicityList("Hispanic/Latino"))
        ethnicityList.add(EthnicityList("Asian"))
        ethnicityList.add(EthnicityList("South Asian"))
        ethnicityList.add(EthnicityList("Other"))

        builder.withActivity(this@AddChildActivity)
            .customFilter(arrayListOf("jpg", "jpeg", "png"))
            .withFragmentManager(fragmentManager)
            .withMemoryBar(true)
            .allowCustomPath(true)
            .setType(StorageChooser.FILE_PICKER)
            .build()

        chooser = builder.build()

        chooser?.setOnMultipleSelectListener { paths ->
            Log.e("SELECTED_PATH", paths.size.toString())
            newFilesSelected = true
            documents.addAll(paths)
            filesAdapter?.setData(documents)
        }
        chooser?.setOnSelectListener { path ->
            Log.e("SELECTED_PATH", path)
            newFilesSelected = true
            documents.addAll(listOf(path))
            filesAdapter?.setData(documents)
        }
    }

    private fun updateFoodAllergiesList() {
        foodAllergies.clear()
        foodAllergies = ArrayList()
        if (binding.rbEgg.isChecked) {
            foodAllergies.add("eggs")
        }
        if (binding.rbMilk.isChecked) {
            foodAllergies.add("milk")
        }

        if (binding.rbTreeNuts.isChecked) {
            foodAllergies.add("tree")
        }
        if (binding.rbSoy.isChecked) {
            foodAllergies.add("soy")
        }
        if (binding.rbWheat.isChecked) {
            foodAllergies.add("wheat")
        }
        if (binding.rbFish.isChecked) {
            foodAllergies.add("fish")
        }
        if (binding.rbShellfish.isChecked) {
            foodAllergies.add("shelfish")
        }
        if (binding.rbOther.isChecked) {
            foodAllergies.add(binding.etTypeAnythingForFoodAllergy.text.toString())
        }
    }

    private fun updateEnvironmentalAllergiesList() {
        environmentalAllergies.clear()
        environmentalAllergies = ArrayList()
        if (binding.rbDog.isChecked) {
            environmentalAllergies.add("dog")
        }
        if (binding.rbMold.isChecked) {
            environmentalAllergies.add("mold")
        }
        if (binding.rbCat.isChecked) {
            environmentalAllergies.add("cat")
        }
        if (binding.rbDustMites.isChecked) {
            environmentalAllergies.add("dust")
        }
        if (binding.rbOtherEnvironmentAllergy.isChecked) {
            environmentalAllergies.add(binding.etTypeAnythingForEnvironmentAllergy.text.toString())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showTimerClock(
        callback: (String) -> Unit
    ) {

        // var miniTimeCalendar = Calendar.getInstance()

        val dpd = TimePickerDialog.newInstance(
            { _, hourOfDay, minute, second ->
                val h = String.format("%02d", hourOfDay)
                val m = String.format("%02d", minute)
                val s = String.format("%02d", second)
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                mCalendar.set(Calendar.MINUTE, minute)
                mCalendar.set(Calendar.SECOND, second)

                val time = "$h:$m:$s"

                //tvTime.text = DateConverter.getFormattedTime(hourOfDay, minute)


                //callback(DateConverter.convertToFromTimePicker(time))
                //callback(time)

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

                val selectedTime = "" + hourr + ":" + mm_precede + minute + AM_PM
                callback(selectedTime)
            },
            mCalendar.get(Calendar.HOUR_OF_DAY),
            mCalendar.get(Calendar.MINUTE),
            mCalendar.get(Calendar.SECOND),
            false
        )
        dpd.version = TimePickerDialog.Version.VERSION_1
        //dpd.setTimeInterval(1)
        dpd.show(supportFragmentManager, "Datepickerdialog")
    }

    private fun validateTimeSelection(tvOpeningTime: TextView): Boolean {
        if (tvOpeningTime.text.toString().trim().isEmpty()) {
            CustomToasts.failureToast(
                this@AddChildActivity,
                "Please select drop off time first!"
            )
            return false
        }
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
        val dpd = android.app.TimePickerDialog(
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

            }, miniTimeCalendar.get(Calendar.HOUR_OF_DAY),
            miniTimeCalendar.get(Calendar.MINUTE),
            false
        )
        dpd.show()
//        val dpd = TimePickerDialog.newInstance(
//            { _, hourOfDay, minute, second ->
//
//                val time = "$hourOfDay:$minute:$second"
//                callback(DateConverter.convertToFromTimePicker(time))
//            },
//            calendar.get(Calendar.HOUR_OF_DAY),
//            calendar.get(Calendar.MINUTE),
//            calendar.get(Calendar.SECOND),
//            false
//        )
//        if (setMinTime != "") {
//            dpd.setMinTime(
//                miniTimeCalendar.get(Calendar.HOUR_OF_DAY),
//                miniTimeCalendar.get(Calendar.MINUTE),
//                miniTimeCalendar.get(Calendar.SECOND)
//            )
//        }
//        dpd.version = TimePickerDialog.Version.VERSION_1
////        dpd.setTimeInterval(1)
//        dpd.show(supportFragmentManager, "Datepickerdialog")
    }

    private fun selectImageWithCop() {
        CropImage.activity()
            .start(this);
    }

    private fun validate(): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@AddChildActivity, "No internet connection!")
            return false
        }

        if (binding.etFirstname.text.toString().trim().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please enter First Name!")
//            setFocus(binding.etFirstname)
            return false
        }

//        if (binding.etMiddleName.text.toString().trim().isEmpty()) {
//            showErrorToast(this@AddChildActivity, "Please enter Middle Name!")
//            return false
//        }

        if (binding.etLastName.text.toString().trim().isEmpty()) {
//            setFocus(binding.etLastName)
            showErrorToast(this@AddChildActivity, "Please enter Last Name!")
            return false
        }
//        if (binding.etNickName.text.toString().trim().isEmpty()) {
//            showErrorToast(this@AddChildActivity, "Please enter Nick Name!")
//            return false
//        }
        if (binding.etCallYou.text.toString().trim().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please enter What child calls you?")
            return false
        }

        if ((userManager.user?.type == "provider" || userManager.user?.type == "staff") && selectedParent == null) {
            showErrorToast(this@AddChildActivity, "Please select Parent!")
            return false
        }

        if (dob.trim().isEmpty()) {
//            setFocus(binding.llDob)
            showErrorToast(this@AddChildActivity, "Please select your Date Of Birth!")
            return false
        }

        if (selectedEthnicity == null) {
            showErrorToast(this@AddChildActivity, "Please select Ethnicity!")
            return false
        }

        if (gender.trim().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please select your Gender!")
            return false
        }

        if (filePath.isEmpty() && childInfo == null) {
            showErrorToast(this@AddChildActivity, "Please select Display Picture!")
            return false
        }

        if (binding.etAddress.text.toString().trim().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please enter your Primary Address!")
            return false
        }

        if (binding.etState.text.toString().trim().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please enter your Primary State!")
            return false
        }

        if (binding.etCity.text.toString().trim().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please enter your Primary City!")
            return false
        }
        if (binding.etZipCode.text.toString().trim().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please enter your Primary Zip Code!")
            return false
        }

        if (binding.rbPartTime.isChecked && binding.etAddress2.text.toString().trim()
                .isEmpty()
        ) {
            showErrorToast(this@AddChildActivity, "Please enter your Secondary Address!")
            return false
        }

        if (binding.rbPartTime.isChecked && binding.etState2.text.toString().trim().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please enter your Secondary State!")
            return false
        }

        if (binding.rbPartTime.isChecked && binding.etCity2.text.toString().trim().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please enter your Secondary City!")
            return false
        }
        if (binding.rbPartTime.isChecked && binding.etZipCode2.text.toString().trim()
                .isEmpty()
        ) {
            showErrorToast(this@AddChildActivity, "Please enter your Secondary Zip Code!")
            return false
        }

        if (custodyConsideration.isEmpty()) {
//            setFocus(binding.radioGroupCustody)
            showErrorToast(this@AddChildActivity, "Please select Custody considerations!")
            return false
        }

        if (custodyConsideration == "Yes" && binding.etDesc.text.toString().trim().isEmpty()) {
//            setFocus(binding.etDesc)
            showErrorToast(this@AddChildActivity, "Please enter Custody Description!")
            return false
        }
        if (custodyConsideration == "Yes" && documents.size == 0) {
            showErrorToast(this@AddChildActivity, "Please select custody documents!")
            return false
        }

//        if (binding.etDesc.text.toString().trim().isEmpty()) {
//            showErrorToast(this@AddChildActivity, "Please enter your Description!")
//            return false
//        }

        if (iep == "Yes" && binding.etIepDesc.text.toString().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please enter IEP Description!")
            return false
        }
        if (iep == "Yes" && binding.etIEpNote.text.toString().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please enter Who Diagnosed your child!")
            return false
        }

        if (allergy == "Yes" && (foodAllergies.size < 1 && environmentalAllergies.size < 1)) {
            showErrorToast(this@AddChildActivity, "Please Select Allergies!")
            return false
        }

        if (binding.rbOther.isChecked && binding.etTypeAnythingForFoodAllergy.text.toString()
                .isEmpty()
        ) {
            showErrorToast(this@AddChildActivity, "Please enter other Allergies!")
            return false
        }

        if (binding.rbOtherEnvironmentAllergy.isChecked && binding.etTypeAnythingForEnvironmentAllergy.text.toString()
                .isEmpty()
        ) {
            showErrorToast(this@AddChildActivity, "Please enter other environmental Allergies!")
            return false
        }

        if (time.trim().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please select Custody Information!")
            return false
        }

        if (epiPen1 == "Yes" && binding.tvEPIPenExpiration.text.toString().isEmpty()) {
            showErrorToast(this@AddChildActivity, "Please select EPI Pen Expiration Date!")
            return false
        }

        if ((!binding.tvOpeningTimeMon.text.isNullOrBlank() && binding.tvClosingTimeMon.text.isNullOrBlank())
            || (!binding.tvOpeningTimeTue.text.isNullOrBlank() && binding.tvClosingTimeTue.text.isNullOrBlank())
            || (!binding.tvOpeningTimeWed.text.isNullOrBlank() && binding.tvClosingTimeWed.text.isNullOrBlank())
            || (!binding.tvOpeningTimeThu.text.isNullOrBlank() && binding.tvClosingTimeThu.text.isNullOrBlank())
            || (!binding.tvOpeningTimeFri.text.isNullOrBlank() && binding.tvClosingTimeFri.text.isNullOrBlank())
        ) {
            showErrorToast(this@AddChildActivity, "Please enter the time to help provider")
            return false
        }


        if (binding.tvClosingTimeMon.text.isNullOrEmpty() && binding.tvOpeningTimeMon.text.isNotEmpty()) {
            CustomToasts.failureToast(
                this@AddChildActivity,
                "Select pickup time for Monday or set it day off!"
            )
            return false
        }

        if (binding.tvClosingTimeTue.text.isNullOrEmpty() && binding.tvOpeningTimeTue.text.isNotEmpty()) {
            CustomToasts.failureToast(
                this@AddChildActivity,
                "Select pickup time for Tuesday or set it day off!"
            )
            return false
        }

        if (binding.tvClosingTimeWed.text.isNullOrEmpty() && binding.tvOpeningTimeWed.text.isNotEmpty()) {
            CustomToasts.failureToast(
                this@AddChildActivity,
                "Select pickup time for Wednesday or set it day off!"
            )
            return false
        }

        if (binding.tvClosingTimeThu.text.isNullOrEmpty() && binding.tvOpeningTimeThu.text.isNotEmpty()) {
            CustomToasts.failureToast(
                this@AddChildActivity,
                "Select pickup time for Thursday or set it day off!"
            )
            return false
        }

        if (binding.tvClosingTimeFri.text.isNullOrEmpty() && binding.tvOpeningTimeFri.text.isNotEmpty()) {
            CustomToasts.failureToast(
                this@AddChildActivity,
                "Select pickup time for Friday or set it day off!"
            )
            return false
        }

//        if (binding.tvOpeningTimeMon.text.isNullOrEmpty() && binding.tvOpeningTimeTue.text.isNullOrEmpty() && binding.tvOpeningTimeWed.text.isNullOrEmpty() && binding.tvOpeningTimeThu.text.isNullOrEmpty() && binding.tvOpeningTimeFri.text.isNullOrEmpty()) {
//            CustomToasts.failureToast(
//                this@AddChildActivity,
//                "Select drop off and pickup time for at least single day!"
//            )
//            return false
//        }
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val resultUri = result.uri
                filePath = resultUri.path.toString()
                fileUri = resultUri
                //  fileUri = Uri.fromFile(File(image.path))
                Log.e("SELECTED_URI", fileUri.toString() + "")
                Glide.with(this).load(filePath)
                    .into(binding.ivProfile)
            }
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            //   val error = result.error
        }


        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {

            Log.i("CodeSearch", "Request Code")
            val TAG = "GoogleMapsLog"
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                Log.v("TestingNewLoc", "Place: " + place.name + ", " + place.id)

                Log.i("SelectedLocation", "" + place.latLng)


                val locModel = AddPaymentMethodsActivity.LocationModel()
                locModel.name = place.name ?: ""
                locModel.lat = place.latLng?.latitude ?: 0.0
                locModel.lng = place.latLng?.longitude ?: 0.0
                locModel.image =
                    "https://maps.googleapis.com/maps/api/place/photo?key=${NetworkURLs.KEY_GOOGLE}&photoreference=${
                        place.photoMetadatas?.first()?.zza()
                    }&maxwidth=2784"
                getIdPlaceData(place.id.toString(), this, this)

                val photoreference: String =
                    "https://maps.googleapis.com/maps/api/place/details/json?placeid=${
                        place.id
                    }&sensor=true&key=${NetworkURLs.KEY_GOOGLE}"

                Log.i("Key", "" + photoreference)
                place.photoMetadatas?.first()?.attributions


                /* Log.i("PlacesInfo", "" + locModel.name)
                 Log.i("PlacesInfo", "" + locModel.lat)
                 Log.i("PlacesInfo", "" + locModel.lng)
                 Log.i("PlacesInfo", "" + locModel.image)*/
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status = Autocomplete.getStatusFromIntent(data!!)
                assert(status.statusMessage != null)
                Log.v(TAG, status.statusMessage!!)
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }


    }

    private val AUTOCOMPLETE_REQUEST_CODE = 12121
    private var MapCheck = 0

    private fun startSearchActivity(i: Int) {
// Create a new Places client instance
        val placesClient = Places.createClient(this)
        //Start AutoComplete by Intent
        val fields = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS
        )
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.OVERLAY, fields
        )
            .build(this)

        MapCheck = i

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    private fun sendToServer(
        filePath: String
    ) {

        showProgressBar()
        updateFoodAllergiesList()
        updateEnvironmentalAllergiesList()

        val registrarId: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            (selectedParent?.id ?: 0).toString().trim(),
        )

        val fName: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etFirstname.text.toString().trim(),
        )
        val middleName: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etMiddleName.text.toString().trim(),
        )
        val lastName: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etLastName.text.toString().trim(),
        )

        val nickName: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etNickName.text.toString().trim(),
        )
        val childCallYou: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etCallYou.text.toString().trim(),
        )
        val gender: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            gender,
        )
        val dob: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            dob,
        )
        val primaryAddress: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etAddress.text.toString().trim(),
        )
        val apt: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etAprOrSuiteNo.text.toString().trim(),
        )
        val city: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etCity.text.toString().trim(),
        )
        val state: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etState.text.toString().trim(),
        )
        val zip_code: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etZipCode.text.toString().trim(),
        )
        val secondaryAddress: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etAddress2.text.toString().trim(),
        )
        val s_apt: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etAprOrSuiteNo2.text.toString().trim(),
        )
        val s_city: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etState2.text.toString().trim(),
        )
        val s_state: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etCity2.text.toString().trim(),
        )
        val s_zip_code: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etZipCode2.text.toString().trim(),
        )
        val custody: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            time,
        )
        val iep: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            iep,
        )
        val epiPenExpiration: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            epiPen1Expiration,
        )
        val comment: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etIepDesc.text.toString().trim(),
        )
        val iepDiagnosedBy: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etIEpNote.text.toString().trim(),
        )
        val doesHaveAnyAllergies: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            allergy,
        )

        val fAllergies = ArrayList<RequestBody>()
        foodAllergies.forEachIndexed { index, s ->
            val alg: RequestBody = RequestBody.create(
                "multipart/form-data".toMediaTypeOrNull(),
                s,
            )
            fAllergies.add(alg)
        }

        val eAllergies = ArrayList<RequestBody>()
        environmentalAllergies.forEachIndexed { index, s ->
            val alg: RequestBody = RequestBody.create(
                "multipart/form-data".toMediaTypeOrNull(),
                s,
            )
            eAllergies.add(alg)
        }
//
//        val environmentalAllergies: RequestBody = RequestBody.create(
//            "multipart/form-data".toMediaTypeOrNull(),
//            environmentalAllergies[0],
//        )
        val doesChildRequireEpiPin: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            epiPen1
        )

        val custodyConsideration: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            custodyConsideration,
        )

        val custodyDescription: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etDesc.text.toString().trim(),
        )

        val mondayOpening: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.tvOpeningTimeMon.text.toString().trim(),
        )

        val mondayClosing: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.tvClosingTimeMon.text.toString().trim(),
        )

        val tuesdayOpening: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.tvOpeningTimeTue.text.toString().trim(),
        )

        val tuesdayClosing: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.tvClosingTimeTue.text.toString().trim(),
        )

        val wednesdayOpening: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.tvOpeningTimeWed.text.toString().trim(),
        )

        val wednesdayClosing: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.tvClosingTimeWed.text.toString().trim(),
        )

        val thursdayOpening: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.tvOpeningTimeThu.text.toString().trim(),
        )

        val thursdayClosing: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.tvClosingTimeThu.text.toString().trim(),
        )

        val fridayOpening: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.tvOpeningTimeFri.text.toString().trim(),
        )

        val fridayClosing: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.tvClosingTimeFri.text.toString().trim(),
        )


        val lat: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            lat,
        )

        val lng: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            lng,
        )

        val ethnicity: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            selectedEthnicity?.text ?: "",
        )

        val etSpecialNeedsConsideration: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etSpecialNeedsConsideration.text.toString().trim(),
        )
        val ethnicityOtherText: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            selectedEthnicityOtherText,
        )

        var call: Call<LoginResponse>? = null

        var documentParts = ArrayList<MultipartBody.Part?>()
        documents.forEachIndexed { index, s ->
            if (!s.contains("/childs/custody_document")) {
                val documentURI = Uri.parse(s)
                val document = File(documentURI.path)
                documentParts.add(
                    MultipartBody.Part.createFormData(
                        "custody_document[]",
                        document.name,
                        RequestBody.create("*/*".toMediaTypeOrNull(), document)
                    )
                )
            }
        }
        //profile == 0 && document == 0
        if ((this.custodyConsideration == "No" || !newFilesSelected) && filePath == "") {
            call =
                RetrofitClassNew.getInstance().webRequestsInstance.registerOrUpdateChildWithNoImage(
                    "Bearer " + userManager.accessToken ?: "",
                    registrarId,
                    if (childInfo != null) {
                        childInfo?.id ?: 0
                    } else {
                        null
                    },
                    fName,
                    middleName,
                    lastName,
                    nickName,
                    childCallYou,
                    gender,
                    dob,
                    primaryAddress,
                    apt,
                    state,
                    city,
                    zip_code,
                    secondaryAddress,
                    s_apt,
                    s_state,
                    s_city,
                    s_zip_code,
                    custody,
                    iep,
                    etSpecialNeedsConsideration,
                    epiPenExpiration,
                    comment,
                    iepDiagnosedBy,
                    doesHaveAnyAllergies,
                    fAllergies,
                    eAllergies,
                    doesChildRequireEpiPin,
                    custodyConsideration,
                    custodyDescription,
                    mondayOpening,
                    mondayClosing,
                    tuesdayOpening,
                    tuesdayClosing,
                    wednesdayOpening,
                    wednesdayClosing,
                    thursdayOpening,
                    thursdayClosing,
                    fridayOpening,
                    fridayClosing,
                    lat,
                    lng, ethnicity, ethnicityOtherText
                )
        }

        //profile = 1, document = 0
        else if ((this.custodyConsideration == "No" || !newFilesSelected) && filePath != "") {

            val fileURI = Uri.parse(filePath)
            val file = File(fileURI.path)
            val fileParts = MultipartBody.Part.createFormData(
                "profile_picture",
                file.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), file)
            )

            call =
                RetrofitClassNew.getInstance().webRequestsInstance.registerOrUpdateChildWithProfileImageNoDocument(
                    "Bearer " + userManager.accessToken ?: "",
                    registrarId,
                    fileParts,

                    if (childInfo != null) {
                        childInfo?.id ?: 0
                    } else {
                        null
                    },
                    fName,
                    middleName,
                    lastName,
                    nickName,
                    childCallYou,
                    gender,
                    dob,
                    primaryAddress,
                    apt,
                    state,
                    city,
                    zip_code,
                    secondaryAddress,
                    s_apt,
                    s_state,
                    s_city,
                    s_zip_code,
                    custody,
                    iep,
                    etSpecialNeedsConsideration,
                    epiPenExpiration,
                    comment,
                    iepDiagnosedBy,
                    doesHaveAnyAllergies,
                    fAllergies,
                    eAllergies,
                    doesChildRequireEpiPin,
                    custodyConsideration,
                    custodyDescription,
                    mondayOpening,
                    mondayClosing,
                    tuesdayOpening,
                    tuesdayClosing,
                    wednesdayOpening,
                    wednesdayClosing,
                    thursdayOpening,
                    thursdayClosing,
                    fridayOpening,
                    fridayClosing,
                    lat,
                    lng, ethnicity, ethnicityOtherText
                )
        }

        //profile = 0, document = 1
        else if (this.custodyConsideration == "Yes" && newFilesSelected && filePath == "") {

            call =
                RetrofitClassNew.getInstance().webRequestsInstance.registerOrUpdateChildWithDocument(
                    "Bearer " + userManager.accessToken ?: "",
                    registrarId,
                    documentParts,

                    if (childInfo != null) {
                        childInfo?.id ?: 0
                    } else {
                        null
                    },
                    fName,
                    middleName,
                    lastName,
                    nickName,
                    childCallYou,
                    gender,
                    dob,
                    primaryAddress,
                    apt,
                    state,
                    city,
                    zip_code,
                    secondaryAddress,
                    s_apt,
                    s_state,
                    s_city,
                    s_zip_code,
                    custody,
                    iep,
                    etSpecialNeedsConsideration,
                    epiPenExpiration,
                    comment,
                    iepDiagnosedBy,
                    doesHaveAnyAllergies,
                    fAllergies,
                    eAllergies,
                    doesChildRequireEpiPin,
                    custodyConsideration,
                    custodyDescription,
                    mondayOpening,
                    mondayClosing,
                    tuesdayOpening,
                    tuesdayClosing,
                    wednesdayOpening,
                    wednesdayClosing,
                    thursdayOpening,
                    thursdayClosing,
                    fridayOpening,
                    fridayClosing,
                    lat,
                    lng, ethnicity, ethnicityOtherText
                )
        }

        //profile = 0, document = 1
        else if (this.custodyConsideration == "Yes" && newFilesSelected && filePath != "") {

            val fileURI = Uri.parse(filePath)
            val file = File(fileURI.path)
            val fileParts = MultipartBody.Part.createFormData(
                "profile_picture",
                file.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), file)
            )

            call =
                RetrofitClassNew.getInstance().webRequestsInstance.registerOrUpdateChildWithDocumentAndProfile(
                    "Bearer " + userManager.accessToken ?: "",
                    registrarId,
                    fileParts,
                    documentParts,

                    if (childInfo != null) {
                        childInfo?.id ?: 0
                    } else {
                        null
                    },
                    fName,
                    middleName,
                    lastName,
                    nickName,
                    childCallYou,
                    gender,
                    dob,
                    primaryAddress,
                    apt,
                    state,
                    city,
                    zip_code,
                    secondaryAddress,
                    s_apt,
                    s_state,
                    s_city,
                    s_zip_code,
                    custody,
                    iep,
                    etSpecialNeedsConsideration,
                    epiPenExpiration,
                    comment,
                    iepDiagnosedBy,
                    doesHaveAnyAllergies,
                    fAllergies,
                    eAllergies,
                    doesChildRequireEpiPin,
                    custodyConsideration,
                    custodyDescription,
                    mondayOpening,
                    mondayClosing,
                    tuesdayOpening,
                    tuesdayClosing,
                    wednesdayOpening,
                    wednesdayClosing,
                    thursdayOpening,
                    thursdayClosing,
                    fridayOpening,
                    fridayClosing,
                    lat,
                    lng, ethnicity,
                    ethnicityOtherText
                )
        }

        call?.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                runOnUiThread {
                    hideLoading()
                }
                if (response.body()?.status == true) {
                    if (fromList) {
                        finish()
                    } else if (fromAddAnother) {
                        finishAffinity()
                        startActivity<RegistrationSuccessfulActivity>()
                    } else {
                        UserData.setUserLogin(this@AddChildActivity, true)
                        finishAffinity()
                        startActivity<ProviderMainActivity>()
                    }
                    showSuccessToast(this@AddChildActivity, response.body()?.message ?: "")
                    hideProgressBar()
                    finish()
                } else {

                    hideProgressBar()
                    try {
                        val jObjError = JSONObject(response.errorBody()!!.string())
                        showErrorToast(this@AddChildActivity, jObjError.getString("message"))
                    } catch (e: Exception) {
                        showErrorToast(this@AddChildActivity, response.body()?.message ?: "")
                    }
                }
                hideProgressBar()
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                hideProgressBar()
                t.printStackTrace()

                Log.i("errordetail", "${t.printStackTrace().toString()}")

                Toast.makeText(
                    this@AddChildActivity,
                    "Can't connect to server",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

    private fun showProgressBar() {
        binding.tvSubmit.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvSubmit.viewVisible()
        binding.progressbar.viewGone()
    }

    var lng = "";
    var lat = "";

    override fun invoke(p1: Place?, p2: Exception?) {

        try {

            if (MapCheck == 1) {

                Log.i("CodeSearch", "invoked")

                binding.etAddress.setText(p1!!.address + "")
                val name = p1?.name.toString()
                var address = p1?.address.toString()


                binding.etAddress.text = address + ""
                val geocoder = Geocoder(this, Locale.getDefault())

                lat = p1?.latLng?.latitude.toString()
                lng = p1?.latLng?.longitude.toString()

                val addresses: List<Address> =
                    geocoder.getFromLocation(p1?.latLng!!.latitude, p1.latLng!!.longitude, 1)
                val stateName: String = addresses[0].adminArea
                val cityName: String = addresses[0].locality
                val country = addresses[0].countryName

                binding.etState.setText(stateName)
                binding.etCity.setText(cityName)
                binding.etCountry.setText(country)

                binding.etZipCode.setText(
                    getZipCode(
                        this, p1?.latLng?.latitude!!,
                        p1.latLng?.longitude!!
                    )
                )

//        binding.tvAddress.setText("${p1?.address}")

                Log.i("PlacesInfo", "" + p1?.address)
                Log.i("PlacesInfo", "" + p1?.latLng?.latitude)
                Log.i("PlacesInfo", "" + p1?.latLng?.longitude)

            } else if (MapCheck == 2) {
                binding.etAddress2.setText(p1!!.address + "")
                val name = p1?.name.toString()
                var address = p1?.address.toString()


                binding.etAddress2.text = address + ""
                val geocoder = Geocoder(this, Locale.getDefault())

                lat = p1?.latLng?.latitude.toString()
                lng = p1?.latLng?.longitude.toString()

                val addresses: List<Address> =
                    geocoder.getFromLocation(p1?.latLng!!.latitude, p1.latLng!!.longitude, 1)
                val stateName2: String = addresses[0].adminArea
                val cityName2: String = addresses[0].locality
                val country = addresses[0].countryName

                binding.etState2.setText(stateName2)
                binding.etCity2.setText(cityName2)
//                binding.etCountry2.setText(country)

                binding.etZipCode2.setText(
                    getZipCode(
                        this, p1?.latLng?.latitude!!,
                        p1.latLng?.longitude!!
                    )
                )
//        binding.tvAddress.setText("${p1?.address}")

                Log.i("PlacesInfo", "" + p1?.address)
                Log.i("PlacesInfo", "" + p1?.latLng?.latitude)
                Log.i("PlacesInfo", "" + p1?.latLng?.longitude)
            }

            MapCheck = 0


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private fun validTime(start: String, end: String): Boolean {
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