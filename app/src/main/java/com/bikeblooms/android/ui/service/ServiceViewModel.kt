package com.bikeblooms.android.ui.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.data.VehiclesRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.ServiceType
import com.bikeblooms.android.util.FCMPushNotificationProvider
import com.bikeblooms.android.util.FirebaseConstants.FCM.SERVICE_UPDATE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val repository: Repository, private val vehiclesRepository: VehiclesRepository
) : ViewModel() {
    private var _serviceState =
        MutableStateFlow<Service>(Service(serviceType = ServiceType.GENERAL_SERVICE))
    val serviceState = _serviceState.asStateFlow()

    private var _addServiceState = MutableStateFlow<ApiResponse<Service>>(ApiResponse.Empty())
    val addServiceState = _addServiceState.asStateFlow()

    private var _myServicesState: MutableStateFlow<ApiResponse<List<Service>>> =
        MutableStateFlow(ApiResponse.Empty())
    val myServicesState = _myServicesState.asStateFlow()

    private var _notifyState: MutableSharedFlow<ApiResponse<Service>> =
        MutableSharedFlow<ApiResponse<Service>>()
    val notifyState = _notifyState.asSharedFlow()

    init {
        getMyServices()
    }

    fun getMyServices() {
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

    fun addService(service: Service) {
        _addServiceState.value = ApiResponse.Loading()
        viewModelScope.launch {
            repository.addService(service, object : LoginCallback<Service> {
                override fun onSuccess(service: Service) {
                    _addServiceState.value = ApiResponse.Success(service)
                    updateAllMyVehiclesList(service)
                    notifyAdminForServiceRequest(service)
                    viewModelScope.launch(Dispatchers.Main) {
                        _notifyState.emit(ApiResponse.Success(service))
                    }
                }

                override fun onError(message: String) {
                    _addServiceState.value = ApiResponse.Error(message)
                }
            })
        }
    }

    fun notifyAdminForServiceRequest(service: Service) {
        viewModelScope.launch(Dispatchers.IO) {
            AppState.user?.let { user ->
                val body = mutableMapOf(
                    "serviceId" to "",
                    "title" to "New service request",
                    "message" to "${user.name}) has requested a service for his vehicle ${service.vehicleName}"
                )
                FCMPushNotificationProvider.sendMessage(
                    AppState.user?.fcmToken,
                    body["title"].toString(),
                    body["message"].toString(),
                    false,
                    SERVICE_UPDATE,
                    body
                )
            }

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

    fun updateAllMyVehiclesList(service: Service) {
        val list = _myServicesState.value.data?.toMutableList()
        list?.add(service)
        list?.let {
            _myServicesState.value = ApiResponse.Success(it)
        }
    }
}