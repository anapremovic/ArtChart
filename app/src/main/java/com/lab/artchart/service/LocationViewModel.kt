package com.lab.artchart.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class LocationViewModel: ViewModel(), ServiceConnection {
    private var locationMessageHandler = LocationMessageHandler(Looper.getMainLooper())

    var userLocation = MutableLiveData<LatLng>()

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val binder = service as LocationService.LocationBinder
        binder.setMessageHandler(locationMessageHandler)
        Log.d("LOCATION_VIEW_MODEL", "Setting up communication between location service and its view model")
    }

    override fun onServiceDisconnected(name: ComponentName?) {}

    // receive user location from LocationService
    inner class LocationMessageHandler(looper: Looper): Handler(looper) {
        override fun handleMessage(message: Message) {
            if (message.what == LocationService.LOCATION_MESSAGE_CODE) {
                val bundle = message.data
                val latitude = bundle.getDouble("latitude")
                val longitude = bundle.getDouble("longitude")

                userLocation.value = LatLng(latitude, longitude)
            }
        }
    }
}