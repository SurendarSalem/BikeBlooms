package com.bikeblooms.android.model

open class User(
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
    ADMIN, CONSUMER, VENDOR
}

fun User.isAdmin(): Boolean {
    return this.userType == UserType.ADMIN
}