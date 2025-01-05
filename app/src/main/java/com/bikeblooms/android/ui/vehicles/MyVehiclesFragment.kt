package com.bikeblooms.android.ui.vehicles

import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentMyVehiclesBinding
import com.bikeblooms.android.databinding.VehicleItemBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.NotifyState
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
class MyVehiclesFragment : BaseFragment() {

    private val adapter: GenericAdapter<Any> by lazy {
        val layoutResIds = SparseIntArray().apply {
            put(0, R.layout.vehicle_item)
        }
        GenericAdapter(requireContext(), layoutResIds, ::bindViewHolder, ::onItemClick)
    }
    private val viewModel: VehicleViewModel by activityViewModels()
    private lateinit var binding: FragmentMyVehiclesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyVehiclesBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.progressBar
        binding.rvVehicles.layoutManager = LinearLayoutManager(requireContext())

        binding.emptyLayout.btnAddItem.setOnClickListener {
            findNavController().navigate(R.id.navigation_add_vehicle)
        }
        binding.fabAddVehicle.setOnClickListener {
            findNavController().navigate(R.id.navigation_add_vehicle)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myVehiclesState.collectLatest {
                when (it) {
                    is ApiResponse.Success -> {
                        hideProgress()
                        onVehiclesLoaded(it.data)
                    }

                    is ApiResponse.Error -> {
                        binding.fabAddVehicle.visibility = View.GONE
                        hideProgress()
                    }

                    is ApiResponse.Loading -> {
                        binding.fabAddVehicle.visibility = View.GONE
                        showProgress()
                    }

                    else -> {}
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notifyState.collect {
                if (it is NotifyState.Success) {
                    showToast(it.message)
                } else if (it is NotifyState.Error) {
                    showToast(it.message)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deletedVehicleState.collectLatest {
                when (it) {
                    is ApiResponse.Loading -> {
                        showProgress()
                    }

                    else -> {
                        hideProgress()
                    }
                }
            }
        }
    }

    fun onVehiclesLoaded(vehicles: List<Vehicle>?) {
        if (vehicles.isNullOrEmpty()) {
            adapter.setItem(emptyList())
            binding.emptyLayout.root.visibility = View.VISIBLE
            binding.fabAddVehicle.visibility = View.GONE
        } else {
            binding.emptyLayout.root.visibility = View.GONE
            binding.rvVehicles.adapter = adapter
            adapter.setItem(vehicles)
            binding.fabAddVehicle.visibility = View.VISIBLE
        }
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
                                viewModel.delete(item, AppState.user?.firebaseId.toString())
                            })
                    }
                }
            }
        }
    }

    private fun onItemClick(item: Any) {
        if (item is Vehicle) {
            
        }
    }
}