package com.example.myapplication.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_run")
data class Run(
    var img: Bitmap?=null,
    var timestamp: Long = 0L,
    var avgSpeedInKmh: Float=0f,
    var distanceInMeters: Int=0,
    var timeInMillis:Long=0L,
    var caloriesBurned : Int = 0
){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
