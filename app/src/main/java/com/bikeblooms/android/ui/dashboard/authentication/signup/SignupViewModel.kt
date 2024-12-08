package com.bikeblooms.android.ui.dashboard.authentication.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.User
import com.bikeblooms.android.model.UserValidator
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
class SignupViewModel @Inject constructor() : ViewModel() {
    private var _loadingState = MutableStateFlow<ApiResponse<FirebaseUser>>(ApiResponse.Empty())
    val loadingState = _loadingState.asStateFlow()

    private var _userValidState = MutableStateFlow<String>("")
    val userValidState = _userValidState.asStateFlow()

    private var _notifyState = MutableSharedFlow<ApiResponse<String>>()
    val notifyState = _notifyState.asSharedFlow()


    fun createUser(user: User) {
        _loadingState.value = ApiResponse.Loading()
        viewModelScope.launch {
            Firebase.auth.createUserWithEmailAndPassword(user.emailId, user.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.user?.let {
                            _loadingState.value = ApiResponse.Success(it)
                            viewModelScope.launch {
                                withContext(Dispatchers.Main) {
                                    _notifyState.emit(ApiResponse.Success("Successfully logged in"))
                                }
                            }
                        }
                    }
                }.addOnCanceledListener {
                    viewModelScope.launch {
                        withContext(Dispatchers.Main) {
                            _notifyState.emit(ApiResponse.Error("Successfully logged in"))
                        }
                    }
                }.addOnFailureListener { res ->
                    _loadingState.value = ApiResponse.Error(res.message)
                    viewModelScope.launch {
                        withContext(Dispatchers.Main) {
                            _notifyState.emit(ApiResponse.Error(res.message))
                        }
                    }
                }
        }
    }

    fun validateUser(user: User): String {
        val isValid = UserValidator().isValidForSignUp(user)
        _userValidState.value = isValid
        return isValid
    }
}