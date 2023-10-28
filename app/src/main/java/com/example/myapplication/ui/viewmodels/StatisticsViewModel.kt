package com.example.myapplication.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.myapplication.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val mainRepository: MainRepository
): ViewModel() {
    fun getSummary()=mainRepository.getSummary()
    fun getRunsSortedByDate()=mainRepository.getAllRunsSortedByDate()
}