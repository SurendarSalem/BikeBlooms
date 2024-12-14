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
    var regNo: String = ""
) : Parcelable {
}

@Parcelize
class Brands : ArrayList<Brand>(), Parcelable

@Parcelize
class Vehicles : ArrayList<Vehicle>(), Parcelable

@Parcelize
enum class VehicleType(var value: String) : Parcelable {
    CAR("Car"), BIKE("Bike")
}

@Parcelize
enum class FuelType(var value: String) : Parcelable {
    PETROL("Petrol"), DIESEL("Diesel")
}

@Parcelize
data class Brand(val id: String = "", val name: String = "") : Parcelable
