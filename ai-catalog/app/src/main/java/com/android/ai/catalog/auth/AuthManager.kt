package com.android.ai.catalog.auth

/**
 * Simple in-memory authentication manager
 * Stores JWT token and login state
 */
object AuthManager {
    private var jwtToken: String? = null
    private var loggedIn: Boolean = false
    private var userEmail: String? = null
    private var username: String? = null
    private var userRoles: List<String>? = null
    private var expiresAtMs: Long? = null
    
    fun saveToken(token: String) {
        jwtToken = token
        loggedIn = true
    }
    
    fun saveAuth(email: String?, username: String?, token: String?, roles: List<String>?, expiresInMs: Long = 0) {
        this.userEmail = email
        this.username = username
        this.jwtToken = token
        this.userRoles = roles
        this.loggedIn = true
        if (expiresInMs > 0) {
            this.expiresAtMs = System.currentTimeMillis() + expiresInMs
        }
    }
    
    fun getToken(): String? = jwtToken
    
    fun getEmail(): String? = userEmail
    
    fun getUsername(): String? = username
    
    fun getRoles(): List<String> = userRoles ?: emptyList()
    
    fun isLoggedIn(): Boolean {
        val tokenValid = loggedIn && jwtToken != null
        val notExpired = expiresAtMs?.let { it > System.currentTimeMillis() } ?: true
        return tokenValid && notExpired
    }
    
    fun isAdmin(): Boolean = userRoles?.any { it.equals("ADMIN", ignoreCase = true) || it.equals("ROLE_ADMIN", ignoreCase = true) } ?: false
    
    fun logout() {
        jwtToken = null
        loggedIn = false
        userEmail = null
        username = null
        userRoles = null
        expiresAtMs = null
    }
    
    fun clearAuth() {
        logout()
    }
}
