package com.bikeblooms.android.ui

import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentMyVehiclesBinding
import com.bikeblooms.android.databinding.VendorItemBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Vendor
import com.bikeblooms.android.ui.adapter.GenericAdapter
import com.bikeblooms.android.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VendorListFragment : BaseFragment() {

    private val args: VendorListFragmentArgs by navArgs()
    private val adapter: GenericAdapter<Any> by lazy {
        val layoutResIds = SparseIntArray().apply {
            put(0, R.layout.vendor_item)
        }
        GenericAdapter(requireContext(), layoutResIds, ::bindViewHolder, ::onItemClick)
    }
    private val viewModel: VendorListViewModel by viewModels()
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.vendorListState.collectLatest {
                when (it) {
                    is ApiResponse.Success -> {
                        hideProgress()
                        onVendorsLoaded(it.data)
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
            viewModel._assignServiceState.collectLatest {
                when (it) {
                    is ApiResponse.Success -> {
                        hideProgress()/* findNavController().previousBackStackEntry?.savedStateHandle?.set(
                             SELECTED_VENDOR, item
                         )
                         findNavController().popBackStack()*/
                        findNavController().popBackStack()
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

    fun onVendorsLoaded(vehicles: List<Vendor>?) {
        if (vehicles.isNullOrEmpty()) {
            adapter.setItem(emptyList())
            binding.emptyLayout.root.visibility = View.VISIBLE
            binding.fabAddVehicle.visibility = View.GONE
        } else {
            binding.emptyLayout.root.visibility = View.GONE
            binding.rvVehicles.adapter = adapter
            adapter.setItem(vehicles)
            binding.fabAddVehicle.visibility = View.VISIBLE
            binding.fabAddVehicle.text = getString(R.string.add_vendor)
        }
    }

    private fun bindViewHolder(view: View, item: Any, viewType: Int) {
        val binding: ViewBinding = when (viewType) {
            0 -> VendorItemBinding.bind(view)
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
        when (binding) {
            is VendorItemBinding -> {
                if (item is Vendor) {
                    binding.tvName.text = item.name
                    binding.sivInitial.text = item.name.substring(0, 1).uppercase()
                }
            }
        }
    }

    private fun onItemClick(item: Any) {
        if (item is Vendor) {
            if (args.fromAssign) {
                Utils.showAlertDialog(context = requireContext(),
                    message = "Do you want to assign this service to ${item.name}?",
                    positiveBtnText = "Assign",
                    positiveBtnCallback = {
                        args.service?.let {
                            viewModel.assignService(item, it)
                        }
                    },
                    negativeBtnText = "Cancel",
                    negativeBtnCallback = {
                        findNavController().popBackStack()
                        Unit
                    })
            } else {
                if (item is Vendor) {
                    findNavController().navigate(R.id.action_navigation_vendors_to_navigation_user_detail)
                }
            }
        }
    }

}