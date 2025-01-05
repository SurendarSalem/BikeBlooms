package com.bikeblooms.android;

import android.app.Application
import android.os.StrictMode
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
public class BikeBloomsApp : Application() {
    @Override
    override fun onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(applicationContext)
        FirebaseAuth.getInstance().firebaseAuthSettings.setAppVerificationDisabledForTesting(false);
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        Places.initialize(applicationContext, "AIzaSyB1E_XAP2VhvW2c3aFIAZywY6jcgvseucc")
    }
}
