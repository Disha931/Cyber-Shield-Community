package com.disha.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fragment_ScreenshotScanner extends Fragment {

    private static final List<String> URGENCY_PHRASES = Arrays.asList(
            "act now", "immediately", "within 24 hours", "account has been blocked",
            "account will be suspended", "urgent"
    );

    private static final List<String> SENSITIVE_REQUEST_PHRASES = Arrays.asList(
            "otp", "pin", "password", "verify your account", "click here",
            "cvv", "card number"
    );

    private static final List<String> PRIZE_BAIT_PHRASES = Arrays.asList(
            "winner", "lottery", "congratulations", "claim your prize", "you have won"
    );

    private ImageView ivPreview;
    private TextView tvOcrStatus;
    private CardView cardExtractedText;
    private TextView tvExtractedText;
    private LinearLayout llResults;
    private LinearLayout llIndicators;
    private TextView tvVerdict;
    private TextView tvRiskScore;

    private TextRecognizer textRecognizer;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_screenshot_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivPreview = view.findViewById(R.id.ivPreview);
        tvOcrStatus = view.findViewById(R.id.tvOcrStatus);
        cardExtractedText = view.findViewById(R.id.cardExtractedText);
        tvExtractedText = view.findViewById(R.id.tvExtractedText);
        llResults = view.findViewById(R.id.llResults);
        llIndicators = view.findViewById(R.id.llIndicators);
        tvVerdict = view.findViewById(R.id.tvVerdict);
        tvRiskScore = view.findViewById(R.id.tvRiskScore);

        CardView cardPickImage = view.findViewById(R.id.cardPickImage);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handlePickedImage(uri);
                    }
                });

        cardPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void handlePickedImage(Uri uri) {
        ivPreview.setVisibility(View.VISIBLE);
        ivPreview.setImageURI(uri);

        cardExtractedText.setVisibility(View.GONE);
        llResults.setVisibility(View.GONE);
        tvOcrStatus.setVisibility(View.VISIBLE);
        tvOcrStatus.setText("Reading text from image...");

        try {
            InputImage image = InputImage.fromFilePath(requireContext(), uri);

            textRecognizer.process(image)
                    .addOnSuccessListener(this::handleOcrSuccess)
                    .addOnFailureListener(e -> {
                        tvOcrStatus.setText("Failed to read text from image");
                        Toast.makeText(getContext(), "OCR failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (IOException e) {
            tvOcrStatus.setText("Unable to load the selected image");
        }
    }

    private void handleOcrSuccess(Text visionText) {
        String extractedText = visionText.getText();

        tvOcrStatus.setVisibility(View.GONE);

        if (extractedText.trim().isEmpty()) {
            cardExtractedText.setVisibility(View.GONE);
            llResults.setVisibility(View.GONE);
            Toast.makeText(getContext(), "No readable text found in this image", Toast.LENGTH_SHORT).show();
            return;
        }

        cardExtractedText.setVisibility(View.VISIBLE);
        tvExtractedText.setText(extractedText);

        int riskScore = analyzeTextRisk(extractedText);
        showAnalysisResults(riskScore, extractedText);
    }

    private int analyzeTextRisk(String text) {
        String lower = text.toLowerCase();
        int score = 0;

        if (containsAny(lower, URGENCY_PHRASES)) score += 30;
        if (containsAny(lower, SENSITIVE_REQUEST_PHRASES)) score += 40;
        if (containsAny(lower, PRIZE_BAIT_PHRASES)) score += 30;

        return Math.min(100, score);
    }

    private boolean containsAny(String text, List<String> phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private void showAnalysisResults(int riskScore, String extractedText) {
        llResults.setVisibility(View.VISIBLE);
        llIndicators.removeAllViews();

        List<String> indicators = new ArrayList<>();
        String lower = extractedText.toLowerCase();

        if (containsAny(lower, URGENCY_PHRASES)) {
            indicators.add("Creates a sense of urgency to pressure quick action");
        }
        if (containsAny(lower, SENSITIVE_REQUEST_PHRASES)) {
            indicators.add("Requests sensitive information (OTP, PIN, password, or card details)");
        }
        if (containsAny(lower, PRIZE_BAIT_PHRASES)) {
            indicators.add("Uses prize/lottery bait — a classic scam pattern");
        }

        if (indicators.isEmpty()) {
            TextView noIssues = new TextView(getContext());
            noIssues.setText("✓ No common scam patterns detected in this text");
            noIssues.setTextColor(getResources().getColor(R.color.Safegreen));
            noIssues.setTextSize(13);
            llIndicators.addView(noIssues);
        } else {
            for (String indicator : indicators) {
                TextView row = new TextView(getContext());
                row.setText("• " + indicator);
                row.setTextColor(getResources().getColor(R.color.txtSecond));
                row.setTextSize(13);
                row.setPadding(0, 4, 0, 4);
                llIndicators.addView(row);
            }
        }

        tvRiskScore.setText("Risk Score: " + riskScore + "%");

        if (riskScore >= 50) {
            tvVerdict.setText("⚠ HIGH RISK - LIKELY SCAM");
            tvVerdict.setTextColor(getResources().getColor(R.color.Dangerred));
        } else if (riskScore > 0) {
            tvVerdict.setText("⚠ MEDIUM RISK - BE CAUTIOUS");
            tvVerdict.setTextColor(getResources().getColor(R.color.accent));
        } else {
            tvVerdict.setText("✓ LOW RISK");
            tvVerdict.setTextColor(getResources().getColor(R.color.Safegreen));
        }
    }
}