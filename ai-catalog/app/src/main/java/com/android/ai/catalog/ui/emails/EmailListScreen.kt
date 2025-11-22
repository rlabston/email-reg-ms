package com.android.ai.catalog.ui.emails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.TextButton
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.ai.catalog.network.ApiService
import com.android.ai.catalog.network.RegisteredEmailItem
import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.launch

@Composable
fun EmailListScreen(
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    onLogin: () -> Unit = {},
    initialMode: String? = null // "menu" | "register" | "list"
) {
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

    // Determine if current user is logged in and their role
    val isLoggedIn = com.android.ai.catalog.auth.AuthManager.isLoggedIn()
    val isAdmin = com.android.ai.catalog.auth.AuthManager.isAdmin()
    
    // Track which view to show: "menu", "register", or "list"; allow override by initialMode
    var currentView by remember { mutableStateOf(initialMode ?: if (isLoggedIn) "menu" else "register") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Header with back and login/logout buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back to Home",
                    tint = Color.White
                )
            }
            
            // Show Login or Logout based on authentication state
            if (isLoggedIn) {
                TextButton(onClick = {
                    com.android.ai.catalog.auth.AuthManager.clearAuth()
                    onLogout()
                }) {
                    Text(
                        text = "Logout",
                        color = Color.White
                    )
                }
            } else {
                TextButton(onClick = onLogin) {
                    Text(
                        text = "Login",
                        color = Color.White
                    )
                }
            }
        }
        
        // Show menu or content based on state
        when (currentView) {
            "menu" -> {
                // Menu view for logged-in users
                Text(
                    text = "Email Management",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors()
                ) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text("Home (Technet7 Services)")
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors()
                ) {
                    Button(
                        onClick = { currentView = "register" },
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text("Register New Users")
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors()
                ) {
                    Button(
                        onClick = { 
                            currentView = "list"
                            fetch()
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Text("View Registered Users")
                    }
                }
            }
            
            "register" -> {
                // Registration form view
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Register New User", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    if (isLoggedIn) {
                        TextButton(onClick = { currentView = "menu" }) {
                            Text("Back to Menu", color = Color.White)
                        }
                    }
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
            }
            
            "list" -> {
                // User list view
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Registered Users", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    if (isLoggedIn) {
                        TextButton(onClick = { currentView = "menu" }) {
                            Text("Back to Menu", color = Color.White)
                        }
                    }
                }
                
                Button(onClick = { fetch() }, modifier = Modifier.padding(top = 8.dp)) {
                    Text(text = "Refresh")
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
    }
}
