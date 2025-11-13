package com.technet7.microsvc.email.client;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.technet7.microsvc.email.client.EmailResponse;

public class MainActivity extends AppCompatActivity {
    private TextInputEditText emailInput;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private Button registerButton;
    private EmailService emailService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    emailInput = findViewById(R.id.emailInput);
    usernameInput = findViewById(R.id.usernameInput);
    passwordInput = findViewById(R.id.passwordInput);
    registerButton = findViewById(R.id.registerButton);

        setupRetrofit();
        setupClickListeners();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/api/") // Android emulator localhost
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        emailService = retrofit.create(EmailService.class);
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> {
            String email = safeText(emailInput);
            String username = safeText(usernameInput);
            String password = safeText(passwordInput);

            boolean valid = true;
            if (TextUtils.isEmpty(email)) {
                emailInput.setError("Email is required");
                valid = false;
            }
            if (TextUtils.isEmpty(username)) {
                usernameInput.setError("Username is required");
                valid = false;
            }
            if (TextUtils.isEmpty(password)) {
                passwordInput.setError("Password is required");
                valid = false;
            }
            if (!valid) return;

            registerEmail(email, username, password);
        });
    }

    private String safeText(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void registerEmail(String email, String username, String password) {
        EmailRequest request = new EmailRequest(email, username, password);
        emailService.registerEmail(request).enqueue(new Callback<EmailResponse>() {
            @Override
            public void onResponse(Call<EmailResponse> call, Response<EmailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EmailResponse resp = response.body();
                    Toast.makeText(MainActivity.this, resp.getMessage(), Toast.LENGTH_SHORT).show();
                    emailInput.setText("");
                    usernameInput.setText("");
                    passwordInput.setText("");
                    return;
                }

                // Enhanced error handling
                String errorMsg = "Failed to register email";
                int code = response.code();
                if (code == 400) errorMsg = "Invalid request (400)";
                else if (code == 401) errorMsg = "Unauthorized (401) - check server security";
                else if (code == 409) errorMsg = "Email already registered (409)";

                try {
                    if (response.errorBody() != null) {
                        String raw = response.errorBody().string();
                        // Try to parse server error as EmailResponse JSON {message: ...}
                        EmailResponse parsed = new com.google.gson.Gson().fromJson(raw, EmailResponse.class);
                        if (parsed != null && parsed.getMessage() != null) {
                            errorMsg = parsed.getMessage();
                        } else if (!TextUtils.isEmpty(raw)) {
                            errorMsg = raw;
                        }
                    }
                } catch (Exception ignored) {}

                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<EmailResponse> call, Throwable t) {
                String msg = t.getMessage();
                if (t instanceof java.net.UnknownHostException) {
                    msg = "Cannot reach server (Unknown host)";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    msg = "Request timed out";
                } else if (t instanceof javax.net.ssl.SSLException) {
                    msg = "SSL error: " + t.getMessage();
                }
                Toast.makeText(MainActivity.this, "Network error: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}