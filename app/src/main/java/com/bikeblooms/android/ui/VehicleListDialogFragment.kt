package com.bikeblooms.android.ui

import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentVehicleListDialogBinding
import com.bikeblooms.android.databinding.NameItemBinding
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.ui.adapter.GenericAdapter
import com.bikeblooms.android.ui.vehicles.VehicleViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.Any
import kotlin.getValue

const val ARG_VEHICLES = "vehicles"

@AndroidEntryPoint
class VehicleListDialogFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentVehicleListDialogBinding
    private val viewModel: VehicleViewModel by activityViewModels()
    private val args: VehicleListDialogFragmentArgs by navArgs()
    private val adapter: GenericAdapter<Any> by lazy {
        val layoutResIds = SparseIntArray().apply {
            put(0, R.layout.name_item)
        }
        GenericAdapter(requireContext(), layoutResIds, ::bindViewHolder, ::onItemClick)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVehicleListDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter
        args.vehicles?.let {
            adapter.setItem(it)
        }
    }

    private fun bindViewHolder(view: View, item: Any, viewType: Int) {
        val binding: ViewBinding = when (viewType) {
            0 -> NameItemBinding.bind(view)
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
        when (binding) {
            is NameItemBinding -> {
                if (item is Vehicle) {
                    binding.tvName.text = item.name
                    binding.cbSelected.visibility = if (args.selectedVehicle?.regNo == item.regNo) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }
        }
    }

    private fun onItemClick(item: Any, isBookServiceClicked: Boolean = false) {
        if (item is Vehicle) {
            viewModel.updateSelectedVehicle(item)
            this@VehicleListDialogFragment.dismiss()
        }
    }

}