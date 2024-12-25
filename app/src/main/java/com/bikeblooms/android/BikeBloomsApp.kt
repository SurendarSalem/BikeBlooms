package com.bikeblooms.android;

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.bikeblooms.android.util.FirebaseConstants.FCM.SERVICE_UPDATE
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
public class BikeBloomsApp : Application() {
    @Override
    override fun onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(applicationContext);


        Firebase.messaging.subscribeToTopic(SERVICE_UPDATE).addOnCompleteListener { task ->
            Log.d("New FCM token", "Subscribed")
        }
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        Places.initialize(applicationContext, "AIzaSyB1E_XAP2VhvW2c3aFIAZywY6jcgvseucc")
    }
}
