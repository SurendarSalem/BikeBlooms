package com.bikeblooms.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SpareCategory(
    var id: String = "", var name: String = "", var items: List<SpareItem> = emptyList()
) : UISPareItem(), Parcelable

@Parcelize
data class SpareItem(
    var id: String = "",
    var name: String = "",
    var price: Double = 0.0,
    var qty: Int = 0,
    var spareCategoryId: String = ""
) : UISPareItem(), Parcelable

open class UISPareItem()

@Parcelize
class SpareCategories : ArrayList<SpareCategory>(), Parcelable
