package com.bikeblooms.android.ui.service

import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentMyServicesBinding
import com.bikeblooms.android.databinding.ServiceItemBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.Service
import com.bikeblooms.android.ui.adapter.GenericAdapter
import com.bikeblooms.android.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.Any

@AndroidEntryPoint
class MyServicesFragment : BaseFragment() {

    private val adapter: GenericAdapter<Any> by lazy {
        val layoutResIds = SparseIntArray().apply {
            put(0, R.layout.service_item)
        }
        GenericAdapter(requireContext(), layoutResIds, ::bindViewHolder, ::onItemClick)
    }
    private val viewModel: MyServicesViewModel by viewModels()
    private lateinit var binding: FragmentMyServicesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyServicesBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.progressBar
        binding.rvServices.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myServicesState.collectLatest {
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

    fun onServicesLoaded(services: List<Service>?) {
        if (services.isNullOrEmpty()) {

        } else {
            binding.rvServices.adapter = adapter
            adapter.setItem(services)
        }
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
                    binding.tvRegNum.text = item.regNum
                    binding.tvStatus.text = item.progress.title
                }
            }
        }
    }

    private fun onItemClick(item: Any, isBookServiceClicked: Boolean = false) {}
}