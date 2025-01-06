package com.bikeblooms.android.ui.authentication.vendor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.NotifyState
import com.bikeblooms.android.model.Vendor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VendorRequestViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private var _addVendor = MutableStateFlow<ApiResponse<Vendor>>(ApiResponse.Empty())
    val addVendorState = _addVendor.asStateFlow()

    private var _notifyState = MutableSharedFlow<NotifyState>()
    val notifyState = _notifyState.asSharedFlow()

    fun addVendor(vendor: Vendor) {
        _addVendor.value = ApiResponse.Loading()
        viewModelScope.launch {
            repository.addVendor(vendor, object : LoginCallback<Vendor> {
                override fun onSuccess(t: Vendor) {
                    _addVendor.value = ApiResponse.Success(t)
                    viewModelScope.launch(Dispatchers.Main) {
                        _notifyState.emit(NotifyState.Success("Vendor added successfully"))
                    }
                }

                override fun onError(message: String) {
                    _addVendor.value = ApiResponse.Error(message)
                    viewModelScope.launch(Dispatchers.Main) {
                        _notifyState.emit(NotifyState.Error(message))
                    }
                }
            })
        }
    }
}