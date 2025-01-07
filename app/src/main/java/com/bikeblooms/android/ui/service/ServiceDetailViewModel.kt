package com.bikeblooms.android.ui.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.ServiceRepository
import com.bikeblooms.android.data.SpareRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Bill
import com.bikeblooms.android.model.NotifyState
import com.bikeblooms.android.model.Service
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceDetailViewModel @Inject constructor(
    private var serviceRepository: ServiceRepository, private var spareRepository: SpareRepository
) : ViewModel() {

    var charges = serviceRepository.chargesState

    private var _updateServiceState = MutableStateFlow<ApiResponse<Service>>(ApiResponse.Empty())
    var updateServiceState = _updateServiceState.asStateFlow()

    private var _cancelServiceState = MutableStateFlow<ApiResponse<Service>>(ApiResponse.Empty())
    var cancelServiceState = _cancelServiceState.asStateFlow()

    var notifyState = MutableSharedFlow<NotifyState>()

    private var _serviceState = MutableStateFlow<Service?>(null)
    val serviceState = _serviceState.asStateFlow()

    init {
        viewModelScope.launch {
            serviceState.collectLatest { service ->
                service?.let {
                    var spareAmount = it.spareParts?.sumOf { it.price } ?: 0.0
                    var complaintsAmount = it.complaints?.sumOf { it.price } ?: 0.0
                    _serviceState.value = it.copy(
                        bill = Bill(
                            hiddenCharges = 0.0,
                            totalAmount = getChargesAmount(service) + service.hiddenCharges + spareAmount + complaintsAmount,
                            inspectionCharges = getChargesAmount(service),
                            service.startDate
                        )
                    )
                }
            }
        }
    }

    fun getChargesAmount(service: Service): Double {
        if (charges.value.isNotEmpty()) {
            return charges.value.first { it.name == service.serviceType?.title }.price
        }
        return 0.0
    }


    fun setService(service: Service) {
        viewModelScope.launch {
            _serviceState.value = service
        }
    }

    fun updateService(service: Service?) {
        viewModelScope.launch {
            _serviceState.value = service
        }
    }

    val myVehiclesState = serviceRepository.myVehicleState
    val allSparesState = spareRepository.allSparesState


    fun updateServiceInFirebase() {
        viewModelScope.launch {
            with(_serviceState.value) {
                this?.let {
                    _updateServiceState.value = ApiResponse.Loading()
                    serviceRepository.updateService(it, object : LoginCallback<Service> {
                        override fun onSuccess(service: Service) {
                            _updateServiceState.value = ApiResponse.Success(service)
                            viewModelScope.launch(Dispatchers.Main) {
                                notifyState.emit(NotifyState.Success("Service updated successfully"))
                            }

                        }

                        override fun onError(message: String) {
                            _updateServiceState.value = ApiResponse.Error(message)
                            viewModelScope.launch(Dispatchers.Main) {
                                notifyState.emit(NotifyState.Error(message))
                            }
                        }

                    })
                }
            }
        }
    }

    fun cancelService() {
        viewModelScope.launch {
            with(_serviceState.value) {
                this?.let {
                    _cancelServiceState.value = ApiResponse.Loading()
                    serviceRepository.cancelService(it, object : LoginCallback<Service> {
                        override fun onSuccess(service: Service) {
                            _cancelServiceState.value = ApiResponse.Success(service)
                            viewModelScope.launch(Dispatchers.Main) {
                                notifyState.emit(NotifyState.Success("Service cancelled successfully"))
                            }

                        }

                        override fun onError(message: String) {
                            _cancelServiceState.value = ApiResponse.Error(message)
                            viewModelScope.launch(Dispatchers.Main) {
                                notifyState.emit(NotifyState.Error(message))
                            }
                        }

                    })
                }
            }
        }
    }

}