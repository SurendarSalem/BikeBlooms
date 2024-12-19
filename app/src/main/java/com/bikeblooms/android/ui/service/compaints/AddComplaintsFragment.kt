package com.bikeblooms.android.ui.service.compaints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentAddComplaintsBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Complaint
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.ServiceType
import com.bikeblooms.android.model.Spare
import com.bikeblooms.android.model.SpareListArgs
import com.bikeblooms.android.ui.base.BaseFragment
import com.bikeblooms.android.util.toRegNum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddComplaintsFragment : BaseFragment() {

    lateinit var binding: FragmentAddComplaintsBinding

    private val addComplaintsViewModel: AddComplaintsViewModel by activityViewModels()

    private val args: AddComplaintsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddComplaintsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.progressBar
        args.service?.let {
            addComplaintsViewModel.serviceState.value = it
            binding.setVehicleData(it)
            observeStates()
        }
    }

    private fun observeStates() {

        with(findNavController().currentBackStackEntry?.savedStateHandle) {
            this?.let {
                getLiveData<List<Complaint>>("selectedComplaints").observe(viewLifecycleOwner) { complaints ->
                    var complaintsNames = ""
                    val selectedComplaints = complaints.filter {
                        it.isSelected == true
                    }
                    addComplaintsViewModel.serviceState.value =
                        addComplaintsViewModel.serviceState.value?.copy(complaints = selectedComplaints)
                    selectedComplaints.forEach {
                        complaintsNames += it.name + "\n"
                    }
                    if (selectedComplaints.isEmpty()) {
                        complaintsNames = getString(R.string.select_vehicle_problem)
                    }
                    binding.tvComplaints.text = complaintsNames

                }
            }
        }

        if (addComplaintsViewModel.serviceState.value?.serviceType == ServiceType.GENERAL_SERVICE) {
            viewLifecycleOwner.lifecycleScope.launch {
                addComplaintsViewModel.spareState.collectLatest { result ->
                    if (result is ApiResponse.Success) {
                        result.data?.first()?.let { spare ->
                            addComplaintsViewModel.updateSelectedVehicle(spare)
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            addComplaintsViewModel.billState.collectLatest {
                binding.tvInspectionCharges.text = it?.totalAmount.toString()
                binding.tvTotalAmt.text = it?.totalAmount.toString()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            addComplaintsViewModel.addServiceState.collectLatest { result ->
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
            addComplaintsViewModel.notifyState.collectLatest { result ->
                when (result) {
                    is ApiResponse.Success -> {
                        showToast("Service request has been sent!")
                        findNavController().popBackStack()
                    }

                    is ApiResponse.Error -> {
                        showToast(result.message.toString())
                    }

                    else -> {}
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            addComplaintsViewModel.selectedSpareState.collectLatest {
                it?.let {
                    addComplaintsViewModel.serviceState.value =
                        addComplaintsViewModel.serviceState.value?.copy(spareParts = listOf(it))
                    binding.tvEngineOil.text = it.name
                }
            }
        }
    }

    private fun FragmentAddComplaintsBinding.setVehicleData(service: Service) {
        tvVehicleName.text = service.vehicleName
        tvVehicleNumber.text = service.regNum.toRegNum()
        tvServiceType.text = service.serviceType.title
        if (service.serviceType == ServiceType.GENERAL_SERVICE) {
            llEngineOil.visibility = View.VISIBLE
        } else if (service.serviceType == ServiceType.VEHICLE_REPAIR) {
            llVehicleRepair.visibility = View.VISIBLE
        }
        tvComplaints.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_add_complaints_to_navigation_complaints_selection)
        }
        binding.tvEngineOil.setOnClickListener {
            if (addComplaintsViewModel.spareState.value is ApiResponse.Success) {
                val spares = addComplaintsViewModel.spareState.value.data
                spares?.let {
                    val args = SpareSelectionFragmentArgs(SpareListArgs().apply {
                        addAll(spares)
                    })
                    findNavController().navigate(
                        R.id.action_navigation_add_complaints_to_navigation_spares_selection,
                        args.toBundle()
                    )
                }
            }
        }
        btnAddService.setOnClickListener {
            addComplaintsViewModel.isServiceValid()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        with(findNavController().currentBackStackEntry?.savedStateHandle) {
            this?.let {
                remove<List<Complaint>>("selectedComplaints")
                remove<Spare>("selectedSpare")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        addComplaintsViewModel.clearSelections()
    }
}


