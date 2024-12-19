package com.bikeblooms.android.ui.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bikeblooms.android.databinding.FragmentServiceDetailBinding
import com.bikeblooms.android.model.Service

class ServiceDetailFragment : Fragment() {


    private val viewModel: ServiceDetailViewModel by viewModels()
    private lateinit var binding: FragmentServiceDetailBinding
    private val args: ServiceDetailFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentServiceDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initViews(args.service)
    }


    private fun FragmentServiceDetailBinding.initViews(service: Service?) {

        var spareText = ""
        var priceText = ""
        var totalAmount = 0.0
        service?.spareParts?.forEach {
            spareText += it.name + "\n\n"
            priceText += it.price.toString() + "\n\n"
            totalAmount += it.price
        }
        service?.complaints?.forEach {
            spareText += it.name + "\n\n"
            priceText += it.price.toString() + "\n\n"
            totalAmount += it.price
        }

        spareText += "Total Amount"
        priceText += totalAmount.toString()

        tvSpares.text = spareText
        tvPrice.text = priceText
    }

}
