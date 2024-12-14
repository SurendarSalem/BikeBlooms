package com.bikeblooms.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.User
import com.bikeblooms.android.util.SharedPrefHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    var prefHelper: SharedPrefHelper, private val repository: Repository
) : ViewModel() {

    private var _userState = MutableStateFlow<ApiResponse<User>>(ApiResponse.Empty())
    val userState = _userState.asStateFlow()

    fun getCurrentUser() {
        viewModelScope.launch {
            if (prefHelper.isLoggedIn()) {
                repository.getUser(
                    prefHelper.getUserFirebaseId(),
                    object : LoginCallback<ApiResponse<User>> {
                        override fun onSuccess(t: ApiResponse<User>) {
                            t.data?.let {
                                AppState.user = t.data
                                _userState.value = ApiResponse.Success(t.data)
                            }
                        }

                        override fun onError(message: String) {
                            _userState.value = ApiResponse.Error(message)
                        }
                    })
            }
        }
    }
}