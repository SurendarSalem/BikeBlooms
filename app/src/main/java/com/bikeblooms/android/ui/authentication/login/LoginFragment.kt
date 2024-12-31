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
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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

    private fun login() {
        val options = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber("+918778054457") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            //signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            showToast("Verification failed " + e.message)

            if (e is FirebaseAuthInvalidCredentialsException) {
                showToast("Invalid request ${e.message}")
            } else if (e is FirebaseTooManyRequestsException) {
                showToast("Too many requests")
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                showToast("Missing activity")
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            showToast("Code sent $verificationId")

        }
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
