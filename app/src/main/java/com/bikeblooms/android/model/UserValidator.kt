package com.bikeblooms.android.model

import android.util.Patterns

class UserValidator() {

    fun isValidForSignUp(user: User): String {
        if (user.name.isEmpty()) {
            return "Name must be not empty"
        }
        if (user.name.length < 3) {
            return "Name must contain atleast 3 letters"
        }
        if (user.mobileNum.isEmpty()) {
            return "Mobile Number must be not empty"
        }
        if (user.mobileNum.length < 10) {
            return "Please enter valid mobile number"
        }
        if (user.emailId.isEmpty()) {
            return "Email id must not be empty"
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(user.emailId).matches()) {
            return "Please enter valid mail id"
        }
        if (user.password.isEmpty()) {
            return "Password must be not empty"
        }
        if (user.password.length < 8) {
            return "Name must contain atleast 8 letters"
        }
        if (user.confirmPassword != user.password) {
            return "Password and confirm password should be same"
        }
        return "success"
    }

    fun isValidForLogin(user: User): String {
        if (user.emailId.isEmpty()) {
            return "Email id must not be empty"
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(user.emailId).matches()) {
            return "Please enter valid mail id"
        }
        if (user.password.isEmpty()) {
            return "Password must be not empty"
        }
        if (user.password.length < 8) {
            return "Name must contain atleast 8 letters"
        }
        return "success"
    }

    fun isValidVendorForRegister(user: Vendor): String {
        if (user.emailId.isEmpty()) {
            return "Email id must not be empty"
        }
        if (user.mobileNum.isEmpty() || user.mobileNum.length < 10) {
            return "Please enter a valid mobile number"
        }
        if (user.emailId.isEmpty()) {
            return "Email id must not be empty"
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(user.emailId).matches()) {
            return "Please enter valid mail id"
        }
        return "success"
    }

}