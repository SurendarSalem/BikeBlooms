package com.bikeblooms.android.ui.dashboard.authentication.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentLoginBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.User
import com.bikeblooms.android.ui.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : BaseFragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.progressBar
        observeForBackStack()

        observeStates()

        binding.initViews()
    }

    private fun observeForBackStack() {
        val navController = findNavController()
        navController.currentBackStackEntry?.savedStateHandle?.let {
            it.getLiveData<String>("emailId").observe(viewLifecycleOwner) { emailId ->
                binding.etEmailId.setText(emailId)
            }
            it.getLiveData<String>("password").observe(viewLifecycleOwner) { password ->
                binding.etPassword.setText(password)
            }
        }
    }

    private fun FragmentLoginBinding.initViews() {
        tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.navigation_signup)
        }
        btnLogin.setOnClickListener {
            val user = createUserFromInput()
            val isValid = viewModel.validateUser(user)
            if (isValid == "success") {
                viewModel.login(user)
            }
        }
    }

    private fun FragmentLoginBinding.createUserFromInput(): User {
        val emailId = etEmailId.text.toString()
        val password = etPassword.text.toString()
        return User().apply {
            this.emailId = emailId
            this.password = password
        }
    }

    private fun observeStates() {
        lifecycleScope.launch {
            viewModel.loadingState.collectLatest { loadingState ->
                when (loadingState) {
                    is ApiResponse.Loading -> {
                        showProgress()
                    }

                    else -> {
                        hideProgress()
                    }

                }
            }
        }
        lifecycleScope.launch {
            viewModel.userValidState.collectLatest { userValidState ->
                if (userValidState == "success") {
                    hideError()
                } else {
                    showError(userValidState)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.loginState.collectLatest { notifyState ->
                when (notifyState) {
                    is ApiResponse.Success -> {
                        showToast(notifyState.data.toString())
                    }

                    is ApiResponse.Error -> {
                        showToast(notifyState.message.toString())
                    }

                    else -> {}
                }
            }
        }
    }

    private fun hideError() {
        binding.tvLoginError.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.tvLoginError.visibility = View.VISIBLE
        binding.tvLoginError.text = message
    }
}
