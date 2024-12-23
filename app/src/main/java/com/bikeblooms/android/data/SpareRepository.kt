package com.bikeblooms.android.data

import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Spare
import com.bikeblooms.android.model.SpareType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpareRepository @Inject constructor(private var firebaseHelper: FirebaseHelper) {

    private var _allSparesState = MutableStateFlow<ApiResponse<List<Spare>>>(ApiResponse.Empty())
    val allSparesState = _allSparesState.asStateFlow()

    fun getAllSpares(type: SpareType = SpareType.ALL, callback: LoginCallback<List<Spare>>) {
        firebaseHelper.getAllSpares(type, callback)
    }

    fun getAllSparesAndReturn(isForceRefresh: Boolean = false, type: SpareType = SpareType.ALL) {
        if (isForceRefresh) {
            firebaseHelper.getAllSparesAndReturn(object : LoginCallback<List<Spare>> {
                override fun onSuccess(spares: List<Spare>) {
                    _allSparesState.value = ApiResponse.Success(spares)
                }

                override fun onError(message: String) {
                    _allSparesState.value = ApiResponse.Error(message)
                }
            }, type)
        }
    }

}