package com.bikeblooms.android.ui.authentication.login

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bikeblooms.android.AdminActivity
import com.bikeblooms.android.MainActivity
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentLoginBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.User
import com.bikeblooms.android.model.isAdmin
import com.bikeblooms.android.ui.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : BaseFragment() {

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
        tvResetPassword.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("Do you want to reset password. Mail will send be sent to your email id")
                .setPositiveButton(getString(
                    R.string.send
                ), object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        val emailId = etEmailId.text.toString()
                        if (emailId.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailId)
                                .matches()
                        ) {
                            viewModel.resetPassword(emailId)
                        } else {
                            showToast("Please enter a valid email id")
                        }
                    }
                }).show()
        }
        tvBecomePartner.setOnClickListener {
            findNavController().navigate(R.id.navigation_vendor_request)
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
        viewLifecycleOwner.lifecycleScope.launch {
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userValidState.collectLatest { userValidState ->
                if (userValidState == "success") {
                    hideError()
                } else {
                    showError(userValidState)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collectLatest { notifyState ->
                when (notifyState) {
                    is ApiResponse.Success -> {
                        showToast(notifyState.data.toString())
                        val className = if (AppState.user?.isAdmin() == true) {
                            AdminActivity::class.java
                        } else {
                            MainActivity::class.java
                        }
                        val intent = Intent(requireContext(), className)
                        startActivity(intent)
                        requireActivity().finish()
                    }

                    is ApiResponse.Error -> {
                        showToast(notifyState.message.toString())
                    }

                    else -> {}
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notifyState.collectLatest { notifyState ->
                showToast(notifyState)
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
