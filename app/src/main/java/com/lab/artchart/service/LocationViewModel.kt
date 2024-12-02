package com.lab.artchart.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationViewModel: ViewModel(), ServiceConnection {
    private var locationMessageHandler = LocationMessageHandler(Looper.getMainLooper())

    var userLocation = MutableLiveData<Location>()

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as LocationService.LocationBinder
        binder.setMessageHandler(locationMessageHandler)
        Log.d("LOCATION_VIEW_MODEL", "Setting up communication between location service and its view model")
    }

    override fun onServiceDisconnected(name: ComponentName?) {}

    inner class LocationMessageHandler(looper: Looper): Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }
}