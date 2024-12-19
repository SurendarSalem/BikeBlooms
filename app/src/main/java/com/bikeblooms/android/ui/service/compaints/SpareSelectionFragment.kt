package com.bikeblooms.android.ui.service.compaints

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
import com.bikeblooms.android.model.Spare
import com.bikeblooms.android.ui.adapter.GenericAdapter
import com.bikeblooms.android.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SpareSelectionFragment : BaseFragment() {
    lateinit var binding: FragmentListItemsBinding
    private lateinit var selectedSpare: Spare
    private val adapter: GenericAdapter<Any> by lazy {
        val layoutResIds = SparseIntArray().apply {
            put(0, R.layout.name_item)
        }
        GenericAdapter(requireContext(), layoutResIds, ::bindViewHolder, ::onItemClick)
    }

    private val args: SpareSelectionFragmentArgs by navArgs()

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
                }
            }
        }
    }


    private fun onItemClick(item: Any) {
        if (item is Spare) {
            selectedSpare = item
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                "selectedSpare",
                selectedSpare
            )
            findNavController().popBackStack()
        }
    }

}

