package com.bikeblooms.android.data

import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Brand
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.User
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.model.VehicleType
import com.bikeblooms.android.util.AppConstants.VEHICLES
import com.bikeblooms.android.util.FirebaseConstants.Bike.BIKE_BRANDS
import com.bikeblooms.android.util.FirebaseConstants.Bike.BIKE_MODELS
import com.bikeblooms.android.util.FirebaseConstants.Car.CAR_BRANDS
import com.bikeblooms.android.util.FirebaseConstants.Car.CAR_MODELS
import com.bikeblooms.android.util.FirebaseConstants.SERVICES
import com.bikeblooms.android.util.FirebaseConstants.USERS
import com.bikeblooms.android.util.FirebaseConstants.USER_VEHICLES
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirebaseHelper {
    fun login(user: User, callback: LoginCallback<FirebaseUser?>) {
        Firebase.auth.signInWithEmailAndPassword(user.emailId, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val usersFromFb = task.result.user
                    callback.onSuccess(usersFromFb)
                }
            }.addOnFailureListener { res ->
                callback.onError(res.message.toString())
            }.addOnCanceledListener {
                callback.onError("Something went wrong")
            }

    }

    fun signup(user: User, callback: LoginCallback<FirebaseUser?>) {
        Firebase.auth.createUserWithEmailAndPassword(user.emailId, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val usersFromFb = task.result.user
                    callback.onSuccess(usersFromFb)
                }
            }.addOnFailureListener { res ->
                callback.onError(res.message.toString())
            }.addOnCanceledListener {
                callback.onError("Something went wrong")
            }
    }

    fun addUser(user: User, callback: LoginCallback<User>) {
        Firebase.firestore.collection(USERS).document(user.firebaseId).set(user)
            .addOnSuccessListener {
                callback.onSuccess(user)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }

    fun getAllVehicles(
        vehicleType: VehicleType, callback: LoginCallback<HashMap<Brand, List<Vehicle>>>
    ) {
        var vehiclesMap = HashMap<Brand, List<Vehicle>>()
        val brandCollectionName: String = if (vehicleType == VehicleType.BIKE) {
            BIKE_BRANDS
        } else {
            CAR_BRANDS
        }
        val brandDocumentName: String = if (vehicleType == VehicleType.BIKE) {
            BIKE_MODELS
        } else {
            CAR_MODELS
        }
        Firebase.firestore.collection(brandCollectionName).get().addOnSuccessListener { brands ->
            brands.documents.forEach { brand ->
                val vehicleBrand = Brand(brand.id, (brand.data?.get("Name") ?: "") as String)
                brand.reference.collection(brandDocumentName).get()
                    .addOnSuccessListener { vehicles ->
                        var vehicleList = mutableListOf<Vehicle>()
                        vehicles.documents.forEach { vehicle ->
                            val vehicleItem = vehicle.toObject(Vehicle::class.java)
                            vehicleItem?.let { vehicleList.add(it) }
                        }
                        vehiclesMap[vehicleBrand] = vehicleList
                        if (vehiclesMap.keys.size == brands.size()) {
                            callback.onSuccess(vehiclesMap)
                        }
                    }.addOnFailureListener {
                        callback.onError(it.message.toString())
                    }
            }
        }.addOnFailureListener {
            callback.onError(it.message.toString())
        }
    }

    fun getUserDetails(uid: String, callback: LoginCallback<ApiResponse<User>>) {
        if (uid.isEmpty()) {
            callback.onError("Something went wrong")
        } else {
            Firebase.firestore.collection(USERS).document(uid).get()
                .addOnSuccessListener { result ->
                    val user = result.toObject<User>(User::class.java)
                    user?.let {
                        callback.onSuccess(ApiResponse.Success(user))
                    } ?: run {
                        callback.onError("Something went wrong")
                    }
                }.addOnFailureListener {
                    callback.onError(it.message.toString())
                }.addOnCanceledListener {
                    callback.onError("Something went wrong")
                }
        }
    }

    fun addVehicle(vehicle: Vehicle, firebaseId: String, callback: LoginCallback<Vehicle>) {
        Firebase.firestore.collection(USER_VEHICLES).document(firebaseId).collection(VEHICLES)
            .document(vehicle.regNo).set(vehicle).addOnSuccessListener {
                vehicle
                callback.onSuccess(vehicle)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }

    fun getMyVehicles(uid: String, callback: LoginCallback<List<Vehicle>>) {
        Firebase.firestore.collection(USER_VEHICLES).document(uid).collection(VEHICLES).get()
            .addOnSuccessListener { result ->
                val vehicleList = mutableListOf<Vehicle>()
                result.forEach {
                    val vehicle = it.toObject<Vehicle>(Vehicle::class.java)
                    vehicleList.add(vehicle)
                }
                callback.onSuccess(vehicleList)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }

    fun addService(service: Service, callback: LoginCallback<Service>) {
        Firebase.firestore.collection(SERVICES).document().set(service)
            .addOnSuccessListener { result ->
                callback.onSuccess(service)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }

    fun getMyServices(uid: String, callback: LoginCallback<List<Service>>) {
        Firebase.firestore.collection(SERVICES).get()
            .addOnSuccessListener { result ->
                val vehicleList = mutableListOf<Service>()
                result.forEach {
                    val service = it.toObject<Service>(Service::class.java)
                    vehicleList.add(service)
                }
                vehicleList.filter {
                    it.firebaseId == uid
                }
                callback.onSuccess(vehicleList)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }

}