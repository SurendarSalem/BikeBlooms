package com.bikeblooms.android.ui.service

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Calendar
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class AddServiceFragment : BaseFragment(), OnMapReadyCallback {

    private val viewModel: ServiceViewModel by activityViewModels()
    private val vehicleViewModel: VehicleViewModel by activityViewModels()
    lateinit var binding: FragmentAddServiceBinding
    private var myVehicles = listOf<Vehicle>()
    private lateinit var mMap: GoogleMap
    private val FINE_PERSMISSION_CODE = 1
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    val startAutocomplete: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data;
            if (intent != null) {
                val place = Autocomplete.getPlaceFromIntent(intent)

                Log.i("Surendar", "Place: $place")
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), "AIzaSyBXhQq1EGtqL8gDzOfiXVcHE0j8Vu2pits")

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        val mapFragment = fragmentManager?.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        getLastLocation()

    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLocation = it
                val mapFragment =
                    childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }

        }
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
                    firebaseId = firebaseId,
                    pickDrop = viewModel.serviceState.value.pickDrop,
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
        btnSearchPlaces.setOnClickListener {
            // return after the user has made a selection.
            val fields = listOf(
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG, Place.Field.VIEWPORT
            );

            // Build the autocomplete intent with field, country, and type filters applied
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountries(listOf("IN"))
                .setTypesFilter(listOf(TypeFilter.ADDRESS.toString().toLowerCase()))
                .build(requireContext());
            startAutocomplete.launch(intent);
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        mMap.addMarker(
            MarkerOptions().position(
                LatLng(
                    currentLocation.latitude,
                    currentLocation.longitude
                )
            ).title("You are here")
        )
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    currentLocation.latitude,
                    currentLocation.longitude
                ), 20F
            )
        )
        val mGeocoder = Geocoder(requireContext(), Locale.getDefault())
        var addressString = ""

        // Reverse-Geocoding starts
        try {
            val addressList: List<Address>? =
                mGeocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1)

            // use your lat, long value here
            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                val sb = StringBuilder()
                for (i in 0 until address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i)).append("\n")
                }

                // Various Parameters of an Address are appended
                // to generate a complete Address
                if (address.premises != null)
                    sb.append(address.premises).append(", ")

                sb.append(address.subAdminArea).append("\n")
                sb.append(address.locality).append(", ")
                sb.append(address.adminArea).append(", ")
                sb.append(address.countryName).append(", ")
                sb.append(address.postalCode)
                addressString = sb.toString()
                viewModel.updateAddress(addressString)

            }
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Unable connect to Geocoder", Toast.LENGTH_LONG)
                .show()
        }
    }

}
