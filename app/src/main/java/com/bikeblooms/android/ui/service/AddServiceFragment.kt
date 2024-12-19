package com.bikeblooms.android.ui.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentAddServiceBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.ServiceType
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.model.Vehicles
import com.bikeblooms.android.ui.VehicleListDialogFragmentArgs
import com.bikeblooms.android.ui.base.BaseFragment
import com.bikeblooms.android.ui.service.compaints.AddComplaintsFragmentArgs
import com.bikeblooms.android.ui.vehicles.VehicleViewModel
import com.bikeblooms.android.util.toRegNum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.getValue

@AndroidEntryPoint
class AddServiceFragment : BaseFragment() {


    private val viewModel: ServiceViewModel by activityViewModels()
    private val vehicleViewModel: VehicleViewModel by activityViewModels()
    lateinit var binding: FragmentAddServiceBinding
    private var myVehicles = listOf<Vehicle>()

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
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            vehicleViewModel.selectedVehicleState.collectLatest { vehicle ->
                vehicle?.let {
                    binding.showVehicleDetails(vehicle)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.serviceState.collectLatest { service ->
                if (service.serviceType == ServiceType.GENERAL_SERVICE) {
                    binding.rbGeneralService.isChecked = true
                } else if (service.serviceType == ServiceType.VEHICLE_REPAIR) {
                    binding.rbVehicleRepair.isChecked = true
                }
            }
        }
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
        viewLifecycleOwner.lifecycleScope.launch {
            vehicleViewModel.myVehiclesState.collectLatest { result ->
                if (result is ApiResponse.Success && result.data?.isNotEmpty() == true) {
                    this@AddServiceFragment.myVehicles = result.data
                }
            }
        }
    }

    fun closeThisScreen() {
        findNavController().popBackStack()
    }

    private fun FragmentAddServiceBinding.showVehicleDetails(vehicle: Vehicle) {
        tvVehicleName.text = vehicle.name
        tvVehicleNumber.text = vehicle.regNo.toRegNum()

        initViews(vehicle)
    }

    private fun FragmentAddServiceBinding.initViews(vehicle: Vehicle) {

        rgServiceType.setOnCheckedChangeListener(object : OnCheckedChangeListener {
            override fun onCheckedChanged(p0: RadioGroup?, id: Int) {
                if (id == binding.rbGeneralService.id) {
                    viewModel.serviceState.value.serviceType = ServiceType.GENERAL_SERVICE
                } else if (id == binding.rbVehicleRepair.id) {
                    viewModel.serviceState.value.serviceType = ServiceType.VEHICLE_REPAIR
                }
            }
        })
        cbPickDrop.setOnCheckedChangeListener { _, isChecked ->
            viewModel.serviceState.value.pickDrop = isChecked
        }

        btnNext.setOnClickListener {
            AppState.user?.firebaseId?.let { firebaseId ->
                var service = Service(
                    serviceType = viewModel.serviceState.value.serviceType,
                    vehicleName = vehicle.name,
                    vehicleId = vehicle.regNo,
                    regNum = vehicle.regNo,
                    startDate = Calendar.getInstance().time,
                    endDate = null,
                    spareParts = emptyList(),
                    complaint = "",
                    firebaseId = firebaseId
                )
                val args = AddComplaintsFragmentArgs(service)
                findNavController().navigate(
                    R.id.action_navigation_add_service_to_navigation_add_complaints, args.toBundle()
                )
            }
        }
        tvAddChange.setOnClickListener {
            if (myVehicles.isNotEmpty()) {
                val vehicles = Vehicles()
                vehicles.addAll(myVehicles)
                val args = VehicleListDialogFragmentArgs(vehicles, vehicle)
                findNavController().navigate(R.id.vehicle_list_fragment_dialog, args.toBundle())
            } else {
                showToast("Vehicle empty")
            }

        }
    }

}
