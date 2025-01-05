package com.bikeblooms.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vehicle(
    var name: String = "",
    var model: String = "",
    var brand: Brand? = null,
    var type: VehicleType? = null,
    var fuelType: FuelType? = null,
    var regNo: String = "",
    var firebaseId: String = "",
    var vehicleStatus: VehicleStatus = VehicleStatus.ACTIVE
) : Parcelable {
}

@Parcelize
class Brands : ArrayList<Brand>(), Parcelable

@Parcelize
class Vehicles : ArrayList<Vehicle>(), Parcelable

@Parcelize
enum class VehicleType(var value: String) : Parcelable {
    BIKE("Bike")
}

@Parcelize
enum class VehicleStatus(var value: String) : Parcelable {
    ACTIVE("Active"), INACTIVE("Inactive")
}

@Parcelize
enum class FuelType(var value: String) : Parcelable {
    PETROL("Petrol"), DIESEL("Diesel")
}

@Parcelize
data class Brand(val id: String = "", val name: String = "") : Parcelable
