package com.bikeblooms.android.data

import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.model.Spare
import com.bikeblooms.android.model.SpareType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpareRepository @Inject constructor(private var firebaseHelper: FirebaseHelper) {
    fun getAllSpares(type: SpareType = SpareType.ALL, callback: LoginCallback<List<Spare>>) {
        firebaseHelper.getAllSpares(callback, type)
    }

}