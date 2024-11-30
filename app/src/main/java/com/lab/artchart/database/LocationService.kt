package com.lab.artchart.database

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Handler
import android.os.IBinder

class LocationService : Service(), LocationHelper.LocationCallback {

    companion object{
        const val MSG_LOCATION_VALUE = 1
    }

    private lateinit var myBinder : LocationBinder
    private var msgHandler : Handler? = null

    private lateinit var locationHelper: LocationHelper
    private lateinit var myBroadcastReceiver : MyBroadcastReceiver

//    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        myBinder = LocationBinder()
        //initialize locationHelper
        locationHelper = LocationHelper(this)
        locationHelper.setLocationCallback(this)
        locationHelper.initLocationManager()
    }

    override fun onLocationChanged(location: Location) {
//        serviceScope.launch {
            sendMsgHandler(location)
//        }
    }

    private fun sendMsgHandler(location : Location){
        if (msgHandler != null){
//            val bundle = Bundle()
            val message = msgHandler!!.obtainMessage()
//            message.data = bundle
            message.obj = location
            message.what = MSG_LOCATION_VALUE

//            withContext(Dispatchers.Main){
            msgHandler!!.sendMessage(message)
//            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return myBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        msgHandler = null
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.destroyLocationManager()
//        serviceScope.cancel()
    }

    inner class LocationBinder:Binder(){
        fun setmsgHandler(handler: Handler){
            msgHandler = handler
        }
    }

    inner class MyBroadcastReceiver : BroadcastReceiver(){
        //stops the service
        override fun onReceive(context: Context?, intent: Intent?) {
            stopSelf()
            unregisterReceiver(myBroadcastReceiver)
        }
    }
}