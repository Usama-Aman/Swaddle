package com.android.swaddle.payment_screens

import android.app.DatePickerDialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.android.swaddle.R
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.databinding.ActivityAddPaymentMethodesBinding
import com.android.swaddle.helper.showErrorToast
import com.android.swaddle.helper.showSuccessToast
import com.android.swaddle.helper.viewGone
import com.android.swaddle.helper.viewVisible
import com.android.swaddle.models.LoginResponse
import com.android.swaddle.models.PaymentCardsResponse
import com.android.swaddle.networkManager.NetworkURLs
import com.android.swaddle.networkManager.RetrofitClass
import com.android.swaddle.utils.CustomToasts
import com.android.swaddle.utils.maskeditor.MaskTextWatcher
import com.android.swaddle.utils.maskeditor.getIdPlaceData
import com.android.swaddle.utils.maskeditor.getZipCode
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.stripe.android.ApiResultCallback
import com.stripe.android.Stripe
import com.stripe.android.model.Card
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.model.Token
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.Serializable
import java.util.*


class AddPaymentMethodsActivity : BaseActivity(), (Place?, Exception?) -> Unit {
    private var address: String = ""
    private lateinit var binding: ActivityAddPaymentMethodesBinding
    private val AUTOCOMPLETE_REQUEST_CODE = 12121
    private var cardType = ""
    private var date: String = ""
    private var mCalendar: Calendar = Calendar.getInstance()
    private var picker: DatePickerDialog? = null

