package com.bikeblooms.android.ui.service

import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.data.LocationRepository
import com.bikeblooms.android.data.VehiclesRepository
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.util.ReverseGeocoder
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val vehiclesRepository: VehiclesRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {
    private var _myServicesState: MutableStateFlow<ApiResponse<List<Service>>> =
        MutableStateFlow(ApiResponse.Empty())
    val myServicesState = _myServicesState.asStateFlow()

    private var _notifyState: MutableSharedFlow<ApiResponse<Service>> =
        MutableSharedFlow<ApiResponse<Service>>()
    val notifyState = _notifyState.asSharedFlow()

    var myLocationState = locationRepository.getLocation

    fun getMyServices(isAdmin: Boolean = false) {
        viewModelScope.launch {
            _myServicesState.value = ApiResponse.Loading()
            AppState.user?.firebaseId?.run {
                vehiclesRepository.getMyServices(this, object : LoginCallback<List<Service>> {
                    override fun onSuccess(services: List<Service>) {
                        _myServicesState.value = ApiResponse.Success(services)
                    }

                    override fun onError(message: String) {
                        _myServicesState.value = ApiResponse.Error(message)
                    }
                }, isAdmin)
            }
        }
    }

    fun validate(service: Service): String {
        with(service) {
            if (vehicleName.isEmpty() or vehicleId.isEmpty() or regNum.isEmpty()) {
                return "Please select a vehicle"
            }
            return ""
        }
    }

    fun reverseGeocode(
        lng: LatLng, mGeocoder: Geocoder
    ): String {
        return ReverseGeocoder.reverseGeocode(lng, mGeocoder)
    }
}