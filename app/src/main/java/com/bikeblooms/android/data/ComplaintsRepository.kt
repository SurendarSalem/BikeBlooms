package com.bikeblooms.android.data

import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.model.Complaint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComplaintsRepository @Inject constructor(private var firebaseHelper: FirebaseHelper) {
    fun getAllComplaints(callback: LoginCallback<List<Complaint>>) {
        firebaseHelper.getAllComplaints(callback)
    }

}