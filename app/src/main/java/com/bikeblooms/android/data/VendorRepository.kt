package com.bikeblooms.android.data

import com.bikeblooms.android.LoginCallback
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Vendor
import com.bikeblooms.android.util.FirebaseConstants.VENDORS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
class VendorRepository(var firebaseHelper: FirebaseHelper) {

    private var _vendorListState = MutableStateFlow<ApiResponse<List<Vendor>>>(ApiResponse.Empty())
    var vendorListState = _vendorListState.asStateFlow()


    fun getAllVendors() {
        _vendorListState.value = ApiResponse.Loading()
        firebaseHelper.getAllItems<Vendor>(VENDORS, object : LoginCallback<List<Vendor>> {
            override fun onSuccess(vendors: List<Vendor>) {
                _vendorListState.value = ApiResponse.Success(vendors)
            }

            override fun onError(message: String) {
                _vendorListState.value = ApiResponse.Error(message)
            }
        })
    }

    fun updateVendor(vendor: Vendor, callback: LoginCallback<Vendor>) {
        firebaseHelper.updateVendor(vendor, object : LoginCallback<Vendor> {
            override fun onSuccess(t: Vendor) {
                callback.onSuccess(t)

                if (_vendorListState.value is ApiResponse.Success && _vendorListState.value.data?.isNotEmpty() == true) {
                    var vendors = _vendorListState.value.data
                    vendors?.let {
                        it.map {
                            if (it.firebaseId == vendor.firebaseId) {
                                it.isActive = vendor.isActive
                            }
                        }
                        _vendorListState.value = ApiResponse.Success(it)
                    }
                }
            }

            override fun onError(message: String) {
                callback.onError(message)
            }

        })
    }
}