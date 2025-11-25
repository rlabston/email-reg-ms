package com.android.ai.catalog.network

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("email") val email: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("roles") val roles: Set<String>?,
    @SerializedName("token") val token: String?,
    @SerializedName("expiresInMs") val expiresInMs: Long?
)
