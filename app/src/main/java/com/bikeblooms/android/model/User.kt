package com.bikeblooms.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable, Cloneable {
    override fun clone(): User {
        return User(
            name,
            mobileNum,
            emailId,
            password,
            confirmPassword,
            firebaseId,
            userType,
            vehicles,
            fcmToken
        )
    }

    open fun deepCopy(): User {
        return clone()
    }
}

@Parcelize
enum class UserType : Parcelable {
    ADMIN, CONSUMER, VENDOR
}

fun User.isAdmin(): Boolean {
    return this.userType == UserType.ADMIN
}