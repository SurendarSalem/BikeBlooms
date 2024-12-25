package com.bikeblooms.android.util

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getCurrentLocation(): Flow<Location>
}