package com.lab.artchart.database

import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import android.widget.Toast

class LocationHelper(private val context: Context) : LocationListener {

    private lateinit var locationManager: LocationManager
    private var listener : LocationCallback? = null

    fun initLocationManager() {
        try {
            locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

            // check if GPS enabled on device
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.w("HOME_FRAG", "GPS not enabled, cannot use location")
                Toast.makeText(context, "Enable GPS on your device to find art near you", Toast.LENGTH_SHORT).show()
                return
            }

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                onLocationChanged(location)
            }

            // request location updates from GPS
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5f, this)
        } catch (e: SecurityException) {
            Log.e("HOME_FRAG", "Security error when initializing location manager: $e")
            Toast.makeText(context, "Allow location services to find art near you", Toast.LENGTH_SHORT).show()
        }
    }

    fun setLocationCallback(_listener : LocationCallback){
        listener = _listener
    }

    fun destroyLocationManager(){
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        if (listener!=null){
            listener?.onLocationChanged(location)
        }
    }

    interface LocationCallback{
        fun onLocationChanged(location:Location)
    }
}