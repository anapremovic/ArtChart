package com.lab.artchart.database

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationViewModel():ViewModel(), ServiceConnection {

    private var myMessageHandler: MyMessageHandler = MyMessageHandler(Looper.getMainLooper())

    private var _location = MutableLiveData<Location>()
    val location: LiveData<Location>
        get() {
            return _location
        }

    //initialize the message handler in location binder
    override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
        val locationBinder = iBinder as LocationService.LocationBinder
        locationBinder.setmsgHandler(myMessageHandler)

//        when (name?.className) {
//            LocationService::class.java.name -> {
//                // Handle MapService connection
//                val mapBinder = iBinder as LocationService.LocationBinder
//                mapBinder.setmsgHandler(myMessageHandler) // Set the message handler for MapService
////                println("MapService Connected")
//            }
//
//            TrackingService::class.java.name -> {
//                val trackingBinder = iBinder as? TrackingService.TrackBinder
//                if (trackingBinder != null) {
//                    trackingBinder.setmsgHandler(myMessageHandler)
////                    println("TrackingService Connected")
//                } else {
////                    println("Failed to cast iBinder to TrackingService.MyBinder")
//                }
//            }
//        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        TODO("Not yet implemented")
    }

//    fun startLocationService(context: Context){
////        println("LOCATION IS BINDING")
//        val locationIntent = Intent(context, LocationService::class.java)
//        context.bindService(locationIntent, this, Context.BIND_AUTO_CREATE)
//    }
//
//    fun stopLocationService(context: Context){
//        context.unbindService(this)
//    }

    inner class MyMessageHandler(looper: Looper) : Handler(looper){

        override fun handleMessage(msg: Message) {
            if (msg.what == LocationService.MSG_LOCATION_VALUE){
                _location.value = msg.obj as Location
            }

//            super.handleMessage(msg)
        }
    }
}