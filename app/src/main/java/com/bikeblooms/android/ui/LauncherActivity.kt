package com.bikeblooms.android.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.bikeblooms.android.MainActivity
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.ui.authentication.AuthenticationActivity
import com.bikeblooms.android.ui.home.UserViewModel
import com.bikeblooms.android.util.SharedPrefHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel by viewModels()

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper
    var isLoading: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition(condition = { isLoading })
        }
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            if (sharedPrefHelper.isLoggedIn()) {
                userViewModel.getCurrentUser()
                userViewModel.userState.collectLatest { userResponse ->
                    when (userResponse) {
                        is ApiResponse.Success -> {
                            doSplashWork(500)
                        }

                        is ApiResponse.Error -> {
                            showToast("Some problem with the previous login. Try later")
                            delay(1000)
                            finish()
                        }

                        is ApiResponse.Empty -> {}
                        is ApiResponse.Loading -> {}
                        null -> {}
                    }
                }
            } else {
                doSplashWork(2000)
            }
        }
    }

    suspend fun doSplashWork(time: Long) {
        delay(time)
        isLoading = false
        val intent = Intent(
            this@LauncherActivity, if (sharedPrefHelper.isLoggedIn()) {
                MainActivity::class.java
            } else AuthenticationActivity::class.java
        )
        startActivity(intent)
        finish()
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}