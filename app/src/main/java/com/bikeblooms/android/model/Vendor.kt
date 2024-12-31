package com.bikeblooms.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vendor(var shop: Shop? = null, var isActive: Boolean = false) : User(), Parcelable

@Parcelize
data class Shop(var shopId: String = "",var shopName: String = "", var address: Address? = null) : Parcelable {}

@Parcelize
data class Address(
    var latitude: Double = 0.0, var longitude: Double = 0.0, var address: String = ""
) : Parcelable {}