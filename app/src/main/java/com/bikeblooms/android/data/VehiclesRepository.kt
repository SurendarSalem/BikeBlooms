package com.bikeblooms.android.data

import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Brand
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.model.VehicleType
import com.bikeblooms.android.util.vehicleTypes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class VehicleMap(
    var carMap: HashMap<Brand, List<Vehicle>>? = null,
    var bikeMap: HashMap<Brand, List<Vehicle>>? = null
)

@Singleton
class VehiclesRepository @Inject constructor(private val firebaseHelper: FirebaseHelper) {
    var vehicleMap = VehicleMap()
    val _vehiclesState = MutableStateFlow<ApiResponse<VehicleMap?>>(ApiResponse.Empty())
    var vehicleState = _vehiclesState.asStateFlow()

    fun getAllVehicles() {

        vehicleTypes.forEach { type ->
            _vehiclesState.value = ApiResponse.Loading()
            getAllVehicles(type, object : LoginCallback<HashMap<Brand, List<Vehicle>>> {
                override fun onSuccess(vehicles: HashMap<Brand, List<Vehicle>>) {
                    if (type == VehicleType.CAR) {
                        vehicleMap.carMap = vehicles
                    } else if (type == VehicleType.BIKE) {
                        vehicleMap.bikeMap = vehicles
                    }
                    if (vehicleMap.carMap != null && vehicleMap.bikeMap != null) {
                        _vehiclesState.value = ApiResponse.Success(vehicleMap)
                    }
                }

                override fun onError(message: String) {
                    _vehiclesState.value = ApiResponse.Error(message)
                }
            })
        }
    }

    fun getAllVehicles(
        vehicleType: VehicleType, callback: LoginCallback<HashMap<Brand, List<Vehicle>>>
    ) {
        firebaseHelper.getAllVehicles(vehicleType, callback)
    }

    fun addVehicle(vehicle: Vehicle, firebaseId: String, callback: LoginCallback<Vehicle>) {
        firebaseHelper.addVehicle(vehicle, firebaseId, callback)
    }

    fun getMyVehicles(uid: String, callback: LoginCallback<List<Vehicle>>) {
        firebaseHelper.getMyVehicles(uid, callback)
    }

    fun getMyServices(uid: String, callback: LoginCallback<List<Service>>) {
        firebaseHelper.getMyServices(uid, callback)
    }

    fun addVehicleToState(vehicle: Vehicle) {

    }

    fun deleteVehicle(
        userId: String,
        vehicle: Vehicle,
        callback: LoginCallback<Vehicle>
    ) {
        firebaseHelper.deleteVehicle(userId, vehicle, callback)
    }
}