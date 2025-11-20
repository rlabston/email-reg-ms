package com.android.ai.catalog.debug

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import android.util.Log
import com.android.ai.catalog.network.ApiService
import com.android.ai.catalog.network.EmailRequest
import kotlinx.coroutines.launch

/**
 * Debug-only activity that makes a single registration request and logs the result.
 * Launched via adb to exercise the backend without needing fragile UI automation.
 */
class TestRegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            Log.d("TestRegistration", "Starting test registration")
            try {
                val req = EmailRequest(email = "test@example.com", username = "tester", password = "Password123")
                val (ok, resp) = ApiService.register(req)
                Log.d("TestRegistration", "register ok=$ok resp=$resp")
                Toast.makeText(this@TestRegistrationActivity, "Registration ${if (ok) "succeeded" else "failed"}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("TestRegistration", "error during registration", e)
                Toast.makeText(this@TestRegistrationActivity, "Registration error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                // Close the activity after a short delay so the toast is visible
                finish()
            }
        }
    }
}
