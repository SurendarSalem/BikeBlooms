package com.bikeblooms.android.data

import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.util.AppConstants.VEHICLES
import com.bikeblooms.android.util.FirebaseConstants.SERVICES
import com.bikeblooms.android.util.FirebaseConstants.USER_VEHICLES
import com.google.firebase.firestore.FirebaseFirestoreException.Code.CANCELLED
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(private val repository: VehiclesRepository) {

    private var _myVehicleState = MutableStateFlow<ApiResponse<List<Vehicle>>>(ApiResponse.Empty())
    val myVehicleState = _myVehicleState.asStateFlow()

    fun getMyVehicles(isForceRefresh: Boolean = false) {
        if (isForceRefresh) {
            val uid = AppState.user?.firebaseId.toString()
            if (uid.isNotEmpty()) {
                Firebase.firestore.collection(USER_VEHICLES).document(uid).collection(VEHICLES)
                    .get()
                    .addOnSuccessListener { result ->
                        val vehicleList = mutableListOf<Vehicle>()
                        result.forEach {
                            val vehicle = it.toObject<Vehicle>(Vehicle::class.java)
                            vehicleList.add(vehicle)
                        }
                        _myVehicleState.value = ApiResponse.Success(vehicleList)
                    }.addOnFailureListener {
                        _myVehicleState.value = ApiResponse.Error(it.message.toString())
                    }
            }
        }
    }


    fun updateService(service: Service, callback: LoginCallback<Service>) {
        Firebase.firestore.collection(SERVICES).document(service.id).set(service)
            .addOnSuccessListener {
                callback.onSuccess(service)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }

    fun cancelService(service: Service, callback: LoginCallback<Service>) {
        Firebase.firestore.collection(SERVICES).document(service.id).update("progress", CANCELLED)
            .addOnSuccessListener {
                callback.onSuccess(service)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }
}