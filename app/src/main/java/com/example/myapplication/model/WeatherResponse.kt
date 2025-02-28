package com.example.myapplication.model

data class WeatherResponse(
    val code: String,
    val daily: List<WeatherDaily>
)

data class WeatherDaily(
    val fxDate: String,
    val textDay: String
)