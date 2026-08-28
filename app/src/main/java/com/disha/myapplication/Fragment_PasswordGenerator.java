package com.disha.myapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.security.SecureRandom;

public class Fragment_PasswordGenerator extends Fragment {

    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBER_CHARS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+";

    private static final int MIN_LENGTH = 8;

    private TextView tvGeneratedPassword;
    private TextView tvLengthLabel;
    private SeekBar seekBarLength;
    private CheckBox checkUppercase, checkLowercase, checkNumbers, checkSpecial;

    private final SecureRandom secureRandom = new SecureRandom();
    private String currentPassword = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_password_generator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGeneratedPassword = view.findViewById(R.id.tvGeneratedPassword);
        tvLengthLabel = view.findViewById(R.id.tvLengthLabel);
        seekBarLength = view.findViewById(R.id.seekBarLength);
        checkUppercase = view.findViewById(R.id.checkUppercase);
        checkLowercase = view.findViewById(R.id.checkLowercase);
        checkNumbers = view.findViewById(R.id.checkNumbers);
        checkSpecial = view.findViewById(R.id.checkSpecial);

        ImageView ivCopyPassword = view.findViewById(R.id.ivCopyPassword);
        CardView cardGenerate = view.findViewById(R.id.cardGenerate);

        seekBarLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int length = MIN_LENGTH + progress;
                tvLengthLabel.setText("Length: " + length);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        cardGenerate.setOnClickListener(v -> generatePassword());
        ivCopyPassword.setOnClickListener(v -> copyToClipboard());
    }

    private void generatePassword() {
        StringBuilder charPool = new StringBuilder();
        if (checkUppercase.isChecked()) charPool.append(UPPERCASE_CHARS);
        if (checkLowercase.isChecked()) charPool.append(LOWERCASE_CHARS);
        if (checkNumbers.isChecked()) charPool.append(NUMBER_CHARS);
        if (checkSpecial.isChecked()) charPool.append(SPECIAL_CHARS);

        if (charPool.length() == 0) {
            Toast.makeText(getContext(), "Select at least one character type", Toast.LENGTH_SHORT).show();
            return;
        }

        int length = MIN_LENGTH + seekBarLength.getProgress();

        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(charPool.length());
            password.append(charPool.charAt(randomIndex));
        }

        currentPassword = password.toString();
        tvGeneratedPassword.setText(currentPassword);
    }

    private void copyToClipboard() {
        if (currentPassword.isEmpty()) {
            Toast.makeText(getContext(), "Generate a password first", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Generated Password", currentPassword);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(getContext(), "Password copied to clipboard", Toast.LENGTH_SHORT).show();
    }
}