package com.bikeblooms.android.util

import com.bikeblooms.android.model.VehicleType

const val BASE_URL = ""
val vehicleTypes = arrayOf(VehicleType.CAR, VehicleType.BIKE)

object AppConstants {
    const val BRAND = "brand"
    const val VEHICLE = "vehicle"
    const val CURRENT_VEHICLE = "current_vehicle"
    const val BRANDS = "brands"
    const val SOURCE = "source"
    const val VEHICLES = "vehicles"
}

object FirebaseConstants {
    const val USERS = "users"
    const val USER_VEHICLES = "user_vehicles"
    const val NAME = "Name"
    const val SERVICES = "services"

    object Bike {
        const val BIKE_BRANDS = "bikebrands"
        const val BIKE_MODELS = "bikes"
    }

    object Car {
        const val CAR_BRANDS = "carbrands"
        const val CAR_MODELS = "cars"
    }
}

fun String.toRegNum() = buildString {
    append(substring(0, 2))
    append(" ")
    append(substring(3, 5))
    append(" ")
    append(substring(6, 8))
    append(" ")
    append(
        substring(9, length - 1)
    )
}