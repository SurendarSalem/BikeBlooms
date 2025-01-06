package com.bikeblooms.android.ui.authentication.phoneauth

import androidx.lifecycle.ViewModel
import com.bikeblooms.android.data.Repository
import com.bikeblooms.android.model.LoginState
import com.bikeblooms.android.model.PhoneAuthResponse
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PhoneAuthViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private var _otpRequestState =
        MutableStateFlow<LoginState>(LoginState.Empty())
    val otpRequestState = _otpRequestState.asStateFlow()

    fun requestCode(mobileNum: String) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (e is FirebaseAuthInvalidCredentialsException) {
                    _otpRequestState.value =
                        LoginState.OnVerificationFailed("Invalid request ${e.message}")
                } else if (e is FirebaseTooManyRequestsException) {
                    _otpRequestState.value = LoginState.OnVerificationFailed("Too many requests")
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    _otpRequestState.value = LoginState.OnVerificationFailed("Missing activity")
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                _otpRequestState.value = LoginState.OnCodeSent(
                    verificationId
                )
            }
        }
        repository.requestCode(mobileNum, callbacks)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    _otpRequestState.value = LoginState.Success(
                        PhoneAuthResponse(
                            true, "Login successful", "", user
                        )
                    )
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        _otpRequestState.value = LoginState.Error("Invalid code")
                    } else {
                        _otpRequestState.value =
                            LoginState.Error(task.exception?.message.toString())
                    }
                }
            }
    }

}