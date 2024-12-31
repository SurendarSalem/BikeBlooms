package com.bikeblooms.android.ui.spares

import android.os.Bundle
import android.util.Log
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
import com.bikeblooms.android.databinding.FragmentListItemsBinding
import com.bikeblooms.android.databinding.SpareHeaderItemBinding
import com.bikeblooms.android.databinding.SpareItemBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.SpareCategories
import com.bikeblooms.android.model.SpareCategory
import com.bikeblooms.android.model.SpareItem
import com.bikeblooms.android.model.UISPareItem
import com.bikeblooms.android.ui.adapter.GenericAdapter
import com.bikeblooms.android.ui.base.BaseFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.Any

class SparesFragment : BaseFragment() {

    private val viewModel: SparesViewModel by viewModels()
    private lateinit var binding: FragmentListItemsBinding
    private var spareCategories = emptyList<SpareCategory>()
    private val adapter: GenericAdapter<Any> by lazy {
        val layoutResIds = SparseIntArray().apply {
            put(0, R.layout.spare_header_item)
            put(1, R.layout.spare_item)
        }
        GenericAdapter(requireContext(), layoutResIds, ::bindViewHolder, ::onItemClick)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListItemsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.progressBar
        initViews()
        observeStates()
        viewModel.getAllSparesAndAccessories()
    }

    private fun initViews() {
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sparesAndAccessoriesState.collectLatest { result ->
                when (result) {
                    is ApiResponse.Loading -> {
                        showProgress()
                    }

                    is ApiResponse.Error -> {
                        hideProgress()
                    }

                    is ApiResponse.Success -> {
                        hideProgress()
                        onSparesLoaded(result)
                    }

                    else -> {}
                }
            }
        }
    }

    fun onSparesLoaded(apiResponse: ApiResponse.Success<List<SpareCategory>>) {
        apiResponse.data?.let { items ->
            this.spareCategories = items
            var uiSpareItems = mutableListOf<UISPareItem>()
            items.forEach {
                uiSpareItems.add(it)
                uiSpareItems.addAll(it.items)
            }
            Log.d("Surendar", uiSpareItems.toString())
            adapter.setItem(uiSpareItems)
            binding.fabAdd.visibility = View.VISIBLE
            binding.fabAdd.text = "Add Spare"
            binding.fabAdd.setOnClickListener {
                val spareCategoryList = SpareCategories()
                spareCategoryList.addAll(spareCategories)
                var args = SpareAddUpdateFragmentArgs(false, null, spareCategoryList)
                findNavController().navigate(
                    R.id.action_navigation_spares_to_navigation_spares_update, args.toBundle()
                )
            }
        }
    }


    private fun bindViewHolder(view: View, item: Any, viewType: Int) {
        val binding: ViewBinding = when (viewType) {
            0 -> SpareHeaderItemBinding.bind(view)
            1 -> SpareItemBinding.bind(view)
            else -> {
                throw IllegalArgumentException("Invalid view type")
            }
        }
        when (binding) {
            is SpareHeaderItemBinding -> {
                if (item is SpareCategory) binding.tvCategoryName.text = item.name
            }

            is SpareItemBinding -> {
                if (item is SpareItem) {
                    binding.tvSpareName.text = item.name
                    binding.tvQty.text = "Qty :${item.qty}"
                    binding.tvPrice.text = "Price: \u20B9 ${item.price}"
                }
            }
        }
    }

    private fun onItemClick(item: Any) {
        if (item is SpareItem) {
            val spareCategoryList = SpareCategories()
            spareCategoryList.addAll(spareCategories)
            var args = SpareAddUpdateFragmentArgs(true, item, spareCategoryList)
            findNavController().navigate(
                R.id.action_navigation_spares_to_navigation_spares_update, args.toBundle()
            )
        }
    }
}
