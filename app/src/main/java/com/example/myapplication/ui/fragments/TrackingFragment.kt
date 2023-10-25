package com.example.myapplication.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTrackingBinding
import com.example.myapplication.db.Run
import com.example.myapplication.services.TrackingService
import com.example.myapplication.ui.viewmodels.MainViewModel
import com.example.myapplication.util.RunConstants
import com.example.myapplication.util.RunConstants.ACTION_PAUSE_SERVICE
import com.example.myapplication.util.RunConstants.ACTION_START_OR_RESUME_SERVICE
import com.example.myapplication.util.RunConstants.ACTION_STOP_SERVICE
import com.example.myapplication.util.RunConstants.POLYLINE_CAMERA_ZOOM
import com.example.myapplication.util.RunConstants.POLYLINE_COLOR
import com.example.myapplication.util.RunConstants.POLYLINE_WIDTH
import com.example.myapplication.util.RunConstants.PREF_WEIGHT
import com.example.myapplication.util.RunConstants.getPolylineLength
import com.example.myapplication.util.RunConstants.remove
import com.example.myapplication.util.RunConstants.show
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.lang.Math.round
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class TrackingFragment : Fragment(){
    private lateinit var binding:FragmentTrackingBinding
    private val viewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var appPreferences: SharedPreferences

    private var pathPoints = mutableListOf(mutableListOf<LatLng>())
    private var isTracking=false
    // this is the actual map and mapView represents it.
    private var map:GoogleMap? = null
    private var currentTimeInMillis = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentTrackingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            mapView.getMapAsync {
                map=it
                drawAllPolylines()
            }
            mapView.onCreate(savedInstanceState)
            btnToggleRun.setOnClickListener{
                if(!isTracking){
                    sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
                    btnToggleRun.text = "Pause"
                    btnFinishRun.remove()
                }else{
                    sendCommandToService(ACTION_PAUSE_SERVICE)
                    btnToggleRun.text="Start"
                    btnFinishRun.show()
                }
            }
            btnFinishRun.setOnClickListener {
                endRunAndSaveDb()
                btnFinishRun.remove()
//                tvTimer.text="00:00:00:00"
            }
            btnCancel.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Are you sure?")
                    .setCancelable(false)
                    .setPositiveButton(R.string.alert_positive){_,_->
                        stopRun()
                    }
                    .setNegativeButton(R.string.alert_negative){dialogInterface,_->
                        dialogInterface.dismiss()
                    }.show()
            }
        }
        observeData()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    // this method hashes our map so that it can be re-used instead of creating one everytime.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    private fun observeData() {
        lifecycleScope.launch {
            TrackingService.isTracking.collect { isTracking = it }
        }
        lifecycleScope.launch {
            TrackingService.timerInMillis.collect{
                binding.tvTimer.text = RunConstants.formatMillisToHoursMinutesSecondsMilliseconds(it)
                currentTimeInMillis=it
            }
        }
        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            drawLatestPolylines()
            updateCameraPosition()
        }
    }

    // it doesn't really start a new service every time we call this function, but it just sends intents to it.
    private fun sendCommandToService(action:String)= Intent(requireContext(),TrackingService::class.java).
    also {
        it.action=action
        requireContext().startService(it)
    }

    private fun drawLatestPolylines(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size>1){
            val latestLine = pathPoints.last()
            val secondLastPoint = latestLine[latestLine.size-2]
            val lastPoint = latestLine.last()
            // polylineOptions describes how the line must look like
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(secondLastPoint)
                .add(lastPoint)
            map?.addPolyline(polylineOptions)
        }
    }

    // drawing all polylines when the config changes
    private fun drawAllPolylines(){
        for(polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(Color.BLUE)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun updateCameraPosition(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty())
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(pathPoints.last().last(),POLYLINE_CAMERA_ZOOM))
    }

    private fun stopRun(){
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().run {
            navigate(TrackingFragmentDirections.actionTrackingFragmentToRunFragment())
            popBackStack(R.id.trackingFragment,true)
        }
    }

    private fun zoomOutForEntirePath(){
        val bounds = LatLngBounds.Builder()
        for(polyline in pathPoints)
            for(pos in polyline)
                bounds.include(pos)

        var distanceM = 0
        for(polyline in pathPoints) distanceM+= getPolylineLength(polyline).toInt()
        val distanceKM = (distanceM/1000f).toInt()
        if(distanceKM>=1)
            try {
                map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),binding.mapView.width,binding.mapView.height,(binding.mapView.height*0.5f).toInt() ))
            }catch (e:Exception){
                map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),100 ))
            }
    }

    private fun endRunAndSaveDb(){
        zoomOutForEntirePath()
        val weight : Double=  appPreferences.getString(PREF_WEIGHT,"")?.toDoubleOrNull() ?: 65.0
        map?.snapshot {
            var distanceInMeters = 0
            for(polyline in pathPoints) distanceInMeters+= getPolylineLength(polyline).toInt()
            val timestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters/1000f)*weight).toInt()
            val avgSpeedInKmph = round((distanceInMeters/1000f)/(currentTimeInMillis/1000f/60/60)*10)/10f
            val run = Run(it,timestamp,avgSpeedInKmph,distanceInMeters,currentTimeInMillis,caloriesBurned)
            viewModel.saveRunInDb(run)
            Snackbar.make(requireActivity().findViewById(R.id.rootView),"saved the run successfully",Snackbar.LENGTH_LONG).show()
            stopRun()
            }
    }
}