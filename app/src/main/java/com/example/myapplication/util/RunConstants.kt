package com.example.myapplication.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myapplication.services.Polyline
import pub.devrel.easypermissions.EasyPermissions

object RunConstants {
    const val DATABASE_NAME = "database_running"

    const val ACTION_START_OR_RESUME_SERVICE="ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE="ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE="ACTION_STOP_SERVICE"
    const val ACTION_TRACKING_FRAGMENT = "ACTION_TRACKING_FRAGMENT"
    const val ACTION_LOCATION_PERMISSION = "ACTION_LOCATION_PERMISSION"

    const val EXTRA_PERMISSION = "permissions_granted"

    const val CHANNEL_NAME = "notification_channel_foreground_service"
    const val CHANNEL_ID = "id_foreground_service"
    const val NOTIFICATION_ID = 1
    const val NOTIFICATION_TITLE = "Running App"

    const val LOCATION_INTERVAL=4000L
    const val LOCATION_FASTEST_INTERVAL = 2000L
    const val LOCATION_MAX_WAIT_TIME = 2000L
    const val LOCATION_PERMISSION_REQUEST_CODE = 911

    const val POLYLINE_WIDTH = 8f
    const val POLYLINE_COLOR = Color.BLACK
    const val POLYLINE_CAMERA_ZOOM = 16f

    val locationPermissions= mutableListOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION)
        .apply {if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)}.toTypedArray()

    enum class SortingOptions { DATE, TIME, DISTANCE, AVG_SPEED, CALORIES }

    fun View.show(){this.visibility=View.VISIBLE}
    fun View.remove(){this.visibility=View.GONE}
    fun View.hide(){this.visibility=View.INVISIBLE}


    fun Context.hasLocationPermissions() = locationPermissions.all {
        ContextCompat.checkSelfPermission(this,it) == PackageManager.PERMISSION_GRANTED
    }

    fun Context.hasLocationPerm()=EasyPermissions.hasPermissions(this, *locationPermissions)

    fun formatMillisToMinutesSeconds(milliseconds: Long): String {
        val minutes = (milliseconds / (1000 * 60)) % 60
        val seconds = (milliseconds / 1000) % 60

        return String.format("%02d:%02d", minutes, seconds)
    }

    fun formatMillisToHoursMinutesSecondsMilliseconds(milliseconds: Long): String {
        val hours = (milliseconds / (1000 * 60 * 60)) % 24
        val minutes = (milliseconds / (1000 * 60)) % 60
        val seconds = (milliseconds / 1000) % 60
        val millisecondsRemainder = milliseconds % 1000
        return String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, millisecondsRemainder)
    }

    fun getPolylineLength(polyline:Polyline):Float{
        var distance=0f
        for(i in polyline.size-1 downTo 1 ){
            val pos1=polyline[i]
            val pos2=polyline[i-1]
            val result = FloatArray(1)
            Location.distanceBetween(pos1.latitude,pos1.longitude,pos2.latitude,pos2.longitude,result)
            distance+=result[0]
        }
        return distance
    }

}

