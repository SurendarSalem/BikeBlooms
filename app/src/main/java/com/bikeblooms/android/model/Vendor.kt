package com.bikeblooms.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vendor(var shop: Shop? = null, var isActive: Boolean = false) : User(), Parcelable,
    Cloneable {
    override fun clone(): User {
        var user = super<User>.clone()
        return Vendor(shop?.copy(), isActive).apply {
            this.name = user.name
            this.mobileNum = user.mobileNum
            this.emailId = user.emailId
            this.password = user.password
            this.confirmPassword = user.confirmPassword
            this.firebaseId = user.firebaseId
            this.userType = UserType.VENDOR
            this.fcmToken = user.fcmToken
        }
    }

    override fun deepCopy(): User {
        return clone()
    }
}

@Parcelize
data class Shop(var shopId: String = "", var shopName: String = "", var address: Address? = null) :
    Parcelable {}

@Parcelize
data class Address(
    var latitude: Double = 0.0, var longitude: Double = 0.0, var address: String = ""
) : Parcelable {}