package com.example.myapplication.model

data class CityResponse(
    val code: String,
    val location: List<CityLocation>
)

data class CityLocation(
    val name: String,
    val id: String
)
