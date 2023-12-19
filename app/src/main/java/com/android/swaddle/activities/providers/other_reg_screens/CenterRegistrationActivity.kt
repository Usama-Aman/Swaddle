package com.android.swaddle.activities.providers.other_reg_screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.swaddle.R
import com.android.swaddle.activities.parent.ParentSignUpActivity
import com.android.swaddle.activities.providers.ProviderMainActivity
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityCenterRegistrationBinding
import com.android.swaddle.databinding.ActivityProviderRegistrationBinding
import com.android.swaddle.helper.*
import com.android.swaddle.models.CenterInfo
import com.android.swaddle.models.CenterInfoResponse
import com.android.swaddle.models.LoginData
import com.android.swaddle.models.UpdateProfileResponse
import com.android.swaddle.networkManager.NetworkURLs
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.payment_screens.AddPaymentMethodsActivity
import com.android.swaddle.prefrences.UserData
import com.android.swaddle.utils.Constants
import com.android.swaddle.utils.CustomToasts
import com.android.swaddle.utils.DateConverter
import com.android.swaddle.utils.maskeditor.MaskTextWatcher
import com.android.swaddle.utils.maskeditor.getIdPlaceData
import com.android.swaddle.utils.maskeditor.getZipCode
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.theartofdev.edmodo.cropper.CropImage
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
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
import java.io.IOException
import java.util.*

