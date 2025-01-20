package com.example.reasy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reasy.data.entity.UserEntitiy
import com.example.reasy.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Initial)
    val signUpState: StateFlow<SignUpState> = _signUpState

    fun login(username: String, password: String, isClient: Boolean) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            try {
                val user = userRepository.authenticateUser(username, password)
                when {
                    user == null -> {
                        _loginState.value = LoginState.Error("Invalid username or password")
                    }
                    (isClient && user.role != "client") || (!isClient && user.role != "business") -> {
                        _loginState.value = LoginState.Error("Invalid user type for selected login mode")
                    }
                    else -> {
                        _loginState.value = LoginState.Success(user)
                    }
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Login failed: ${e.message}")
            }
        }
    }

    fun signUp(username: String, password: String, isClient: Boolean) {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            
            try {
                // Check if username already exists
                val existingUser = userRepository.authenticateUser(username, password)
                if (existingUser != null) {
                    _signUpState.value = SignUpState.Error("Username already exists")
                    return@launch
                }

                // Create new user
                val newUser = UserEntitiy(
                    username = username,
                    password = password,
                    role = if (isClient) "client" else "business"
                )
                
                val userId = userRepository.insertUser(newUser)
                if (userId > 0) {
                    _signUpState.value = SignUpState.Success(newUser)
                } else {
                    _signUpState.value = SignUpState.Error("Failed to create account")
                }
            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error("Sign up failed: ${e.message}")
            }
        }
    }

    sealed class LoginState {
        object Initial : LoginState()
        object Loading : LoginState()
        data class Success(val user: UserEntitiy) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    sealed class SignUpState {
        object Initial : SignUpState()
        object Loading : SignUpState()
        data class Success(val user: UserEntitiy) : SignUpState()
        data class Error(val message: String) : SignUpState()
    }

    class LoginViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 