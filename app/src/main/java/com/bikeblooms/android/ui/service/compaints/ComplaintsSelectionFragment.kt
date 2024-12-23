package com.bikeblooms.android.ui.service.compaints

import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentListItemsBinding
import com.bikeblooms.android.databinding.ListItemCheckboxBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Complaint
import com.bikeblooms.android.ui.adapter.GenericAdapter
import com.bikeblooms.android.ui.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

class ComplaintsSelectionFragment : BaseFragment() {
    lateinit var binding: FragmentListItemsBinding
    private val addComplaintsViewModel: AddComplaintsViewModel by activityViewModels()
    private var selectedComplaints = listOf<Complaint>()
    lateinit var callback: OnBackPressedCallback
    private val adapter: GenericAdapter<Any> by lazy {
        val layoutResIds = SparseIntArray().apply {
            put(0, R.layout.list_item_checkbox)
        }
        GenericAdapter(requireContext(), layoutResIds, ::bindViewHolder, ::onItemClick)
    }
    val args: ComplaintsSelectionFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeStates()
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "selectedComplaints", selectedComplaints.filter { it.isSelected == true }
                )
                findNavController().popBackStack()
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            addComplaintsViewModel.complaintsState.collectLatest { result ->
                if (result is ApiResponse.Success) {
                    result.data?.let {
                        this@ComplaintsSelectionFragment.selectedComplaints = it
                        adapter.setItem(it)
                    }
                }
            }
        }
    }

    private fun bindViewHolder(view: View, item: Any, viewType: Int) {
        val binding: ViewBinding = when (viewType) {
            0 -> ListItemCheckboxBinding.bind(view)
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
        when (binding) {
            is ListItemCheckboxBinding -> {
                if (item is Complaint) {
                    binding.tvName.text = item.name
                    binding.cbSelected.visibility = View.VISIBLE
                    if (args.complaints?.contains(item) == true) {
                        item.isSelected = true
                    }
                    binding.cbSelected.isChecked = item.isSelected
                    binding.cbSelected.setOnCheckedChangeListener { _, isChecked ->
                        item.isSelected = isChecked
                    }
                }
            }
        }
    }


    private fun onItemClick(item: Any) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        //unregister listener here
        callback.isEnabled = false
        callback.remove()
    }

}

