package com.bikeblooms.android.ui.spares

import androidx.lifecycle.ViewModel
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.SpareRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.SpareItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AddUpdateSpareViewModel @Inject constructor(val spareRepository: SpareRepository) :
    ViewModel() {

    private var _updateSpareState = MutableStateFlow<ApiResponse<SpareItem>>(
        ApiResponse.Empty()
    )
    val updateSpareState = _updateSpareState.asStateFlow()

    fun updateSpare(item: SpareItem) {
        _updateSpareState.value = ApiResponse.Loading()
        spareRepository.updateSpare(item, object : LoginCallback<SpareItem> {
            override fun onSuccess(spareItem: SpareItem) {
                _updateSpareState.value = ApiResponse.Success(spareItem)
            }

            override fun onError(message: String) {
                _updateSpareState.value = ApiResponse.Error(message)
            }
        })
    }
}