package com.bikeblooms.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Bill(
    var hiddenCharges: Double = 0.0,
    var totalAmount: Double = 0.0,
    var billDate: Date? = null
) : Parcelable {

}