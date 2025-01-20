package com.example.reasy.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope

import com.example.reasy.data.entity.UserEntitiy
import com.example.reasy.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun insertUser(user: UserEntitiy): LiveData<Long> = liveData {
        emit(userRepository.insertUser(user))
    }

    fun updateUser(user: UserEntitiy) = viewModelScope.launch {
        userRepository.updateUser(user)
    }

    fun deleteUser(user: UserEntitiy) = viewModelScope.launch {
        userRepository.deleteUser(user)
    }

    fun getUserById(id: Int): LiveData<UserEntitiy?> = liveData {
        emit(userRepository.getUserById(id))
    }

    fun authenticateUser(username: String, password: String): LiveData<UserEntitiy?> = liveData {
        emit(userRepository.authenticateUser(username, password))
    }

    fun getAllUsers(): LiveData<List<UserEntitiy>> = liveData {
        emit(userRepository.getAllUsers())
    }
}
