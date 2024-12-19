package com.bikeblooms.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Calendar
import java.util.Date

@Parcelize
data class Service(
    var vehicleName: String = "",
    var vehicleId: String = "",
    var regNum: String = "",
    var startDate: Date = Calendar.getInstance().time,
    var endDate: Date? = null,
    var spareParts: List<Spare>? = null,
    var complaints: List<Complaint>? = null,
    var complaint: String? = "",
    var firebaseId: String = "",
    var progress: Progress = Progress.STARTED,
    var serviceType: ServiceType = ServiceType.GENERAL_SERVICE,
    var pickDrop: Boolean = false
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