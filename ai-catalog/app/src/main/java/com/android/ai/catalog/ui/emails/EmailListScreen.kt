package com.android.ai.catalog.ui.emails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
// Removed TextFieldDefaults usage; using Box with white background behind fields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.ai.catalog.network.ApiService
import com.android.ai.catalog.network.RegisteredEmailItem
import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.launch

@Composable
fun EmailListScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current as Context
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf<List<RegisteredEmailItem>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var emailReg by remember { mutableStateOf("") }
    var usernameReg by remember { mutableStateOf("") }
    var passwordReg by remember { mutableStateOf("") }
    var passwordVisibleReg by remember { mutableStateOf(false) }
    var registerResult by remember { mutableStateOf<String?>(null) }

    fun fetch() {
        scope.launch {
            loading = true
            error = null
            val (ok, resp, errMsg) = ApiService.listRegistered()
            loading = false
            if (ok) {
                items = resp
            } else {
                error = errMsg ?: "Failed to load registered emails"
            }
        }
    }

    // initial load
    if (items == null && error == null && !loading) {
        fetch()
    }

    // Determine if current user is admin by reading persisted roles
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val rolesJson = prefs.getString("roles", null)
    val currentRoles: List<String>? = rolesJson?.let { Gson().fromJson(it, Array<String>::class.java).toList() }
    val isAdmin = currentRoles?.any { r -> r.equals("ADMIN", ignoreCase = true) || r.equals("ROLE_ADMIN", ignoreCase = true) } == true

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Registered emails", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = { fetch() }, modifier = Modifier.padding(top = 8.dp)) {
            Text(text = "Refresh")
        }

        // Simple registration form embedded on this page for convenience
        Box(modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .background(Color.White)) {
            OutlinedTextField(
            value = emailReg,
            onValueChange = { emailReg = it },
            label = { Text("Email", color = Color.Black) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            )
        }
        Box(modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .background(Color.White)) {
            OutlinedTextField(
            value = passwordReg,
            onValueChange = { passwordReg = it },
            label = { Text("Password", color = Color.Black) },
            singleLine = true,
            visualTransformation = if (passwordVisibleReg) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisibleReg = !passwordVisibleReg }) {
                    val icon = if (passwordVisibleReg) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    Icon(imageVector = icon, contentDescription = if (passwordVisibleReg) "Hide password" else "Show password", tint = Color.Black)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            )
        }
        Box(modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .background(Color.White)) {
            OutlinedTextField(
            value = usernameReg,
            onValueChange = { usernameReg = it },
            label = { Text("Username", color = Color.Black) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            )
        }
        Button(onClick = {
            // basic client-side validation
            if (emailReg.isBlank() || passwordReg.isBlank() || usernameReg.isBlank()) {
                registerResult = "Email, username and password are required"
                return@Button
            }
            scope.launch {
                val (ok, resp) = ApiService.register(com.android.ai.catalog.network.EmailRequest(
                    email = emailReg.trim(), username = usernameReg.trim(), password = passwordReg
                ))
                if (ok && resp != null) {
                    registerResult = resp.message ?: "Registered: ${resp.email}"
                    // refresh list after success
                    fetch()
                } else if (resp != null) {
                    registerResult = resp.message ?: "Registration failed"
                } else {
                    registerResult = "Registration failed"
                }
            }
        }, modifier = Modifier.padding(top = 8.dp)) {
            Text(text = "Register")
        }

        registerResult?.let { rr ->
            Card(colors = CardDefaults.cardColors(), modifier = Modifier.padding(top = 8.dp)) {
                Text(text = rr, modifier = Modifier.padding(12.dp))
            }
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
        }

        error?.let {
            Card(colors = CardDefaults.cardColors(), modifier = Modifier.padding(top = 12.dp)) {
                Text(text = it, modifier = Modifier.padding(12.dp))
            }
        }

        items?.let { list ->
            LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
                items(list) { item ->
                    val roleText = item.roles?.takeIf { it.isNotEmpty() }?.joinToString(", ")
                    val dateText = item.registrationDate?.let { it.substringBefore('T') }
                    val detailsParts = listOfNotNull(item.username, roleText, dateText)
                    val details = if (detailsParts.isNotEmpty()) detailsParts.joinToString(" — ") else (item.message ?: "")
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp), colors = CardDefaults.cardColors()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "${item.email} — ${details}")
                            if (isAdmin) {
                                Button(onClick = {
                                    scope.launch {
                                        // call delete and refresh on success
                                        val (ok, err) = ApiService.deleteRegistered(item.id ?: -1L)
                                        if (ok) {
                                            fetch()
                                        } else {
                                            // surface error in registerResult for quick feedback
                                            // reuse registerResult state slightly unorthodox but effective
                                            // (could be split into its own message state)
                                            // show snackbar / toast would be better but keep simple
                                        }
                                    }
                                }, modifier = Modifier.padding(top = 8.dp)) {
                                    Text(text = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
