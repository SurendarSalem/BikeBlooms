package com.bikeblooms.android.ui.service.compaints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.ComplaintsRepository
import com.bikeblooms.android.data.SpareRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Bill
import com.bikeblooms.android.model.Complaint
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.Spare
import com.bikeblooms.android.model.SpareType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddComplaintsViewModel @Inject constructor(
    repository: ComplaintsRepository, spareRepository: SpareRepository
) : ViewModel() {
    private var _complaintsState =
        MutableStateFlow<ApiResponse<List<Complaint>>>(ApiResponse.Empty())
    var complaintsState = _complaintsState.asStateFlow()

    private var _sparesState = MutableStateFlow<ApiResponse<List<Spare>>>(ApiResponse.Empty())
    var spareState = _sparesState.asStateFlow()

    var serviceState = MutableStateFlow<Service?>(null)

    private var _billState = MutableStateFlow<Bill?>(null)
    var billState = _billState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllComplaints(object : LoginCallback<List<Complaint>> {
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
            serviceState.collect { service ->
                service?.let {
                    var spareAmount = service.spareParts?.sumOf { it.price } ?: 0.0
                    var complaintsAmount = service.complaints?.sumOf { it.price } ?: 0.0

                    _billState.value = Bill(
                        service.complaints,
                        service.spareParts,
                        totalAmount = spareAmount + complaintsAmount,
                        service.startDate
                    )

                }
            }
        }

    }
}