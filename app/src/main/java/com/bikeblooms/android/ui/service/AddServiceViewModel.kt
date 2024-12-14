package com.bikeblooms.android.ui.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.util.SharedPrefHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddServiceViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private var _addServiceState = MutableStateFlow<ApiResponse<Service>>(ApiResponse.Empty())
    val addServiceState = _addServiceState.asStateFlow()

    private var _notifyState: MutableSharedFlow<ApiResponse<Service>> =
        MutableSharedFlow<ApiResponse<Service>>()
    val notifyState = _notifyState.asSharedFlow()

    fun addService(service: Service) {
        _addServiceState.value = ApiResponse.Loading()
        viewModelScope.launch {
            repository.addService(service, object : LoginCallback<Service> {
                override fun onSuccess(t: Service) {
                    _addServiceState.value = ApiResponse.Success(t)
                    viewModelScope.launch(Dispatchers.Main) {
                        _notifyState.emit(ApiResponse.Success(t))
                    }
                }

                override fun onError(message: String) {
                    _addServiceState.value = ApiResponse.Error(message)
                }
            })
        }
    }

    fun validate(service: Service): String {
        with(service) {
            if (vehicleName.isEmpty() or vehicleId.isEmpty() or regNum.isEmpty()) {
                return "Please select a vehicle"
            }
            return ""
        }
    }
}