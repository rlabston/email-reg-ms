package com.android.ai.catalog.network

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
}

object ApiService {
    // Mutable auth token used by the auth interceptor. Set after successful login.
    @Volatile
    var authToken: String? = null

    private val authInterceptor = Interceptor { chain ->
        val reqBuilder = chain.request().newBuilder()
        authToken?.let { token ->
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
        .baseUrl(BuildConfig.BASE_URL)
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
                // store token for subsequent requests
                authToken = body?.token
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
}
