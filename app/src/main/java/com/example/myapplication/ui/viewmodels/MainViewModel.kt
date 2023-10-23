package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.db.Run
import com.example.myapplication.repositories.MainRepository
import com.example.myapplication.util.RunConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository
): ViewModel() {

    private val runsByDate: LiveData<List<Run>> = mainRepository.getAllRunsSortedByDate()
    private val runsByTime: LiveData<List<Run>> = mainRepository.getAllRunsSortedByTimeInMillis()
    private val runsByDistance: LiveData<List<Run>> = mainRepository.getAllRunsSortedByDistance()
    private val runsByAvgSpeed: LiveData<List<Run>> = mainRepository.getAllRunsSortedByAvgSpeed()
    private val runsByCalories: LiveData<List<Run>> = mainRepository.getAllRunsSortedByCalories()

    var runLiveData = MediatorLiveData<List<Run>>()

    init {
        // default sort logic is by date
        runLiveData.addSource(runsByDate){runLiveData.value=it}
    }

    fun saveRunInDb(run: Run) = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.insertRun(run)
    }

    fun switchSortingStrategy(sort : RunConstants.SortingOptions){
        runLiveData.removeSource(getCurrentSource())
        val newSource = when(sort){
            RunConstants.SortingOptions.DATE -> {runsByDate}
            RunConstants.SortingOptions.TIME -> {runsByTime}
            RunConstants.SortingOptions.DISTANCE -> {runsByDistance}
            RunConstants.SortingOptions.AVG_SPEED -> {runsByAvgSpeed}
            RunConstants.SortingOptions.CALORIES -> {runsByCalories}
        }
        runLiveData.addSource(newSource){
            runLiveData.value=it
        }
    }

    private fun getCurrentSource() = when(runLiveData.value){
        runsByDate.value -> {runsByDate}
        runsByTime.value -> {runsByTime}
        runsByDistance.value -> {runsByDistance}
        runsByAvgSpeed.value -> {runsByAvgSpeed}
        runsByCalories.value -> {runsByCalories}
        else-> runsByDate
    }
}