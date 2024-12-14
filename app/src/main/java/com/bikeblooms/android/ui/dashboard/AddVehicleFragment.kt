package com.bikeblooms.android.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bikeblooms.android.R
import com.bikeblooms.android.data.VehicleMap
import com.bikeblooms.android.databinding.FragmentDashboardBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Brand
import com.bikeblooms.android.model.Brands
import com.bikeblooms.android.model.FuelType
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.model.VehicleType
import com.bikeblooms.android.model.Vehicles
import com.bikeblooms.android.ui.base.BaseFragment
import com.bikeblooms.android.util.AppConstants.BRAND
import com.bikeblooms.android.util.AppConstants.VEHICLE
import com.bikeblooms.android.util.GenericKeyEvent
import com.bikeblooms.android.util.GenericTextWatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddVehicleFragment : BaseFragment() {

    private lateinit var binding: FragmentDashboardBinding
    val dashboardViewModel: DashboardViewModel by viewModels()
    private var currentVehicle = Vehicle()
    private var totalVehicleMap: VehicleMap? = null
    private var selectedBrands: List<Brand> = emptyList()
    private var selectedVehicles: List<Vehicle> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this@AddVehicleFragment.progressBar = binding.progressBar
        observeStates()
        setRegNumListeners()
        binding.initViews()
        listenForBackStackResult()
    }

    private fun listenForBackStackResult() {
        with(findNavController().currentBackStackEntry?.savedStateHandle) {
            this?.let {
                getLiveData<Brand>(BRAND).observe(viewLifecycleOwner) { brand ->
                    setVehicles(brand)
                    currentVehicle.brand = brand
                    binding.tvVehicleType.text = brand.name

                    currentVehicle.name = ""
                    currentVehicle.model = ""
                    binding.tvVehicleModel.text = currentVehicle.model
                }
                getLiveData<Vehicle>(VEHICLE).observe(viewLifecycleOwner) { vehicle ->
                    currentVehicle.name = vehicle.name
                    binding.tvVehicleModel.text = currentVehicle.name
                    currentVehicle.model = vehicle.name
                }
                remove<Brand>(BRAND)
                remove<Vehicle>(VEHICLE)
            }
        }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.vehiclesState.collectLatest { vehicleState ->
                when (vehicleState) {
                    is ApiResponse.Loading -> {
                        showProgress()
                    }

                    is ApiResponse.Success -> {
                        hideProgress()
                        totalVehicleMap = vehicleState.data
                        if (currentVehicle.type == null) {
                            currentVehicle.type = VehicleType.BIKE
                            setTheViewBasedOnVehicleType(currentVehicle)
                        }
                    }

                    is ApiResponse.Error -> {
                        hideProgress()
                    }

                    is ApiResponse.Empty -> {
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.currentVehicleState.collectLatest { it ->
                it?.let {
                    currentVehicle = it
                    setTheViewBasedOnVehicleType(currentVehicle)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.addVehicleState.collectLatest { addVehicleState ->
                when (addVehicleState) {
                    is ApiResponse.Loading -> {
                        showProgress()
                    }

                    is ApiResponse.Success -> {
                        hideProgress()
                    }

                    is ApiResponse.Error -> {
                        hideProgress()
                    }

                    else -> {}
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.vehicleAddedFlow.collectLatest {
                showToast("Vehicle Added")
                findNavController().popBackStack()
            }
        }
    }

    fun setTheViewBasedOnVehicleType(vehicle: Vehicle) {
        currentVehicle.type = vehicle.type
        if (currentVehicle.type == VehicleType.BIKE) {
            binding.ivBike.isSelected = true
        } else if (currentVehicle.type == VehicleType.CAR) {
            binding.ivCar.isSelected = true
        }
        setBrands(currentVehicle.type)
        binding.tvVehicleType.text = currentVehicle.brand?.name
        binding.tvVehicleModel.text = currentVehicle.name
        binding.tgBtnFuelType.check(
            if (vehicle.fuelType == FuelType.PETROL) R.id.btn_petrol
            else if (vehicle.fuelType == FuelType.DIESEL) R.id.btn_diesel
            else View.NO_ID
        )
    }

    fun setBrands(type: VehicleType?) {
        if (type == VehicleType.BIKE) {
            totalVehicleMap?.bikeMap?.keys?.let {
                selectedBrands = it.toList()
            }
        } else {
            totalVehicleMap?.carMap?.keys?.let {
                selectedBrands = it.toList()
            }
        }
    }

    fun setVehicles(brand: Brand) {
        if (currentVehicle.type == VehicleType.BIKE) {
            totalVehicleMap?.bikeMap?.get(brand)?.let {
                selectedVehicles = it
            }
        } else {
            totalVehicleMap?.carMap?.get(brand)?.let {
                selectedVehicles = it
            }
        }
    }

    private fun setRegNumListeners() {
        binding.rn1.addTextChangedListener(GenericTextWatcher(binding.rn1, binding.rn2));
        binding.rn2.addTextChangedListener(GenericTextWatcher(binding.rn2, binding.rn3));
        binding.rn3.addTextChangedListener(GenericTextWatcher(binding.rn3, binding.rn4));
        binding.rn4.addTextChangedListener(GenericTextWatcher(binding.rn4, null));
        binding.rn2.setOnKeyListener(GenericKeyEvent(binding.rn2, binding.rn1));
        binding.rn3.setOnKeyListener(GenericKeyEvent(binding.rn3, binding.rn2));
        binding.rn4.setOnKeyListener(GenericKeyEvent(binding.rn4, binding.rn3));
    }

    private fun FragmentDashboardBinding.initViews() {
        binding.ivBike.setOnClickListener {
            if (!it.isSelected) {
                binding.ivBike.isSelected = true
                binding.ivCar.isSelected = false
                resetVehicleAndViews()
                currentVehicle.type = VehicleType.BIKE
                setTheViewBasedOnVehicleType(currentVehicle)
            }
        }
        binding.ivCar.setOnClickListener {
            if (!it.isSelected) {
                binding.ivCar.isSelected = true
                binding.ivBike.isSelected = false
                resetVehicleAndViews()
                currentVehicle.type = VehicleType.CAR
                setTheViewBasedOnVehicleType(currentVehicle)
            }
        }

        rlVehicleManufacture.setOnClickListener {
            if (selectedBrands.isNotEmpty()) {
                val args =
                    AddVehicleFragmentDirections.actionNavigationDashboardToNavigationVehicleList(
                        Brands().apply {
                            addAll(selectedBrands)
                        }, null
                    )
                findNavController().navigate(
                    R.id.navigation_vehicle_list, args.arguments
                )
            }
        }
        rlVehicleModel.setOnClickListener {
            if (selectedVehicles.isNotEmpty()) {
                val args =
                    AddVehicleFragmentDirections.actionNavigationDashboardToNavigationVehicleList(
                        null,
                        Vehicles().apply {
                            addAll(selectedVehicles)
                        })
                findNavController().navigate(R.id.navigation_vehicle_list, args.arguments)
            }
        }
        tgBtnFuelType.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (checkedId == binding.btnPetrol.id) {
                currentVehicle.fuelType = FuelType.PETROL
            } else if (checkedId == binding.btnDiesel.id) {
                currentVehicle.fuelType = FuelType.DIESEL
            }
        }
        btnAddVehicle.setOnClickListener {
            val regNumsViews = arrayOf(binding.rn1, binding.rn2, binding.rn3, binding.rn4)
            val minLengths = arrayOf(2, 2, 2, 4)
            if (regNumsValid(regNumsViews, minLengths)) {
                val regNum = readRegNums(regNumsViews)
                currentVehicle.regNo = regNum
            }
            if (dashboardViewModel.isValid(currentVehicle)) {
                AppState.user?.firebaseId?.let {
                    dashboardViewModel.addVehicle(currentVehicle, it)
                }

            } else {
                showToast("Vehicle not valid")
            }
        }
    }

    private fun resetVehicleAndViews() {
        currentVehicle.brand = null
        currentVehicle.name = ""
        currentVehicle.model = ""
        currentVehicle.fuelType = null

        binding.tvVehicleType.text = null
        binding.tvVehicleModel.text = null
        binding.tgBtnFuelType.clearChecked()
    }

    private fun readRegNums(fields: Array<AppCompatEditText>): String {
        var regNumber = ""
        fields.forEach {
            regNumber += it.text.toString()
        }
        return regNumber
    }

    private fun regNumsValid(
        fields: Array<AppCompatEditText>, values: Array<Int>
    ): Boolean {
        fields.forEachIndexed { index, field ->
            if (field.length() != values[index]) {
                return false
            }
        }
        return true
    }

    override fun onStop() {
        super.onStop()
        dashboardViewModel.updateCurrentState(currentVehicle)
    }
}