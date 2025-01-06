package com.bikeblooms.android.data

import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Brand
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.User
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.model.VehicleType
import com.bikeblooms.android.model.Vendor
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class Repository @Inject constructor(
    private val roomDBHelper: RoomDBHelper, private val firebaseHelper: FirebaseHelper
) {

    fun login(user: User, callback: LoginCallback<FirebaseUser?>) {
        firebaseHelper.login(user, callback)
    }

    fun signup(user: User, callback: LoginCallback<FirebaseUser?>) {
        firebaseHelper.signup(user, callback)
    }

    fun addUser(
        user: User, callback: LoginCallback<User>
    ) {
        firebaseHelper.addUser(user, callback)
    }

    fun addVendor(
        vendor: Vendor, callback: LoginCallback<Vendor>
    ) {
        firebaseHelper.addVendor(vendor, callback)
    }

    fun updateVendor(
        user: User, callback: LoginCallback<User>
    ) {
        firebaseHelper.updateVendor((user as Vendor), callback)
    }

    fun getAllVehicles(
        vehicleType: VehicleType, callback: LoginCallback<HashMap<Brand, List<Vehicle>>>
    ) {
        firebaseHelper.getAllVehicles(vehicleType, callback)
    }

    fun getUser(uid: String, callback: LoginCallback<ApiResponse<User>>) {
        firebaseHelper.getUserDetails(uid, callback)
    }

    fun addService(service: Service, callback: LoginCallback<Service>) {
        firebaseHelper.addService(service, callback)
    }

    fun setPassword(
        email: String, callback: LoginCallback<String>
    ) {
        firebaseHelper.setPassword(email, callback)
    }

    fun requestCode(
        mobileNum: String, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber("+91$mobileNum") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


}