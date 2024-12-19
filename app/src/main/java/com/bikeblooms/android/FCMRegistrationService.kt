package com.bikeblooms.android

import android.util.Log
import com.bikeblooms.android.data.FirebaseHelper
import com.bikeblooms.android.util.NotificationHelper
import com.bikeblooms.android.util.SharedPrefHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FCMRegistrationService : FirebaseMessagingService() {
    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("onNewToken", "Token received --> $token")
        scope.launch {
            sendRegistrationToServer(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("Message Received", message.data.toString())
        NotificationHelper(applicationContext).showNotification(
            contentTitle = message.data["title"].toString(),
            messageBody = message.data["message"].toString()
        )

    }

    private fun sendRegistrationToServer(token: String) {
        val userId = sharedPrefHelper.getUserFirebaseId()
        FirebaseHelper().updateUserFcmToken(userId, token)
    }
}