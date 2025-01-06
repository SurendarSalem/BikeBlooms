package com.bikeblooms.android.ui.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentServiceDetailBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Complaint
import com.bikeblooms.android.model.ComplaintsList
import com.bikeblooms.android.model.NotifyState
import com.bikeblooms.android.model.Progress
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.ServiceType
import com.bikeblooms.android.model.Spare
import com.bikeblooms.android.model.SpareListArgs
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.model.Vehicles
import com.bikeblooms.android.model.isAdmin
import com.bikeblooms.android.ui.Utils
import com.bikeblooms.android.ui.VehicleListDialogFragmentArgs
import com.bikeblooms.android.ui.base.BaseFragment
import com.bikeblooms.android.ui.service.compaints.ComplaintsSelectionFragmentArgs
import com.bikeblooms.android.ui.service.compaints.SpareSelectionFragmentArgs
import com.bikeblooms.android.ui.toDisplayDate
import com.bikeblooms.android.util.AppConstants.SELECTED_COMPLAINTS
import com.bikeblooms.android.util.AppConstants.SELECTED_SPARE
import com.bikeblooms.android.util.AppConstants.VEHICLE
import com.bikeblooms.android.util.toRegNum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class ServiceDetailFragment : BaseFragment() {

    private val serviceDetailViewModel: ServiceDetailViewModel by viewModels()
    private lateinit var binding: FragmentServiceDetailBinding
    private val serviceDetailFragmentArgs: ServiceDetailFragmentArgs by navArgs()
    private lateinit var service: Service
    private lateinit var serviceCopy: Service
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceDetailFragmentArgs.service?.let {
            this.service = it
            serviceCopy = it.copy()
            serviceDetailViewModel.setService(it)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentServiceDetailBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.progressBar
        viewLifecycleOwner.lifecycleScope.launch {
            serviceDetailViewModel.serviceState.collectLatest { service ->
                service?.let {
                    this@ServiceDetailFragment.service = it
                    binding.displayServiceData(it)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            serviceDetailViewModel.notifyState.collectLatest { notifyState ->
                if (notifyState is NotifyState.Success) {
                    showToast(notifyState.message)
                    findNavController().popBackStack()
                } else if (notifyState is NotifyState.Error) {
                    showToast(notifyState.message)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            serviceDetailViewModel.updateServiceState.collectLatest { result ->
                when (result) {
                    is ApiResponse.Loading -> {
                        showProgress()
                    }

                    is ApiResponse.Error -> {
                        hideProgress()
                    }

                    is ApiResponse.Success -> {
                        hideProgress()
                    }

                    else -> {}
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            serviceDetailViewModel.cancelServiceState.collectLatest { result ->
                when (result) {
                    is ApiResponse.Loading -> {
                        showProgress()
                    }

                    is ApiResponse.Error -> {
                        hideProgress()
                    }

                    is ApiResponse.Success -> {
                        hideProgress()
                    }

                    else -> {}
                }
            }
        }

        addBackStackListener()
        binding.addListeners()
    }


    private fun addBackStackListener() {
        val navController = findNavController();
        val navBackStackEntry = navController.currentBackStackEntry
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (navBackStackEntry?.savedStateHandle?.contains(VEHICLE) == true) {
                    val result = navBackStackEntry.savedStateHandle.get<Vehicle>(VEHICLE)
                    serviceDetailViewModel.updateService(service.copy(vehicleName = result?.name.toString()))
                    navBackStackEntry.savedStateHandle.remove<Vehicle>(VEHICLE)
                } else if (navBackStackEntry?.savedStateHandle?.contains(SELECTED_SPARE) == true) {
                    val selectedSpare =
                        navBackStackEntry.savedStateHandle.get<Spare>(SELECTED_SPARE)
                    selectedSpare?.let {
                        serviceDetailViewModel.updateService(service.copy(spareParts = listOf(it)))
                    }
                    navBackStackEntry.savedStateHandle.remove<Spare>(SELECTED_SPARE)
                } else if (navBackStackEntry?.savedStateHandle?.contains(SELECTED_COMPLAINTS) == true) {
                    val selectedComplaints =
                        navBackStackEntry.savedStateHandle.get<List<Complaint>>(SELECTED_COMPLAINTS)
                    serviceDetailViewModel.updateService(service.copy(complaints = selectedComplaints))
                    navBackStackEntry.savedStateHandle.remove<List<Complaint>>(SELECTED_COMPLAINTS)
                }
            }
        }
        navBackStackEntry?.lifecycle?.addObserver(observer)

        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                navBackStackEntry?.lifecycle?.removeObserver(observer)
            }
        })
    }

    private fun FragmentServiceDetailBinding.displayServiceData(service: Service) {
        with(service) {
            if (serviceType == ServiceType.GENERAL_SERVICE) {
                rbGeneralService.isChecked = true
                llEngineOil.visibility = View.VISIBLE
                if (!spareParts.isNullOrEmpty()) {
                    tvEngineOil.text = spareParts?.first()?.name
                }

            } else if (service.serviceType == ServiceType.VEHICLE_REPAIR) {
                rbVehicleRepair.isChecked = true
                llVehicleRepair.visibility = View.VISIBLE
                var complaintsNames = ""
                complaints?.forEachIndexed { index, complaint ->
                    complaintsNames += "${index + 1}) ${complaint.name} - ${getString(R.string.rupee_symbol)} ${complaint.price}\n"
                }

                if (complaintsNames.isEmpty()) {
                    tvComplaints.text = getString(R.string.select_vehicle_problem)
                } else {
                    tvComplaints.text = complaintsNames
                }
            }
            tvVehicleName.text = vehicleName
            tvAddress.text = address
            tvVehicleNumber.text = regNum.toRegNum()
            service.updateDate?.let {
                tvBookingDate.text = it.toDisplayDate()
            } ?: run {
                tvBookingDate.text = service.bookingDate.toDisplayDate()
            }
            cbPickDrop.isChecked = pickDrop
            if (AppState.user?.isAdmin() == true) {
                binding.rlOtherCharges.visibility = View.VISIBLE
                binding.etOtherCharges.setText(buildString {
                    append(service.hiddenCharges.toString())
                })
                binding.etOtherCharges.setSelection(binding.etOtherCharges.length())

            } else {
                binding.etOtherCharges.isEnabled = false
            }
            tvTotalAmt.text = buildString {
                append(getString(R.string.rupee_symbol))
                append(bill?.totalAmount.toString())
            }
            if (service.progress == Progress.CANCELLED) {
                btnUpdateService.visibility = View.GONE
                btnCancelService.visibility = View.GONE
            }
            binding.etOtherComplaints.setText(complaint)
            if (ownerName.isNotEmpty() && mobileNumber.isNotEmpty()) {
                llUserDetails.visibility = View.VISIBLE
                binding.tvUserName.text = ownerName
                binding.tvMobileNumber.text = mobileNumber
            }
        }
    }


    fun FragmentServiceDetailBinding.addListeners() {
        rgServiceType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                rbGeneralService.id -> {
                    serviceDetailViewModel.updateService(service.copy(serviceType = ServiceType.GENERAL_SERVICE))
                    llEngineOil.visibility = View.VISIBLE
                    llVehicleRepair.visibility = View.GONE
                }

                rbVehicleRepair.id -> {
                    serviceDetailViewModel.updateService(service.copy(serviceType = ServiceType.VEHICLE_REPAIR))
                    llEngineOil.visibility = View.GONE
                    llVehicleRepair.visibility = View.VISIBLE
                }
            }
        }
        tvComplaints.setOnClickListener {
            val complaintList = ComplaintsList()
            serviceDetailViewModel.serviceState.value?.complaints?.let {
                complaintList.addAll(it)
            }
            val args = ComplaintsSelectionFragmentArgs(complaintList)
            findNavController().navigate(
                R.id.action_navigation_service_detail_to_navigation_complaints_selection,
                args.toBundle()
            )
        }
        ivPickDate.setOnClickListener {
            Utils.showDatePicker(
                requireContext(), Calendar.getInstance()
            ) { calendar ->
                service.updateDate = calendar.time
                binding.tvBookingDate.text = calendar.time.toDisplayDate()
            }
        }
        cbPickDrop.setOnCheckedChangeListener { _, isChecked ->
            serviceDetailViewModel.updateService(service.copy(pickDrop = isChecked))
        }
        binding.tvEngineOil.setOnClickListener {
            with(serviceDetailViewModel.allSparesState.value.data) {
                this?.let {
                    if (it.isNotEmpty()) {
                        val args = SpareSelectionFragmentArgs(SpareListArgs().apply {
                            addAll(it)
                        })
                        findNavController().navigate(
                            R.id.action_navigation_service_detail_to_navigation_spares_selection,
                            args.toBundle()
                        )
                    }
                }
            }
        }
        tvAddChange.setOnClickListener {
            with(serviceDetailViewModel.myVehiclesState.value.data) {
                this?.let {
                    if (it.isNotEmpty()) {
                        val vehicles = Vehicles()
                        vehicles.addAll(it)
                        val vehicle = it.filter { it.regNo == service.regNum }.firstOrNull()
                        vehicle?.let {
                            val args = VehicleListDialogFragmentArgs(vehicles, vehicle)
                            findNavController().navigate(
                                R.id.vehicle_list_fragment_dialog, args.toBundle()
                            )
                        }

                    }
                }
            }
        }
        btnUpdateService.setOnClickListener {
            val complaints = binding.etOtherComplaints.text.toString()
            serviceDetailViewModel.updateService(
                service.copy(
                    complaint = complaints
                )
            )
            Utils.showAlertDialog(context = requireContext(),
                message = "Are you sure want to update this service?",
                positiveBtnText = "Update",
                positiveBtnCallback = {
                    serviceDetailViewModel.updateServiceInFirebase()
                },
                negativeBtnText = "Cancel",
                negativeBtnCallback = {

                })
        }
        btnCancelService.setOnClickListener {
            Utils.showAlertDialog(
                context = requireContext(),
                message = "Are you sure want to update this service?",
                positiveBtnText = "Yes",
                positiveBtnCallback = {
                    serviceDetailViewModel.cancelService()
                },
                negativeBtnText = "No"
            )
        }
        binding.etOtherCharges.addTextChangedListener(afterTextChanged = {
            try {
                serviceDetailViewModel.updateService(
                    service.copy(
                        hiddenCharges = it.toString().toInt()
                    )
                )
            } catch (e: NumberFormatException) {
            }
        })
        tvMobileNumber.setOnClickListener {
            Utils.callPhone(requireContext(), service.mobileNumber)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        with(findNavController().currentBackStackEntry?.savedStateHandle) {
            this?.let {
                remove<Vehicle>(VEHICLE)
                remove<Spare>(SELECTED_SPARE)
                remove<List<Complaint>>(SELECTED_COMPLAINTS)
            }
        }
    }
}


