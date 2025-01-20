package com.example.reasy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.reasy.data.entity.ReservationEntity
import com.example.reasy.data.repository.ReservationRepository
import kotlinx.coroutines.launch

class ReservationViewModel(private val reservationRepository: ReservationRepository) : ViewModel() {

    fun insertReservation(reservation: ReservationEntity): LiveData<Long> = liveData {
        emit(reservationRepository.insertReservation(reservation))
    }

    fun updateReservation(reservation: ReservationEntity) = viewModelScope.launch {
        reservationRepository.updateReservation(reservation)
    }

    fun deleteReservation(reservation: ReservationEntity) = viewModelScope.launch {
        reservationRepository.deleteReservation(reservation)
    }

    fun getReservationById(id: Int): LiveData<ReservationEntity?> = liveData {
        emit(reservationRepository.getReservationById(id))
    }

    fun getReservationsByClient(clientId: Int): LiveData<List<ReservationEntity>> = liveData {
        emit(reservationRepository.getReservationsByClient(clientId))
    }

    fun getReservationsByBusiness(businessId: Int): LiveData<List<ReservationEntity>> = liveData {
        emit(reservationRepository.getReservationsByBusiness(businessId))
    }

    fun getReservationsByTimeSlot(timeSlotId: Int): LiveData<List<ReservationEntity>> = liveData {
        emit(reservationRepository.getReservationsByTimeSlot(timeSlotId))
    }

    fun getAllReservations(): LiveData<List<ReservationEntity>> = liveData {
        emit(reservationRepository.getAllReservations())
    }
}
