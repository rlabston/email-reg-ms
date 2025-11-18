package com.technet7.microsvc.email.client;

import android.content.Intent;
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

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private EmailService emailService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.loginEmailInput);
        passwordInput = findViewById(R.id.loginPasswordInput);
        loginButton = findViewById(R.id.loginButton);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        emailService = retrofit.create(EmailService.class);

        loginButton.setOnClickListener(v -> doLogin());
    }

    private String safeText(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void doLogin() {
        String email = safeText(emailInput);
        String password = safeText(passwordInput);

        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            valid = false;
        }
        if (!valid) return;

        EmailLoginRequest req = new EmailLoginRequest(email, password);
        emailService.login(req).enqueue(new Callback<EmailLoginResponse>() {
            @Override
            public void onResponse(Call<EmailLoginResponse> call, Response<EmailLoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Persist session
                    getSharedPreferences("auth_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("email", response.body().getEmail())
                            .putString("username", response.body().getUsername())
                            .apply();

                    Toast.makeText(LoginActivity.this, "Welcome " + response.body().getUsername(), Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                } else if (response.code() == 401) {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<EmailLoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error: " + (t.getMessage()==null?"unknown":t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }
}
