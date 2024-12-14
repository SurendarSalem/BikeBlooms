package com.bikeblooms.android.ui.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.data.VehiclesRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.util.SharedPrefHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyVehiclesViewModel @Inject constructor(
    private val vehiclesRepository: VehiclesRepository
) : ViewModel() {
    private var _myVehiclesStatic: MutableStateFlow<ApiResponse<List<Vehicle>>> =
        MutableStateFlow(ApiResponse.Empty())
    val myVehiclesStatic = _myVehiclesStatic.asStateFlow()

    init {
        viewModelScope.launch {
            _myVehiclesStatic.value = ApiResponse.Loading()
            AppState.user?.firebaseId?.run {
                vehiclesRepository.getMyVehicles(this, object : LoginCallback<List<Vehicle>> {
                    override fun onSuccess(vehicles: List<Vehicle>) {
                        _myVehiclesStatic.value = ApiResponse.Success(vehicles)
                    }

                    override fun onError(message: String) {
                        _myVehiclesStatic.value = ApiResponse.Error(message)
                    }
                })
            }
        }
    }
}