package com.android.ai.catalog.network

import android.os.Build
import com.android.ai.catalog.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {
    @GET("/")
    suspend fun root(): Response<String>

    @POST("api/emails/register")
    suspend fun registerEmail(@Body req: EmailRequest): Response<EmailResponse>

    // Backend exposes a light-weight list at /api/emails and a joined-roles
    // variant at /api/emails/with-roles. Use the with-roles endpoint so the
    // client receives per-user role lists (joined via the user_role_link table).
    @GET("api/emails/with-roles")
    suspend fun getRegisteredEmailsWithRoles(): Response<List<RegisteredEmailItem>>

    @POST("api/emails/login")
    suspend fun login(@Body req: EmailLoginRequest): Response<EmailLoginResponse>
    
    @kotlin.jvm.Throws(Exception::class)
    @retrofit2.http.DELETE("api/emails/id/{id}")
    suspend fun deleteRegisteredById(@retrofit2.http.Path("id") id: Long): Response<Void>

    // Home screen endpoints (served at /home/* via gateway => /api/home/* here)
    @GET("api/home/data")
    suspend fun getHomeData(): Response<HomeScreenData>
    @GET("api/home/featured")
    suspend fun getFeatured(): Response<List<ServiceItem>>
    
    // Chatbot endpoints
    @POST("api/chat/message")
    suspend fun sendChatMessage(@Body req: ChatRequest): Response<ChatResponse>
    
    @GET("api/chat/history/{conversationId}")
    suspend fun getChatHistory(@retrofit2.http.Path("conversationId") conversationId: String): Response<ConversationHistoryResponse>
}

object ApiService {
    /**
     * Detect if running on emulator by checking Build properties.
     * Common emulator indicators: FINGERPRINT contains "generic", BRAND is "generic",
     * MODEL contains "sdk" or "Emulator", or MANUFACTURER is "Google" with MODEL containing "sdk"
     */
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    /**
     * Get the appropriate base URL based on whether we're running on emulator or device
     */
    private fun getBaseUrl(): String {
        return if (isEmulator()) {
            BuildConfig.EMULATOR_SERVER_URL
        } else {
            BuildConfig.DEVICE_SERVER_URL
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val reqBuilder = chain.request().newBuilder()
        com.android.ai.catalog.auth.AuthManager.getToken()?.let { token ->
            reqBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(reqBuilder.build())
    }
    private val logging = HttpLoggingInterceptor().apply {
        // Log request/response bodies in debug builds to aid diagnosis; keep BASIC in release.
        level = if (com.android.ai.catalog.BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.BASIC
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(getBaseUrl())
        .client(client)
        // Scalars first (for simple health checks) then Gson for JSON bodies
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: Api = retrofit.create(Api::class.java)

    suspend fun ping(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val resp = api.root()
            val body = resp.body() ?: "(no body)"
            val msg = "HTTP ${resp.code()}: ${body.take(200)}"
            Pair(resp.isSuccessful, msg)
        } catch (e: Exception) {
            Pair(false, "${e::class.simpleName}: ${e.message}")
        }
    }

    suspend fun login(email: String, password: String): Pair<Boolean, EmailLoginResponse?> = withContext(Dispatchers.IO) {
        try {
            val resp = api.login(EmailLoginRequest(email = email, password = password))
            val body = resp.body()
            if (resp.isSuccessful) {
                // Token is now stored in AuthManager by LoginScreen
                Pair(true, body)
            } else {
                Pair(false, body)
            }
        } catch (e: Exception) {
            Pair(false, null)
        }
    }

    suspend fun register(request: EmailRequest): Pair<Boolean, EmailResponse?> = withContext(Dispatchers.IO) {
        try {
            val resp = api.registerEmail(request)
            Pair(resp.isSuccessful, resp.body())
        } catch (e: Exception) {
            Pair(false, null)
        }
    }

    suspend fun deleteRegistered(id: Long): Pair<Boolean, String?> = withContext(Dispatchers.IO) {
        try {
            val resp = api.deleteRegisteredById(id)
            if (resp.isSuccessful) {
                Triple(true, null, null) // not used but keep shape
                Pair(true, null)
            } else {
                val err = try { resp.errorBody()?.string() } catch (e: Exception) { null }
                Pair(false, err ?: "HTTP ${resp.code()}")
            }
        } catch (e: Exception) {
            Pair(false, "${e::class.simpleName}: ${e.message}")
        }
    }

    suspend fun listRegistered(): Triple<Boolean, List<RegisteredEmailItem>?, String?> = withContext(Dispatchers.IO) {
        try {
            // Call the server endpoint that returns roles via the join table.
            val resp = api.getRegisteredEmailsWithRoles()
            if (resp.isSuccessful) {
                Triple(true, resp.body(), null)
            } else {
                // Try to surface an error body if present to the UI for debugging
                val err = try { resp.errorBody()?.string() } catch (e: Exception) { null }
                Triple(false, resp.body(), err ?: "HTTP ${resp.code()}")
            }
        } catch (e: Exception) {
            Triple(false, null, "${e::class.simpleName}: ${e.message}")
        }
    }

    suspend fun homeData(): Triple<Boolean, HomeScreenData?, String?> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getHomeData()
            if (resp.isSuccessful) {
                Triple(true, resp.body(), null)
            } else {
                val err = try { resp.errorBody()?.string() } catch (e: Exception) { null }
                Triple(false, resp.body(), err ?: "HTTP ${resp.code()}")
            }
        } catch (e: Exception) {
            Triple(false, null, "${e::class.simpleName}: ${e.message}")
        }
    }

    suspend fun homeFeatured(): Triple<Boolean, List<ServiceItem>?, String?> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getFeatured()
            if (resp.isSuccessful) {
                Triple(true, resp.body(), null)
            } else {
                val err = try { resp.errorBody()?.string() } catch (e: Exception) { null }
                Triple(false, resp.body(), err ?: "HTTP ${resp.code()}")
            }
        } catch (e: Exception) {
            Triple(false, null, "${e::class.simpleName}: ${e.message}")
        }
    }

    suspend fun sendChatMessage(conversationId: String, message: String): Triple<Boolean, ChatResponse?, String?> = withContext(Dispatchers.IO) {
        try {
            val req = ChatRequest(conversationId = conversationId, message = message)
            val resp = api.sendChatMessage(req)
            if (resp.isSuccessful) {
                Triple(true, resp.body(), null)
            } else {
                val err = try { resp.errorBody()?.string() } catch (e: Exception) { null }
                Triple(false, resp.body(), err ?: "HTTP ${resp.code()}")
            }
        } catch (e: Exception) {
            Triple(false, null, "${e::class.simpleName}: ${e.message}")
        }
    }

    suspend fun getChatHistory(conversationId: String): Triple<Boolean, ConversationHistoryResponse?, String?> = withContext(Dispatchers.IO) {
        try {
            val resp = api.getChatHistory(conversationId)
            if (resp.isSuccessful) {
                Triple(true, resp.body(), null)
            } else {
                val err = try { resp.errorBody()?.string() } catch (e: Exception) { null }
                Triple(false, resp.body(), err ?: "HTTP ${resp.code()}")
            }
        } catch (e: Exception) {
            Triple(false, null, "${e::class.simpleName}: ${e.message}")
        }
    }

}
