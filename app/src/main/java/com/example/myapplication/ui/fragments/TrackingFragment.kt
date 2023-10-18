package com.example.myapplication.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.myapplication.databinding.FragmentTrackingBinding
import com.example.myapplication.services.TrackingService
import com.example.myapplication.ui.viewmodels.MainViewModel
import com.example.myapplication.util.RunConstants.ACTION_START_OR_RESUME_SERVICE
import com.google.android.gms.maps.GoogleMap
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment : Fragment(){
    private lateinit var binding:FragmentTrackingBinding
    private val viewModel by viewModels<MainViewModel>()
    // this is the actual map and mapView represents it.
    private var map:GoogleMap? = null

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
            mapView.getMapAsync { map=it }
            mapView.onCreate(savedInstanceState)
            btnToggleRun.setOnClickListener{
                sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
            }
        }
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

    // it doesn't really start a new service every time we call this function, but it just sends intents to it.
    private fun sendCommandToService(action:String)= Intent(requireContext(),TrackingService::class.java).
    also {
        it.action=action
        requireContext().startService(it)
    }

}