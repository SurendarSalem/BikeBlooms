package com.bikeblooms.android.model

import androidx.databinding.BaseObservable

data class User(
    var name: String,
    var mobileNum: String,
    var emailId: String,
    var password: String,
    var confirmPassword: String,
    var firebaseId: String,
    var vehicles: List<Vehicle>
) : BaseObservable() {
    constructor() : this("", "", "", "", "", "", emptyList())
}