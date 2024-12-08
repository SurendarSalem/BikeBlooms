package com.bikeblooms.android.ui.dashboard.authentication.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentSignupBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.User
import com.bikeblooms.android.ui.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SignupFragment() : BaseFragment() {

    private val viewModel: SignupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    lateinit var user: User

    private lateinit var binding: FragmentSignupBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeStates()
        binding.initViews()
    }

    private fun createUserFromInput(): User {
        return User().apply {
            name = binding.etName.text.toString()
            mobileNum = binding.etMobileNumber.text.toString()
            emailId = binding.etEmailId.text.toString()
            password = binding.etPassword.text.toString()
            confirmPassword = binding.etConfirmPassword.text.toString()
        }
    }


    private fun observeStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingState.collectLatest { result ->
                    when (result) {
                        is ApiResponse.Loading -> {
                            showProgress()
                        }

                        else -> {
                            hideProgress()
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userValidState.collectLatest {
                    if (it == "success") {
                        binding.tvError.visibility = View.GONE
                    } else {
                        binding.tvError.visibility = View.VISIBLE
                        binding.tvError.text = it
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.notifyState.collect { state ->
                when (state) {
                    is ApiResponse.Error -> {
                        showToast(state.message.toString())
                    }

                    is ApiResponse.Success -> {
                        showToast(state.data.toString())
                        val navController = findNavController()
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "emailId",
                            user.emailId
                        )
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "password",
                            user.password
                        )
                        navController.popBackStack()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun FragmentSignupBinding.initViews() {
        tvLogin.setOnClickListener {
            findNavController().navigate(R.id.navigation_login)
        }
        this@SignupFragment.progressBar = binding.progressBar
        btnSignUp.setOnClickListener {
            user = createUserFromInput()
            val isValid = viewModel.validateUser(user)
            if (isValid == "success") {
                viewModel.createUser(user)
            }
        }
    }
}