    private var stripe: Stripe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPaymentMethodesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVar()
        listener()
        callAPItoGetPaymentMethods()
    }

    private fun callAPItoGetPaymentMethods() {
        val call: Call<PaymentCardsResponse> = RetrofitClass.getInstance().webRequestsInstance
            .getPaymentMethods("Bearer " + userManager.accessToken ?: "")
        call.enqueue(object : Callback<PaymentCardsResponse> {
            override fun onResponse(
                call: Call<PaymentCardsResponse>,
                response: Response<PaymentCardsResponse>
            ) {
                hideLoading()
                var list = response.body()?.data ?: ArrayList()
                binding.cbDefault.isChecked = list.size <= 0
            }

            override fun onFailure(
                call: Call<PaymentCardsResponse>,
                t: Throwable
            ) {
//                showErrorToast(this@PaymentMethodListActivity, "Can't Connect to Server!")
                hideLoading()
                t.printStackTrace()
            }
        })
    }

    private fun listener() {
        binding.etAddress.setOnClickListener {
            startSearchActivity()
        }

        binding.lnrVisa.setOnClickListener {
            cardType = "visa"
            binding.lnrVisa.setBackgroundResource(R.drawable.bg_card_selected)
            binding.lnrMasterCard.setBackgroundResource(R.drawable.bg_card_normal)
        }

        binding.lnrMasterCard.setOnClickListener {
            binding.lnrVisa.setBackgroundResource(R.drawable.bg_card_normal)
            binding.lnrMasterCard.setBackgroundResource(R.drawable.bg_card_selected)
            cardType = "master"
        }

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvSave.setOnClickListener {
            if (validation()) {
                showProgressBar()
                saveCard()
            }
        }
    }

    private fun showProgressBar() {
        binding.tvSave.viewGone()
        binding.progressbar.viewVisible()
    }

    private fun hideProgressBar() {
        binding.tvSave.viewVisible()
        binding.progressbar.viewGone()
    }

    private fun saveCard() {
        val cardNumber = binding.etCardNumber.text.toString().trim()
        val cardNo = cardNumber.replace(" ", "")
        val cardMonth = binding.etDayMonth.text.toString().trim().split("/").first().toInt()
        val cardYear = binding.etDayMonth.text.toString().trim().split("/").last().toInt()
        val cardCVC = binding.etCvc.text.toString().trim()
        val cardToSave: Card = Card.create(
            cardNumber,
            cardMonth,
            cardYear,
            binding.etCvc.text.toString().trim()
        )

        if (cardToSave.validateCard() && cardToSave.validateNumber() &&
            cardToSave.validateCVC()
        ) {

            generateToke(
                cardToSave,
                cardCVC,
                cardMonth,
                cardYear,
                cardNo
            )
            binding.cardCheck.viewVisible()
            binding.cardCheck.setColorFilter(
                ContextCompat.getColor(
                    this@AddPaymentMethodsActivity,
                    R.color.green
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )

        } else {
            binding.cardCheck.viewVisible()
            binding.cardCheck.setColorFilter(
                ContextCompat.getColor(
                    this@AddPaymentMethodsActivity,
                    R.color.red
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )

            hideProgressBar()
            CustomToasts.failureToast(this@AddPaymentMethodsActivity, "Invalid card data")
        }
    }

    private fun generateToke(
        cardToSave: Card,
        cardCVC: String,
        cardMonth: Int,
        cardYear: Int,
        cardNo: String
    ) {
        stripe?.createToken(cardToSave, object : ApiResultCallback<Token> {
            override fun onSuccess(result: Token) {
                Log.e("Stripe", result.toString())

                generatePaymentMethod(
                    result.id, cardCVC,
                    cardMonth,
                    cardYear,
                    cardNo
                )
            }

            override fun onError(e: Exception) {
                Log.e("Stripe", e.message ?: "")
                CustomToasts.failureToast(this@AddPaymentMethodsActivity, e.message ?: "")
                e.printStackTrace()
                hideProgressBar()

            }
        })
    }

    private fun generatePaymentMethod(
        cardId: String,
        cardCVC: String,
        cardMonth: Int,
        cardYear: Int,
        cardNo: String
    ) {
        var card = PaymentMethodCreateParams.Card.create(cardId)
        var params = PaymentMethodCreateParams.create(card)
        stripe?.createPaymentMethod(params, object : ApiResultCallback<PaymentMethod> {
            override fun onSuccess(result: PaymentMethod) {
                ////call API to send result.id ?: "" to server...
                callAPItoAddPaymentMethod(result.id ?: "")
            }

            override fun onError(e: java.lang.Exception) {
                CustomToasts.failureToast(this@AddPaymentMethodsActivity, e.message ?: "")
                hideProgressBar()
            }
        })
    }

    private fun initVar() {
        stripe = Stripe(this@AddPaymentMethodsActivity, NetworkURLs.stripe_publishKey)

        val textWatcherNumber = MaskTextWatcher(binding.etCardNumber, "#### #### #### ####")
        val textWatcherDate = MaskTextWatcher(binding.etDayMonth, "##/####")
        binding.etDayMonth.addTextChangedListener(textWatcherDate)
        binding.etCardNumber.addTextChangedListener(textWatcherNumber)
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            val TAG = "GoogleMapsLog"
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                Log.v("TestingNewLoc", "Place: " + place.name + ", " + place.id)

                Log.i("SelectedLocation", "" + place.latLng)


                val locModel = LocationModel()
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

    override fun invoke(p1: Place?, p2: Exception?) {
        //  binding.etAddress.setText(p1!!.address + "")
        val name = p1?.name.toString()
        address = p1?.address.toString()

        binding.etZipCode.setText(
            getZipCode(
                this, p1?.latLng?.latitude!!,
                p1.latLng?.longitude!!
            )
        )

        binding.etAddress.text = address + ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address> =
                geocoder.getFromLocation(p1.latLng!!.latitude, p1.latLng!!.longitude, 1)
            val stateName: String = addresses[0].adminArea
            val cityName: String = addresses[0].locality
            val country = addresses[0].countryName


            binding.etState.setText(stateName)
            binding.etCity.setText(cityName)
            binding.etCountry.setText(country)


        } catch (e: IOException) {
            e.printStackTrace()
        }

        Log.i("PlacesInfo", "" + p1?.address)
        Log.i("PlacesInfo", "" + p1?.latLng?.latitude)
        Log.i("PlacesInfo", "" + p1?.latLng?.longitude)
    }

    open class LocationModel : Serializable {
        var lat: Double = 0.0
        var lng: Double = 0.0
        var name = ""
        var distance: Double = 0.0
        var image: String = ""
        var zipCode: String = ""
    }

    private fun datePickerDialog(type: String) {
        val day: Int = mCalendar.get(Calendar.DAY_OF_MONTH)
        val month: Int = mCalendar.get(Calendar.MONTH)
        val year: Int = mCalendar.get(Calendar.YEAR)
        picker = DatePickerDialog(
            this, R.style.MySpinnerDatePickerStyle,
            { view, year, monthOfYear, dayOfMonth ->
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                binding.etDayMonth.setText("${month + 1} / $year")



                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }, year, month, day
        )
        picker?.show()
    }

    private fun callAPItoAddPaymentMethod(id: String) {
        showProgressBar()
        val call: Call<LoginResponse> = RetrofitClass.getInstance().webRequestsInstance
            .addPaymentMethod(
                "Bearer " + userManager.accessToken ?: "",
                (id ?: 0).toString(),
                if (binding.cbDefault.isChecked) {
                    1
                } else {
                    0
                }
            )
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.body()?.status == true) {
                    showSuccessToast(this@AddPaymentMethodsActivity, response.body()?.message ?: "")
                    finish()
                } else {
                    showErrorToast(this@AddPaymentMethodsActivity, response.body()?.message ?: "")
                }
                hideProgressBar()
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                showSuccessToast(this@AddPaymentMethodsActivity, "Can't Connect to Server!")
                hideProgressBar()
                t.printStackTrace()
            }
        })
    }

    private fun validation(): Boolean {
        val name = binding.etCardName.text.toString()
        val cardNumber = binding.etCardNumber.text.toString()
        val date = binding.etDayMonth.text.toString()
        val cvc = binding.etCvc.text.toString()
        val country = binding.etCountry.text.toString()
        val state = binding.etState.text.toString()
        val city = binding.etCity.text.toString()
        val zipCode = binding.etZipCode.text.toString()
        if (name.isEmpty()) {
            showErrorToast(this, "Please enter name on card")
            return false
        } else if (cardNumber.isEmpty()) {
            showErrorToast(this, "Please enter card number")
            return false
        } else if (date.isEmpty()) {
            showErrorToast(this, "Please enter card expiration date")
            return false

        } else if (address.isEmpty()) {
            showErrorToast(this, "Please enter address")
            return false
        } else if (country.isEmpty()) {
            showErrorToast(this, "Please enter country name")
            return false
        } else if (state.isEmpty()) {
            showErrorToast(this, "Please enter state name")
            return false
        } else if (city.isEmpty()) {
            showErrorToast(this, "Please enter city name")
            return false
        } else if (zipCode.isEmpty()) {
            showErrorToast(this, "Please enter zip name")
            return false
        }
        return true
    }
}