package com.example.reasy.screens.Login

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.reasy.data.ReasyDatabase
import com.example.reasy.data.repository.UserRepository
import com.example.reasy.viewmodel.LoginViewModel
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.CircularProgressIndicator
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onClientLoginSuccess: () -> Unit,
    onBusinessLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    var isClient by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Initialize database, repository and viewModel
    val context = LocalContext.current
    val database = ReasyDatabase.getDatabase(context)
    val userRepository = UserRepository(database.userDao())
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.LoginViewModelFactory(userRepository)
    )
    
    // Observe login state
    val loginState by loginViewModel.loginState.collectAsState()

    // Add sign-up state observation
    val signUpState by loginViewModel.signUpState.collectAsState()

    // Handle login state changes
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginViewModel.LoginState.Success -> {
                val user = (loginState as LoginViewModel.LoginState.Success).user
                if (user.role == "client") {
                    onClientLoginSuccess()
                } else {
                    onBusinessLoginSuccess()
                }
                context.getSharedPreferences("reasy_preferences", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("user_id", user.usrId)
                    .apply()
            }
            else -> {} // Handle other states if needed
        }
    }

    // Handle sign-up state changes
    LaunchedEffect(signUpState) {
        when (signUpState) {
            is LoginViewModel.SignUpState.Success -> {
                val user = (signUpState as LoginViewModel.SignUpState.Success).user
                if (user.role == "client") {
                    onClientLoginSuccess()
                } else {
                    onBusinessLoginSuccess()
                }
            }
            else -> {} // Handle other states if needed
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isClient) "Client Login" else "Business Login") }
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
            // Toggle Button
            LoginToggleButton(
                isClient = isClient,
                onToggleChange = { isClient = it }
            )

            // Login Fields
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, "Username") },
                modifier = Modifier.fillMaxWidth(),
                isError = loginState is LoginViewModel.LoginState.Error
            )
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = loginState is LoginViewModel.LoginState.Error
            )

            // Show error messages
            if (loginState is LoginViewModel.LoginState.Error) {
                Text(
                    text = (loginState as LoginViewModel.LoginState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            if (signUpState is LoginViewModel.SignUpState.Error) {
                Text(
                    text = (signUpState as LoginViewModel.SignUpState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            // Show loading indicator
            if (loginState is LoginViewModel.LoginState.Loading || 
                signUpState is LoginViewModel.SignUpState.Loading) {
                CircularProgressIndicator()
            }

            // Login Button
            Button(
                onClick = { loginViewModel.login(username, password, isClient) },
                modifier = Modifier.fillMaxWidth(),
                enabled = loginState !is LoginViewModel.LoginState.Loading
            ) {
                Text("Login")
            }

            if (isClient) {
                // Sign Up Navigation Button
                OutlinedButton(
                    onClick = { onSignUpClick() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create New Account")
                }
            }

            // Additional Options
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Forgot Password?",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable { onForgotPasswordClick() }
                )


            }
        }
    }
}

@Composable
fun LoginToggleButton(
    isClient: Boolean,
    onToggleChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Client Toggle
            val clientColor by animateColorAsState(
                targetValue = if (isClient) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                label = "clientColor"
            )
            
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .padding(4.dp),
                color = clientColor,
                onClick = { onToggleChange(true) }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Client",
                        tint = if (isClient) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Client",
                        color = if (isClient) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Business Toggle
            val businessColor by animateColorAsState(
                targetValue = if (!isClient) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                label = "businessColor"
            )
            
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .padding(4.dp),
                color = businessColor,
                onClick = { onToggleChange(false) }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Business",
                        tint = if (!isClient) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Business",
                        color = if (!isClient) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
} 