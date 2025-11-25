package com.android.ai.catalog.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.android.ai.catalog.auth.AuthManager
import com.android.ai.catalog.network.ApiService
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNavigateToRegisterPage: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        if (error != null) {
            Text(error!!, color = Color.Red)
        }
        Button(
            onClick = {
                scope.launch {
                    loading = true
                    error = null
                    try {
                        val (success, resp) = ApiService.login(email.trim(), password)
                        if (success && resp != null) {
                            // Save auth info to AuthManager
                            AuthManager.saveAuth(
                                email = resp.email ?: email.trim(),
                                username = resp.username,
                                token = resp.token,
                                roles = resp.roles,
                                expiresInMs = resp.expiresInMs ?: 0
                            )
                            onLoggedIn()
                        } else {
                            error = resp?.message ?: "Login failed"
                        }
                    } catch (e: Exception) {
                        error = e.message ?: "Network error"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (loading) "Logging in..." else "Login") }
        Button(
            onClick = onNavigateToRegisterPage,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Register") }
    }
}
