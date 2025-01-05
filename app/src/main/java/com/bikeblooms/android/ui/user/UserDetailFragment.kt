package com.bikeblooms.android.ui.user

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentUserDetailBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.NotifyState
import com.bikeblooms.android.model.User
import com.bikeblooms.android.model.Vendor
import com.bikeblooms.android.ui.Utils
import com.bikeblooms.android.ui.base.BaseFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailFragment : BaseFragment(), OnMapReadyCallback {
    lateinit var binding: FragmentUserDetailBinding

    private lateinit var mMap: GoogleMap
    private lateinit var marker: Marker
    private lateinit var markerOptions: MarkerOptions

    @Inject
    lateinit var mGeocoder: Geocoder

    private var user: User? = null
    private lateinit var userCopy: User

    var editMode = MutableStateFlow<Boolean>(false)

    val args: UserDetailFragmentArgs by navArgs()

    private val userDetailViewModel: UserDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = args.user
        user?.let {
            userCopy = it.deepCopy()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.progressBar
        observeStates()
        user?.let {
            displayUserDetails(it)
            if (user is Vendor) {
                with(user as Vendor) {
                    shop?.let {
                        binding.llShopDetails.visibility = View.VISIBLE
                        if ((it.address?.latitude?.toInt() != 0 && it.address?.longitude?.toInt() != 0)) {
                            binding.mapContainer.visibility = View.VISIBLE
                            initGoogleMap()
                        }
                    }
                }
            }
        }
        initListeners()
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            editMode.collectLatest { inEditMode ->
                binding.ivEdit.isSelected = inEditMode
                binding.etName.isEnabled = inEditMode
                binding.etMobileNum.isEnabled = inEditMode
                if (user is Vendor) {
                    binding.etShopName.isEnabled = inEditMode
                    binding.etAddress.isEnabled = inEditMode
                }
                if (!inEditMode) {
                    displayUserDetails(userCopy)
                }
                binding.btnUpdate.visibility = if (inEditMode) View.VISIBLE else View.INVISIBLE
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            userDetailViewModel.updateUserState.collectLatest { result ->
                when (result) {
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
            userDetailViewModel.notifyState.collectLatest { notifyState ->
                editMode.value = false
                if (notifyState is NotifyState.Success) {
                    showToast(notifyState.message)
                } else if (notifyState is NotifyState.Error) {
                    showToast(notifyState.message)
                }
            }
        }
    }

    private fun displayUserDetails(user: User) {
        binding.etName.setText(user.name.toString())
        binding.etEmail.text = user.emailId.toString()
        binding.etMobileNum.setText(user.mobileNum.toString())
        if (user is Vendor) {
            binding.etShopId.text = user.shop?.shopId.toString()
            binding.etShopName.setText(user.shop?.shopName.toString())
            binding.etAddress.setText(user.shop?.address?.address.toString())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
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

        binding.ivEdit.setOnClickListener {
            editMode.value = !editMode.value
        }
        binding.llEmail.setOnClickListener {
            Utils.sendEmail(requireContext(), user?.emailId.toString())
        }
        binding.llCall.setOnClickListener {
            Utils.callPhone(requireContext(), user?.mobileNum.toString())
        }
        binding.llWhatsapp.setOnClickListener {
            Utils.openWhatsapp(requireContext(), user?.mobileNum.toString())
        }
        binding.btnUpdate.setOnClickListener {
            val name = binding.etName.text.toString()
            val mobileNum = binding.etMobileNum.text.toString()
            userCopy.name = name
            userCopy.mobileNum = mobileNum
            if (userCopy is Vendor) {
                val shopName = binding.etShopName.text.toString()
                val address = binding.etAddress.text.toString()
                (userCopy as Vendor).shop?.shopName = shopName
                (userCopy as Vendor).shop?.address?.address = address
            }
            val errorMgs = userDetailViewModel.isValid(userCopy)
            if (errorMgs.isEmpty()) {
                userDetailViewModel.updateUser(userCopy)
            } else {
                showToast(errorMgs)
            }
        }
    }

    private fun initGoogleMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        markerOptions =
            MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_marker))
        user?.let {
            if (it is Vendor) {
                it.shop?.address?.let { address ->
                    if (address.latitude != 0.0 && address.longitude != 0.0) {
                        setLocationToMap(
                            mMap, LatLng(address.latitude, address.longitude), markerOptions
                        )
                        mMap.setOnCameraIdleListener {
                            setMarkersToMap(mMap, mMap.cameraPosition.target, markerOptions)
                        }
                    }
                }
            }
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
        if (this::marker.isInitialized) {
            marker.remove()
        }
        marker = map.addMarker(markerOptions)!!
    }

}