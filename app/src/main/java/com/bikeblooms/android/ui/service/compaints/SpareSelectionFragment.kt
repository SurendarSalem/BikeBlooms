package com.bikeblooms.android.ui.service.compaints

import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentVehicleListDialogBinding
import com.bikeblooms.android.databinding.NameItemBinding
import com.bikeblooms.android.model.Spare
import com.bikeblooms.android.ui.adapter.GenericAdapter
import com.bikeblooms.android.util.AppConstants.SELECTED_SPARE
import com.bikeblooms.android.util.AppConstants.VEHICLE
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.Any
import kotlin.getValue

const val ARG_VEHICLES = "vehicles"

@AndroidEntryPoint
class SpareSelectionFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentVehicleListDialogBinding
    private val viewModel: AddComplaintsViewModel by activityViewModels()
    private val args: SpareSelectionFragmentArgs by navArgs()
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
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddVehicle.visibility = View.GONE
        binding.tvHeading.text = "Select your Engine Oil"
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter
        args.spares?.let {
            adapter.setItem(it.toList())
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
                if (item is Spare) {
                    binding.tvName.text = item.name
                    binding.cbSelected.visibility =
                        if (viewModel.selectedSpareState.value?.name == item.name) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }
            }
        }
    }

    private fun onItemClick(item: Any, isBookServiceClicked: Boolean = false) {
        if (item is Spare) {
            viewModel.updateSelectedVehicle(item)
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                SELECTED_SPARE, item
            )
            this@SpareSelectionFragment.dismiss()
        }
    }

}