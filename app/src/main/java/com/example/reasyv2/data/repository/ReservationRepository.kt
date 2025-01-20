package com.example.reasy.data.repository

import com.example.reasy.data.dao.ReservationDao
import com.example.reasy.data.entity.ReservationEntity

class ReservationRepository(private val reservationDao: ReservationDao) {

    suspend fun insertReservation(reservation: ReservationEntity): Long {
        return reservationDao.insertReservation(reservation)
    }

    suspend fun updateReservation(reservation: ReservationEntity) {
        reservationDao.updateReservation(reservation)
    }

    suspend fun deleteReservation(reservation: ReservationEntity) {
        reservationDao.deleteReservation(reservation)
    }

    suspend fun getReservationById(id: Int): ReservationEntity? {
        return reservationDao.getReservationById(id)
    }

    suspend fun getReservationsByClient(clientId: Int): List<ReservationEntity> {
        return reservationDao.getReservationsByClient(clientId)
    }

    suspend fun getReservationsByBusiness(businessId: Int): List<ReservationEntity> {
        return reservationDao.getReservationsByBusiness(businessId)
    }

    suspend fun getReservationsByTimeSlot(timeSlotId: Int): List<ReservationEntity> {
        return reservationDao.getReservationsByTimeSlot(timeSlotId)
    }

    suspend fun getAllReservations(): List<ReservationEntity> {
        return reservationDao.getAllReservations()
    }
}
