package com.example.reasy.data.repository

import com.example.reasy.data.dao.TimeSlotDao
import com.example.reasy.data.entity.TimeSlotEntity

class TimeSlotRepository(private val timeSlotDao: TimeSlotDao) {

    suspend fun isTimeSlotExists(busId: Int, date: String, startTime: String, endTime: String): Int {
        return timeSlotDao.isTimeSlotExists(busId, date, startTime, endTime)
    }

    suspend fun insertTimeSlot(timeSlot: TimeSlotEntity): Long {
        return timeSlotDao.insertTimeSlot(timeSlot)
    }

    suspend fun updateTimeSlot(timeSlot: TimeSlotEntity) {
        timeSlotDao.updateTimeSlot(timeSlot)
    }

    suspend fun updateSlotStatus(timeSlot: TimeSlotEntity, newStatus: String) {
        timeSlotDao.updateStatus(timeSlot.busId, timeSlot.date, timeSlot.startTime, timeSlot.endTime, newStatus)
    }


    suspend fun deleteTimeSlot(timeSlot: TimeSlotEntity) {
        timeSlotDao.deleteTimeSlot(timeSlot)
    }

    suspend fun getTimeSlotById(id: Int): TimeSlotEntity? {
        return timeSlotDao.getTimeSlotById(id)
    }

    suspend fun getTimeSlotsByBusinessAndDate(businessId: Int, date: String): List<TimeSlotEntity> {
        return timeSlotDao.getTimeSlotsByBusinessAndDate(businessId, date)
    }

    suspend fun getAllTimeSlots(): List<TimeSlotEntity> {
        return timeSlotDao.getAllTimeSlots()
    }
}
