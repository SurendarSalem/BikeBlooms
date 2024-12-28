package com.bikeblooms.android.ui.user

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentUserDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import javax.inject.Inject

class UserDetailFragment : Fragment(), OnMapReadyCallback {
    lateinit var binding: FragmentUserDetailBinding

    private lateinit var mMap: GoogleMap
    private lateinit var marker: Marker
    private lateinit var markerOptions: MarkerOptions

    @Inject
    lateinit var mGeocoder: Geocoder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initGoogleMap()
        initListeners()
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
    }

    private fun initGoogleMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        markerOptions =
            MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.bike_marker))
        mMap = googleMap
        setLocationToMap(mMap, LatLng(28.704060, 77.102493), markerOptions)

        mMap.setOnCameraIdleListener {
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
        if (this::marker.isInitialized) {
            marker.remove()
        }
        marker = map.addMarker(markerOptions)!!
    }

}