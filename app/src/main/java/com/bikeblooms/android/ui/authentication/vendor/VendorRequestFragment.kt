package com.bikeblooms.android.ui.authentication.vendor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bikeblooms.android.databinding.FragmentVendorRequestBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.NotifyState
import com.bikeblooms.android.model.UserValidator
import com.bikeblooms.android.model.Vendor
import com.bikeblooms.android.ui.Utils
import com.bikeblooms.android.ui.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VendorRequestFragment : BaseFragment() {

    private val viewModel: VendorRequestViewModel by viewModels()
    private lateinit var binding: FragmentVendorRequestBinding
    var vendor = Vendor()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentVendorRequestBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.progressBar
        observeState()
        binding.initViews()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addVendorState.collectLatest { result ->
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notifyState.collectLatest { result ->
                when (result) {
                    is NotifyState.Success -> {
                        Utils.showAlertDialog(requireContext(),
                            "You are registered. Admin will get back to you soon",
                            "OK",
                            positiveBtnCallback = {
                                findNavController().popBackStack()
                            })
                    }

                    is NotifyState.Error -> {
                        showToast(result.message)
                    }
                }
            }
        }
    }

    private fun FragmentVendorRequestBinding.initViews() {
        btnSignUp.setOnClickListener {
            val name = etName.text.toString()
            val mobileNum = etMobileNumber.text.toString()
            val emailId = etEmailId.text.toString()
            vendor.apply {
                this.name = name
                this.mobileNum = mobileNum
                this.emailId = emailId
            }
            val isValid = UserValidator().isValidVendorForRegister(vendor)
            if (isValid == "success") {
                tvError.text = ""
                viewModel.addVendor(vendor)

            } else {
                tvError.text = isValid
            }
        }
    }
}

