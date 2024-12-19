package com.bikeblooms.android.util

import android.util.Log
import com.bikeblooms.android.util.FirebaseConstants.FCM.QueryParams.AUTHORIZATION
import com.bikeblooms.android.util.FirebaseConstants.FCM.QueryParams.BODY
import com.bikeblooms.android.util.FirebaseConstants.FCM.QueryParams.CONTENT_TYPE
import com.bikeblooms.android.util.FirebaseConstants.FCM.QueryParams.DATA
import com.bikeblooms.android.util.FirebaseConstants.FCM.QueryParams.MESSAGE
import com.bikeblooms.android.util.FirebaseConstants.FCM.QueryParams.NOTIFICATION
import com.bikeblooms.android.util.FirebaseConstants.FCM.QueryParams.TITLE
import com.bikeblooms.android.util.FirebaseConstants.FCM.QueryParams.TO
import com.bikeblooms.android.util.FirebaseConstants.FCM.QueryParams.TOPIC
import com.bikeblooms.android.util.FirebaseConstants.FCM.URL_FIREBASE_MESSAGING_SEND_MSG
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

object FCMPushNotificationProvider {

    fun sendMessage(
        userFcmToken: String?,
        title: String,
        body: String,
        isTopic: Boolean = false,
        topicName: String,
        bodyMap: MutableMap<String, String>,
    ) {
        val okhttpClient = OkHttpClient()
        val jsonMainObj = JSONObject()
        try {
            val jsonMessage = JSONObject()
            if (isTopic) {
                jsonMessage.put(TOPIC, topicName)
            } else {
                jsonMessage.put(TO, userFcmToken)
            }
            val jsonNotification = JSONObject()
            jsonNotification.put(TITLE, title)
            jsonNotification.put(BODY, body)
            jsonMessage.put(NOTIFICATION, jsonNotification)
            val jsonData = JSONObject()
            bodyMap.keys.forEach {
                jsonData.put(it, bodyMap[it])
            }
            jsonMessage.put(DATA, jsonData)
            jsonMainObj.put(MESSAGE, jsonMessage)
        } catch (e: JSONException) {
        }
        val requestBody = RequestBody.create(
            FirebaseConstants.FCM.QueryParams.mediaTypeJson, jsonMainObj.toString()
        )
        val accessToken = FCMTokenGenerator().getAccessToken()
        val request = Request.Builder().url(URL_FIREBASE_MESSAGING_SEND_MSG).post(requestBody)
            .addHeader(AUTHORIZATION, "Bearer $accessToken")
            .addHeader(CONTENT_TYPE, FirebaseConstants.FCM.QueryParams.mediaTypeJson.toString())
            .build()
        try {
            val response = okhttpClient.newCall(request).execute()
            Log.d("Surendar", response.toString())
        } catch (e: IOException) {
            Log.d("Surendar", e.toString())
        }

    }
}