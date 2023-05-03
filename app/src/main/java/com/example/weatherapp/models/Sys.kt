package com.weatherapp.models

import java.io.Serializable

data class Sys(
    val type: Int,
    val id:Long,
    val country: String,
    val sunrise: Int,
    val sunset: Int
) : Serializable