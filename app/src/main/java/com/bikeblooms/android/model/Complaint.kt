package com.bikeblooms.android.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class Complaint(
    @Exclude var isSelected: Boolean = false, var name: String = "", var price: Double = 0.0
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        return this.name == (other as Complaint).name
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + isSelected.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + price.hashCode()
        return result
    }
}

@Parcelize
class ComplaintsList : ArrayList<Complaint>(), Parcelable