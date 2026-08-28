package com.disha.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.ComponentActivity;

import com.disha.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends ComponentActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvError, tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginUser());

        // ab sidha Register screen pe le jaata hai, registerUser() nahi bulata
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, RegisterActivity.class));
        });

        // NAYA: Forgot password click
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        // Register se success hoke wapas aane par message dikhana
        if (getIntent().getBooleanExtra("registration_success", false)) {
            tvError.setTextColor(getResources().getColor(R.color.Safegreen));
            showError("Account created successfully. Please sign in.");
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInput(email, password)) return;

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        goToDashboard();
                    } else {
                        showError(task.getException() != null
                                ? task.getException().getMessage()
                                : "Login failed");
                    }
                });
    }

    // NAYA: password bhool jaane par reset link bhejta hai
    private void showForgotPasswordDialog() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            showError("Enter your email first, then tap 'Forgot password?'");
            return;
        }

        setLoading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        tvError.setTextColor(getResources().getColor(R.color.Safegreen));
                        showError("Password reset link sent to " + email + ". Check your inbox.");
                    } else {
                        showError(task.getException() != null
                                ? task.getException().getMessage()
                                : "Could not send reset email. Check the email address.");
                    }
                });
    }

    // registerUser() aur saveUserToDatabase() poori tarah HATA diye gaye
    // — yeh kaam ab Register.java karta hai.

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            showError("Enter your email");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            showError("Enter your password");
            return false;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return false;
        }
        tvError.setVisibility(View.GONE);
        return true;
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }

    private void goToDashboard() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}