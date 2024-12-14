package com.bikeblooms.android.ui.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bikeblooms.android.databinding.FragmentAddServiceBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.getValue

@AndroidEntryPoint
class AddServiceFragment : BaseFragment() {

    val args: AddServiceFragmentArgs by navArgs()

    private val viewModel: AddServiceViewModel by viewModels()
    lateinit var binding: FragmentAddServiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddServiceBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.progressBar
        observeStates()
        binding.populateData(args.vehicle)
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addServiceState.collectLatest { result ->
                when (result) {
                    is ApiResponse.Loading -> {
                        showProgress()
                    }

                    is ApiResponse.Success -> {
                        hideProgress()
                    }

                    else -> hideProgress()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.notifyState.collectLatest { result ->
                when (result) {
                    is ApiResponse.Success -> {
                        showToast("Service request has been sent!")
                        closeThisScreen()
                    }

                    is ApiResponse.Error -> {
                        showToast(result.message.toString())
                    }

                    else -> {}
                }
            }
        }
    }

    fun closeThisScreen() {
        findNavController().popBackStack()
    }

    private fun FragmentAddServiceBinding.populateData(vehicle: Vehicle) {
        tvVehicleName.text = vehicle.name
        tvVehicleNumber.text = vehicle.regNo
        btnAddService.setOnClickListener {
            AppState.user?.firebaseId?.let {
                val complaints = etComplaints.text.toString()
                var service = Service(
                    vehicleName = vehicle.name,
                    vehicleId = vehicle.regNo,
                    regNum = vehicle.regNo,
                    startDate = Calendar.getInstance().time,
                    endDate = null,
                    spareParts = emptyList(),
                    complaint = complaints,
                    firebaseId = it
                )
                val validator = viewModel.validate(service)
                if (validator.isEmpty()) {
                    viewModel.addService(service)
                } else {
                    showToast(validator)
                }
            }
        }
    }
}

