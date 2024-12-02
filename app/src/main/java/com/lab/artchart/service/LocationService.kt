package com.lab.artchart.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder

class LocationService : Service() {
    private lateinit var locationBinder: Binder
    private lateinit var locationMessageHandler: Handler

    override fun onCreate() {
        super.onCreate()
        locationBinder = LocationBinder()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY // when service is killed, remain stopped until explicitly started again
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    inner class LocationBinder : Binder() {
        fun setMessageHandler(handler: Handler) {
            locationMessageHandler = handler
        }
    }
}