package com.bikeblooms.android.util

import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import java.io.IOException

object ReverseGeocoder {
    fun reverseGeocode(
        lng: LatLng, mGeocoder: Geocoder
    ): String {
        var addressString = ""
        try {
            val addressList: List<Address>? =
                mGeocoder.getFromLocation(lng.latitude, lng.longitude, 1)

            // use your lat, long value here
            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                val sb = StringBuilder()
                sb.append(address.getAddressLine(0))
                addressString = sb.toString()
                return addressString
            }
        } catch (e: IOException) {
            return ""
        }
        return ""
    }
}