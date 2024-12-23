package com.bikeblooms.android.ui.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.VehiclesRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
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
class VehicleViewModel @Inject constructor(
    private val vehiclesRepository: VehiclesRepository
) : ViewModel() {
    private var _myVehiclesState: MutableStateFlow<ApiResponse<List<Vehicle>>> =
        MutableStateFlow(ApiResponse.Empty())
    val myVehiclesState = _myVehiclesState.asStateFlow()

    val vehiclesState = vehiclesRepository.vehicleState
    private var _currentVehicleState = MutableStateFlow<Vehicle?>(null)
    var currentVehicleState = _currentVehicleState.asStateFlow()

    private var _vehicleAddedFlow = MutableSharedFlow<Boolean>()
    var vehicleAddedFlow = _vehicleAddedFlow.asSharedFlow()

    private var _addVehicleState = MutableStateFlow<ApiResponse<Vehicle>>(ApiResponse.Empty())
    var addVehicleState = _addVehicleState.asStateFlow()

    private var _selectedVehicleState = MutableStateFlow<Vehicle?>(null)
    var selectedVehicleState = _selectedVehicleState.asStateFlow()


    init {
        viewModelScope.launch {
            vehiclesRepository.getAllVehicles()
        }

        viewModelScope.launch {
            _myVehiclesState.value = ApiResponse.Loading()
            AppState.user?.firebaseId?.run {
                vehiclesRepository.getMyVehicles(this, object : LoginCallback<List<Vehicle>> {
                    override fun onSuccess(vehicles: List<Vehicle>) {
                        _selectedVehicleState.value = vehicles.firstOrNull()
                        _myVehiclesState.value = ApiResponse.Success(vehicles)
                    }

                    override fun onError(message: String) {
                        _myVehiclesState.value = ApiResponse.Error(message)
                    }
                })
            }
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
        vehiclesRepository.addVehicle(vehicle, firebaseId, object : LoginCallback<Vehicle> {
            override fun onSuccess(vehicle: Vehicle) {
                _addVehicleState.value = ApiResponse.Success(vehicle)
                updateAddedInMyVehicles(vehicle)

                viewModelScope.launch(Dispatchers.Main) {
                    _vehicleAddedFlow.emit(true)
                }
            }

            override fun onError(message: String) {
                _addVehicleState.value = ApiResponse.Error(message)
            }
        })
    }

    private fun updateAddedInMyVehicles(vehicle: Vehicle) {
        val myVehicles = _myVehiclesState.value.data?.toMutableList()
        myVehicles?.add(vehicle)
        myVehicles?.let {
            _myVehiclesState.value = ApiResponse.Success(it)
        }
    }

    fun updateSelectedVehicle(vehicle: Vehicle) {
        _selectedVehicleState.value = vehicle
    }

    fun delete(vehicle: Vehicle, firebaseId: String) {
        vehiclesRepository.deleteVehicle(firebaseId,vehicle, object : LoginCallback<Vehicle>{
            override fun onSuccess(t: Vehicle) {
                TODO("Not yet implemented")
            }

            override fun onError(message: String) {
                TODO("Not yet implemented")
            }

        })
    }
}