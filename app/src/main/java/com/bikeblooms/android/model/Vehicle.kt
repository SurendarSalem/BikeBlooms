package com.bikeblooms.android.model

data class Vehicle(
    val name: String,
    val manufacture: String,
    val model: String,
    val ownerId: String,
    val services: List<Service>
) {}