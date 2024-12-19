package com.bikeblooms.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Spare(
    var name: String = "",
    var price: Double = 0.0,
    var brand: String = "",
    var spareType: SpareType = SpareType.ALL
) : Parcelable

@Parcelize
enum class SpareType(var title: String) : Parcelable {
    ALL("ALL"), ENGINE_OIL("Engine Oil")
}

@Parcelize
class SpareListArgs : ArrayList<Spare>(), Parcelable