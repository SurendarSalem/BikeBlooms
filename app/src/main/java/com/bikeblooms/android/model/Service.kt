package com.bikeblooms.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Calendar
import java.util.Date

@Parcelize
data class Service(
    var id: String = "",
    var vehicleName: String = "",
    var vehicleId: String = "",
    var regNum: String = "",
    var bookingDate: Date = Calendar.getInstance().time,
    var updateDate: Date? = Calendar.getInstance().time,
    var startDate: Date = Calendar.getInstance().time,
    var endDate: Date? = null,
    var spareParts: List<Spare>? = null,
    var complaints: List<Complaint>? = null,
    var complaint: String? = "",
    var firebaseId: String = "",
    var progress: Progress = Progress.PENDING,
    var serviceType: ServiceType? = null,
    var pickDrop: Boolean = false,
    var address: String = "",
    var bill: Bill? = null,
    var assignee: Vendor? = null,
    var hiddenCharges: Int = 0,
    var ownerFcmToken: String = "",
    var mobileNumber: String = "",
    var ownerName: String = ""
) : Parcelable {}

@Parcelize
enum class Progress(var title: String) : Parcelable {
    STARTED("Started"), PENDING("Pending"), IN_PROGRESS("In Progress"), COMPLETED("Completed"), CANCELLED(
        "Cancelled"
    )
}

@Parcelize
enum class ServiceType(var title: String) : Parcelable {
    GENERAL_SERVICE("General Service"), VEHICLE_REPAIR("Vehicle Repair")
}
