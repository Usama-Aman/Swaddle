package com.android.swaddle.activities.parent

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.android.swaddle.R
import com.android.swaddle.activities.providers.LoginActivity
import com.android.swaddle.adapters.ethnicity_bottom_sheet_adapter.EthnicityList
import com.android.swaddle.adapters.spinneradapter.RelationsSpinnerAdapter
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityParentSignupBinding
import com.android.swaddle.fragments.bottomSheetFragment.EthnicityBottomSheet
import com.android.swaddle.helper.*
import com.android.swaddle.models.LoginData
import com.android.swaddle.models.UpdateProfileResponse
import com.android.swaddle.models.User
import com.android.swaddle.networkManager.NetworkURLs
import com.android.swaddle.payment_screens.AddPaymentMethodsActivity
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.maskeditor.getIdPlaceData
import com.android.swaddle.utils.maskeditor.getZipCode
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.codekidlabs.storagechooser.StorageChooser
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.theartofdev.edmodo.cropper.CropImage
import org.jetbrains.anko.startActivity
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ParentSignUpActivity : BaseActivity(), (Place?, Exception?) -> Unit {
    private lateinit var binding: ActivityParentSignupBinding
    var fileUri: Uri? = null
    var filePath = ""
    var gender = ""
    var relation = "Mother"

    val arrayList = ArrayList<String>()//Creating an empty arraylist
    var user: User? = null

    var ethnicityList = java.util.ArrayList<EthnicityList>()
    var selectedEthnicity: EthnicityList? = null
    var selectedEthnicityOtherText = ""
    var isAddressClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParentSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        user = intent.getSerializableExtra("item") as User?

        if (user != null) {
            binding.imgBack.viewVisible()
            binding.lnrSigninLabel.viewGone()
        } else {
            binding.imgBack.viewGone()
            binding.lnrSigninLabel.viewGone()
        }
        initVar()
        listener()
        setUpData()
        if (user != null) {
            binding.tvSubmit.text = "Update"
        } else if (userManager.user?.type == "authorized_adult") {
            binding.tvSubmit.text = "Register"
        } else {
            binding.tvSubmit.text = "Next Step (Add Children)"
        }

        if (user != null)
            setupUpdatingData()
    }

    private fun setUpRelationsSpinner() {
        arrayList.add("Mother")//Adding object in arraylist
        arrayList.add("Father")
        arrayList.add("Parent")
        arrayList.add("Guardian")
        arrayList.add("Family")
        arrayList.add("Friend")

        val adapter = RelationsSpinnerAdapter(this, arrayList)
        binding.spRelation.adapter = adapter
        binding.spRelation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                relation = arrayList[position]
                //  showSuccessToast(this@PaymentDetailActivity, "Click")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun setupUpdatingData() {
        binding.tvEthnicityText.text = user?.ethnicity
        selectedEthnicity = ethnicityList.find { it.text == user?.ethnicity }

        if (user?.ethnicity_others != null) {
            binding.tvEthnicityText.text = user?.ethnicity
            selectedEthnicity = ethnicityList.find { it.text == "Other" }
            binding.tvEthnicityOther.viewVisible()
            binding.cvEthnicityOther.viewVisible()
            binding.tvEthnicityTextOther.text = user?.ethnicity_others ?: ""
            selectedEthnicityOtherText = user?.ethnicity_others ?: ""
        }
        binding.etFirstname.setText(user?.firstName ?: "")
        binding.etLastName.setText(user?.lastName ?: "")
        binding.etMiddleName.setText(user?.middleName ?: "")
        binding.spRelation.setSelection(arrayList.indexOf(user?.relation ?: ""))
        binding.etAddress.setText(user?.homeAddress ?: "")
        binding.etCellPhone.setText(user?.phoneNumber ?: "")
        binding.etWorPlaceName.setText(user?.workplace ?: "")
        binding.etWorkAddress.setText(user?.workplaceAddress ?: "")
        binding.etWorkPhone.setText(user?.workplacePhoneNumber ?: "")

        Glide.with(this@ParentSignUpActivity)
            .load(Constants.IMG_BASE_PATH + user?.profilePicture)
            .placeholder(R.drawable.ic_user_placeholder).into(binding.ivProfile)

        when (user?.gender ?: "") {
            "Female" -> {
                binding.rbFemal.isChecked = true
                gender = "Female"
            }
            "Unspecified" -> {
                binding.rbOther.isChecked = true
                gender = "Unspecified"
            }
            "Male" -> {
                binding.rbMale.isChecked = true
                gender = "Male"
            }
        }


    }

    private val AUTOCOMPLETE_REQUEST_CODE = 12121

    private fun startSearchActivity() {
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
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }


    private fun listener() {
        binding.imgBack.setOnClickListener {
            finish()
        }
        //PhoneNumberUtils.formatNumber(binding.etCellPhone.text.toString(),"US")
        binding.etCellPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        binding.tvEthnicityText.setOnClickListener {
            ethnicityList.forEachIndexed { index, item ->
                if (item.text == binding.tvEthnicityText.text) {
                    ethnicityList[index].isChecked = true
                } else ethnicityList[index].isChecked =
                    item.text == "Other" && selectedEthnicityOtherText != ""
            }
            var sheet = EthnicityBottomSheet(
                this@ParentSignUpActivity,
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

        binding.etAddress.setOnClickListener {
            isAddressClick = true
            startSearchActivity()

        }
        binding.etWorkAddress.setOnClickListener {

            startSearchActivity()

        }

        binding.ivChangeProfilePic.setOnClickListener {
            selectImageWithCop()
        }

        binding.tvSubmit.setOnClickListener {
            if (validate()) {
                showProgressBar()
                callAPiToUpdate()
            }
        }

        binding.radioGroupGender.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == binding.rbFemal.id) {
                gender = "Female"
            }

            if (checkedId == binding.rbOther.id) {
                gender = "Unspecified"
            }

            if (checkedId == binding.rbMale.id) {
                gender = "Male"
            }
        }

    }

    private fun initVar() {
        ethnicityList.add(EthnicityList("Caucasian"))
        ethnicityList.add(EthnicityList("African American"))
        ethnicityList.add(EthnicityList("Native American"))
        ethnicityList.add(EthnicityList("Hispanic/Latino"))
        ethnicityList.add(EthnicityList("Asian"))
        ethnicityList.add(EthnicityList("South Asian"))
        ethnicityList.add(EthnicityList("Other"))

        binding.lnrSigninLabel.setOnClickListener {
            val intent = Intent(this@ParentSignUpActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }


    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (user != null) {
            super.onBackPressed()
            return
        }

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.myLooper()!!).postDelayed(
            Runnable { doubleBackToExitPressedOnce = false },
            2000
        )

    }

    private fun setUpData() {
//        if (userManager.email != null) {
        binding.etFirstname.setText(userManager.user?.firstName ?: "")
        binding.etLastName.setText(userManager.user?.lastName ?: "")
//        binding.etWorkEmail.setText(userManager.user?.email ?: "")
        binding.etEmailAddress.setText(userManager.user?.email ?: "")
//        }

        setUpRelationsSpinner()
    }

    private fun selectImageWithCop() {
        CropImage.activity()
            .start(this)
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
                Glide.with(this).load(filePath).into(binding.ivProfile)
            }
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            //   val error = result.error
        }

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
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
                isAddressClick = false
                val status = Autocomplete.getStatusFromIntent(data!!)
                assert(status.statusMessage != null)
                Log.v(TAG, status.statusMessage!!)
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                isAddressClick = false
            }
        }

    }

    private fun showProgressBar() {
        binding.tvSubmit.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvSubmit.viewVisible()
        binding.progressbar.viewGone()
    }

    private fun callAPiToUpdate() {

        var api = AndroidNetworking.upload(Constants.SERVER_ADDRESS_NEW + "update_user_profile")

        if (filePath != "") {
            val fileURI = Uri.parse(filePath)
            val file = File(fileURI.path ?: "")
            api.addMultipartFile("profile_picture", file)
        }
        api.addHeaders("Authorization", "Bearer " + userManager.accessToken ?: "")
            .addMultipartParameter("_method", "PUT")
            .addMultipartParameter("first_name", binding.etFirstname.text.toString().trim())
            .addMultipartParameter("middle_name", binding.etMiddleName.text.toString().trim())
            .addMultipartParameter("last_name", binding.etLastName.text.toString().trim())
            .addMultipartParameter("relation", relation.trim())
            .addMultipartParameter("gender", gender.trim())
            .addMultipartParameter("home_address", binding.etAddress.text.toString().trim())
            .addMultipartParameter("phone_number", binding.etCellPhone.text.toString().trim())
            .addMultipartParameter("ethnicity", selectedEthnicity?.text)
            .addMultipartParameter("ethnicity_others", selectedEthnicityOtherText)
            .addMultipartParameter("workplace", binding.etWorPlaceName.text.toString().trim())
            .addMultipartParameter(
                "workplace_address",
                binding.etWorkAddress.text.toString().trim()
            )
            .addMultipartParameter(
                "workplace_phone_number",
                binding.etWorkPhone.text.toString().trim()
            )

        api.setTag("update_user_profile")
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { bytesUploaded, totalBytes ->

            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.i("Response", response.toString())

                    var data = gson.fromJson(response.toString(), UpdateProfileResponse::class.java)
                    if (data.status == true) {

                        var loginData = userManager
                        loginData.user = data.user
                        UserData.saveUserData(loginData, this@ParentSignUpActivity)
                        showSuccessToast(this@ParentSignUpActivity, data.message ?: "")
                        UserData.setUserLogin(this@ParentSignUpActivity, true)
                        if (user != null) {
                            finish()
                        } else {
                            if (loginData.user?.type == "authorized_adult") {
                                startActivity<GetCallYouWithChildForAuthorizedAdults>()
                            } else {
                                startActivity<AddChildActivity>()
                            }
                            finishAffinity()
                        }
                        // do anything with response
                        hideProgressBar()
                    } else {
                        showErrorToast(this@ParentSignUpActivity, data.message ?: "")
                    }
                }

                override fun onError(error: ANError) {
                    // handle error
                    hideProgressBar()
                    error.printStackTrace()
                    showErrorToast(this@ParentSignUpActivity, error.message ?: "")
                }
            })
    }

    private fun validate(): Boolean {

        if (!isNetworkConnected()) {
            showErrorToast(this@ParentSignUpActivity, "No internet connection!")
            return false
        }

        if (binding.etFirstname.text.toString().trim().isEmpty()) {
            binding.etFirstname.requestFocus()
            showErrorToast(this@ParentSignUpActivity, "Please enter First Name!")
            return false
        }

//        if (binding.etMiddleName.text.toString().trim().isEmpty()) {
//            showErrorToast(this@ParentSignUpActivity, "Please enter Middle Name!")
//            return false
//        }

        if (binding.etLastName.text.toString().trim().isEmpty()) {
            binding.etLastName.requestFocus()
            showErrorToast(this@ParentSignUpActivity, "Please enter Last Name!")
            return false
        }
        if (relation.trim().isEmpty()) {
            binding.spRelation.requestFocus()
            showErrorToast(this@ParentSignUpActivity, "Please Selection Relation!")
            return false
        }

        if (selectedEthnicity == null) {
            showErrorToast(this@ParentSignUpActivity, "Please select Ethnicity!")
            return false
        }

        if (gender.trim().isEmpty()) {
            binding.radioGroupGender.requestFocus()
            showErrorToast(this@ParentSignUpActivity, "Please select your Gender!")
            return false
        }
        if (binding.etAddress.text.toString().trim().isEmpty()) {
            binding.etAddress.requestFocus()
            showErrorToast(this@ParentSignUpActivity, "Please enter your Address!")
            return false
        }
        if (binding.etCellPhone.text.toString().trim().isEmpty()) {
            binding.etCellPhone.requestFocus()
            showErrorToast(this@ParentSignUpActivity, "Please enter your Cell Phone!")
            return false
        }
        if (binding.etWorPlaceName.text.toString().trim().isEmpty()) {
            binding.etWorPlaceName.requestFocus()
            showErrorToast(this@ParentSignUpActivity, "Please enter your WorkPlace name!")
            return false
        }
        if (binding.etWorkAddress.text.toString().trim().isEmpty()) {
            binding.etWorkAddress.requestFocus()
            showErrorToast(this@ParentSignUpActivity, "Please enter your Work Address!")
            return false
        }
        if (binding.etWorkPhone.text.toString().trim().isEmpty()) {
            binding.etWorkPhone.requestFocus()
            showErrorToast(this@ParentSignUpActivity, "Please enter your Work Phone!")
            return false
        }

        if (filePath.isEmpty() && user == null) {
            showErrorToast(this@ParentSignUpActivity, "Please select Display Picture!")
            return false
        }

//        if (binding.etWorkEmail.text.toString().trim().isEmpty()) {
//            showErrorToast(this@ParentSignUpActivity, "Please enter your First Work Email!")
//            return false
//        }

//        if (binding.etWorkEmail2.text.toString().trim().isEmpty()) {
//            showErrorToast(this@ParentSignUpActivity, "Please enter your Second Work Email!")
//            return false
//        }

//        if (binding.etDesc.text.toString().trim().isEmpty()) {
//            showErrorToast(this@ParentSignupActivity, "Please enter your Description!")
//            return false
//        }

        return true
    }


    var lng = "";
    var lat = "";

    override fun invoke(p1: Place?, p2: Exception?) {

        if(isAddressClick) {
            binding.etAddress.setText(p1!!.address + "")
            val name = p1?.name.toString()
            var address = p1?.address.toString()
//

            binding.etAddress.text = address + ""
            val geocoder = Geocoder(this, Locale.getDefault())

            lat = p1?.latLng?.latitude.toString()
            lng = p1?.latLng?.longitude.toString()

            try {
                val addresses: List<Address> =
                    geocoder.getFromLocation(p1?.latLng!!.latitude, p1.latLng!!.longitude, 1)
                val stateName: String = addresses[0].adminArea
                val cityName: String = addresses[0].locality
                val country = addresses[0].countryName

//            binding.etState.setText(stateName)
//            binding.etCity.setText(cityName)
//            binding.etCountry.setText(country)
//
//            binding.etZipCode.setText(
//                getZipCode(
//                    this, p1?.latLng?.latitude!!,
//                    p1.latLng?.longitude!!
//                )
//            )


            } catch (e: Exception) {
                e.printStackTrace()
            }

//        binding.tvAddress.setText("${p1?.address}")

            Log.i("PlacesInfo", "" + p1?.address)
            Log.i("PlacesInfo", "" + p1?.latLng?.latitude)
            Log.i("PlacesInfo", "" + p1?.latLng?.longitude)

            binding.etAddress.setText("${p1?.address}")

        }
        else{
            binding.etWorkAddress.setText(p1!!.address + "")
            val name = p1?.name.toString()
            var address = p1?.address.toString()
//

            binding.etWorkAddress.text = address + ""
            val geocoder = Geocoder(this, Locale.getDefault())

            //lat = p1?.latLng?.latitude.toString()
            //lng = p1?.latLng?.longitude.toString()

            try {
                val addresses: List<Address> =
                    geocoder.getFromLocation(p1?.latLng!!.latitude, p1.latLng!!.longitude, 1)
                val stateName: String = addresses[0].adminArea
                val cityName: String = addresses[0].locality
                val country = addresses[0].countryName

//            binding.etState.setText(stateName)
//            binding.etCity.setText(cityName)
//            binding.etCountry.setText(country)
//
//            binding.etZipCode.setText(
//                getZipCode(
//                    this, p1?.latLng?.latitude!!,
//                    p1.latLng?.longitude!!
//                )
//            )


            } catch (e: Exception) {
                e.printStackTrace()
            }

//        binding.tvAddress.setText("${p1?.address}")

            Log.i("PlacesInfo", "" + p1?.address)
            Log.i("PlacesInfo", "" + p1?.latLng?.latitude)
            Log.i("PlacesInfo", "" + p1?.latLng?.longitude)

            binding.etWorkAddress.setText("${p1?.address}")

        }

        isAddressClick = false
    }

}