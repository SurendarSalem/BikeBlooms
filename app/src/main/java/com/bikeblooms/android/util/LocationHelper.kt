package com.bikeblooms.android.util

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationHelper @Inject constructor(
    private val client: FusedLocationProviderClient
) : LocationClient {
    @SuppressLint("MissingPermission")
    override fun getCurrentLocation(): Flow<Location> {
        return callbackFlow {
            client.lastLocation.addOnSuccessListener {
                trySend(it)
            }
            awaitClose()
        }
    }
}
