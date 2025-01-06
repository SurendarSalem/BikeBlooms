package com.bikeblooms.android.ui.contactus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bikeblooms.android.databinding.FragmentContactUsBinding
import com.bikeblooms.android.ui.Utils
import com.bikeblooms.android.ui.base.BaseFragment
import com.bikeblooms.android.util.FirebaseConstants


class ContactUsFragment : BaseFragment() {

    private lateinit var binding: FragmentContactUsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactUsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvMobileNum.setOnClickListener {
            Utils.callPhone(requireContext(), binding.tvMobileNum.text.toString())
        }
        binding.tvEmail.setOnClickListener {
            Utils.sendEmail(requireContext(), binding.tvEmail.text.toString())
        }
        binding.ivFb.setOnClickListener {
            Utils.openUrl(requireContext(), FirebaseConstants.Social.FB_URL)
        }
        binding.ivInsta.setOnClickListener {
            Utils.openUrl(requireContext(), FirebaseConstants.Social.INSTA_URL)
        }
    }
}