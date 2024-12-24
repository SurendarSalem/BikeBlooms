package com.bikeblooms.android.util

import com.bikeblooms.android.model.VehicleType
import okhttp3.MediaType.Companion.toMediaType

const val BASE_URL = ""
val vehicleTypes = arrayOf(VehicleType.BIKE)

object AppConstants {
    const val BRAND = "brand"
    const val VEHICLE = "vehicle"
    const val CURRENT_VEHICLE = "current_vehicle"
    const val BRANDS = "brands"
    const val SOURCE = "source"
    const val VEHICLES = "vehicles"
    const val SELECTED_SPARE = "selectedSpare"
    const val SELECTED_COMPLAINTS = "selectedComplaints"
}

object FirebaseConstants {
    const val USERS = "users"
    const val USER_VEHICLES = "user_vehicles"
    const val COMPLAINTS = "complaints"
    const val NAME = "Name"
    const val SERVICES = "services"
    const val SPARES = "spares"

    object Bike {
        const val BIKE_BRANDS = "bikebrands"
        const val BIKE_MODELS = "bikes"
    }

    object FCM {
        const val SERVICE_UPDATE = "serviceupdate"
        const val FIREBASE_MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging"
        const val URL_FIREBASE_MESSAGING_SEND_MSG =
            "https://fcm.googleapis.com/v1/projects/bike-blooms/messages:send"

        object QueryParams {
            const val TITLE = "title"
            const val BODY = "body"
            const val NOTIFICATION = "notification"
            const val MESSAGE = "message"
            const val DATA = "data"
            const val AUTHORIZATION = "authorization"
            const val CONTENT_TYPE = "content-type"
            const val TOPIC = "topic"
            const val TO = "token"
            val mediaTypeJson = "application/json".toMediaType()
        }
    }
}

fun String.toRegNum() = this.substring(0, 2) + " " + this.substring(2, 4) + " " + this.substring(
    4, 6
) + " " + this.substring(6, length)
