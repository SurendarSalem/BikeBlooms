package com.bikeblooms.android.ui.authentication.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.User
import com.bikeblooms.android.model.UserValidator
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
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
class SignupViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private var _loadingState = MutableStateFlow<ApiResponse<User>>(ApiResponse.Empty())
    val loadingState = _loadingState.asStateFlow()

    private var _userValidState = MutableStateFlow<String>("")
    val userValidState = _userValidState.asStateFlow()

    private var _notifyState = MutableSharedFlow<ApiResponse<String>>()
    val notifyState = _notifyState.asSharedFlow()
    val db = Firebase.firestore

    fun createUser(user: User) {
        _loadingState.value = ApiResponse.Loading()
        viewModelScope.launch {
            repository.signup(user, object : LoginCallback<FirebaseUser?> {
                override fun onSuccess(userFromFB: FirebaseUser?) {
                    userFromFB?.let {
                        user.firebaseId = userFromFB.uid
                        addUsersInFireStore(user)
                    }
                }

                override fun onError(message: String) {
                    viewModelScope.launch {
                        withContext(Dispatchers.Main) {
                            _notifyState.emit(ApiResponse.Error(message))
                        }
                    }
                }
            })
        }
    }

    fun validateUser(user: User): String {
        val isValid = UserValidator().isValidForSignUp(user)
        _userValidState.value = isValid
        return isValid
    }

    fun addUsersInFireStore(user: User) {
        repository.addUser(user, object : LoginCallback<User> {
            override fun onSuccess(user: User) {
                _loadingState.value = ApiResponse.Success(user)
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        _notifyState.emit(ApiResponse.Success("Successfully registered"))
                    }
                }
            }

            override fun onError(message: String) {
                _loadingState.value = ApiResponse.Error(message)
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        _notifyState.emit(ApiResponse.Error(message))
                    }
                }
            }

        })
    }
}