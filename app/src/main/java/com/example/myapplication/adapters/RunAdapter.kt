package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.myapplication.R
import com.example.myapplication.db.Run
import com.example.myapplication.util.RunConstants.formatMillisToMinutesSeconds
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RunAdapter(private var runs:List<Run>) : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {
    inner class RunViewHolder(itemView: View) : ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_run,parent,false)
        return RunViewHolder(view)
    }
    override fun getItemCount(): Int {
        return runs.size
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        holder.itemView.apply {
            val run = runs[position]
            findViewById<ImageView>(R.id.iv_run_image).setImageBitmap(run.img)
            findViewById<MaterialTextView>(R.id.tv_time).text=formatMillisToMinutesSeconds(run.timeInMillis)

            val avgSpeed = "${run.avgSpeedInKmh}kmph"
            findViewById<MaterialTextView>(R.id.tv_avg_speed).text=avgSpeed

            val distanceKm = "${run.distanceInMeters/1000f}KM"
            findViewById<MaterialTextView>(R.id.tv_distance).text=distanceKm

            val calendar = Calendar.getInstance().also { it.timeInMillis=run.timestamp }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            findViewById<MaterialTextView>(R.id.tv_date).text=dateFormat.format(calendar.time)

            val caloriesBurned = "${run.caloriesBurned}Kcal"
            findViewById<MaterialTextView>(R.id.tv_calories).text = caloriesBurned
        }
    }

    fun submitList(newRuns:List<Run>){
        runs=newRuns
        notifyDataSetChanged()
    }
}