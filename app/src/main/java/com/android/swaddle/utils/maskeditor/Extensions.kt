package com.android.swaddle.utils.maskeditor

import android.content.Context
import android.database.Cursor
import android.location.Geocoder
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import java.util.*


internal fun Char.isPlaceHolder(): Boolean = this == '#'


fun getIdPlaceData(
    placeId: String,
    mContext: Context,
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


fun getZipCode(context: Context?, LATITUDE: Double, LONGITUDE: Double): String {
    var strAdd = ""
    var zipCode = ""

    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
        if (addresses != null) {
            val returnedAddress = addresses[0]
            val strReturnedAddress = StringBuilder("")
            for (i in 0..returnedAddress.maxAddressLineIndex) {
                strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
            }
            strAdd = strReturnedAddress.toString()
            zipCode = addresses[0].postalCode
            return zipCode
        } else {
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        var zipCode = "00000"
    }
    return zipCode

}


fun getFileName(uri: Uri, context: Context?): String? {
    var result: String? = null
    if (uri.scheme.equals("content")) {
        val cursor: Cursor? = context?.contentResolver!!.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != -1) {
            result = result?.substring(cut!! + 1)
        }
    }
    return result
}

fun getFileExtension(uri: Uri, context: Context?): String? {
    var result: String? = null
    if (uri.scheme.equals("content")) {
        val cursor: Cursor? = context?.contentResolver!!.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('.')
        if (cut != -1) {
            result = result?.substring(cut!! + 1)
        }
    }
    Log.e("Extension", result ?: "")
    return result
}