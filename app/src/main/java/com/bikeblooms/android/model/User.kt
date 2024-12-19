package com.bikeblooms.android.model

data class User(
    var name: String = "",
    var mobileNum: String = "",
    var emailId: String = "",
    var password: String = "",
    var confirmPassword: String = "",
    var firebaseId: String = "",
    var userType: UserType = UserType.CONSUMER,
    var vehicles: List<Vehicle> = emptyList<Vehicle>(),
    var fcmToken: String = ""
)

enum class UserType {
    ADMIN, CONSUMER
}