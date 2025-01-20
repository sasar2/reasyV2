package com.example.reasy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reasy.data.entity.TimeSlotEntity
import com.example.reasy.data.repository.TimeSlotRepository
import kotlinx.coroutines.launch

class TimeSlotViewModel(private val timeSlotRepository: TimeSlotRepository) : ViewModel() {

    suspend fun isTimeSlotExists(busId: Int, date: String, startTime: String, endTime: String): Boolean  {
        return timeSlotRepository.isTimeSlotExists(busId, date, startTime, endTime) > 0
    }

    fun insertTimeSlot(timeSlot: TimeSlotEntity): LiveData<Long> = liveData {
        emit(timeSlotRepository.insertTimeSlot(timeSlot))
    }

    fun updateTimeSlot(timeSlot: TimeSlotEntity) = viewModelScope.launch {
        timeSlotRepository.updateTimeSlot(timeSlot)
    }

    fun updateSlotStatus(timeSlot: TimeSlotEntity, newStatus: String) = viewModelScope.launch {
        timeSlotRepository.updateSlotStatus(timeSlot, newStatus)
    }

    fun deleteTimeSlot(timeSlot: TimeSlotEntity) = viewModelScope.launch {
        timeSlotRepository.deleteTimeSlot(timeSlot)
    }

    fun getTimeSlotById(id: Int): LiveData<TimeSlotEntity?> = liveData {
        emit(timeSlotRepository.getTimeSlotById(id))
    }

    fun getTimeSlotsByBusinessAndDate(businessId: Int, date: String): LiveData<List<TimeSlotEntity>> = liveData {
        emit(timeSlotRepository.getTimeSlotsByBusinessAndDate(businessId, date))
    }

    fun getAllTimeSlots(): LiveData<List<TimeSlotEntity>> = liveData {
        emit(timeSlotRepository.getAllTimeSlots())
    }

    fun insertTimeSlots(timeSlots: List<TimeSlotEntity>) = viewModelScope.launch {
        timeSlots.forEach { timeSlot ->
            timeSlotRepository.insertTimeSlot(timeSlot)
        }
    }
}
