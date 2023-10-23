package com.example.myapplication.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.util.RunConstants.ACTION_LOCATION_PERMISSION
import com.example.myapplication.util.RunConstants.ACTION_PAUSE_SERVICE
import com.example.myapplication.util.RunConstants.ACTION_START_OR_RESUME_SERVICE
import com.example.myapplication.util.RunConstants.ACTION_STOP_SERVICE
import com.example.myapplication.util.RunConstants.CHANNEL_ID
import com.example.myapplication.util.RunConstants.CHANNEL_NAME
import com.example.myapplication.util.RunConstants.EXTRA_PERMISSION
import com.example.myapplication.util.RunConstants.LOCATION_FASTEST_INTERVAL
import com.example.myapplication.util.RunConstants.LOCATION_INTERVAL
import com.example.myapplication.util.RunConstants.LOCATION_MAX_WAIT_TIME
import com.example.myapplication.util.RunConstants.NOTIFICATION_ID
import com.example.myapplication.util.RunConstants.formatMillisToMinutesSeconds
import com.example.myapplication.util.RunConstants.hasLocationPermissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {
    private val TAG=TrackingService::class.java.simpleName
    private var isFirstRun = true
    private var isServiceActive = false
    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient
    @Inject
    lateinit var baseNotificationBuilder:NotificationCompat.Builder
    lateinit var updateNotificationBuilder:NotificationCompat.Builder
    // used by the notification of the service
    private var timerInSeconds = MutableStateFlow("")

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

    // instead of onBind, the components bound to service use these
    companion object{
        // tells the observers whether the service is tracking the location or not
        val isTracking = MutableStateFlow(false)
        //val pathPoints = MutableLiveData<MutableList<MutableList<LatLng>>>()
        val pathPoints = MutableLiveData<Polylines>()
        // will be observed by the fragment
        val timerInMillis = MutableStateFlow(0L)
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(receiver, IntentFilter(ACTION_LOCATION_PERMISSION))
        postInitialValues()
        updateNotificationBuilder=baseNotificationBuilder

        lifecycleScope.launch(Dispatchers.IO) {
            isTracking.collect{
                requestLocationUpdates(it)
                updateNotificationActions(it)
            }
        }
        lifecycleScope.launch {
            timerInSeconds.collect{
                updateNotificationBuilder.setContentText(it)
                NotificationManagerCompat.from(this@TrackingService).notify(NOTIFICATION_ID,updateNotificationBuilder.build())
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_STOP_SERVICE-> {
                    killService()
                    Log.d(TAG, "stop service")
                }
                ACTION_START_OR_RESUME_SERVICE -> {
                    isServiceActive=true
                    if(isFirstRun) {
                        startForegroundService()
                        isFirstRun=false
                    }
                    else{
                        startTimer()
                        Log.d(TAG, "service is running already")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    isServiceActive=true
                    pauseService()
                    Log.d(TAG, "service is paused")
                }
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
        isTracking.value=true
        startTimer()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)
        startForeground(NOTIFICATION_ID,baseNotificationBuilder.build())
    }

    private fun pauseService() {
        isTracking.value=false
        isTimerEnabled=false
    }

    private fun postInitialValues(){
        pathPoints.postValue(mutableListOf())
        isTracking.value=false
        timerInMillis.value=(0L)
        timerInSeconds.value=""
        isFirstRun=true
        isServiceActive=false
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
//        if(!hasLocationPermissions()){
//            // an activity which can give us the activity context for requesting permissions
//            startActivity(Intent(this,PermissionsActivity::class.java)
//                .also { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
//        }

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

    private var isTimerEnabled = false
    private var totalRunTime = 0L
    private var lapTime = 0L // time for each polyline run
    private var timeStarted = 0L
    private var secondsPassed = 0L

    private fun startTimer(){
        // moved from startForegroundService(), because you start a timer only when its a new run or after being paused, this is when we need an empty polyline
        addEmptyPolyline()
        isTracking.value = true
        isTimerEnabled=true
        timeStarted = System.currentTimeMillis()

        lifecycleScope.launch(Dispatchers.Default) {
            while (isTracking.value){
                lapTime = System.currentTimeMillis() - timeStarted
                timerInMillis.value = (lapTime+totalRunTime)
                // updates every second
                if (secondsPassed + 1000L <= timerInMillis.value){
                    secondsPassed+=1000L
                    timerInSeconds.value=formatMillisToMinutesSeconds(timerInMillis.value)
                }
            }
            totalRunTime+=lapTime
        }
    }

    @SuppressLint("RestrictedApi")
    private fun updateNotificationActions(isTracking:Boolean){
        val actionName = if(isTracking) "Pause" else "Resume"
        val intent = Intent(this,TrackingService::class.java).also {
            it.action = if(isTracking) ACTION_PAUSE_SERVICE else ACTION_START_OR_RESUME_SERVICE
        }
        val pendingIntent = PendingIntent.getService(this, if(isTracking) 1 else 2,intent,PendingIntent.FLAG_MUTABLE)
        // better approach is to build a new one each time, Lint warning has been suppressed
        updateNotificationBuilder.mActions.clear()
        if(isServiceActive){
            updateNotificationBuilder =baseNotificationBuilder.addAction(R.drawable.ic_run, actionName, pendingIntent)
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, updateNotificationBuilder.build())
        }
    }

    private fun killService(){
        postInitialValues()
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}