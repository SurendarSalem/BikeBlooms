package com.bikeblooms.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Complaint(
    var isSelected: Boolean = false,
    var name: String = "",
    var price: Double = 0.0
) :
    Parcelable