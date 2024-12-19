package com.bikeblooms.android.ui.authentication.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.User
import com.bikeblooms.android.model.UserValidator
import com.bikeblooms.android.util.SharedPrefHelper
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    var prefHelper: SharedPrefHelper, private val repository: Repository
) : ViewModel() {
    private var _loadingState = MutableStateFlow<ApiResponse<FirebaseUser>>(ApiResponse.Empty())
    val loadingState = _loadingState.asStateFlow()

    private var _userValidState = MutableStateFlow<String>("")
    val userValidState = _userValidState.asStateFlow()

    private var _loginState = MutableSharedFlow<ApiResponse<String>>()
    val loginState = _loginState.asSharedFlow()

    private var _notifyState = MutableSharedFlow<String>()
    val notifyState = _notifyState.asSharedFlow()


    fun login(user: User) {
        _loadingState.value = ApiResponse.Loading()
        viewModelScope.launch {
            repository.login(user, object : LoginCallback<FirebaseUser?> {
                override fun onSuccess(firebaseUser: FirebaseUser?) {
                    firebaseUser?.let {
                        _loadingState.value = ApiResponse.Success(it)
                        viewModelScope.launch {
                            prefHelper.setLoggedIn(true)
                            prefHelper.setFirebaseUserID(firebaseUser.uid)
                            getCurrentUser(firebaseUser.uid)
                        }
                    }
                }

                override fun onError(message: String) {
                    viewModelScope.launch {
                        withContext(Dispatchers.Main) {
                            _loginState.emit(ApiResponse.Error(message))
                            _loadingState.value = ApiResponse.Error(message)
                        }
                    }
                }
            })
        }
    }

    fun getCurrentUser(uid: String) {
        repository.getUser(uid, object : LoginCallback<ApiResponse<User>> {
            override fun onSuccess(t: ApiResponse<User>) {
                t.data?.let {
                    AppState.user = t.data
                    viewModelScope.launch(Dispatchers.Main) {
                        _loginState.emit(ApiResponse.Success("Successfully logged in"))
                    }
                }
            }

            override fun onError(message: String) {
                viewModelScope.launch(Dispatchers.Main) {
                    _loginState.emit(ApiResponse.Error(message))
                }
            }
        })
    }

    fun validateUser(user: User): String {
        val isValid = UserValidator().isValidForLogin(user)
        _userValidState.value = isValid
        return isValid
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            repository.setPassword(email, object : LoginCallback<String> {
                override fun onSuccess(message: String) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _notifyState.emit("Reset password mail has been to your mail id")
                    }
                }

                override fun onError(message: String) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _notifyState.emit("Failed to send a mail to reset password")
                    }
                }

            })
        }
    }
}