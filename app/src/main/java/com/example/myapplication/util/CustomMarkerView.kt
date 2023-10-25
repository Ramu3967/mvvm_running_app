package com.example.myapplication.util

import android.content.Context
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomMarkerView(private val runs:List<Run>, context: Context, layoutId: Int) : MarkerView(context,layoutId) {

    override fun getOffset(): MPPointF {
        return MPPointF(-width/2f,-height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if(e==null) return
        val currentRunId = e.x.toInt() // as x contains the positions in the barEntry list
        val run = runs[currentRunId]

        findViewById<TextView>(R.id.tv_marker_time).text=
            RunConstants.formatMillisToMinutesSeconds(run.timeInMillis)

        val avgSpeed = "${run.avgSpeedInKmh}kmph"
        findViewById<TextView>(R.id.tv_marker_speed).text=avgSpeed

        val distanceKm = "${run.distanceInMeters/1000f}KM"
        findViewById<TextView>(R.id.tv_marker_distance).text=distanceKm

        val calendar = Calendar.getInstance().also { it.timeInMillis=run.timestamp }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        findViewById<TextView>(R.id.tv_marker_date).text=dateFormat.format(calendar.time)

        val caloriesBurned = "${run.caloriesBurned}Kcal"
        findViewById<TextView>(R.id.tv_marker_calories).text = caloriesBurned
    }
}