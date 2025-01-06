package com.bikeblooms.android.ui.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bikeblooms.android.databinding.FragmentPhoneAuthBinding
import com.bikeblooms.android.model.LoginState
import com.bikeblooms.android.ui.authentication.phoneauth.PhoneAuthViewModel
import com.bikeblooms.android.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhoneAuthFragment : BaseFragment() {

    private lateinit var binding: FragmentPhoneAuthBinding
    private val phoneAuthViewModel: PhoneAuthViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPhoneAuthBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.progressBar
        observeStates()
        binding.initViews()
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            phoneAuthViewModel.otpRequestState.collectLatest { result ->
                when (result) {
                    is LoginState.Empty -> {}
                    is LoginState.Error -> {
                        hideProgress()
                    }

                    is LoginState.Loading -> {
                        showProgress()
                    }

                    is LoginState.OnCodeSent -> {
                        hideProgress()
                        binding.tvLoginError.text = result.verificationId
                    }

                    is LoginState.OnVerificationCompleted -> {
                        binding.tvLoginError.text = "Login successful"
                        hideProgress()
                    }

                    is LoginState.OnVerificationFailed -> {
                        binding.tvLoginError.text = result.message
                        hideProgress()
                    }

                    is LoginState.Success -> {
                        binding.tvLoginError.text = result.data.message
                        hideProgress()
                    }
                }
            }
        }

    }

    fun FragmentPhoneAuthBinding.initViews() {
        btnLogin.setOnClickListener {
            val phoneNum = etPhoneNum.text.toString()
            phoneAuthViewModel.requestCode(phoneNum)
        }
    }
}
