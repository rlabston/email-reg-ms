package com.android.ai.catalog.network

import kotlinx.serialization.Serializable

// Mirrors the simple POJOs used by the android-client sample. These are kept minimal
// and used with Retrofit's Gson converter.
@Serializable
data class EmailRequest(
    val email: String,
    val username: String? = null,
    val password: String? = null,
)

@Serializable
data class EmailResponse(
    val email: String? = null,
    val registrationDate: String? = null,
    val message: String? = null,
)

@Serializable
data class RegisteredEmailItem(
    val id: Long? = null,
    val email: String? = null,
    val username: String? = null,
    val registrationDate: String? = null,
    val message: String? = null,
    val roles: List<String>? = null,
)

@Serializable
data class EmailLoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class EmailLoginResponse(
    val email: String? = null,
    val username: String? = null,
    val message: String? = null,
    val token: String? = null,
    val roles: List<String>? = null,
    val expiresInMs: Long? = null,
)
