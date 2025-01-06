package com.bikeblooms.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Charge(var name: String = "", var price: Double = 0.0) : Parcelable