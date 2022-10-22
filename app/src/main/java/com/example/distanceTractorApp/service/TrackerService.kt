package com.example.distanceTractorApp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.distanceTractorApp.util.Constant
import com.example.distanceTractorApp.util.Constant.LOCATION_FASTEST_UPDATE_INTERVAL
import com.example.distanceTractorApp.util.Constant.LOCATION_UPDATE_INTERVAL
import com.example.distanceTractorApp.util.Constant.NOTIFICATION_ID
import com.example.distanceTractorApp.util.MapUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService:LifecycleService() {
    private  val TAG = "TrackerService"
    @Inject
    lateinit var notification: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    companion object{
        val started=MutableLiveData<Boolean>()

        var locationList=MutableLiveData<MutableList<LatLng>>()

        var startTime=MutableLiveData<Long>()
        var stopTime=MutableLiveData<Long>()
    }
    private fun setIntialValue(){
        started.postValue(false)
        locationList.postValue(mutableListOf())
    }
    override fun onCreate() {
        Log.d(TAG, "onCreate() called")
        setIntialValue()
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                Constant.ACTION_SERVICE_START->{
                    started.postValue(true)
                    Log.d(TAG, "onStartCommand() called")
                    startForegroundService()
                    startLocationUpdates()
                }
                Constant.ACTION_SERVICE_STOP->
                {
                    started.postValue(false)
                    stopForegroundService()
                }

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private  fun startForegroundService(){
        Log.d(TAG, "startForegroundService() called")
         createNotificationChannel()
         startForeground(NOTIFICATION_ID,notification.build())
     }


    private fun createNotificationChannel(){
        Log.d(TAG, "createNotificationChannel() called")
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
            val channel= NotificationChannel(Constant.NOTOFICATION_CHANNEL_ID,Constant.NOTIFICATION_CHANNEL_NAME,IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)

        }

    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(){
        val locationRequest=LocationRequest.create().apply {
            interval= LOCATION_UPDATE_INTERVAL
            fastestInterval= LOCATION_FASTEST_UPDATE_INTERVAL
            priority=Priority.PRIORITY_HIGH_ACCURACY


        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper())

        startTime.postValue(System.currentTimeMillis())
    }
    private val locationCallback=object :LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.let{
                locations->
                for(location in locations){
//                    val newlatlong=LatLng(location.latitude,location.longitude)
                     updateLocationList(location)
                     updaateNotificationPeriodcly()
                }
            }

        }
    }

    private  fun updateLocationList(location: Location){
        val newLatLng = LatLng(location.latitude,location.latitude)
        locationList.value?.apply {
            add(newLatLng)
            locationList.postValue(this)


        }

    }

    private  fun stopForegroundService(){
        removeLocationUpdate()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(Constant.NOTIFICATION_ID)
        stopForeground(true)
        stopSelf()
        stopTime.postValue(System.currentTimeMillis())

    }

    private fun removeLocationUpdate(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


    private fun updaateNotificationPeriodcly(){
        notification.apply {
            setContentTitle("Distance Travelled")
            setContentText(locationList.value?.let { MapUtil.calculatedTotalDistance(it) } +"km")
        }
        notificationManager.notify(NOTIFICATION_ID,notification.build())
    }
}