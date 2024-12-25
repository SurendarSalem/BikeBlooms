package com.bikeblooms.android.data

import com.bikeblooms.android.util.LocationClient
import javax.inject.Singleton

@Singleton
class LocationRepository(private val locationClient: LocationClient) {

    var getLocation = locationClient.getCurrentLocation()
}