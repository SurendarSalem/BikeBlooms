package com.bikeblooms.android.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.bikeblooms.android.R
import com.bikeblooms.android.databinding.FragmentListItemsBinding
import com.bikeblooms.android.databinding.NameItemBinding
import com.bikeblooms.android.model.AppState
import com.bikeblooms.android.model.ProfileItem
import com.bikeblooms.android.model.isAdmin
import com.bikeblooms.android.ui.Utils
import com.bikeblooms.android.ui.adapter.GenericAdapter
import com.bikeblooms.android.ui.authentication.AuthenticationActivity
import com.bikeblooms.android.ui.base.BaseFragment
import com.bikeblooms.android.ui.user.UserDetailFragmentArgs
import com.bikeblooms.android.util.SharedPrefHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.Any

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper
    lateinit var binding: FragmentListItemsBinding
    var profileItems = ProfileItem.entries.toList()

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
        binding = FragmentListItemsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter
        if (AppState.user?.isAdmin() == true) {
            profileItems = profileItems.filter {
                it != ProfileItem.MY_VEHICLES
            }
        }
        adapter.setItem(profileItems)
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
                if (item is ProfileItem) {
                    if (item.title == ProfileItem.SETTINGS.title) {
                        binding.tvName.text = buildString {
                            append("Our Services:").append("\n")
                            append("\u2713 Free Pick and Drop").append("\n")
                            append("\u2713 Expert Workmanship").append("\n")
                            append("\u2713 Genuine Spare parts and Best Pricing").append("\n")
                        }
                    } else {
                        binding.tvName.text = item.title
                    }
                }
            }
        }
    }

    private fun onItemClick(item: Any) {
        if (item is ProfileItem) {
            if (item == ProfileItem.MY_VEHICLES) {
                findNavController().navigate(R.id.navigation_vehicle_list)
            } else if (item == ProfileItem.BASIC_INFO) {
                val args = UserDetailFragmentArgs(AppState.user)
                findNavController().navigate(R.id.navigation_user_detail, args.toBundle())
            } else if (item == ProfileItem.CONTACT_US) {
                findNavController().navigate(R.id.navigation_contact_us)
            } else if (item == ProfileItem.LOGOUT) {
                Utils.showAlertDialog(
                    context = requireContext(),
                    message = "Are you sure want to logout?",
                    positiveBtnText = "Yes, Logout",
                    positiveBtnCallback = {
                        logout()
                    },
                    negativeBtnText = "Cancel"
                )
            } else {
                showToast("Development in Progress")
            }
        }
    }

    private fun logout() {
        Firebase.auth.signOut()
        sharedPrefHelper.clearAll()
        AppState.user = null
        val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}