package com.bikeblooms.android

interface LoginCallback<in T> {
    fun onSuccess(t: T)
    fun onError(message: String)
}