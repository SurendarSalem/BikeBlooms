package com.bikeblooms.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bikeblooms.android.data.FirebaseHelper
import com.bikeblooms.android.data.SpareRepository
import com.bikeblooms.android.databinding.ActivityAdminBinding
import com.bikeblooms.android.util.FirebaseConstants.FCM.SERVICE_UPDATE
import com.bikeblooms.android.util.SharedPrefHelper
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AdminActivity() : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    lateinit var navController: NavController

    @Inject
    lateinit var spareRepository: SpareRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        getFirebaseTokenForCurrentUser()
        setContentView(binding.root)
        lifecycleScope.launch {
            spareRepository.getAllSparesAndReturn(isForceRefresh = true)
        }
        val navView: BottomNavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_admin) as NavHostFragment
        navController = navHostFragment.navController
        val set = mutableSetOf<Int>()
        navController.graph.toMutableList().toSet().forEach {
            set.add(it.id)
        }
        val appBarConfiguration = AppBarConfiguration(set)
        setSupportActionBar(binding.toolbar)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbar.findViewById<AppCompatTextView>(R.id.tv_custom_title).text =
                destination.label
        }
        binding.toolbar.findViewById<ShapeableImageView>(R.id.siv_profile).setOnClickListener {
            navController.navigate(R.id.navigation_profile)
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        Firebase.messaging.subscribeToTopic(SERVICE_UPDATE).addOnCompleteListener { task ->
            Log.d("New FCM token", "Subscribed")
        }
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}