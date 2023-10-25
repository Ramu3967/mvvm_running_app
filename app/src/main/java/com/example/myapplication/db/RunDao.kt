package com.example.myapplication.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run) : Int

    @Query("select * from table_run order by timestamp desc") // most recent run on top
    fun getAllRunsSortedByDate(): LiveData<List<Run>>
    @Query("select * from table_run order by avgSpeedInKmh desc")
    fun getAllRunsSortedBySpeed(): LiveData<List<Run>>
    @Query("select * from table_run order by caloriesBurned desc")
    fun getAllRunsSortedByCalories(): LiveData<List<Run>>
    @Query("select * from table_run order by distanceInMeters desc")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>
    @Query("select * from table_run order by timeInMillis desc")
    fun getAllRunsSortedByTimeDuration(): LiveData<List<Run>>

    @Query("select sum(timeInMillis) from table_run")
    fun getTotalTimeInMillis():Flow<Long>
    @Query("select sum(caloriesBurned) from table_run")
    fun getTotalCaloriesBurned():LiveData<Int>
    @Query("select sum(distanceInMeters) from table_run")
    fun getTotalDistanceInKm():Flow<Int>
    @Query("select avg(avgSpeedInKmh) from table_run")
    fun getTotalAvgSpeed():Flow<Float>

    // get the raw data as they are added in the db
    @Query("select * from table_run where id = :id")
    fun getRuns(id:Int): List<Run>

    @Query("select avg(avgSpeedInKmh) as avgSpeedInKmh,sum(distanceInMeters) as distanceInMeters,sum(timeInMillis) as timeInMillis,sum(caloriesBurned) as caloriesBurned, 0 as timestamp from table_run")
    fun getSummary():Flow<Run>
}
