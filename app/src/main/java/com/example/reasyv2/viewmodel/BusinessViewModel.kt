package com.example.reasy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.reasy.data.ReasyDatabase
import com.example.reasy.data.entity.BusinessEntity
import com.example.reasy.data.repository.BusinessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider

class BusinessViewModel(private val businessRepository: BusinessRepository) : ViewModel() {

    fun insertBusiness(business: BusinessEntity): LiveData<Long> = liveData {
        emit(businessRepository.insertBusiness(business))
    }

    fun updateBusiness(business: BusinessEntity) = viewModelScope.launch {
        businessRepository.updateBusiness(business)
    }

    fun deleteBusiness(business: BusinessEntity) = viewModelScope.launch {
        businessRepository.deleteBusiness(business)
    }

    fun getBusinessById(id: Long): LiveData<BusinessEntity?> = liveData {
        emit(businessRepository.getBusinessById(id))
    }

    fun getBusinessesByUserId(userId: Int): LiveData<List<BusinessEntity>> = liveData {
        emit(businessRepository.getBusinessesByUserId(userId))
    }

    fun getAllBusinesses(): LiveData<List<BusinessEntity>> = liveData {
        emit(businessRepository.getAllBusinesses())
    }

    class BusinessViewModelFactory(private val repository: BusinessRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BusinessViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return BusinessViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
