package com.example.myapplication.repositories

import com.example.myapplication.db.Run
import com.example.myapplication.db.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDao: RunDao
) {
    suspend fun insertRun(run:Run) = runDao.insertRun(run)

    suspend fun deleteRun(run:Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByCalories() = runDao.getAllRunsSortedByCalories()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeDuration()

    fun getAllRunsSortedByAvgSpeed() = runDao.getAllRunsSortedBySpeed()

    fun getTotalAvgSpeed()=runDao.getTotalAvgSpeed()

    fun getTotalCaloriesBurned()=runDao.getTotalCaloriesBurned()

    fun getTotalDistance()=runDao.getTotalDistanceInKm()

    fun getTotalTimeInMillis()=runDao.getTotalTimeInMillis()

    fun getRunsWithId(id:Int)=runDao.getRuns(id)

    fun getSummary()=runDao.getSummary()
}