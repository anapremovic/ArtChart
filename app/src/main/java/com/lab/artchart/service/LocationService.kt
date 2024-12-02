package com.lab.artchart.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast

class LocationService : Service(), LocationListener {
    companion object {
        const val LOCATION_MESSAGE_CODE = 100
    }

    private lateinit var locationBinder: Binder
    private lateinit var locationMessageHandler: Handler
    private lateinit var locationManager: LocationManager
    private var initialLocation: Location? = null

    override fun onCreate() {
        super.onCreate()
        locationBinder = LocationBinder()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY // when service is killed, remain stopped until explicitly started again
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("LOCATION_SERVICE", "Binding Location Service")
        initLocationManager()
        return locationBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
    }

    // configure location manager system service
    private fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            // check if GPS enabled on device
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.w("LOCATION_SERVICE", "GPS not enabled, cannot use location")
                Toast.makeText(this, "Enable GPS on your device to find art near you", Toast.LENGTH_SHORT).show()
                return
            }

            // request location updates from GPS
            initialLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            // update location every 5 seconds
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 0f, this)
        } catch (e: SecurityException) {
            Log.e("LOCATION_SERVICE", "Security error when initializing location manager: $e")
            Toast.makeText(this, "Allow location services to find art near you", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLocationChanged(location: Location) {
        sendLocation(location)
    }

    // send location to LocationViewModel
    private fun sendLocation(location: Location) {
        val message = locationMessageHandler.obtainMessage()
        message.what = LOCATION_MESSAGE_CODE
        val bundle = Bundle()
        bundle.putDouble("latitude", location.latitude)
        bundle.putDouble("longitude", location.longitude)
        message.data = bundle
        locationMessageHandler.sendMessage(message)
    }

    inner class LocationBinder : Binder() {
        fun setMessageHandler(handler: Handler) {
            locationMessageHandler = handler

            // immediately send initial location
            initialLocation?.let { sendLocation(it) }
        }
    }
}