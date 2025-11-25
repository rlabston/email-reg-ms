package com.technet7.microsvc.email.client;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import android.view.FrameLayout;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText emailInput, usernameInput, passwordInput;
    private Button registerButton, logoutButton, loadButton, deleteButton;
    private TextView welcomeText, statusMessage;
    private CardView listCard;
    private RecyclerView emailsRecyclerView;
    private FrameLayout loadingOverlay;
    
    private EmailAdapter emailAdapter;
    private String selectedEmail = null;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupListeners();
        
        // TODO: Check authentication and load user info
        checkAuthentication();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerButton = findViewById(R.id.registerButton);
        logoutButton = findViewById(R.id.logoutButton);
        loadButton = findViewById(R.id.loadButton);
        deleteButton = findViewById(R.id.deleteButton);
        welcomeText = findViewById(R.id.welcomeText);
        statusMessage = findViewById(R.id.statusMessage);
        listCard = findViewById(R.id.listCard);
        emailsRecyclerView = findViewById(R.id.emailsRecyclerView);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Setup RecyclerView
        emailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        emailAdapter = new EmailAdapter(email -> {
            // Toggle selection
            if (email.equals(selectedEmail)) {
                selectedEmail = null;
                deleteButton.setEnabled(false);
            } else {
                selectedEmail = email;
                deleteButton.setEnabled(true);
            }
            emailAdapter.setSelectedEmail(selectedEmail);
        });
        emailsRecyclerView.setAdapter(emailAdapter);
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> onRegister());
        logoutButton.setOnClickListener(v -> onLogout());
        loadButton.setOnClickListener(v -> loadEmails());
        deleteButton.setOnClickListener(v -> deleteSelected());
    }

    private void checkAuthentication() {
        // TODO: Implement actual authentication check
        // For now, show welcome message
        String username = "User"; // Get from AuthService
        welcomeText.setText("Welcome, " + username);
        
        // TODO: Check if user is admin
        isAdmin = true; // Get from AuthService
        if (isAdmin) {
            listCard.setVisibility(View.VISIBLE);
            loadEmails();
        }
    }

    private void onRegister() {
        String email = emailInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            showError("Please enter an email address");
            return;
        }

        if (username.isEmpty()) {
            showError("Please enter a username");
            return;
        }

        if (password.isEmpty()) {
            showError("Please enter a password");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return;
        }

        showLoading(true);

        // TODO: Implement actual API call
        // For now, simulate success
        new android.os.Handler().postDelayed(() -> {
            showLoading(false);
            showSuccess("Registration successful! Welcome, " + username);
            clearForm();
            if (isAdmin) {
                loadEmails();
            }
        }, 1000);
    }

    private void loadEmails() {
        showLoading(true);

        // TODO: Implement actual API call
        // For now, simulate loading with mock data
        new android.os.Handler().postDelayed(() -> {
            showLoading(false);
            java.util.List<EmailItem> mockEmails = new java.util.ArrayList<>();
            mockEmails.add(new EmailItem("1", "user1@example.com", "User One", "2024-01-15"));
            mockEmails.add(new EmailItem("2", "user2@example.com", "User Two", "2024-01-16"));
            mockEmails.add(new EmailItem("3", "user3@example.com", "User Three", "2024-01-17"));
            emailAdapter.setEmails(mockEmails);
        }, 1000);
    }

    private void deleteSelected() {
        if (selectedEmail == null) return;

        showLoading(true);

        // TODO: Implement actual API call
        // For now, simulate deletion
        new android.os.Handler().postDelayed(() -> {
            showLoading(false);
            selectedEmail = null;
            deleteButton.setEnabled(false);
            loadEmails();
            showSuccess("Email deleted successfully");
        }, 1000);
    }

    private void onLogout() {
        // TODO: Implement actual logout logic
        showSuccess("Logged out successfully");
        // Navigate to login screen
        finish();
    }

    private void clearForm() {
        emailInput.setText("");
        usernameInput.setText("");
        passwordInput.setText("");
    }

    private void showSuccess(String message) {
        statusMessage.setText(message);
        statusMessage.setBackgroundColor(ContextCompat.getColor(this, R.color.success_background));
        statusMessage.setTextColor(ContextCompat.getColor(this, R.color.success_text));
        statusMessage.setVisibility(View.VISIBLE);
        
        // Hide after 3 seconds
        new android.os.Handler().postDelayed(() -> 
            statusMessage.setVisibility(View.GONE), 3000);
    }

    private void showError(String message) {
        statusMessage.setText(message);
        statusMessage.setBackgroundColor(ContextCompat.getColor(this, R.color.error_background));
        statusMessage.setTextColor(ContextCompat.getColor(this, R.color.error_text));
        statusMessage.setVisibility(View.VISIBLE);
        
        // Hide after 3 seconds
        new android.os.Handler().postDelayed(() -> 
            statusMessage.setVisibility(View.GONE), 3000);
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    // Data class for email items
    public static class EmailItem {
        public final String id;
        public final String email;
        public final String username;
        public final String registrationDate;

        public EmailItem(String id, String email, String username, String registrationDate) {
            this.id = id;
            this.email = email;
            this.username = username;
            this.registrationDate = registrationDate;
        }
    }
}
