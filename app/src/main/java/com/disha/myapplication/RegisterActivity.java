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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends ComponentActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvError, tvGoToLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvError = findViewById(R.id.tvError);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(v -> registerUser());

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, Login.class));
            finish();
        });
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInput(fullName, email, password, confirmPassword)) return;

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserToDatabase(fullName, email);
                    } else {
                        setLoading(false);
                        handleRegisterError(task.getException());
                    }
                });
    }

    // Stores the new user's profile in Firestore ("database storage")
    private void saveUserToDatabase(String fullName, String email) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            setLoading(false);
            goToLoginWithMessage();
            return;
        }

        String uid = firebaseUser.getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("securityScore", 0);
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(uid)
                .set(userData)
                .addOnCompleteListener(dbTask -> {
                    setLoading(false);
                    // Sign the user out here so they land on Login and sign in
                    // fresh, exactly as requested (register -> then login).
                    mAuth.signOut();
                    goToLoginWithMessage();
                });
    }

    private void handleRegisterError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            // This is the "account already exists" case
            showError("An account already exists with this email. Please sign in instead.");
        } else if (exception != null) {
            showError(exception.getMessage());
        } else {
            showError("Registration failed. Please try again.");
        }
    }

    private boolean validateInput(String fullName, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(fullName)) {
            showError("Enter your full name");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            showError("Enter your email");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            showError("Enter a password");
            return false;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
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
        btnRegister.setEnabled(!loading);
    }

    private void goToLoginWithMessage() {
        Intent intent = new Intent(RegisterActivity.this, Login.class);
        intent.putExtra("registration_success", true);
        startActivity(intent);
        finish();
    }
}
