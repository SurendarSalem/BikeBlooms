package com.bikeblooms.android.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.data.VehiclesRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Vehicle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: Repository, private val vehicleRepository: VehiclesRepository
) : ViewModel() {

    val vehiclesState = vehicleRepository.vehicleState
    private var _currentVehicleState = MutableStateFlow<Vehicle?>(null)
    var currentVehicleState = _currentVehicleState.asStateFlow()

    private var _vehicleAddedFlow = MutableSharedFlow<Boolean>()
    var vehicleAddedFlow = _vehicleAddedFlow.asSharedFlow()


    private var _addVehicleState = MutableStateFlow<ApiResponse<Vehicle>>(ApiResponse.Empty())
    var addVehicleState = _addVehicleState.asStateFlow()

    init {
        viewModelScope.launch {
            vehicleRepository.getAllVehicles()
        }
    }


    fun isValid(vehicle: Vehicle): Boolean {
        return vehicle.name.isNotEmpty() && vehicle.model.isNotEmpty() && vehicle.type != null && vehicle.brand != null && vehicle.fuelType != null && vehicle.regNo.isNotEmpty()
    }

    fun updateCurrentState(vehicle: Vehicle) {
        _currentVehicleState.value = vehicle
    }

    fun addVehicle(vehicle: Vehicle, firebaseId: String) {
        _addVehicleState.value = ApiResponse.Loading()
        vehicleRepository.addVehicle(vehicle, firebaseId, object : LoginCallback<Vehicle> {
            override fun onSuccess(vehicle: Vehicle) {
                _addVehicleState.value = ApiResponse.Success(vehicle)
                viewModelScope.launch(Dispatchers.Main) {
                    _vehicleAddedFlow.emit(true)
                }
            }

            override fun onError(message: String) {
                _addVehicleState.value = ApiResponse.Error(message)
            }
        })
    }
}