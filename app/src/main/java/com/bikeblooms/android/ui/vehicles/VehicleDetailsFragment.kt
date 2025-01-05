package com.bikeblooms.android.ui.vehicles

import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentVehicleDetailsBinding
import com.bikeblooms.android.databinding.ServiceItemBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.Progress
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.model.isAdmin
import com.bikeblooms.android.ui.VendorListFragmentArgs
import com.bikeblooms.android.ui.adapter.GenericAdapter
import com.bikeblooms.android.ui.base.BaseFragment
import com.bikeblooms.android.ui.service.ServiceDetailFragmentArgs
import com.bikeblooms.android.util.toRegNum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.Any


@AndroidEntryPoint
class VehicleDetailsFragment : BaseFragment() {

    private val adapter: GenericAdapter<Any> by lazy {
        val layoutResIds = SparseIntArray().apply {
            put(0, R.layout.service_item)
        }
        GenericAdapter(requireContext(), layoutResIds, ::bindViewHolder, ::onItemClick)
    }
    private val viewModel: VehicleDetailsViewModel by viewModels()
    private lateinit var binding: FragmentVehicleDetailsBinding
    private val args: VehicleDetailsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentVehicleDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.progressBar
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        args.vehicle?.let {
            viewModel.getServices("regNum", it.regNo)
            displayVehicleDetails(it)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allServicesState.collectLatest {
                when (it) {
                    is ApiResponse.Success -> {
                        hideProgress()
                        onServicesLoaded(it.data)
                    }

                    is ApiResponse.Error -> {
                        hideProgress()
                    }

                    is ApiResponse.Loading -> {
                        showProgress()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun displayVehicleDetails(vehicle: Vehicle) {
        binding.tvVehicleType.text = vehicle.brand?.name
        binding.tvVehicleModel.text = vehicle.model
        if (vehicle.regNo.isNotEmpty() && vehicle.regNo.length >= 8) {
            binding.rn1.setText(vehicle.regNo.substring(0, 2))
            binding.rn2.setText(vehicle.regNo.substring(2, 4))
            binding.rn3.setText(vehicle.regNo.substring(4, 6))
            binding.rn4.setText(vehicle.regNo.substring(6, vehicle.regNo.length))
        }
    }

    fun onServicesLoaded(services: List<Service>?) = if (services.isNullOrEmpty()) {

    } else {
        binding.rvHistory.adapter = adapter
        adapter.setItem(services)
    }

    private fun bindViewHolder(view: View, item: Any, viewType: Int) {
        val binding: ViewBinding = when (viewType) {
            0 -> ServiceItemBinding.bind(view)
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
        when (binding) {
            is ServiceItemBinding -> {
                if (item is Service) {
                    binding.tvName.text = item.vehicleName
                    binding.tvRegNum.text = item.regNum.toRegNum()
                    binding.tvStatus.text = item.progress.title
                    val df = SimpleDateFormat("MMM dd", Locale.US);
                    binding.tvDate.text = "Updated at " + df.format(item.startDate)
                    binding.tvTotalAmt.text = "\u20B9 " + item.bill?.totalAmount.toString()
                    if (AppState.user?.isAdmin() == true) {
                        binding.tvAssign.visibility = View.VISIBLE
                        item.assignee?.let {
                            binding.tvAssign.text = it.name
                            binding.tvAssign.isClickable = false
                        } ?: run {
                            binding.tvAssign.text = getString(R.string.assign)
                            binding.tvAssign.isClickable = true
                            binding.tvAssign.setOnClickListener {
                                openVendorListScreen(item)
                            }
                        }
                    } else {
                        binding.tvAssign.visibility = View.GONE
                    }
                    if (item.progress == Progress.CANCELLED) {
                        binding.tvAssign.setTextColor(resources.getColor(R.color.cherry_red))
                    } else {
                        binding.tvAssign.setTextColor(resources.getColor(com.google.android.libraries.places.R.color.quantum_googgreen))
                    }
                    binding.tvStatus.text = item.progress.title
                }
            }
        }
    }

    private fun onItemClick(item: Any) {
        if (item is Service) {
            val args = ServiceDetailFragmentArgs(item)
            findNavController().navigate(
                R.id.action_navigation_bookings_to_navigation_service_detail, args.toBundle()
            )
        }
    }

    fun openVendorListScreen(service: Service) {
        var args = VendorListFragmentArgs(true, service)
        findNavController().navigate(R.id.navigation_vendors, args.toBundle())
    }
}