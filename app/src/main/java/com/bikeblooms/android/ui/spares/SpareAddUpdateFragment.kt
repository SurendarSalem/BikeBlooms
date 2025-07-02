package com.bikeblooms.android.ui.spares

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentSpareAddUpdateBinding
import com.bikeblooms.android.model.ApiResponse
import com.bikeblooms.android.model.SpareCategory
import com.bikeblooms.android.model.SpareItem
import com.bikeblooms.android.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class SpareAddUpdateFragment : BaseFragment() {

    lateinit var binding: FragmentSpareAddUpdateBinding
    val args: SpareAddUpdateFragmentArgs by navArgs()
    private val viewModel: AddUpdateSpareViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSpareAddUpdateBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = binding.progressBar
        observeStates()
        setData()
        binding.initViews()
    }

    private fun setData() {
        if (args.isEdit) {
            binding.spCategory.visibility = View.GONE
            args.spareItem?.let {
                binding.etName.setText(it.name)
                binding.etQty.setText(it.qty.toString())
                binding.etPrice.setText(it.price.toString())
            }
        } else {
            binding.btnUpdate.text = getString(R.string.add)
            args.spareCategories?.let {
                binding.spCategory.adapter = SparesAdapter(requireContext(), it.toList())
            }
        }
    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateSpareState.collectLatest { result ->
                when (result) {
                    is ApiResponse.Success -> {
                        hideProgress()
                        showToast("Spare updated successfully")
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

    class SparesAdapter(context: Context, var spareCategories: List<SpareCategory>) :
        ArrayAdapter<SpareCategory>(context, 0) {
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = if (convertView == null) {
                layoutInflater.inflate(R.layout.name_item, parent, false)
            } else {
                convertView
            }
            getItem(position)?.let { spareCategory ->
                setItem(view, spareCategory)
            }
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val view = if (convertView == null) {
                layoutInflater.inflate(R.layout.name_item, parent, false)
            } else {
                convertView
            }
            getItem(position)?.let { spareCategory ->
                setItem(view, spareCategory)
            }
            return view
        }


        override fun getItem(position: Int): SpareCategory? {
            return spareCategories[position]
        }

        override fun getCount(): Int {
            return spareCategories.size
        }

        private fun setItem(view: View, spareCategory: SpareCategory) {
            view.findViewById<TextView>(R.id.tv_name).text = spareCategory.name
        }
    }

    private fun FragmentSpareAddUpdateBinding.initViews() {
        btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        btnUpdate.setOnClickListener {
            if (args.isEdit) {
                val spareItem = args.spareItem
                spareItem?.let {
                    it.qty = binding.etQty.text.toString().toInt()
                    it.price = binding.etPrice.text.toString().toDouble()
                    viewModel.updateSpare(it)
                }
            } else {
                args.spareCategories?.let { spareCategories ->
                    val newSpare = SpareItem().apply {
                        name = binding.etName.text.toString()
                        id = binding.etName.text.toString().lowercase().replace(" ", "")
                        qty = binding.etQty.text.toString().toInt()
                        price = binding.etPrice.text.toString().toDouble()
                        spareCategoryId =
                            spareCategories[binding.spCategory.selectedItemPosition].id
                    }
                    viewModel.updateSpare(newSpare)
                }

            }
        }
    }
}


