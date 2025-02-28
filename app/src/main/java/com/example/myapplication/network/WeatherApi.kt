package com.example.myapplication.network

import com.example.myapplication.model.CityResponse
import com.example.myapplication.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v2/city/lookup")
    fun getCityInfo(
        @Query("key") key: String,
        @Query("location") location: String
    ): Call<CityResponse>

    @GET("v7/weather/3d")
    fun getWeatherInfo(
        @Query("key") key: String,
        @Query("location") locationId: String
    ): Call<WeatherResponse>
}