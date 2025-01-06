package com.bikeblooms.android.model

import com.google.firebase.auth.FirebaseUser

class PhoneAuthResponse(
    val success: Boolean,
    val message: String,
    val verificationId: String,
    val user: FirebaseUser?
)