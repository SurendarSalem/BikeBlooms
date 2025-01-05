package com.bikeblooms.android.ui.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.ServiceRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Service
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleDetailsViewModel @Inject constructor(
    private val vehiclesRepository: ServiceRepository
) : ViewModel() {
    private var _allServicesState: MutableStateFlow<ApiResponse<List<Service>>> =
        MutableStateFlow(ApiResponse.Empty())
    val allServicesState = _allServicesState.asStateFlow()

    private var _notifyState: MutableSharedFlow<ApiResponse<Service>> =
        MutableSharedFlow<ApiResponse<Service>>()
    val notifyState = _notifyState.asSharedFlow()

    fun getServices(fieldName: String, value: String) {
        viewModelScope.launch {
            _allServicesState.value = ApiResponse.Loading()
            AppState.user?.firebaseId?.run {
                vehiclesRepository.getServiceHistory(
                    fieldName,
                    value,
                    object : LoginCallback<List<Service>> {
                        override fun onSuccess(services: List<Service>) {
                            _allServicesState.value = ApiResponse.Success(services)
                        }

                        override fun onError(message: String) {
                            _allServicesState.value = ApiResponse.Error(message)
                        }
                    })
            }
        }
    }
}