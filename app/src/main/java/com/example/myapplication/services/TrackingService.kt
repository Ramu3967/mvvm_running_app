package com.example.myapplication.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.ui.MainActivity
import com.example.myapplication.util.RunConstants.ACTION_LOCATION_PERMISSION
import com.example.myapplication.util.RunConstants.ACTION_PAUSE_SERVICE
import com.example.myapplication.util.RunConstants.ACTION_START_OR_RESUME_SERVICE
import com.example.myapplication.util.RunConstants.ACTION_STOP_SERVICE
import com.example.myapplication.util.RunConstants.ACTION_TRACKING_FRAGMENT
import com.example.myapplication.util.RunConstants.CHANNEL_ID
import com.example.myapplication.util.RunConstants.CHANNEL_NAME
import com.example.myapplication.util.RunConstants.EXTRA_PERMISSION
import com.example.myapplication.util.RunConstants.LOCATION_FASTEST_INTERVAL
import com.example.myapplication.util.RunConstants.LOCATION_INTERVAL
import com.example.myapplication.util.RunConstants.LOCATION_MAX_WAIT_TIME
import com.example.myapplication.util.RunConstants.NOTIFICATION_ID
import com.example.myapplication.util.RunConstants.hasLocationPermissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {
    private val TAG=TrackingService::class.java.simpleName
    private var isFirstRun = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationCallback= object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            for(location in result.locations) {
                addPathPoint(location)
                Log.d(TAG, "onLocationResult: ${location.latitude} ${location.longitude}")
            }
        }
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val granted = intent?.getBooleanExtra(EXTRA_PERMISSION,false)
            if(granted!=null && granted==true) isTracking.value=true
            else requestLocationPermissions()
        }
    }

    companion object{
        // tells the observers whether the service is tracking the location or not
        val isTracking = MutableStateFlow(false)
        //val pathPoints = MutableLiveData<MutableList<MutableList<LatLng>>>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)
        registerReceiver(receiver, IntentFilter(ACTION_LOCATION_PERMISSION))
        postInitialValues()

        lifecycleScope.launch(Dispatchers.IO) {
            isTracking.collect{
                requestLocationUpdates(it)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_STOP_SERVICE-> Log.d(TAG, "stop service")
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun) {
                        startForegroundService()
                        isFirstRun=false
                    }
                    else Log.d(TAG, "service is running already")
                }
                ACTION_PAUSE_SERVICE -> Log.d(TAG, "service paused")
                else-> Log.d(TAG, "NONE")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        isTracking.value=false
    }

    private fun startForegroundService(){
        addEmptyPolyline()
        isTracking.value=true

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)
        val notificationBuilder=NotificationCompat.Builder(this, CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true) // can't be swiped away
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
        notificationBuilder.setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent():PendingIntent{
        val intent = Intent(this,MainActivity::class.java).also { it.action = ACTION_TRACKING_FRAGMENT }
        // this flag is used to update the current intent if it already exists
        return PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_MUTABLE)

    }

    private fun postInitialValues(){
        pathPoints.postValue(mutableListOf())
        isTracking.value=false
    }

    private fun addEmptyPolyline()= pathPoints.value?.apply {
        add(mutableListOf())
        // we've just added an empty list into the livedata's value (which is a list of polyline), but we need to update the livedata itself
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun addPathPoint(location:Location?){
        location?.let {
            val position = LatLng(location.latitude,location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                // post updates immediately after changing the content of the live data
                pathPoints.postValue(this)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates(flag:Boolean){
        if(flag){
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
                .setMaxUpdateDelayMillis(LOCATION_MAX_WAIT_TIME)
                .build()
            if (hasLocationPermissions()) fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
            else requestLocationPermissions()
        }
        else fusedLocationClient.removeLocationUpdates(locationCallback)

    }

    private fun requestLocationPermissions(){
        if(!hasLocationPermissions()){
            // an activity which can give us the activity context for requesting permissions
            startActivity(Intent(this,PermissionsActivity::class.java)
                .also { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).also {notificationManager.createNotificationChannel(it)}
        }
    }
}