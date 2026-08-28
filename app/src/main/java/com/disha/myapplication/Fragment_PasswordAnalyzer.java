package com.disha.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Fragment_PasswordAnalyzer extends Fragment {

    // A short list is enough for a college project demo — not meant to be exhaustive
    private static final List<String> COMMON_PASSWORDS = Arrays.asList(
            "password", "123456", "12345678", "qwerty", "abc123",
            "password1", "111111", "letmein", "iloveyou", "admin"
    );

    private EditText etPasswordInput;
    private ProgressBar progressStrength;
    private TextView tvStrengthLabel;
    private TextView checkLength, checkUppercase, checkLowercase, checkNumbers, checkSpecial, checkDictionary;
    private TextView tvSuggestion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_password_analyzer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etPasswordInput = view.findViewById(R.id.etPasswordInput);
        progressStrength = view.findViewById(R.id.progressStrength);
        tvStrengthLabel = view.findViewById(R.id.tvStrengthLabel);
        checkLength = view.findViewById(R.id.checkLength);
        checkUppercase = view.findViewById(R.id.checkUppercase);
        checkLowercase = view.findViewById(R.id.checkLowercase);
        checkNumbers = view.findViewById(R.id.checkNumbers);
        checkSpecial = view.findViewById(R.id.checkSpecial);
        checkDictionary = view.findViewById(R.id.checkDictionary);
        tvSuggestion = view.findViewById(R.id.tvSuggestion);

        etPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                analyzePassword(s.toString());
            }
        });
    }

    private void analyzePassword(String password) {
        if (password.isEmpty()) {
            resetUi();
            return;
        }

        boolean hasLength = password.length() >= 10;
        boolean hasUpper = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasLower = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasNumber = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[^A-Za-z0-9]").matcher(password).find();
        boolean isDictionaryWord = COMMON_PASSWORDS.contains(password.toLowerCase());

        int score = 0;
        if (hasLength) score += 20;
        if (hasUpper) score += 20;
        if (hasLower) score += 20;
        if (hasNumber) score += 20;
        if (hasSpecial) score += 20;

        updateCheckRow(checkLength, "Length (10+ chars)", hasLength);
        updateCheckRow(checkUppercase, "Uppercase: A-Z", hasUpper);
        updateCheckRow(checkLowercase, "Lowercase: a-z", hasLower);
        updateCheckRow(checkNumbers, "Numbers: 0-9", hasNumber);
        updateCheckRow(checkSpecial, "Special chars: !@#$", hasSpecial);
        updateCheckRow(checkDictionary, "Dictionary word", !isDictionaryWord);

        progressStrength.setProgress(score);
        updateStrengthLabel(score);
        updateSuggestion(hasLength, hasUpper, hasLower, hasNumber, hasSpecial, isDictionaryWord);
    }

    private void updateCheckRow(TextView view, String label, boolean passed) {
        if (passed) {
            view.setText("✓ " + label);
            view.setTextColor(getResources().getColor(R.color.Safegreen));
        } else {
            view.setText("✗ " + label);
            view.setTextColor(getResources().getColor(R.color.Dangerred));
        }
    }

    private void updateStrengthLabel(int score) {
        if (score <= 40) {
            tvStrengthLabel.setText("⚠ WEAK Strength");
            tvStrengthLabel.setTextColor(getResources().getColor(R.color.Dangerred));
        } else if (score <= 80) {
            tvStrengthLabel.setText("⚠ MEDIUM Strength");
            tvStrengthLabel.setTextColor(getResources().getColor(R.color.accent));
        } else {
            tvStrengthLabel.setText("✓ STRONG Strength");
            tvStrengthLabel.setTextColor(getResources().getColor(R.color.Safegreen));
        }
    }

    private void updateSuggestion(boolean hasLength, boolean hasUpper, boolean hasLower,
                                  boolean hasNumber, boolean hasSpecial, boolean isDictionaryWord) {
        StringBuilder suggestion = new StringBuilder();

        if (isDictionaryWord) {
            suggestion.append("This is a commonly used password — avoid it entirely. ");
        }
        if (!hasLength) suggestion.append("Make it at least 10 characters long. ");
        if (!hasUpper) suggestion.append("Add an uppercase letter. ");
        if (!hasLower) suggestion.append("Add a lowercase letter. ");
        if (!hasNumber) suggestion.append("Add a number. ");
        if (!hasSpecial) suggestion.append("Add a special character like !@#$. ");

        if (suggestion.length() == 0) {
            suggestion.append("Great job — this password meets all the criteria!");
        }

        tvSuggestion.setText(suggestion.toString().trim());
    }

    private void resetUi() {
        progressStrength.setProgress(0);
        tvStrengthLabel.setText("Enter a password above");
        tvStrengthLabel.setTextColor(getResources().getColor(R.color.txtSecond));

        updateCheckRow(checkLength, "Length (10+ chars)", false);
        updateCheckRow(checkUppercase, "Uppercase: A-Z", false);
        updateCheckRow(checkLowercase, "Lowercase: a-z", false);
        updateCheckRow(checkNumbers, "Numbers: 0-9", false);
        updateCheckRow(checkSpecial, "Special chars: !@#$", false);
        updateCheckRow(checkDictionary, "Dictionary word", false);

        tvSuggestion.setText("Enter a password to see suggestions.");
    }
}