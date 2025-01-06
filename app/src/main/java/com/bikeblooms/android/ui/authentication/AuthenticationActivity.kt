package com.bikeblooms.android.ui.authentication

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.ActivityAuthenticationBinding
import com.bikeblooms.android.ui.Utils
import com.bikeblooms.android.ui.openAppSystemSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity() : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    private val multiplePermissionContract =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            // permissionStatusMap is of type <String, Boolean>
            // if all permissions accepted
            if (!permissionsStatusMap.containsValue(false)) {

            } else {
                Utils.showAlertDialog(
                    context = this,
                    message = "Please approve all the permissions before opening the app",
                    positiveBtnText = "Open Settings",
                    positiveBtnCallback = {
                        finish()
                        this.openAppSystemSettings()
                    },
                    negativeBtnText = "Cancel",
                    negativeBtnCallback = {
                        finish()
                    },
                    nonCancellable = false
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkForPermissions()
        var binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(emptySet())
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun checkForPermissions() {
        val arr = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CALL_PHONE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arr.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        multiplePermissionContract.launch(arr.toTypedArray())
    }

    override fun onSupportNavigateUp(): Boolean {
        if (navController.currentDestination?.id == navController.graph.startDestinationId) {
            finish()
            return true
        } else {
            return navController.navigateUp()
        }
    }
}