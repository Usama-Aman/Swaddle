package com.android.swaddle.utils;

import android.util.Patterns
import com.android.swaddle.baseClasses.BaseActivity
import com.android.swaddle.baseClasses.toArrayList
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import java.util.ArrayList

fun String.isValidEmail(): Boolean =
    this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidMobile(): Boolean = this.isNotEmpty() && Patterns.PHONE.matcher(this).matches()

fun getAutoCompleteData(
    query: String,
    filter: TypeFilter,
    mContext: BaseActivity,
    callBack: (ArrayList<AutocompletePrediction>?, Exception?) -> Unit
) {

    val placesClient = Places.createClient(mContext)
    val token = AutocompleteSessionToken.newInstance()
    val request = FindAutocompletePredictionsRequest.builder()
        .setTypeFilter(filter)
        .setSessionToken(token)
        .setQuery(query)
        .build()
    placesClient.findAutocompletePredictions(request).addOnSuccessListener {
        callBack(it.autocompletePredictions.toArrayList(), null)
    }.addOnFailureListener {
        //        if (it is ApiException) {
//            val apiException = it
//            Log.e("Error", "Place not found: " + apiException.statusCode)
//        }
        callBack(null, it)
    }
}

fun getIdPlaceData(
    placeId: String,
    mContext: BaseActivity,
    callBack: (Place?, Exception?) -> Unit
) {

    val placesClient = Places.createClient(mContext)
    val token = AutocompleteSessionToken.newInstance()
    val placeRequest = FetchPlaceRequest.builder(
        placeId,
        arrayListOf(
            Place.Field.ID,
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.ADDRESS,
            Place.Field.NAME,
            Place.Field.OPENING_HOURS,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.PLUS_CODE,
            Place.Field.PRICE_LEVEL,
            Place.Field.RATING,
            Place.Field.TYPES,
            Place.Field.USER_RATINGS_TOTAL,
            Place.Field.UTC_OFFSET,
            Place.Field.VIEWPORT,
            Place.Field.WEBSITE_URI,
            Place.Field.LAT_LNG
        )
    ).setSessionToken(token)
        .build()
    placesClient.fetchPlace(placeRequest)
        .addOnSuccessListener {
            //it.place
            callBack(it.place, null)
        }.addOnFailureListener {
            //            if (it is ApiException) {
//                val apiException = it
//                Log.e("Error", "Place not found: " + apiException.statusCode)
//            }
            callBack(null, it)
        }


}