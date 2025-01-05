package com.bikeblooms.android.ui.vehicles

import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentAllVehiclesBinding
import com.bikeblooms.android.databinding.VehicleItemBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.ui.Utils
import com.bikeblooms.android.ui.adapter.GenericAdapter
import com.bikeblooms.android.ui.base.BaseFragment
import com.bikeblooms.android.util.toRegNum
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.Any

@AndroidEntryPoint
class AllVehiclesFragment : BaseFragment() {

    private val adapter: GenericAdapter<Any> by lazy {
        val layoutResIds = SparseIntArray().apply {
            put(0, R.layout.vehicle_item)
        }
        GenericAdapter(requireContext(), layoutResIds, ::bindViewHolder, ::onItemClick)
    }

    private val viewModel: AllVehiclesViewModel by viewModels()
    private lateinit var binding: FragmentAllVehiclesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeStates()
    }

    private fun initViews() {
        progressBar = binding.progressBar
        binding.rvVehicles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVehicles.adapter = adapter
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allVehicleState.collectLatest { result ->
                when (result) {
                    is ApiResponse.Error -> {
                        hideProgress()
                    }

                    is ApiResponse.Loading -> {
                        showProgress()
                    }

                    is ApiResponse.Success -> {
                        hideProgress()
                        onVehicleLoaded(result.data)
                    }

                    else -> {}
                }
            }
        }
    }

    fun onVehicleLoaded(vehicles: List<Vehicle>?) {
        if (!vehicles.isNullOrEmpty()) {
            adapter.setItem(vehicles)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllVehiclesBinding.inflate(inflater)
        return binding.root
    }

    private fun bindViewHolder(view: View, item: Any, viewType: Int) {
        val binding: ViewBinding = when (viewType) {
            0 -> VehicleItemBinding.bind(view)
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
        when (binding) {
            is VehicleItemBinding -> {
                if (item is Vehicle) {
                    binding.tvName.text = item.name
                    binding.tvRegNum.text = item.regNo.toRegNum()
                    val icon = R.drawable.bike
                    binding.sivVehicle.setImageResource(icon)
                    binding.ivDelete.setOnClickListener {
                        Utils.showAlertDialog(context = requireContext(),
                            message = getString(R.string.delete_vehicle_msg),
                            positiveBtnText = "Delete",
                            positiveBtnCallback = {
                                // viewModel.delete(item, AppState.user?.firebaseId.toString())
                            })
                    }
                }
            }
        }
    }

    private fun onItemClick(item: Any) {
        if (item is Vehicle) {
            findNavController().navigate(
                R.id.navigation_vehicle_details, VehicleDetailsFragmentArgs(item).toBundle()
            )
        }
    }
}