package com.bikeblooms.android.ui.service

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
    private lateinit var marker: Marker
    lateinit var mGeocoder: Geocoder


    val startAutocomplete: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data;
            if (intent != null) {
                val place = Autocomplete.getPlaceFromIntent(intent)
                var address = ""
                place.addressComponents?.asList()?.forEach {
                    address += it.name
                }
                place.latLng?.let {
                    setLocationToMap(mMap, it)
                    viewModel.updateAddress(address)
                }
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGeocoder = Geocoder(requireContext(), Locale.getDefault())
        Places.initialize(requireContext(), "AIzaSyB1E_XAP2VhvW2c3aFIAZywY6jcgvseucc")

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
        binding.addListeners()
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
                if (result is ApiResponse.Success) result.data?.let {
                    if (it.isEmpty()) {
                        binding.tvVehicleName.text = getString(R.string.no_vehicle_added)
                    } else {
                        this@AddServiceFragment.myVehicles = result.data
                    }
                }
            }
        }
        binding.addListeners()
    }

    fun closeThisScreen() {
        findNavController().popBackStack()
    }

    private fun FragmentAddServiceBinding.showVehicleDetails(vehicle: Vehicle) {
        tvVehicleName.text = vehicle.name
        tvVehicleNumber.text = vehicle.regNo.toRegNum()
    }

    private fun FragmentAddServiceBinding.addListeners() {

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
                    vehicleName = vehicleViewModel.selectedVehicleState.value?.name.toString(),
                    vehicleId = vehicleViewModel.selectedVehicleState.value?.regNo.toString(),
                    regNum = vehicleViewModel.selectedVehicleState.value?.regNo.toString(),
                    startDate = Calendar.getInstance().time,
                    endDate = null,
                    spareParts = emptyList(),
                    complaint = "",
                    firebaseId = firebaseId,
                    pickDrop = viewModel.serviceState.value.pickDrop,
                    address = viewModel.serviceState.value.address
                )
                if (isValid(service)) {
                    val args = AddComplaintsFragmentArgs(service)
                    findNavController().navigate(
                        R.id.action_navigation_add_service_to_navigation_add_complaints,
                        args.toBundle()
                    )
                } else {
                    showToast("Please enter all the fields")
                }
            }
        }
        tvAddChange.setOnClickListener {
            if (myVehicles.isNotEmpty()) {
                val vehicles = Vehicles()
                vehicles.addAll(myVehicles)
                val args = VehicleListDialogFragmentArgs(
                    vehicles, vehicleViewModel.selectedVehicleState.value
                )
                findNavController().navigate(R.id.vehicle_list_fragment_dialog, args.toBundle())
            } else {
                findNavController().navigate(R.id.navigation_add_vehicle)
            }

        }
        btnSearchPlaces.setOnClickListener {
            // return after the user has made a selection.
            val fields = listOf(
                Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG, Place.Field.VIEWPORT
            );

            // Build the autocomplete intent with field, country, and type filters applied
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountries(listOf("IN"))
                .setTypesFilter(listOf(TypeFilter.ADDRESS.toString().toLowerCase()))
                .build(requireContext());
            startAutocomplete.launch(intent);
        }
    }

    private fun isValid(service: Service): Boolean {
        with(service) {
            return vehicleName.isNotEmpty() && vehicleId.isNotEmpty() && regNum.length>=8 &&
                    firebaseId.isNotEmpty() && address.isNotEmpty()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        setLocationToMap(mMap, currentLocation.toLatLng())
        mMap.setOnCameraMoveListener(object : GoogleMap.OnCameraMoveListener {
            override fun onCameraMove() {/* val options =
                     MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_marker))
                         .position(mMap.cameraPosition.target);
                 marker.remove();
                 marker = mMap.addMarker(options)!!*/
            }
        })
        mMap.setOnCameraIdleListener {
            val options =
                MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_marker))
                    .position(mMap.cameraPosition.target);
            marker.remove();
            options.title(viewModel.serviceState.value.address)
            options.snippet(viewModel.serviceState.value.address)
            marker.title = viewModel.serviceState.value.address
            marker.snippet = viewModel.serviceState.value.address
            marker = mMap.addMarker(options)!!
            marker.showInfoWindow()
            viewModel.reverseGeocode(mMap.cameraPosition.target, mGeocoder)
        }

    }

    private fun setLocationToMap(map: GoogleMap, location: LatLng) {
        val options =
            MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_marker))
                .position(location);
        options.title(viewModel.serviceState.value.address)
        options.snippet(viewModel.serviceState.value.address)
        marker = map.addMarker(options)!!
        marker.showInfoWindow()
        marker.isDraggable = true
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                location, 19f
            )
        )
    }
}

private fun Location.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}
