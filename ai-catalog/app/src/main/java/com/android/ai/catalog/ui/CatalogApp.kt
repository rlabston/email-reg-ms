/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.ai.catalog.ui

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.ai.catalog.R
import com.android.ai.catalog.domain.sampleCatalog
import com.android.ai.catalog.ui.login.LoginScreen
import com.android.ai.catalog.auth.AuthManager
import com.android.ai.catalog.ui.emails.EmailListScreen
import com.android.ai.catalog.ui.chatbot.ChatbotScreen
import com.android.ai.catalog.ui.home.HomeScreen
import com.google.firebase.FirebaseApp
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CatalogApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    // Home page is accessible to everyone (guests and authenticated users)
    // per .copilot-policies.md requirement for consistent behavior across platforms
    val startDestination = "home"
    
    val navController = rememberNavController()
    var isDialogOpened by remember { mutableStateOf(false) }

    // Draw cityscape background image
    Box(modifier = Modifier.fillMaxSize()) {
        // Cityscape background image
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = "App background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable("login") {
                // When login succeeds navigate to the HomeScreen destination
                LoginScreen(
                    onLoggedIn = { 
                    Log.d("CatalogApp", "Login successful, navigating to home")
                    navController.navigate("home") {
                        // Clear login screen from back stack
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                    onNavigateToRegisterPage = {
                        navController.navigate("emails?mode=register") {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable("home") {
                Log.d("CatalogApp", "HomeScreen composing - drawable ids: bg=${R.drawable.bg}")
                HomeScreen(
                    onLogout = {
                        Log.d("CatalogApp", "User logged out, navigating to login")
                        navController.navigate("login") {
                            // Clear home screen from back stack
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToEmails = { mode ->
                        Log.d("CatalogApp", "Navigating to email management with mode=$mode")
                        navController.navigate("emails?mode=$mode") {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToChatbot = {
                        navController.navigate("chatbot") {
                            launchSingleTop = true
                        }
                    },
                    onLogin = {
                        Log.d("CatalogApp", "Guest navigating to login from home")
                        navController.navigate("login") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            // Email management screen with optional mode parameter
            composable(
                route = "emails?mode={mode}",
                arguments = listOf(
                    navArgument("mode") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val mode = backStackEntry.arguments?.getString("mode") ?: "menu"
                val topAppBarState = rememberTopAppBarState()
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    containerColor = Color.Transparent,
                    topBar = {
                        TwoRowsTopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            navigationIcon = { AppBarPill() },
                            title = { expanded ->
                                if (expanded) {
                                    Text(
                                        text = "Email Management",
                                        style = MaterialTheme.typography.displaySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        modifier = Modifier.padding(bottom = 12.dp),
                                    )
                                } else {
                                    Row {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Emails",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            modifier = Modifier.align(Alignment.CenterVertically),
                                        )
                                    }
                                }
                            },
                            scrollBehavior = scrollBehavior,
                        )
                    },
                ) { innerPadding ->
                    LazyColumn(
                        contentPadding = innerPadding,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        item {
                            EmailListScreen(
                                onNavigateBack = {
                                    navController.navigate("home") {
                                        launchSingleTop = true
                                    }
                                },
                                onLogout = {
                                    Log.d("CatalogApp", "User logged out from emails, navigating to login")
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onLogin = {
                                    Log.d("CatalogApp", "Guest navigating to login")
                                    navController.navigate("login") {
                                        launchSingleTop = true
                                    }
                                },
                                initialMode = mode
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        items(sampleCatalog) {
                            val onClick = {
                                if (it.needsFirebase && !isFirebaseInitialized()) {
                                    isDialogOpened = true
                                } else {
                                    navController.navigate(it.route)
                                }
                            }
                            if (it.isFeatured) {
                                CatalogWideCard(catalogItem = it, onClick = onClick)
                            } else {
                                CatalogRowCard(catalogItem = it, onClick = onClick)
                            }
                        }
                    }
                }
            }
            sampleCatalog.forEach {

            composable("chatbot") {
                ChatbotScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
                val catalogItem = it
                composable(catalogItem.route) {
                    catalogItem.sampleEntryScreen()
                }
            }
        }
    }

    if (isDialogOpened) {
        FirebaseRequiredAlert(
            onDismiss = { isDialogOpened = false },
            onOpenFirebaseDocClick = {
                isDialogOpened = false
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://firebase.google.com/docs/vertex-ai/get-started#no-existing-firebase".toUri(),
                )
                context.startActivity(intent)
            },
        )
    }
}


@Composable
fun AppBarPill() {
    Row {
        Spacer(Modifier.width(12.dp))
        Icon(
            painter = painterResource(R.drawable.spark_android),
            contentDescription = null,
            modifier = Modifier.height(40.dp)
                .width(58.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(24.dp),
                ).padding(10.dp),
            tint = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
fun FirebaseRequiredAlert(onDismiss: () -> Unit = {}, onOpenFirebaseDocClick: () -> Unit = {}) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = stringResource(R.string.firebase_required))
        },
        text = {
            Text(stringResource(R.string.firebase_required_description))
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.close))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onOpenFirebaseDocClick()
                },
            ) {
                Text(stringResource(R.string.firebase_doc_button))
            }
        },
    )
}

fun isFirebaseInitialized(): Boolean {
    return try {
        val firebaseApp = FirebaseApp.getInstance()
        return firebaseApp.options.projectId != "mock_project"
    } catch (e: IllegalStateException) {
        Log.e("CatalogScreen", "Firebase is not initialized")
        return false
    }
}
