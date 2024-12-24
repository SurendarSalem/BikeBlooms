package com.bikeblooms.android.ui.service.compaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.ComplaintsRepository
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.data.SpareRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Bill
import com.bikeblooms.android.model.Complaint
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.Spare
import com.bikeblooms.android.model.SpareType
import com.bikeblooms.android.util.FCMPushNotificationProvider
import com.bikeblooms.android.util.FirebaseConstants.FCM.SERVICE_UPDATE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddComplaintsViewModel @Inject constructor(
    complaintsRepository: ComplaintsRepository,
    spareRepository: SpareRepository,
    private val repository: Repository,
) : ViewModel() {

    private var _notifyState: MutableSharedFlow<ApiResponse<Service>> =
        MutableSharedFlow<ApiResponse<Service>>()
    val notifyState = _notifyState.asSharedFlow()

    private var _complaintsState =
        MutableStateFlow<ApiResponse<List<Complaint>>>(ApiResponse.Empty())
    var complaintsState = _complaintsState.asStateFlow()

    private var _addServiceState = MutableStateFlow<ApiResponse<Service>>(ApiResponse.Empty())
    val addServiceState = _addServiceState.asStateFlow()

    private var _sparesState = MutableStateFlow<ApiResponse<List<Spare>>>(ApiResponse.Empty())
    var spareState = _sparesState.asStateFlow()

    var serviceState = MutableStateFlow<Service?>(null)

    private var _billState = MutableStateFlow<Bill?>(null)
    var billState = _billState.asStateFlow()

    private var _selectedSpareState = MutableStateFlow<Spare?>(null)
    var selectedSpareState = _selectedSpareState.asStateFlow()

    init {
        viewModelScope.launch {
            complaintsRepository.getAllComplaints(object : LoginCallback<List<Complaint>> {
                override fun onSuccess(complaints: List<Complaint>) {
                    _complaintsState.value = ApiResponse.Success(complaints)
                }

                override fun onError(message: String) {
                    _complaintsState.value = ApiResponse.Error(message)
                }
            })
        }

        viewModelScope.launch {
            spareRepository.getAllSpares(SpareType.ENGINE_OIL, object : LoginCallback<List<Spare>> {
                override fun onSuccess(spares: List<Spare>) {
                    _sparesState.value = ApiResponse.Success(spares)
                }

                override fun onError(message: String) {
                    _sparesState.value = ApiResponse.Error(message)
                }
            })
        }
        viewModelScope.launch {
            serviceState.collectLatest { service ->
                service?.let {
                    var spareAmount = service.spareParts?.sumOf { it.price } ?: 0.0
                    var complaintsAmount = service.complaints?.sumOf { it.price } ?: 0.0
                    serviceState.value = serviceState.value?.copy(
                        bill = Bill(
                            totalAmount = spareAmount + complaintsAmount, service.startDate
                        )
                    )
                    _billState.value = Bill(
                        totalAmount = spareAmount + complaintsAmount, service.startDate
                    )
                }
            }
        }
    }

    fun clearSelections() {
        val complaints = _complaintsState.value.data?.toMutableList()
        complaints?.map {
            it.isSelected = false
        }
        complaints?.let {
            _complaintsState.update {
                ApiResponse.Success(complaints)
            }
        }
    }

    fun isServiceValid(): Boolean {
        with(serviceState.value) {
            this?.let {
                if (vehicleName.isNotEmpty() && vehicleId.isNotEmpty() && regNum.isNotEmpty() && firebaseId.isNotEmpty() && (billState.value?.totalAmount!! > 0)) {
                    addService(this)
                    return true
                }
            }
        }
        return false
    }

    fun addService(service: Service) {
        _addServiceState.value = ApiResponse.Loading()
        viewModelScope.launch {
            repository.addService(service, object : LoginCallback<Service> {
                override fun onSuccess(service: Service) {
                    _addServiceState.value = ApiResponse.Success(service)
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
                    "title" to "Hi ${user.name}!. New service request",
                    "message" to "${user.name} has requested a service for his vehicle ${service.vehicleName}"
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

    fun updateSelectedVehicle(spare: Spare) {
        _selectedSpareState.value = spare
        serviceState.value = serviceState.value?.copy(
            spareParts = listOf(
                spare
            )
        )
    }
}