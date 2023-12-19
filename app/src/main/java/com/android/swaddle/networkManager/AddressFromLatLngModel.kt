package com.codingpixel.collegiate.networkManager

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.*

class AddressComponent {
    @SerializedName("long_name")
    @Expose
    var longName: String? = null
    @SerializedName("short_name")
    @Expose
    var shortName: String? = null
    @SerializedName("types")
    @Expose
    var types: ArrayList<String>? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("longName", longName).append("shortName", shortName)
            .append("types", types).toString()
    }
}

class AddressFromLatLngModel {
    @SerializedName("plus_code")
    @Expose
    var plusCode: PlusCode? = null
    @SerializedName("results")
    @Expose
    var results: ArrayList<Result>? = null
    @SerializedName("status")
    @Expose
    var status: String? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("plusCode", plusCode).append("results", results)
            .append("status", status).toString()
    }
}

class Bounds {
    @SerializedName("northeast")
    @Expose
    var northeast: Northeast_? = null
    @SerializedName("southwest")
    @Expose
    var southwest: Southwest_? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("northeast", northeast).append("southwest", southwest)
            .toString()
    }
}

class Geometry {
    @SerializedName("location")
    @Expose
    var location: Location? = null
    @SerializedName("location_type")
    @Expose
    var locationType: String? = null
    @SerializedName("viewport")
    @Expose
    var viewport: Viewport? = null
    @SerializedName("bounds")
    @Expose
    var bounds: Bounds? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("location", location)
            .append("locationType", locationType).append("viewport", viewport)
            .append("bounds", bounds).toString()
    }
}


class Location {
    @SerializedName("lat")
    @Expose
    var lat: Double? = null
    @SerializedName("lng")
    @Expose
    var lng: Double? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("lat", lat).append("lng", lng).toString()
    }
}

class Northeast {
    @SerializedName("lat")
    @Expose
    var lat: Double? = null
    @SerializedName("lng")
    @Expose
    var lng: Double? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("lat", lat).append("lng", lng).toString()
    }
}


class Northeast_ {
    @SerializedName("lat")
    @Expose
    var lat: Double? = null
    @SerializedName("lng")
    @Expose
    var lng: Double? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("lat", lat).append("lng", lng).toString()
    }
}

class PlusCode {
    @SerializedName("compound_code")
    @Expose
    var compoundCode: String? = null
    @SerializedName("global_code")
    @Expose
    var globalCode: String? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("compoundCode", compoundCode)
            .append("globalCode", globalCode).toString()
    }
}


class PlusCode_ {
    @SerializedName("compound_code")
    @Expose
    var compoundCode: String? = null
    @SerializedName("global_code")
    @Expose
    var globalCode: String? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("compoundCode", compoundCode)
            .append("globalCode", globalCode).toString()
    }
}


class Result {
    @SerializedName("address_components")
    @Expose
    var addressComponents: ArrayList<AddressComponent>? = null
    @SerializedName("formatted_address")
    @Expose
    var formattedAddress: String? = null
    @SerializedName("geometry")
    @Expose
    var geometry: Geometry? = null
    @SerializedName("place_id")
    @Expose
    var placeId: String? = null
    @SerializedName("plus_code")
    @Expose
    var plusCode: PlusCode_? = null
    @SerializedName("types")
    @Expose
    var types: ArrayList<String>? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("addressComponents", addressComponents)
            .append("formattedAddress", formattedAddress).append("geometry", geometry)
            .append("placeId", placeId).append("plusCode", plusCode).append("types", types)
            .toString()
    }
}

class Southwest {
    @SerializedName("lat")
    @Expose
    var lat: Double? = null
    @SerializedName("lng")
    @Expose
    var lng: Double? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("lat", lat).append("lng", lng).toString()
    }
}


class Southwest_ {
    @SerializedName("lat")
    @Expose
    var lat: Double? = null
    @SerializedName("lng")
    @Expose
    var lng: Double? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("lat", lat).append("lng", lng).toString()
    }
}


class Viewport {

    @SerializedName("northeast")
    @Expose
    var northeast: Northeast? = null
    @SerializedName("southwest")
    @Expose
    var southwest: Southwest? = null

    override fun toString(): String {
        return ToStringBuilder(this).append("northeast", northeast).append("southwest", southwest)
            .toString()
    }

}
