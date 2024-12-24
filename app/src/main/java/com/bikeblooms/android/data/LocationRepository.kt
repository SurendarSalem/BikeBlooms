package com.bikeblooms.android.data

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationRepository {
    private var _myLocationState = MutableStateFlow<Location?>(null)
    val myLocationState = _myLocationState.asStateFlow()
}