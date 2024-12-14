package com.bikeblooms.android.ui.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.VehiclesRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Service
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyServicesViewModel @Inject constructor(
    private val vehiclesRepository: VehiclesRepository
) : ViewModel() {
    private var _myServicesState: MutableStateFlow<ApiResponse<List<Service>>> =
        MutableStateFlow(ApiResponse.Empty())
    val myServicesState = _myServicesState.asStateFlow()

    init {
        viewModelScope.launch {
            _myServicesState.value = ApiResponse.Loading()
            AppState.user?.firebaseId?.run {
                vehiclesRepository.getMyServices(this, object : LoginCallback<List<Service>> {
                    override fun onSuccess(services: List<Service>) {
                        _myServicesState.value = ApiResponse.Success(services)
                    }

                    override fun onError(message: String) {
                        _myServicesState.value = ApiResponse.Error(message)
                    }
                })
            }
        }
    }
}