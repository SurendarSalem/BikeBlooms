package com.bikeblooms.android.data

import android.util.Log
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Brand
import com.bikeblooms.android.model.Complaint
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.Spare
import com.bikeblooms.android.model.SpareCategory
import com.bikeblooms.android.model.SpareItem
import com.bikeblooms.android.model.SpareType
import com.bikeblooms.android.model.User
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.model.VehicleStatus
import com.bikeblooms.android.model.VehicleType
import com.bikeblooms.android.model.Vendor
import com.bikeblooms.android.util.FirebaseConstants.Bike.BIKE_BRANDS
import com.bikeblooms.android.util.FirebaseConstants.Bike.BIKE_MODELS
import com.bikeblooms.android.util.FirebaseConstants.COMPLAINTS
import com.bikeblooms.android.util.FirebaseConstants.ITEMS
import com.bikeblooms.android.util.FirebaseConstants.SERVICES
import com.bikeblooms.android.util.FirebaseConstants.SPARES_AND_ACCESSORIES
import com.bikeblooms.android.util.FirebaseConstants.USERS
import com.bikeblooms.android.util.FirebaseConstants.USER_VEHICLES
import com.bikeblooms.android.util.FirebaseConstants.VENDORS
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

    fun addVendor(vendor: Vendor, callback: LoginCallback<Vendor>) {
        Firebase.firestore.collection(VENDORS).document().apply {
            vendor.firebaseId = id
            set(vendor).addOnSuccessListener { result ->
                callback.onSuccess(vendor)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
        }
    }

    fun updateVendor(vendor: Vendor, callback: LoginCallback<Vendor>) {
        Firebase.firestore.collection(VENDORS).document(vendor.firebaseId).set(vendor)
            .addOnSuccessListener {
                callback.onSuccess(vendor)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }

    fun getAllVehicles(
        vehicleType: VehicleType, callback: LoginCallback<HashMap<Brand, List<Vehicle>>>
    ) {
        var vehiclesMap = HashMap<Brand, List<Vehicle>>()
        val brandCollectionName: String = BIKE_BRANDS
        val brandDocumentName: String = BIKE_MODELS
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
        Firebase.firestore.collection(USER_VEHICLES).document(vehicle.regNo).set(vehicle)
            .addOnSuccessListener {
                vehicle
                callback.onSuccess(vehicle)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }

    fun getMyVehicles(uid: String, callback: LoginCallback<List<Vehicle>>) {
        Firebase.firestore.collection(USER_VEHICLES).whereEqualTo("firebaseId", uid).get()
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
        val document = Firebase.firestore.collection(SERVICES).document()
        document.set(service.apply {
            id = document.id
        }).addOnSuccessListener { result ->
            callback.onSuccess(service)
        }.addOnFailureListener {
            callback.onError(it.message.toString())
        }
    }

    fun getMyServices(
        uid: String, callback: LoginCallback<List<Service>>, isAdmin: Boolean = false
    ) {
        val collectionRef = Firebase.firestore.collection(SERVICES)
        val task = if (isAdmin) {
            collectionRef.get()
        } else {
            collectionRef.whereEqualTo("firebaseId", uid).get()
        }
        task.addOnSuccessListener { result ->
            val services = mutableListOf<Service>()
            result.forEach {
                val service = it.toObject<Service>(Service::class.java)
                service.id = it.id
                services.add(service)
            }
            callback.onSuccess(services)
        }.addOnFailureListener {
            callback.onError(it.message.toString())
        }
    }

    fun getServiceHistory(
        fieldName: String, value: String, callback: LoginCallback<List<Service>>
    ) {
        Firebase.firestore.collection(SERVICES).whereEqualTo(fieldName, value).get()
            .addOnSuccessListener { result ->
                val services = mutableListOf<Service>()
                result.forEach {
                    val service = it.toObject<Service>(Service::class.java)
                    service.id = it.id
                    services.add(service)
                }
                callback.onSuccess(services)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }


    fun updateUserFcmToken(userFirebaseId: String, token: String) {
        Firebase.firestore.collection(USERS).document(userFirebaseId)
            .update(mapOf("fcmToken" to token)).addOnSuccessListener {
                AppState.user?.fcmToken = token
                Log.d("FirebaseHelper", "Token updated to user in firestore")
            }.addOnFailureListener {
                Log.d("FirebaseHelper", "Token updated to user in firestore")
            }
    }

    fun getAllComplaints(callback: LoginCallback<List<Complaint>>) {
        Firebase.firestore.collection(COMPLAINTS).get().addOnSuccessListener { result ->
            val complaints = mutableListOf<Complaint>()
            result.forEach {
                val complaint = it.toObject<Complaint>(Complaint::class.java)
                complaints.add(complaint)
            }
            callback.onSuccess(complaints)
        }.addOnFailureListener {
            callback.onError(it.message.toString())
        }
    }

    fun getAllSpares(type: SpareType, callback: LoginCallback<List<Spare>>) {
        Firebase.firestore.collection(SPARES_AND_ACCESSORIES).document("engine_oil")
            .collection("items").get().addOnSuccessListener { result ->
                val spares = mutableListOf<Spare>()
                result.forEach {
                    val complaint = it.toObject<Spare>(Spare::class.java)
                    spares.add(complaint)
                }
                callback.onSuccess(spares)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }

    fun getAllSparesAndReturn(callback: LoginCallback<List<Spare>>, type: SpareType) {
        Firebase.firestore.collection(SPARES_AND_ACCESSORIES).document("engine_oil")
            .collection("items").get().addOnSuccessListener { result ->
                val spares = mutableListOf<Spare>()
                result.forEach {
                    val spare = it.toObject<Spare>(Spare::class.java)
                    spares.add(spare)
                }
                callback.onSuccess(spares)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }


    fun setPassword(emailId: String, callback: LoginCallback<String>) {
        Firebase.auth.sendPasswordResetEmail(emailId).addOnSuccessListener {
            callback.onSuccess("Success")
        }.addOnFailureListener { error ->
            callback.onError(error.message.toString())
        }
    }

    fun deleteVehicle(userId: String, vehicle: Vehicle, callback: LoginCallback<Vehicle>) {
        Firebase.firestore.collection(USER_VEHICLES).document(vehicle.regNo)
            .update("vehicleStatus", VehicleStatus.INACTIVE).addOnSuccessListener {
                callback.onSuccess(vehicle)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }

    fun getAllVendors(uid: String, callback: LoginCallback<List<Vendor>>) {
        Firebase.firestore.collection(VENDORS).get().addOnSuccessListener { result ->
            val vendors = mutableListOf<Vendor>()
            result.forEach {
                val vehicle = it.toObject<Vendor>(Vendor::class.java)
                vendors.add(vehicle)
            }
            callback.onSuccess(vendors)
        }.addOnFailureListener {
            callback.onError(it.message.toString())
        }
    }

    inline fun <reified T> getAllItems(tableName: String, callback: LoginCallback<List<T>>) {
        Firebase.firestore.collection(VENDORS).get().addOnSuccessListener { result ->
            val vendors = mutableListOf<T>()
            result.forEach {
                val vehicle = it.toObject<T>(T::class.java)
                vendors.add(vehicle)
            }
            callback.onSuccess(vendors)
        }.addOnFailureListener {
            callback.onError(it.message.toString())
        }
    }

    fun getAllSparesAndAccessories(callback: LoginCallback<List<SpareCategory>>) {
        Firebase.firestore.collection(SPARES_AND_ACCESSORIES).get()
            .addOnSuccessListener { sparesCategoriesResult ->
                val categoriesSize = sparesCategoriesResult.size()
                val spareCategories = mutableListOf<SpareCategory>()
                sparesCategoriesResult.forEach {
                    var spareCategory = it.toObject<SpareCategory>(SpareCategory::class.java)
                    spareCategory.id = it.id
                    Firebase.firestore.collection(SPARES_AND_ACCESSORIES).document(spareCategory.id)
                        .collection(ITEMS).get().addOnSuccessListener { sparesResult ->
                            val sparesSize = sparesResult.size()
                            val items = mutableListOf<SpareItem>()
                            sparesResult.forEach {
                                val spareItem = it.toObject<SpareItem>(SpareItem::class.java)
                                spareItem.id = it.id
                                spareItem.spareCategoryId = spareCategory.id
                                items.add(spareItem)
                                if (sparesSize == items.size) {
                                    spareCategory.items = items
                                    spareCategories.add(spareCategory)
                                }
                                if (categoriesSize == spareCategories.size) {
                                    callback.onSuccess(spareCategories)
                                }
                            }
                        }.addOnFailureListener {
                            callback.onError(it.message.toString())
                        }
                }
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }

    fun updateSpare(spareItem: SpareItem, callback: LoginCallback<SpareItem>) {
        Firebase.firestore.collection(SPARES_AND_ACCESSORIES).document(spareItem.spareCategoryId)
            .collection(ITEMS).document(spareItem.id).set(spareItem).addOnSuccessListener {
                callback.onSuccess(spareItem)
            }.addOnFailureListener {
                callback.onError(it.message.toString())
            }
    }

    fun getAllUserVehicles(callback: LoginCallback<List<Vehicle>>) {
        Firebase.firestore.collection(USER_VEHICLES).get().addOnSuccessListener {
            val vehicles = mutableListOf<Vehicle>()
            it.forEach {
                val vehicle = it.toObject<Vehicle>(Vehicle::class.java)
                vehicles.add(vehicle)
            }
            callback.onSuccess(vehicles)
        }.addOnFailureListener {
            callback.onError(it.message.toString())
        }
    }
}