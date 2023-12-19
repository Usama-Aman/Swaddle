package com.android.swaddle.networkManager

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.codingpixel.collegiate.networkManager.AddressFromLatLngModel

import com.google.gson.Gson
import org.json.JSONObject
import java.io.IOException
import java.util.*


class NetworkURLs {
    companion object {

        const val stripe_publishKey =
            "pk_test_51Hdxt9ERuA1djMVYH9USiy4m0VNXbufXkB7uj9tEWTvRe4yBgrXF2allDjTt3ujvd1rDEiqeNMTBukbgKUn0V5ec00hx4IOvHF"  //test

        const val stripe_secretKey =
            "sk_test_51Hdxt9ERuA1djMVYOCozSJtjyox4TgjUMHZhi5JPIEK6fQa5wLs5oL2INpYKLQAbTae6a5Ro8f5jXShCBryKOuWH00sWNSPAUN"  //test

        const val stripe_clientId = "ca_IOoM1bJC6S61pSU5cIYhpRefa3CDj1Wc"  //test
        const val stripe_redirectURL = "http://139.162.3.157/swaddle-web/provider/payment"  //test

        var STRIPE_CONNECT_ACCOUNT =
            "https://connect.stripe.com/express/oauth/authorize?redirect_uri=https://www.duet.icu/stripe_redirect_uri&client_id=$stripe_clientId&scope=read_write&state=0O6NjOkks572wEYuwG5gvaR09EdJQd7X9HY8th1r" // Test

        const val DELETE_MESG_CHAT = "Are you sure you want to delete this chat?"
        const val DELETE_CARD = "Are you sure you want to delete this card?"

        fun getUrlLink(lat: String, lng: String): String { //&zoom=10
            return "https://maps.googleapis.com/maps/api/staticmap?autoscale=2&zoom=4.02&size=1600x1400&maptype=roadmap&style=element:geometry%7Ccolor:0x212121&style=element:labels.text.fill%7Ccolor:0x757575&style=element:labels.icon%7Cvisibility:off&style=element:labels.text.stroke%7Ccolor:0x212121&style=feature:administrative%7Celement:geometry%7Ccolor:0x757575&style=feature:administrative.country%7Celement:labels.text.fill%7Ccolor:0x9e9e9e&style=feature:administrative.land_parcel%7Cvisibility:off&style=feature:administrative.locality%7Celement:labels.text.fill%7Ccolor:0xbdbdbd&style=feature:poi%7Celement:labels.text.fill%7Ccolor:0x757575&style=feature:poi.park%7Celement:geometry%7Ccolor:0x181818&style=feature:poi.park%7Celement:labels.text.fill%7Ccolor:0x616161&style=feature:poi.park%7Celement:labels.text.stroke%7Ccolor:0x1b1b1b&style=feature:road%7Celement:geometry.fill%7Ccolor:0x2c2c2c&style=feature:road%7Celement:labels.text.fill%7Ccolor:0x8a8a8a&style=feature:road.arterial%7Celement:geometry%7Ccolor:0x373737&style=feature:road.highway%7Celement:geometry%7Ccolor:0x3c3c3c&style=feature:road.highway.controlled_access%7Celement:geometry%7Ccolor:0x4e4e4e&style=feature:road.local%7Celement:labels.text.fill%7Ccolor:0x616161&style=feature:transit%7Celement:labels.text.fill%7Ccolor:0x757575&style=feature:water%7Ccolor:0x212121&style=feature:water%7Celement:geometry%7Ccolor:0x000000&style=feature:water%7Celement:geometry.fill%7Ccolor:0x3c4351&style=feature:water%7Celement:labels.text.fill%7Ccolor:0x3d3d3d&key=AIzaSyA-Uk5DNzYvHUS_8a9RbFsHE-99Aw3BwPM&format=gif&visual_refresh=true&markers=size:small%9Ccolor:0xFF2D55%7Clabel:1%7C$lat,$lng"
        }

        const val APP_KEY = "iSEiYCaYkcESApgMAqBQciBpP5qI2HB2Y+iocYesMaI="

        const val STRIPE_BASE_PATH = "https://api.stripe.com/v1/"

        const val STRIPE_TOKEN = "https://connect.stripe.com/oauth/token"

        //        const val KEY_GOOGLE = "AIzaSyCqNk11MiqU1okbbL3HBaEnRWgl81yi-hs"   //project test key
        const val KEY_GOOGLE = "AIzaSyBhMoC9OLQs1fxPJkPWxdgC9dui6pIKQoA"   //CP personal paid key.

        const val token = "token"
        const val customers = STRIPE_BASE_PATH + "customers" // for stripe create customers
        const val tokens = STRIPE_BASE_PATH + "tokens" // for stripe create customers
        fun createCardApi(customerId: String): String {
            return STRIPE_BASE_PATH + "customers/${customerId}/sources"
        }

        fun updateCardApi(customerId: String, cardId: String): String {
            return STRIPE_BASE_PATH + "customers/${customerId}/sources/${cardId}"
        }

        const val user_id = "user_id"
        const val companion_id = "companion_id"
        const val payment_type = "type"
        const val amount = "amount"
        const val post_id = "post_id"
        const val url = "url"
        const val message = "message"
        const val card_id = "card_id"
        const val receiver_id = "receivers_id"
        const val notification_type = "notification_type"
        const val type_id = "type_id"
        const val notification_text = "notification_text"
        const val type_url = "type_url"
        const val companion_Id = "reciever_id"
        const val other_id = "receiver_id"
        const val price = "money"
        const val flag = "flag"
        const val model = "model"

        private fun getLocation(query: String): String {

            return "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?key=$KEY_GOOGLE&input=${
                query.replace(
                    " ",
                    "%20"
                )
            }&inputtype=textquery&fields=photos,formatted_address,name,opening_hours,rating,place_id,geometry"
        }

