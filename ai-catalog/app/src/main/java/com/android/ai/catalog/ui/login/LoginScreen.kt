package com.android.ai.catalog.ui.login

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
// NOTE: Using a Box background instead of TextFieldDefaults.outlinedTextFieldColors (not available in current Compose version)
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.android.ai.catalog.R
import com.android.ai.catalog.network.ApiService
import kotlinx.coroutines.launch

const val LoginRoute = "login"

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoggedIn: () -> Unit = {}, onNavigateToRegisterPage: () -> Unit = {}) {
    Log.d("LoginScreen", "Composing LoginScreen")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRegister by remember { mutableStateOf(false) }

    androidx.compose.material3.Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
            topBar = {
                androidx.compose.material3.TopAppBar(
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    navigationIcon = { 
                        androidx.compose.foundation.layout.Row {
                            androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                            androidx.compose.material3.Icon(
                                painter = painterResource(com.android.ai.catalog.R.drawable.spark_android),
                                contentDescription = null,
                                modifier = Modifier.height(40.dp)
                                    .width(58.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                                    ).padding(10.dp),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    },
                    title = {
                        androidx.compose.foundation.layout.Row {
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = androidx.compose.ui.res.stringResource(id = com.android.ai.catalog.R.string.top_bar_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                modifier = Modifier.align(Alignment.CenterVertically),
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.bg),
                    contentDescription = "Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {

        Box(modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
            .background(Color.White)) {
            OutlinedTextField(
            value = emailOrPhone,
            onValueChange = { emailOrPhone = it },
            label = { Text("Email or Phone Number", color = Color.Black) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            placeholder = { Text("user@example.com or +1234567890", color = Color.Gray) }
            )
        }

        Box(modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .background(Color.White)) {
            OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color.Black) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(imageVector = icon, contentDescription = if (passwordVisible) "Hide password" else "Show password", tint = Color.Black)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            )
        }



        if (showRegister) {
            Box(modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .background(Color.White)) {
                OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = Color.Black) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                )
            }
        }

        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .zIndex(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Login Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
        }

        Button(
            onClick = {
                if (emailOrPhone.isBlank()) {
                    showToast(context, "Email or phone number is required")
                    return@Button
                }
                if (password.isBlank()) {
                    showToast(context, "Password is required")
                    return@Button
                }

                loading = true
                errorMessage = null
                scope.launch {
                    try {
                        // Use emailOrPhone for login - backend should handle both
                        val (ok, resp) = ApiService.login(emailOrPhone.trim(), password)
                        loading = false
                        if (ok && resp != null) {
                            com.android.ai.catalog.auth.AuthManager.saveAuth(resp.email, resp.username, resp.token, resp.roles)
                            showToast(context, "Welcome ${resp.username ?: resp.email}")
                            onLoggedIn()
                        } else {
                            errorMessage = "Invalid email/phone or password. Please try again."
                        }
                    } catch (e: Exception) {
                        loading = false
                        errorMessage = when {
                            e.message?.contains("CLEARTEXT") == true -> 
                                "Network security error. Please check your connection."
                            e.message?.contains("UnknownServiceException") == true -> 
                                "Cannot connect to server. Please check network settings."
                            e.message?.contains("ConnectException") == true -> 
                                "Cannot reach server. Please check if server is running."
                            e.message?.contains("SocketTimeoutException") == true -> 
                                "Connection timeout. Please try again."
                            else -> "Login failed: ${e.message ?: "Unknown error"}"
                        }
                        Log.e("LoginScreen", "Login error", e)
                    }
                }
            },
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Text(text = "Login")
        }

        // Inline registration toggle
        androidx.compose.material3.TextButton(
            onClick = { showRegister = !showRegister },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(
                text = if (showRegister) "Back to Login" else "Quick Register Inline",
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Full registration page navigation
        androidx.compose.material3.TextButton(
            onClick = { onNavigateToRegisterPage() },
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Text(
                text = "Go To Registration Page",
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (showRegister) {
            Button(
                onClick = {
                    if (emailOrPhone.isBlank()) {
                        showToast(context, "Email is required")
                        return@Button
                    }
                    if (username.isBlank()) {
                        showToast(context, "Username is required")
                        return@Button
                    }
                    if (password.isBlank()) {
                        showToast(context, "Password is required")
                        return@Button
                    }
                    if (!emailOrPhone.contains("@")) {
                        showToast(context, "Please use email address for registration")
                        return@Button
                    }

                    loading = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val req = com.android.ai.catalog.network.EmailRequest(
                                email = emailOrPhone.trim(),
                                username = username.trim(),
                                password = password
                            )
                            val (ok, resp) = ApiService.register(req)
                            loading = false
                            if (ok && resp != null) {
                                showToast(context, "Registered: ${resp.email}")
                                showRegister = false
                                username = ""
                            } else {
                                errorMessage = "Registration failed. Email may already be registered."
                            }
                        } catch (e: Exception) {
                            loading = false
                            errorMessage = "Registration error: ${e.message ?: "Unknown error"}"
                            Log.e("LoginScreen", "Registration error", e)
                        }
                    }
                },
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Text(text = "Submit registration")
            }
        }

                // Debug helper: allow quickly skipping to Home in debug builds
                if (com.android.ai.catalog.BuildConfig.DEBUG) {
                    Button(
                        onClick = { onLoggedIn() },
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(text = "Skip to Home (debug)")
                    }
                }
                }
            }
        }
    }



private fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}