class CenterRegistrationActivity : BaseActivity(), OnMapReadyCallback,
        (Place?, Exception?) -> Unit {

    private lateinit var binding: ActivityCenterRegistrationBinding
    var fileUri: Uri? = null
    var filePath = ""
    var item: CenterInfo? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCenterRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        item = intent?.getSerializableExtra("item") as CenterInfo?
        initListeners()
        if (item != null) {
            setData()
        }
    }

    override fun onResume() {
        val mapFragment: SupportMapFragment? = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this@CenterRegistrationActivity)
        super.onResume()
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

    private fun setData() {
        Glide.with(this@CenterRegistrationActivity)
            .load(Constants.IMG_BASE_PATH + item?.centerLogo)
            .placeholder(R.drawable.ic_user_placeholder_new).into(binding.ivProfile)

        binding.etName.setText(item?.centerName ?: "")
        binding.etLocation.setText(item?.location ?: "")
        binding.etPhone.setText(
            item?.phoneNumber
                ?: ""
        )

        binding.etEmail.setText(item?.email ?: "")
        binding.etEmail.isEnabled = false
        binding.etWebsite.setText(item?.website ?: "")

        if (item?.monday == "[null,null]") {
            binding.tvOpeningTimeMon.text = ""
            binding.tvClosingTimeMon.text = ""
            binding.cbMon.isChecked = false
        } else {
            var time = JSONArray(item?.monday)
            binding.tvOpeningTimeMon.text = "${time[0]}"
            binding.tvClosingTimeMon.text = "${time[1]}"
            binding.cbMon.isChecked = true
        }

        if (item?.tuesday == "[null,null]") {
            binding.tvOpeningTimeTue.text = ""
            binding.tvClosingTimeTue.text = ""
            binding.cbTue.isChecked = false
        } else {
            var time = JSONArray(item?.tuesday)
            binding.tvOpeningTimeTue.text = "${time[0]}"
            binding.tvClosingTimeTue.text = "${time[1]}"
            binding.cbTue.isChecked = true
        }

        if (item?.wednesday == "[null,null]") {
            binding.tvOpeningTimeWed.text = ""
            binding.tvClosingTimeWed.text = ""
            binding.cbWed.isChecked = false
        } else {
            var time = JSONArray(item?.wednesday)
            binding.tvOpeningTimeWed.text = "${time[0]}"
            binding.tvClosingTimeWed.text = "${time[1]}"
            binding.cbWed.isChecked = true
        }


        if (item?.thursday == "[null,null]") {
            binding.tvOpeningTimeThu.text = ""
            binding.tvClosingTimeThu.text = ""
            binding.cbThu.isChecked = false
        } else {
            var time = JSONArray(item?.thursday)
            binding.tvOpeningTimeThu.text = "${time[0]}"
            binding.tvClosingTimeThu.text = "${time[1]}"
            binding.cbThu.isChecked = true
        }

        if (item?.friday == "[null,null]") {
            binding.tvOpeningTimeFri.text = ""
            binding.tvClosingTimeFri.text = ""
            binding.cbFri.isChecked = false
        } else {
            var time = JSONArray(item?.friday)
            binding.tvOpeningTimeFri.text = "${time[0]}"
            binding.tvClosingTimeFri.text = "${time[1]}"
            binding.cbFri.isChecked = true
        }

        if (item?.saturday == "[null,null]") {
            binding.tvOpeningTimeSat.text = ""
            binding.tvClosingTimeSat.text = ""
            binding.cbSat.isChecked = false
        } else {
            var time = JSONArray(item?.saturday)
            binding.tvOpeningTimeSat.text = "${time[0]}"
            binding.tvClosingTimeSat.text = "${time[1]}"
            binding.cbSat.isChecked = true
        }

        if (item?.sunday == "[null,null]") {
            binding.tvOpeningTimeSun.text = ""
            binding.tvClosingTimeSun.text = ""
            binding.cbSun.isChecked = false
        } else {
            var time = JSONArray(item?.sunday)
            binding.tvOpeningTimeSun.text = "${time[0]}"
            binding.tvClosingTimeSun.text = "${time[1]}"
            binding.cbSun.isChecked = true
        }
        val mapFragment: SupportMapFragment? = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this@CenterRegistrationActivity)
    }

    private fun initListeners() {

        val textWatcherNumber = MaskTextWatcher(binding.etPhone, "(###) ###-####")
        binding.etPhone.addTextChangedListener(textWatcherNumber)

        binding.ivChangeProfilePic.setOnClickListener {
            selectImageWithCop()
        }

        binding.etLocation.setOnClickListener {

            startSearchActivity()

        }

        binding.btnSubmit.setOnClickListener {
            if (validate()) {
                showProgressBar()
//                callAPiToUpdate()
                sendToServer(filePath)
            }
        }

        binding.tvOpeningTimeMon.setOnClickListener {
            showClock(selectedTime = binding.tvOpeningTimeMon.text.trim().toString()) {
                binding.tvOpeningTimeMon.text = it
                binding.tvClosingTimeMon.text = ""
            }
        }

        binding.tvClosingTimeMon.setOnClickListener {
            if (validateTimeSelection(binding.tvOpeningTimeMon))
                showClock(
                    setMinTime = binding.tvOpeningTimeMon.text.toString(),
                    selectedTime = binding.tvClosingTimeMon.text.trim().toString()
                ) {
                    if (validTime(binding.tvOpeningTimeTue.text.toString(), it)) {
                        binding.tvClosingTimeTue.text = it
                    } else {
                        binding.tvClosingTimeTue.text = ""
                        showErrorToast(this, "To time should be greater than From time.")
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

        binding.tvOpeningTimeSat.setOnClickListener {
            showClock(selectedTime = binding.tvOpeningTimeSat.text.trim().toString()) {
                binding.tvOpeningTimeSat.text = it
                binding.tvClosingTimeSat.text = ""
            }
        }

        binding.tvClosingTimeSat.setOnClickListener {
            if (validateTimeSelection(binding.tvOpeningTimeSat))
                showClock(
                    binding.tvOpeningTimeSat.text.toString(),
                    selectedTime = binding.tvClosingTimeSat.text.trim().toString()
                ) {
                    if (validTime(binding.tvOpeningTimeSat.text.toString(), it)) {
                        binding.tvClosingTimeSat.text = it
                    } else {
                        binding.tvClosingTimeSat.text = ""
                        showErrorToast(this, "To time should be greater than From time.")
                    }
                }
        }

        binding.tvOpeningTimeSun.setOnClickListener {
            showClock(selectedTime = binding.tvOpeningTimeSun.text.trim().toString()) {
                binding.tvOpeningTimeSun.text = it
                binding.tvClosingTimeSun.text = ""
            }
        }

        binding.tvClosingTimeSun.setOnClickListener {
            if (validateTimeSelection(binding.tvOpeningTimeSun))
                showClock(
                    binding.tvOpeningTimeSun.text.toString(),
                    selectedTime = binding.tvClosingTimeSun.text.trim().toString()
                ) {
                    if (validTime(binding.tvOpeningTimeSun.text.toString(), it)) {
                        binding.tvClosingTimeSun.text = it
                    } else {
                        binding.tvClosingTimeSun.text = ""
                        showErrorToast(this, "To time should be greater than From time.")
                    }
                }
        }
    }

    private fun validateTimeSelection(tvOpeningTime: TextView): Boolean {
        if (tvOpeningTime.text.toString().trim().isEmpty()) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Please select opening time first!"
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
    }

//    private fun showClock(
//        setMinTime: String = "",
//        selectedTime: String,
//        callback: (String) -> Unit
//    ) {
//        val calendar = Calendar.getInstance()
//        var miniTimeCalendar = Calendar.getInstance()
//
//        if (setMinTime != "") {
//            miniTimeCalendar.time = DateConverter.convertTimeForTimePicker(setMinTime)
//        }
//        if (selectedTime != "") {
//            calendar.time = DateConverter.convertTimeForTimePicker(selectedTime)
//        }
//        val dpd = TimePickerDialog.newInstance(
//            { _, hourOfDay, minute, second ->
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
//    }

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
                val status = Autocomplete.getStatusFromIntent(data!!)
                assert(status.statusMessage != null)
                Log.v(TAG, status.statusMessage!!)
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
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

    private fun validate(): Boolean {
        if (!isNetworkConnected()) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "No internet connection!"
            )
            return false
        }

        if (binding.etName.text.toString().trim().isEmpty()) {
            showErrorToast(this@CenterRegistrationActivity, "Please enter Center Name!")
            return false
        }

        if (binding.etEmail.text.toString().trim().isEmpty()) {
            showErrorToast(this@CenterRegistrationActivity, "Please enter Center Email!")
            return false
        }

        if (!binding.etEmail.text.toString().trim().isValidEmail()) {
            showErrorToast(this@CenterRegistrationActivity, "Please enter valid Center Email!")
            return false
        }

        if (binding.etWebsite.text.toString().trim().isEmpty()) {
            showErrorToast(this@CenterRegistrationActivity, "Please enter your Website!")
            return false
        }
        if (binding.etWebsite.text.toString().trim().isEmpty()){
            return true
        }
        else if (binding.etWebsite.text.toString().trim().isNotEmpty()) {
            if (!Patterns.WEB_URL.matcher(binding.etWebsite.text.toString().trim()).matches()) {
                showErrorToast(this@CenterRegistrationActivity, "Please enter a valid website link")
                return false
            }
        }

        if (binding.etLocation.text.toString().trim().isEmpty()) {
            showErrorToast(this@CenterRegistrationActivity, "Please enter Center Location!")
            return false
        }
        if (binding.etPhone.text.toString().trim().isEmpty()) {
            showErrorToast(this@CenterRegistrationActivity, "Please enter Center Phone!")
            return false
        }

//        if (item != null) {
//
//        } else {
//
//        }

        if (filePath.isEmpty() && item?.centerLogo == null) {
            showErrorToast(this@CenterRegistrationActivity, "Please select Display Picture!")
            return false
        }

//        if (llMondayOn.visibility == View.VISIBLE && tvOpeningTimeMon.text.isNullOrEmpty()) {
//            CustomToasts.failureToast(
//                this@CenterRegistrationActivity,
//                "Select opening time for Monday or set it day off"
//            )
//            return false
//        }
        val tvOpeningTimeMon = binding.tvOpeningTimeMon.text.toString()
        val tvClosingTimeMon = binding.tvClosingTimeMon.text.toString()
        if (binding.cbMon.isChecked && (tvClosingTimeMon.isNullOrEmpty() || tvOpeningTimeMon.isNullOrEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper opening or closing time for Monday or set it day off"
            )
            return false
        } else if (binding.cbMon.isChecked && (tvClosingTimeMon == "null" && tvOpeningTimeMon.isNotEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper closing time for Monday or set it day off!"
            )
            return false
        }

//        if (llTuesdayOn.visibility == View.VISIBLE && tvOpeningTimeTue.text.isNullOrEmpty()) {
//            CustomToasts.failureToast(
//                this@CenterRegistrationActivity,
//                "Select opening time for Tuesday or set it day off"
//            )
//        }
        if (binding.cbTue.isChecked && (binding.tvClosingTimeTue.text.isNullOrEmpty() || binding.tvOpeningTimeTue.text.isNullOrEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper opening or closing time for Tuesday or set it day off"
            )
            return false
        }

        if (binding.cbTue.isChecked && (binding.tvClosingTimeTue.text == "null" && binding.tvOpeningTimeTue.text.isNotEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper closing time for Tuesday or set it day off"
            )
            return false
        }
//        if (llWednesdayOn.visibility == View.VISIBLE && tvOpeningTimeWed.text.isNullOrEmpty()) {
//            CustomToasts.failureToast(
//                this@CenterRegistrationActivity,
//                "Select opening time for Wednesday or set it day off"
//            )
//            return false
//        }

        if (binding.cbWed.isChecked && (binding.tvClosingTimeWed.text.isNullOrEmpty() || binding.tvOpeningTimeWed.text.isNullOrEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper opening or closing time for Wednesday or set it day off"
            )
            return false
        }

        if (binding.cbWed.isChecked && (binding.tvClosingTimeWed.text == "null" && binding.tvOpeningTimeWed.text.isNotEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper closing time for Wednesday or set it day off"
            )
            return false
        }

//        if (llThursdayOn.visibility == View.VISIBLE && tvOpeningTimeThu.text.isNullOrEmpty()) {
//            CustomToasts.failureToast(
//                this@CenterRegistrationActivity,
//                "Select opening time for Thursday or set it day off"
//            )
//            return false
//        }
        val tvClosingTimeThu = binding.tvClosingTimeThu.text
        val tvOpeningTimeThu = binding.tvOpeningTimeThu.text
        if (binding.cbThu.isChecked && (tvClosingTimeThu.isNullOrEmpty() || tvOpeningTimeThu.isNullOrEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper opening or closing time for Thursday or set it day off"
            )
            return false
        }

        if (binding.cbThu.isChecked && (tvClosingTimeThu == "null" && tvOpeningTimeThu.isNotEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper closing time for Thursday or set it day off"
            )
            return false
        }
//        if (llFridayOn.visibility == View.VISIBLE && tvOpeningTimeFri.text.isNullOrEmpty()) {
//            CustomToasts.failureToast(
//                this@CenterRegistrationActivity,
//                "Select opening time for Friday or set it day off"
//            )
//            return false
//        }
        if (binding.cbFri.isChecked && (binding.tvClosingTimeFri.text.isNullOrEmpty() || binding.tvOpeningTimeFri.text.isNullOrEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper opening or closing time for Friday or set it day off"
            )
            return false
        }

        if (binding.cbFri.isChecked && (binding.tvClosingTimeFri.text == "null" && binding.tvOpeningTimeFri.text.isNotEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper closing time for Friday or set it day off"
            )
            return false
        }
//        if (llSaturdayOn.visibility == View.VISIBLE && tvOpeningTimeSat.text.isNullOrEmpty()) {
//            CustomToasts.failureToast(
//                this@CenterRegistrationActivity,
//                "Select opening time for Saturday or set it day off"
//            )
//            return false
//        }
        if (binding.cbSat.isChecked && (binding.tvClosingTimeSat.text.isNullOrEmpty() || binding.tvOpeningTimeSat.text.isNullOrBlank())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper opening or closing time for Saturday or set it day off"
            )
            return false
        }

        if (binding.cbSat.isChecked && (binding.tvClosingTimeSat.text == "null" && binding.tvOpeningTimeSat.text.isNotEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper closing time for Saturday or set it day off"
            )
            return false
        }
//        if (llSundayOn.visibility == View.VISIBLE && tvOpeningTimeSun.text.isNullOrEmpty()) {
//            CustomToasts.failureToast(
//                this@CenterRegistrationActivity,
//                "Select opening time for Sunday or set it day off"
//            )
//            return false
//        }
        if (binding.cbSun.isChecked && (binding.tvClosingTimeSun.text.isNullOrEmpty() || binding.tvOpeningTimeSun.text.isNullOrEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper opening or closing time for Sunday or set it day off"
            )
            return false
        }

        if (binding.cbSun.isChecked && (binding.tvClosingTimeSun.text == "null" && binding.tvOpeningTimeSun.text.isNotEmpty())) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select proper closing time for Sunday or set it day off"
            )
            return false
        }

        if (binding.tvOpeningTimeMon.text.isNullOrEmpty() && binding.tvOpeningTimeTue.text.isNullOrEmpty() && binding.tvOpeningTimeWed.text.isNullOrEmpty() && binding.tvOpeningTimeThu.text.isNullOrEmpty() && binding.tvOpeningTimeFri.text.isNullOrEmpty()) {
            CustomToasts.failureToast(
                this@CenterRegistrationActivity,
                "Select Opening and Closing time for at least single day!"
            )
            return false
        }
        return true
    }

    private fun callAPiToUpdate() {
        var api = AndroidNetworking.upload(Constants.SERVER_ADDRESS_NEW + "add_or_update_center")

        if (filePath != "") {
            val fileURI = Uri.parse(filePath)
            val file = File(fileURI.path ?: "")
            api.addMultipartFile("center_logo", file)
//            api.addFileBody(file)
        }
        api.addHeaders("Authorization", "Bearer " + userManager.accessToken ?: "")
        if (item != null) {
            api.addMultipartParameter("center_id", (item?.id ?: 0).toString().trim())
        }
        api.addMultipartParameter("center_name", binding.etName.text.toString().trim())
            .addMultipartParameter("email", binding.etEmail.text.toString().trim())
            .addMultipartParameter("location", binding.etLocation.text.toString().trim())
            .addMultipartParameter("website", binding.etWebsite.text.toString().trim())
            .addMultipartParameter("phone_number", binding.etPhone.text.toString().trim())
            .addMultipartParameter("monday[]", binding.tvOpeningTimeMon.text.toString().trim())
            .addMultipartParameter("monday[]", binding.tvClosingTimeMon.text.toString().trim())
            .addMultipartParameter("tuesday[]", binding.tvOpeningTimeTue.text.toString().trim())
            .addMultipartParameter("tuesday[]", binding.tvClosingTimeTue.text.toString().trim())
            .addMultipartParameter("wednesday[]", binding.tvOpeningTimeWed.text.toString().trim())
            .addMultipartParameter("wednesday[]", binding.tvClosingTimeWed.text.toString().trim())
            .addMultipartParameter("thursday[]", binding.tvOpeningTimeThu.text.toString().trim())
            .addMultipartParameter("thursday[]", binding.tvClosingTimeThu.text.toString().trim())
            .addMultipartParameter("friday[]", binding.tvOpeningTimeFri.text.toString().trim())
            .addMultipartParameter("friday[]", binding.tvClosingTimeFri.text.toString().trim())
            .addMultipartParameter("saturday[]", binding.tvOpeningTimeSat.text.toString().trim())
            .addMultipartParameter("saturday[]", binding.tvClosingTimeSat.text.toString().trim())
            .addMultipartParameter("sunday[]", binding.tvOpeningTimeSun.text.toString().trim())
            .addMultipartParameter("sunday[]", binding.tvClosingTimeSun.text.toString().trim())
        api.setTag("update_center_info")
            .setPriority(Priority.HIGH)
            .build()
            .setUploadProgressListener { _, _ ->

            }
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("update_center_info", response.toString())

                    var data = gson.fromJson(response.toString(), CenterInfoResponse::class.java)
                    if (data.status == true) {
                        showSuccessToast(this@CenterRegistrationActivity, data.message ?: "")
                        UserData.setUserLogin(this@CenterRegistrationActivity, true)
                        finishAffinity()
                        startActivity<ProviderMainActivity>()
                    } else {
                        showErrorToast(this@CenterRegistrationActivity, data.message ?: "")
                    }
                    // do anything with response
                    hideProgressBar()
                }

                override fun onError(error: ANError) {
                    // handle error
                    hideProgressBar()
                    error.printStackTrace()
                    showErrorToast(this@CenterRegistrationActivity, error.message ?: "")
                }
            })
    }


    private fun sendToServer(
        filePath: String
    ) {

        showProgressBar()
        var centerId: RequestBody? = null
        if (item != null) {
            centerId = RequestBody.create(
                "multipart/form-data".toMediaTypeOrNull(),
                (item?.id ?: 0).toString().trim(),
            )
        }
        val centerName: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etName.text.toString().trim(),
        )
        val email: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etEmail.text.toString().trim(),
        )
        val website: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etWebsite.text.toString().trim(),
        )
        val location: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),

            binding.etLocation.text.toString().trim(),
        )

        val phoneNo: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            binding.etPhone.text.toString().trim(),
        )

        val mondayOpening: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbMon.isChecked) {
                binding.tvOpeningTimeMon.text.toString().trim()
            } else {
                ""
            },
        )

        val mondayClosing: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbMon.isChecked) {
                binding.tvClosingTimeMon.text.toString().trim()
            } else {
                ""
            }
        )

        val tuesdayOpening: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbTue.isChecked) {
                binding.tvOpeningTimeTue.text.toString().trim()
            } else {
                ""
            }

        )

        val tuesdayClosing: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbTue.isChecked) {
                binding.tvClosingTimeTue.text.toString().trim()
            } else {
                ""
            }
        )

        val wednesdayOpening: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbWed.isChecked) {
                binding.tvOpeningTimeWed.text.toString().trim()
            } else {
                ""
            }
        )

        val wednesdayClosing: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbWed.isChecked) {
                binding.tvClosingTimeWed.text.toString().trim()
            } else {
                ""
            }
        )

        val thursdayOpening: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbThu.isChecked) {
                binding.tvOpeningTimeThu.text.toString().trim()
            } else {
                ""
            }
        )

        val thursdayClosing: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbThu.isChecked) {
                binding.tvClosingTimeThu.text.toString().trim()
            } else {
                ""
            }
        )

        val fridayOpening: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbFri.isChecked) {
                binding.tvOpeningTimeFri.text.toString().trim()
            } else {
                ""
            }
        )

        val fridayClosing: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbFri.isChecked) {
                binding.tvClosingTimeFri.text.toString().trim()
            } else {
                ""
            },
        )

        val saturdayOpening: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbSat.isChecked) {
                binding.tvOpeningTimeSat.text.toString().trim()
            } else {
                ""
            },
        )

        val saturdayClosing: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbSat.isChecked) {
                binding.tvClosingTimeSat.text.toString().trim()
            } else {
                ""
            },
        )

        if (binding.cbSat.isChecked) {
            binding.tvOpeningTimeSat.text.toString().trim()
        } else {
            ""
        }

        val sundayOpening: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbSun.isChecked) {
                binding.tvOpeningTimeSun.text.toString().trim()
            } else {
                ""
            },
        )
        val sundayClosing: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            if (binding.cbSun.isChecked) {
                binding.tvClosingTimeSun.text.toString().trim()
            } else {
                ""
            },
        )


        val lat: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            lat,
        )

        val lng: RequestBody = RequestBody.create(
            "multipart/form-data".toMediaTypeOrNull(),
            lng,
        )

        var call: Call<UpdateProfileResponse>? = null
        if (filePath != "") {
            val fileURI = Uri.parse(filePath)
            val file = File(fileURI.path)
            val fileParts = MultipartBody.Part.createFormData(
                "center_logo",
                file.name,
                RequestBody.create("image/*".toMediaTypeOrNull(), file)
            )
            call =
                RetrofitClass.getInstance().webRequestsInstance.registerOrUpdateCenter(
                    "Bearer " + userManager.accessToken ?: "",
                    centerId,
                    centerName,
                    email,
                    location,
                    phoneNo,
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
                    saturdayOpening, saturdayClosing, sundayOpening, sundayClosing, fileParts,
                    lat, lng, website
                )
        } else {
            call =
                RetrofitClass.getInstance().webRequestsInstance.registerOrUpdateCenterWithoutImage(
                    "Bearer " + userManager.accessToken ?: "",
                    centerId,
                    centerName,
                    email,
                    location,
                    phoneNo,
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
                    saturdayOpening, saturdayClosing, sundayOpening, sundayClosing,
                    lat, lng, website
                )
        }

        call?.enqueue(object : Callback<UpdateProfileResponse> {
            override fun onResponse(
                call: Call<UpdateProfileResponse>,
                response: Response<UpdateProfileResponse>
            ) {
                if (response.isSuccessful) {
                    if (response.body()?.status == true) {
                        showSuccessToast(
                            this@CenterRegistrationActivity,
                            response.body()?.message ?: ""
                        )
                        UserData.setUserLogin(this@CenterRegistrationActivity, true)
                        finishAffinity()
                        startActivity<ProviderMainActivity>()
                    } else {
                        try {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            showErrorToast(
                                this@CenterRegistrationActivity,
                                jObjError.getString("message")
                            )
                        } catch (e: Exception) {
                            showErrorToast(
                                this@CenterRegistrationActivity,
                                response.body()?.message ?: ""
                            )
                        }
                    }
                } else {
                    showErrorToast(
                        this@CenterRegistrationActivity,
                        "Internal Server Error!",
                    )
                }
                hideProgressBar()
            }

            override fun onFailure(
                call: Call<UpdateProfileResponse>,
                t: Throwable
            ) {
                hideProgressBar()
                t.printStackTrace()
                showErrorToast(
                    this@CenterRegistrationActivity,
                    "Can't connect to server"
                )
            }
        })
    }

    var lng = "";
    var lat = "";

    override fun invoke(p1: Place?, p2: Exception?) {

        val name = p1?.name.toString()

        binding.etLocation.text = p1!!.address + ""
        val geocoder = Geocoder(this, Locale.getDefault())

        lat = p1.latLng?.latitude.toString()
        lng = p1.latLng?.longitude.toString()

        try {
            val addresses: List<Address>? =
                geocoder.getFromLocation(p1.latLng!!.latitude, p1.latLng!!.longitude, 1)
            val stateName: String = addresses?.first()?.adminArea ?: ""
            val cityName: String = addresses?.first()?.locality ?: ""
            val country = addresses?.first()?.countryName ?: ""

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


        } catch (e: IOException) {
            e.printStackTrace()
        }

//        binding.tvAddress.setText("${p1?.address}")

        Log.i("PlacesInfo", "" + p1?.address)
        Log.i("PlacesInfo", "" + p1?.latLng?.latitude)
        Log.i("PlacesInfo", "" + p1?.latLng?.longitude)

//        binding.etAddress.setText("${p1?.address}")
    }

    var googleMap: GoogleMap? = null


    override fun onMapReady(p0: GoogleMap?) {
        this.googleMap = p0


//        googleMap?.apply {
//            val sydney = LatLng(-33.852, 151.211)
////            addMarker(
////                MarkerOptions()
////                    .position(sydney)
////                    .title("Marker in Sydney")
////            )
//        }

        if (item?.lat != null) {

            Log.i("jmap", "map ready : ${item?.lng!!} , ${item?.lng!!}")
            var TutorialsPoint =
                LatLng(item?.lat?.toDouble()!!, item?.lng?.toDouble()!!)


//            addMarker(TutorialsPoint,this@CenterInformationActivity
//            ,centerInfo?.centerLogo!!)

            myCustomMarker(TutorialsPoint)

            googleMap?.animateCamera(CameraUpdateFactory.newLatLng(TutorialsPoint))

            //Move the camera to the user's location and zoom in!

            //Move the camera to the user's location and zoom in!
//            googleMap?.animateCamera(
//                CameraUpdateFactory.newLatLngZoom(
//                    LatLng(
//                        centerInfo?.lat?.toDouble()!!,
//                        centerInfo?.lng?.toDouble()!!
//                    ), 12.0f
//                )
//            )

            val pos = LatLng(item?.lat?.toDouble()!!, item?.lng?.toDouble()!!)
            val update = CameraUpdateFactory.newLatLngZoom(pos, 15f)
            googleMap?.moveCamera(update)

        }
    }
    private fun myCustomMarker(latLng: LatLng) {


        val conf = Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(80, 80, conf)
        val canvas1 = Canvas(bmp)

// paint defines the text color, stroke width and size

// paint defines the text color, stroke width and size
        val color = Paint()
        color.setTextSize(35.toFloat())
        color.setColor(Color.BLACK)

// modify canvas

// modify canvas
//        canvas1.drawBitmap()
//        canvas1.drawBitmap(
//            BitmapFactory.decodeResource(
//                resources,
//                R.drawable.ic_person
//            ), 0, 0, color
//        )
//        canvas1.drawText("User Name!", 30f, 40f, color)

//        if(centerInfo?.centerLogo!=null){
//            googleMap?.addMarker(
//                MarkerOptions()
//                    .position(latLng)
//                    .title("Sydney")
//                    .snippet("Population: 4,627,300")
//                    .icon(BitmapDescriptorFactory.fromBitmap()
//
//
//        }
//
//        else {
//            googleMap?.addMarker(MarkerOptions().position(latLng).title(centerInfo?.centerName))
//        }

        googleMap?.addMarker(MarkerOptions().position(latLng).title(item?.centerName))
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