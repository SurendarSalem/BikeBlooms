package com.bikeblooms.android.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.NotifyState
import com.bikeblooms.android.model.User
import com.bikeblooms.android.model.Vendor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private var _updateUserState = MutableStateFlow<ApiResponse<User>>(ApiResponse.Empty())
    val updateUserState = _updateUserState.asStateFlow()
    val notifyState = MutableSharedFlow<NotifyState>()

    fun updateUser(user: User) {
        _updateUserState.value = ApiResponse.Loading()
        val callback = object : LoginCallback<User> {
            override fun onSuccess(user: User) {
                _updateUserState.value = ApiResponse.Success(user)
                viewModelScope.launch(Dispatchers.Main) {
                    notifyState.emit(NotifyState.Success("User details are updated!"))
                }
            }

            override fun onError(message: String) {
                _updateUserState.value = ApiResponse.Error(message)
                viewModelScope.launch(Dispatchers.Main) {
                    notifyState.emit(NotifyState.Success(message))
                }
            }

        }
        viewModelScope.launch {
            if (user is Vendor) {
                repository.updateVendor(user, callback)
            } else {
                repository.addUser(user, callback)
            }
        }
    }

    fun isValid(user: User): String {
        if (user.name.isEmpty()) {
            return "Name must be not empty"
        }
        if (user.name.length < 3) {
            return "Name must contain atleast 3 letters"
        }
        if (user.mobileNum.isEmpty()) {
            return "Mobile Number must be not empty"
        }
        if (user.mobileNum.length < 10) {
            return "Please enter valid mobile number"
        }
        if (user is Vendor) {
            user.shop?.let {
                if (it.shopName.isEmpty()) {
                    return "Shop Name must be not empty"
                }
                if (it.shopName.length < 3) {
                    return "Shop Name must contain atleast 3 letters"
                }
            }
        }
        return ""
    }
}