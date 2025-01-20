package com.example.reasy.screens.Login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reasy.data.ReasyDatabase
import com.example.reasy.data.repository.UserRepository
import com.example.reasy.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var isClient by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordMismatchError by remember { mutableStateOf(false) }


    // Initialize ViewModel
    val context = LocalContext.current
    val database = ReasyDatabase.getDatabase(context)
    val userRepository = UserRepository(database.userDao())
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.LoginViewModelFactory(userRepository)
    )

    // Observe sign-up state
    val signUpState by loginViewModel.signUpState.collectAsState()

    // Handle sign-up state changes
    LaunchedEffect(signUpState) {
        when (signUpState) {
            is LoginViewModel.SignUpState.Success -> {
                onSignUpSuccess()
            }
            else -> {} // Handle other states if needed
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Up") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, "Username") },
                modifier = Modifier.fillMaxWidth(),
                isError = signUpState is LoginViewModel.SignUpState.Error
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = signUpState is LoginViewModel.SignUpState.Error
            )

            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, "Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = signUpState is LoginViewModel.SignUpState.Error
            )

            // Error Messages
            if (signUpState is LoginViewModel.SignUpState.Error) {
                Text(
                    text = (signUpState as LoginViewModel.SignUpState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
            if (passwordMismatchError) {
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            // Loading Indicator
            if (signUpState is LoginViewModel.SignUpState.Loading) {
                CircularProgressIndicator()
            }

            // Sign Up Button
            Button(
                onClick = {
                    if (password != confirmPassword) {
                        // Handle password mismatch
                        passwordMismatchError = true
                        return@Button
                    }
                    loginViewModel.signUp(username, password, isClient)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = username.isNotBlank() && 
                         password.isNotBlank() && 
                         confirmPassword.isNotBlank() &&
                         signUpState !is LoginViewModel.SignUpState.Loading
            ) {
                Text("Create Account")
            }
        }
    }
} 