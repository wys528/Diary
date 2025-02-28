package com.example.myapplication.utils

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

class LocationUtils(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var locationListener: LocationListener? = null

    fun getLocation(onLocationReceived: (Location) -> Unit) {
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onLocationReceived(location)
                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener as LocationListener
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}