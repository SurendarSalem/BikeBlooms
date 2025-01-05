package com.bikeblooms.android.ui.vehicles

import androidx.lifecycle.ViewModel
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.VehiclesRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Vehicle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AllVehiclesViewModel @Inject constructor(private val vehicleRepository: VehiclesRepository) :
    ViewModel() {

    var _allVehicleState = MutableStateFlow<ApiResponse<List<Vehicle>>>(ApiResponse.Empty())
    val allVehicleState = _allVehicleState.asStateFlow()

    init {
        getAllVehicles()
    }

    fun getAllVehicles() {
        _allVehicleState.value = ApiResponse.Loading()
        vehicleRepository.getAllUserVehicles(object : LoginCallback<List<Vehicle>> {
            override fun onSuccess(vehicles: List<Vehicle>) {
                _allVehicleState.value = ApiResponse.Success(vehicles)
            }

            override fun onError(message: String) {
                _allVehicleState.value = ApiResponse.Error(message)
            }
        })
    }
}