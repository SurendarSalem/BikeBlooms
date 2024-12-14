package com.bikeblooms.android.ui.vehicles

import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentListItemsBinding
import com.bikeblooms.android.databinding.NameItemBinding
import com.bikeblooms.android.model.Brand
import com.bikeblooms.android.model.Vehicle
import com.bikeblooms.android.ui.adapter.GenericAdapter
import com.bikeblooms.android.ui.base.BaseFragment
import com.bikeblooms.android.util.AppConstants.BRAND
import com.bikeblooms.android.util.AppConstants.VEHICLE

class VehicleSelectionFragment : BaseFragment() {
    lateinit var binding: FragmentListItemsBinding
    val args: VehicleSelectionFragmentArgs by navArgs()

    private val adapter: GenericAdapter<Any> by lazy {
        val layoutResIds = SparseIntArray().apply {
            put(0, R.layout.name_item)
        }
        GenericAdapter(requireContext(), layoutResIds, ::bindViewHolder, ::onItemClick)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter
        args.brands?.let {
            adapter.setItem(it.toList())
        }
        args.vehicles?.let {
            adapter.setItem(it.toList())
        }
        args.profiles?.let {
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
                if (item is Brand) {
                    binding.tvName.text = item.name
                } else if (item is Vehicle) {
                    binding.tvName.text = item.name
                }
            }
        }
    }

    private fun onItemClick(item: Any) {
        if (item is Brand) {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                BRAND, item
            )
            findNavController().popBackStack()
        } else if (item is Vehicle) {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                VEHICLE, item
            )
            findNavController().popBackStack()
        }
    }
}

