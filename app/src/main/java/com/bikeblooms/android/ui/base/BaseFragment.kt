package com.bikeblooms.android.ui.base

import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.bikeblooms.android.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseFragment : Fragment() {
    lateinit var progressBar: ProgressBar

    fun showProgress() {
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        requireActivity().window.setDimAmount(0.5f)
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgress() {
        progressBar.visibility = View.GONE
        requireActivity().window.setDimAmount(1f)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun showToast(message: String, error: Boolean = false) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).setAnimationMode(
            BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        ).setBackgroundTint(requireContext().getColor(R.color.cherry_red))
            .setTextColor(requireContext().getColor(R.color.white))
            .show()

    }
}