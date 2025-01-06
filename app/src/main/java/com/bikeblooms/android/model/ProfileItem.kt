package com.bikeblooms.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ProfileItem(var title: String) : Parcelable {
    BASIC_INFO("Personal Info"), MY_VEHICLES("My Vehicles"), SETTINGS("Settings"), CONTACT_US("Contact Us"), LOGOUT(
        "Logout"
    )
}

@Parcelize
class ProfileItems : ArrayList<ProfileItem>(), Parcelable