        fun callForLocationFromLatLng(
            lat: String,
            lng: String,
            callBack: (Boolean, String) -> Unit
        ) {
            AndroidNetworking.get("https://maps.googleapis.com/maps/api/geocode/json?key=$KEY_GOOGLE&latlng=$lat,$lng&sensor=false")
                .setTag("LatLng").setPriority(Priority.IMMEDIATE).build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        try {
                            val gson = Gson()
                            val obj: AddressFromLatLngModel = gson.fromJson(
                                response.toString(),
                                AddressFromLatLngModel::class.java
                            )
                            Log.e(
                                "location",
                                obj.results!![0].addressComponents!![2].longName + ", " + obj.results!![0].addressComponents!![5].longName
                            )
                            callBack(
                                true,
                                obj.results!![0].addressComponents!![2].longName + ", "
                                + obj.results!![0].addressComponents!![5].longName ?: ""
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onError(anError: ANError?) {
                        callBack(false, anError?.localizedMessage ?: "")
                    }
                })
        }


        fun callForLocation(query: String, callBack: (Boolean, String) -> Unit) {
            Log.e("path", getLocation(query))
            AndroidNetworking.get(getLocation(query))
                .setTag("LocationSearch").setPriority(Priority.IMMEDIATE).build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {
                        callBack(true, response?.toString() ?: "")
                        Log.e("Success", response.toString())
                    }

                    override fun onError(anError: ANError?) {
                        callBack(false, anError?.localizedMessage ?: "")
                        Log.e("Fail", anError?.localizedMessage ?: "")
                    }

                })
        }

        fun getCityName(context: Context, latitude: Double, longitude: Double): String {
            val geoCoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>
            return try {
                addresses = geoCoder.getFromLocation(latitude, longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    if (addresses[0].locality != null) {
                        addresses[0].locality
                    } else
                        ""
                } else {
                    ""
                }
            } catch (ioe: IOException) {
                ioe.printStackTrace()
                ""
            }
        }

        fun getStateName(context: Context, latitude: Double, longitude: Double): String {
            val geoCoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>
            return try {
                addresses = geoCoder.getFromLocation(latitude, longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    if (addresses[0].adminArea != null) {
                        addresses[0].adminArea
                    } else {
                        ""
                    }
                } else {
                    ""
                }
            } catch (ioe: IOException) {
                ioe.printStackTrace()
                ""
            }
        }

        fun getCountryName(context: Context, latitude: Double, longitude: Double): String {
            val geoCoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>
            return try {
                addresses = geoCoder.getFromLocation(latitude, longitude, 1)
                return if (addresses != null && addresses.isNotEmpty()) {
                    addresses[0].countryName
                } else {
                    ""
                }
            } catch (ioe: IOException) {
                ioe.printStackTrace()
                ""
            }
        }
    }
}