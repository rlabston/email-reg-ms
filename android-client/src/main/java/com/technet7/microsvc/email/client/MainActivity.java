package com.technet7.microsvc.email.client;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
    private Button loadButton;
    private Button logoutButton;
    private RecyclerView emailsRecyclerView;
    private RegisteredEmailAdapter emailAdapter;
    private EmailService emailService;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Require login
        SharedPreferences prefsPre = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        if (prefsPre.getString("email", null) == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    emailInput = findViewById(R.id.emailInput);
    usernameInput = findViewById(R.id.usernameInput);
    passwordInput = findViewById(R.id.passwordInput);
    registerButton = findViewById(R.id.registerButton);
    loadButton = findViewById(R.id.loadButton);
    logoutButton = findViewById(R.id.logoutButton);
    emailsRecyclerView = findViewById(R.id.emailsRecyclerView);
    swipeRefreshLayout = findViewById(R.id.swipeRefresh);
    welcomeText = findViewById(R.id.welcomeText);

    // Recycler setup
    emailAdapter = new RegisteredEmailAdapter();
    emailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    emailsRecyclerView.setAdapter(emailAdapter);

        setupWelcomeHeader();
        setupRetrofit();
        setupClickListeners();

        // Auto-load list on start so the user immediately sees current data
        loadRegisteredEmails();
    }

    private void setupWelcomeHeader() {
        SharedPreferences prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", null);
        String email = prefs.getString("email", "");
        String display = username != null && !username.isEmpty() ? username : email;
        if (welcomeText != null) {
            welcomeText.setText("Welcome, " + (display == null ? "" : display));
        }
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
                } else if (password.length() < 8) {
                    passwordInput.setError("Password must be at least 8 characters");
                    valid = false;
            }
            if (!valid) return;

            registerEmail(email, username, password);
        });

        loadButton.setOnClickListener(v -> loadRegisteredEmails());
        logoutButton.setOnClickListener(v -> logout());

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadRegisteredEmails);
        }
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

    private void loadRegisteredEmails() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        emailService.getRegisteredEmails().enqueue(new Callback<java.util.List<RegisteredEmailItem>>() {
            @Override
            public void onResponse(Call<java.util.List<RegisteredEmailItem>> call, Response<java.util.List<RegisteredEmailItem>> response) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    emailAdapter.setItems(response.body());
                    int count = response.body() == null ? 0 : response.body().size();
                    Toast.makeText(MainActivity.this, "Loaded " + count + " emails", Toast.LENGTH_SHORT).show();
                    Log.d("MainActivity", "Loaded " + count + " registered emails");
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load list (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<java.util.List<RegisteredEmailItem>> call, Throwable t) {
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Error: " + (t.getMessage() == null ? "unknown" : t.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void logout() {
        getSharedPreferences("auth_prefs", MODE_PRIVATE).edit().clear().apply();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}