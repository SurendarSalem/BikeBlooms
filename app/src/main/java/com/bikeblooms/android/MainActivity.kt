package com.bikeblooms.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bikeblooms.android.data.FirebaseHelper
import com.bikeblooms.android.databinding.ActivityMainBinding
import com.bikeblooms.android.util.SharedPrefHelper
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        getFirebaseTokenForCurrentUser()
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_my_vehicles, R.id.navigation_bookings, R.id.navigation_profile
            )
        )
        binding.toolbar.navigationIcon = null
        setSupportActionBar(binding.toolbar)
        actionBar?.setDisplayShowHomeEnabled(false)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun getFirebaseTokenForCurrentUser() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "getFirebaseTokenForCurrentUser() failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            Log.d("MainActivity", "getFirebaseTokenForCurrentUser --> $token")
            sendRegistrationToServer(token)
        })
    }

    private fun sendRegistrationToServer(token: String) {
        val userId = sharedPrefHelper.getUserFirebaseId()
        FirebaseHelper().updateUserFcmToken(userId, token)
    }
}