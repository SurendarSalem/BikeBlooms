package com.bikeblooms.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.ServiceRepository
import com.bikeblooms.android.data.VendorRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.Vendor
import com.bikeblooms.android.util.FCMPushNotificationProvider
import com.bikeblooms.android.util.FirebaseConstants.FCM.SERVICE_UPDATE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VendorListViewModel @Inject constructor(
    var repository: VendorRepository, var serviceRepository: ServiceRepository
) : ViewModel() {

    val vendorListState = repository.vendorListState

    var _assignServiceState = MutableStateFlow<ApiResponse<Service>>(ApiResponse.Empty())
    val assignServiceState = _assignServiceState.asStateFlow()

    init {
        repository.getAllVendors()
    }

    fun assignService(vendor: Vendor, service: Service) {
        _assignServiceState.value = ApiResponse.Loading()
        serviceRepository.assignService(vendor, service, object : LoginCallback<Service> {
            override fun onSuccess(t: Service) {
                _assignServiceState.value = ApiResponse.Success(t)
                AppState.user?.run {
                    sendNotificationToVendor(name, fcmToken)
                    sendNotificationToUser(name, fcmToken)
                }

            }

            override fun onError(message: String) {
                _assignServiceState.value = ApiResponse.Error(message)
            }
        })
    }

    fun sendNotificationToVendor(vendorName: String, userFcmToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val vendorBody = mutableMapOf(
                "serviceId" to "",
                "title" to "Hi ${vendorName}!. New service update",
                "message" to "Admin has allotted a new service to you"
            )
            FCMPushNotificationProvider.sendMessage(
                userFcmToken,
                vendorBody["title"].toString(),
                vendorBody["message"].toString(),
                false,
                SERVICE_UPDATE,
                vendorBody
            )
        }
    }


    fun sendNotificationToUser(userName: String, userFcmToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val vendorBody = mutableMapOf(
                "serviceId" to "",
                "title" to "Hi ${userName}!. Your service update",
                "message" to "You service has been started and assigned to a vendor"
            )
            FCMPushNotificationProvider.sendMessage(
                userFcmToken,
                vendorBody["title"].toString(),
                vendorBody["message"].toString(),
                false,
                SERVICE_UPDATE,
                vendorBody
            )
        }
    }

}