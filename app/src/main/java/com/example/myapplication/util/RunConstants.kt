package com.example.myapplication.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat

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

    const val LOCATION_INTERVAL=10000L
    const val LOCATION_FASTEST_INTERVAL = 5000L
    const val LOCATION_MAX_WAIT_TIME = 20000L
    const val LOCATION_PERMISSION_REQUEST_CODE = 911

    val locationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    ) else arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION)


    fun View.show(){this.visibility=View.VISIBLE}
    fun View.remove(){this.visibility=View.GONE}
    fun View.hide(){this.visibility=View.INVISIBLE}


    fun Context.hasLocationPermissions() = locationPermissions.all {
        ContextCompat.checkSelfPermission(this,it) == PackageManager.PERMISSION_GRANTED
    }
}