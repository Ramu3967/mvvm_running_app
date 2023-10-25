package com.example.myapplication.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentStatisticsBinding
import com.example.myapplication.ui.viewmodels.StatisticsViewModel
import com.example.myapplication.util.CustomMarkerView
import com.example.myapplication.util.RunConstants
import com.example.myapplication.util.RunConstants.formatMillisToMinutesSeconds
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class StatisticsFragment : Fragment(){
    lateinit var binding:FragmentStatisticsBinding
    private val viewModel by viewModels<StatisticsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )=FragmentStatisticsBinding.inflate(inflater,container,false).run { binding=this;binding.root }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.getSummary().collect{run->
                withContext(Dispatchers.Main){
                    binding.apply {
                        val avgSpeed = String.format("%.2f",run.avgSpeedInKmh)
                        val distanceKm = String.format("%.2f",run.distanceInMeters/1000f)
                        tvTotalTime.text=formatMillisToMinutesSeconds(run.timeInMillis)
                        tvTotalDistance.text=distanceKm
                        tvTotalCalories.text=run.caloriesBurned.toString()
                        tvAverageSpeed.text=avgSpeed
                    }
                }
            }
        }
        setupBarChart()
    }

    private fun setupBarChart(){
        binding.barChart.run {
            xAxis.apply {
                position=XAxis.XAxisPosition.BOTTOM
                setDrawLabels(false)
                axisLineColor= Color.WHITE
                textColor=Color.WHITE
                setDrawGridLines(false)
            }
            axisLeft.apply {
                axisLineColor=Color.WHITE
                textColor=Color.WHITE
                setDrawGridLines(false)
            }
            axisLeft.apply {
                axisLineColor=Color.WHITE
                textColor=Color.WHITE
                setDrawGridLines(false)
            }
            description.text="Abg Speed Over Time"
            legend.isEnabled=false

        viewModel.getRunsSortedByDate().observe(viewLifecycleOwner){
            it?.let{
                val avgSpeedBarEntries = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKmh) }
                val barDataSet = BarDataSet(avgSpeedBarEntries,"Avg Speeds Over Time ")
                val barData = BarData(barDataSet)
                data=barData
            }
            // used to create a pop-up menu and shown when clicked on the graph
            marker=CustomMarkerView(it,requireContext(),R.layout.marker_view)
            invalidate()
        }
        }
    }
}