package com.bikeblooms.android.ui.spares

import androidx.lifecycle.ViewModel
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.SpareRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.SpareCategory
import com.bikeblooms.android.model.SpareItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SparesViewModel @Inject constructor(val spareRepository: SpareRepository) : ViewModel() {

    private var _sparesAndAccessoriesState = MutableStateFlow<ApiResponse<List<SpareCategory>>>(
        ApiResponse.Empty()
    )
    val sparesAndAccessoriesState = _sparesAndAccessoriesState.asStateFlow()

    fun getAllSparesAndAccessories() {
        _sparesAndAccessoriesState.value = ApiResponse.Loading()
        spareRepository.getAllSparesAndAccessories(object : LoginCallback<List<SpareCategory>> {
            override fun onSuccess(spareCategories: List<SpareCategory>) {
                _sparesAndAccessoriesState.value = ApiResponse.Success(spareCategories)
            }

            override fun onError(message: String) {
                _sparesAndAccessoriesState.value = ApiResponse.Error(message)
            }

        })
    }

    fun updateSpare(item: SpareItem) {

    }
}