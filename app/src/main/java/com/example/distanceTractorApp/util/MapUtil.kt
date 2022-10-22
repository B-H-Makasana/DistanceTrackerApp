package com.example.distanceTractorApp.util

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

object MapUtil {
    fun setCameraPosition(location:LatLng):CameraPosition{
        return CameraPosition.Builder().target(location).zoom(16f).build()
    }

    fun calculatedElapsedTime(startTime:Long, stopTime:Long):String{
        val elapsedTime=stopTime-startTime
        val second=(elapsedTime/1000).toInt()%60
        val minute =(elapsedTime/(1000*60) % 60)
        val hour =(elapsedTime/(1000*60*60)%24)

        return "$hour:$minute:$second"
    }

    fun calculatedTotalDistance(location: MutableList<LatLng>):String{
        if(location.size>1){
            val meters=SphericalUtil.computeDistanceBetween(location[0],location.last())
            val kilometers=meters/1000
            return java.text.DecimalFormat("#.##").format(kilometers)

        }
        return "0.00"
    }
}