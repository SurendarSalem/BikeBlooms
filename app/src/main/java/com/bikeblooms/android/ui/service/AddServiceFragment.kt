package com.bikeblooms.android.ui.service

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.location.LocationManagerCompat
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
import com.bikeblooms.android.ui.Utils
import com.bikeblooms.android.ui.VehicleListDialogFragmentArgs
import com.bikeblooms.android.ui.base.BaseFragment
import com.bikeblooms.android.ui.openAppSystemSettings
import com.bikeblooms.android.ui.service.compaints.AddComplaintsFragmentArgs
import com.bikeblooms.android.ui.toDisplayDate
import com.bikeblooms.android.ui.vehicles.VehicleViewModel
import com.bikeblooms.android.util.toRegNum
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class AddServiceFragment : BaseFragment(), OnMapReadyCallback {

    private val serviceViewModel: ServiceViewModel by activityViewModels()
    private val vehicleViewModel: VehicleViewModel by activityViewModels()
    lateinit var binding: FragmentAddServiceBinding
    private var myVehicles = listOf<Vehicle>()
    private lateinit var mMap: GoogleMap
    private lateinit var marker: Marker
    private lateinit var markerOptions: MarkerOptions

    @Inject
    lateinit var mGeocoder: Geocoder

    @Inject
    lateinit var mLocationManager: LocationManager

    var service = Service()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppState.user?.let {
            service.firebaseId = it.firebaseId
            service.ownerFcmToken = it.fcmToken
            service.mobileNumber = it.mobileNum
            service.ownerName = it.name
        }
    }

    private fun initGoogleMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddServiceBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkIfLocationEnabled()
        initGoogleMap()
        progressBar = binding.progressBar
        observeStates()
        binding.addListeners()
    }

    private fun checkIfLocationEnabled() {
        if (!LocationManagerCompat.isLocationEnabled(mLocationManager)) {
            Utils.showAlertDialog(
                context = requireContext(),
                message = "Please enable GPS/Location to proceed.",
                positiveBtnText = "Open Settings",
                positiveBtnCallback = {
                    requireActivity().finish()
                    requireContext().openAppSystemSettings()
                },
                negativeBtnText = "Close App",
                negativeBtnCallback = {
                    requireActivity().finish()
                },
                nonCancellable = false
            )
        }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            vehicleViewModel.selectedVehicleState.collectLatest { vehicle ->
                vehicle?.let {
                    service.vehicleName = vehicle.name
                    service.vehicleId = vehicle.regNo
                    service.regNum = vehicle.regNo
                    binding.showVehicleDetails(vehicle)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            serviceViewModel.notifyState.collectLatest { result ->
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
                    this@AddServiceFragment.myVehicles = it
                    if (it.isEmpty()) {
                        binding.tvVehicleName.text = getString(R.string.no_vehicle_added)
                        binding.tvVehicleNumber.text = ""
                        service.vehicleName = ""
                        service.vehicleId = ""
                    }
                }
            }
        }
    }

    private fun FragmentAddServiceBinding.showVehicleDetails(vehicle: Vehicle) {
        tvVehicleName.text = vehicle.name
        tvVehicleNumber.text = vehicle.regNo.toRegNum()
    }

    private fun FragmentAddServiceBinding.addListeners() {

        rgServiceType.setOnCheckedChangeListener(object : OnCheckedChangeListener {
            override fun onCheckedChanged(p0: RadioGroup?, id: Int) {
                if (id == binding.rbGeneralService.id) {
                    service.serviceType = ServiceType.GENERAL_SERVICE
                } else if (id == binding.rbVehicleRepair.id) {
                    service.serviceType = ServiceType.VEHICLE_REPAIR
                }
            }
        })
        cbPickDrop.setOnCheckedChangeListener { _, isChecked ->
            service.pickDrop = isChecked
        }
        btnNext.setOnClickListener {
            service.address = etAddress.text.toString()
            val validationErrorMsg = isValid(service)
            if (validationErrorMsg.isEmpty()) {
                val args = AddComplaintsFragmentArgs(service)
                findNavController().navigate(
                    R.id.action_navigation_add_service_to_navigation_add_complaints, args.toBundle()
                )
            } else {
                showToast(validationErrorMsg)
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
            )

            // Build the autocomplete intent with field, country, and type filters applied
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountries(listOf("IN"))
                .setTypesFilter(listOf(TypeFilter.ADDRESS.toString().lowercase()))
                .build(requireContext())
            startAutocomplete.launch(intent)
        }
        binding.ivOverlay.setOnTouchListener { _, event ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.scrollView.requestDisallowInterceptTouchEvent(true)
                    false
                }

                MotionEvent.ACTION_UP -> {
                    binding.scrollView.requestDisallowInterceptTouchEvent(false)
                    true
                }

                else -> true
            }
        }
        binding.tvBookingDate.text = Calendar.getInstance().time.toDisplayDate()
        binding.ivPickDate.setOnClickListener {
            Utils.showDatePicker(
                requireContext(), Calendar.getInstance()
            ) { calendar ->
                service.bookingDate = calendar.time
                binding.tvBookingDate.text = calendar.time.toDisplayDate()
            }
        }
    }

    private fun isValid(service: Service): String {
        with(service) {
            if (serviceType == null) {
                return "Please select a Service Type"
            }
            if (vehicleName.isEmpty()) {
                return "Please select a Vehicle"
            }
            if (vehicleId.isEmpty()) {
                return "Please select a Vehicle"
            }
            if (regNum.length < 8) {
                return "Please enter a valid Registration Number"
            }
            if (firebaseId.isEmpty()) {
                return "Please select a Vehicle"
            }
            if (address.isEmpty()) {
                return "Please select a location. Please check if you enabled you GPS/Location"
            }
        }
        return ""
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        markerOptions =
            MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_marker))
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        if (service.address.isEmpty()) {
            viewLifecycleOwner.lifecycleScope.launch {
                serviceViewModel.myLocationState.collectLatest { location ->
                    setLocationToMap(
                        mMap, LatLng(location.latitude, location.longitude), markerOptions
                    )
                }
            }
        }

        mMap.setOnCameraIdleListener {
            val address = serviceViewModel.reverseGeocode(mMap.cameraPosition.target, mGeocoder)
            service.address = address
            binding.etAddress.setText(address)
            setMarkersToMap(mMap, mMap.cameraPosition.target, markerOptions)
        }

    }

    private fun setLocationToMap(map: GoogleMap, location: LatLng, markerOptions: MarkerOptions) {
        setMarkersToMap(map, location, markerOptions)
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                location, 19f
            )
        )
    }

    private fun setMarkersToMap(map: GoogleMap, location: LatLng, markerOptions: MarkerOptions) {
        markerOptions.position(location)
        markerOptions.title(service.address)
        markerOptions.snippet(service.address)
        if (this::marker.isInitialized) {
            marker.remove()
        }
        marker = map.addMarker(markerOptions)!!
        marker.showInfoWindow()
        marker.isDraggable = true
    }

    fun closeThisScreen() {
        findNavController().popBackStack()
    }

    val startAutocomplete: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val place = Autocomplete.getPlaceFromIntent(intent)
                var address = ""
                place.addressComponents?.asList()?.forEach { address += it.name }
                place.location?.let {
                    setLocationToMap(mMap, it, markerOptions)
                    service.address = address
                    binding.etAddress.setText(address)
                }
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {

        }
    }
}


