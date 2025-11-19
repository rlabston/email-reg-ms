package com.android.ai.catalog.ui.login

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.android.ai.catalog.network.ApiService
import kotlinx.coroutines.launch

const val LoginRoute = "login"

@Composable
fun LoginScreen(onLoggedIn: () -> Unit = {}) {
    Log.d("LoginScreen", "Composing LoginScreen")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var result by remember { mutableStateOf(if (com.android.ai.catalog.BuildConfig.DEBUG) "Sample server response: 200 OK" else null) }
    var showRegister by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "AI Catalog — Debug Login", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.Black) },
            singleLine = true,
            modifier = Modifier.padding(top = 12.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
        )

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
            modifier = Modifier.padding(top = 8.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
        )

        if (showRegister) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = Color.Black) },
                singleLine = true,
                modifier = Modifier.padding(top = 8.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            )
        }

        result?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .zIndex(1f),
                // Make the result card transparent so the background image shows through
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Text(text = it, color = Color.Black)
                }
            }
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
        }

        Button(
            onClick = {
                if (email.isBlank()) {
                    showToast(context, "Email is required")
                    return@Button
                }
                if (password.isBlank()) {
                    showToast(context, "Password is required")
                    return@Button
                }

                loading = true
                result = null
                scope.launch {
                    val (ok, resp) = ApiService.login(email.trim(), password)
                    loading = false
                    if (ok && resp != null) {
                        persistAuth(context, resp.email, resp.username, resp.token, resp.roles)
                        showToast(context, "Welcome ${resp.username ?: resp.email}")
                        onLoggedIn()
                    } else {
                        result = "Login failed"
                    }
                }
            },
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Text(text = "Login")
        }

        Button(
            onClick = { showRegister = !showRegister },
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(text = if (showRegister) "Cancel" else "Register")
        }

        if (showRegister) {
            Button(
                onClick = {
                    if (email.isBlank()) {
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

                    loading = true
                    result = null
                    scope.launch {
                        val req = com.android.ai.catalog.network.EmailRequest(
                            email = email.trim(),
                            username = username.trim(),
                            password = password
                        )
                        val (ok, resp) = ApiService.register(req)
                        loading = false
                        if (ok && resp != null) {
                            showToast(context, "Registered: ${resp.email}")
                            showRegister = false
                        } else {
                            result = "Registration failed"
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

private fun persistAuth(context: Context, email: String?, username: String?, token: String?, roles: List<String>?) {
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    editor.putString("email", email)
    editor.putString("username", username)
    editor.putString("token", token)
    if (roles != null) {
        val json = com.google.gson.Gson().toJson(roles)
        editor.putString("roles", json)
    } else {
        editor.remove("roles")
    }
    editor.apply()
}

private fun showToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}


