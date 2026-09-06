package com.disha.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Fragment_ScamDetector extends Fragment {

    private static final List<String> URGENCY_PHRASES = Arrays.asList(
            "act now", "immediately", "within 24 hours", "account has been blocked",
            "account will be suspended", "urgent", "act fast", "limited time"
    );

    private static final List<String> SENSITIVE_REQUEST_PHRASES = Arrays.asList(
            "otp", "pin", "password", "verify your account", "click here",
            "cvv", "card number", "update your details"
    );

    private static final List<String> PRIZE_BAIT_PHRASES = Arrays.asList(
            "winner", "lottery", "congratulations", "claim your prize",
            "you have won", "cash reward", "free gift"
    );

    private static final List<String> GENERIC_GREETINGS = Arrays.asList(
            "dear customer", "dear user", "dear sir/madam", "dear valued customer"
    );

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+|www\\.\\S+");

    private EditText etMessageInput;
    private LinearLayout llResults;
    private LinearLayout llIndicators;
    private TextView tvVerdict;
    private TextView tvRiskScore;
    private TextView tvRecommendation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scam_detector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etMessageInput = view.findViewById(R.id.etMessageInput);
        llResults = view.findViewById(R.id.llResults);
        llIndicators = view.findViewById(R.id.llIndicators);
        tvVerdict = view.findViewById(R.id.tvVerdict);
        tvRiskScore = view.findViewById(R.id.tvRiskScore);
        tvRecommendation = view.findViewById(R.id.tvRecommendation);

        CardView cardAnalyzeMessage = view.findViewById(R.id.cardAnalyzeMessage);
        cardAnalyzeMessage.setOnClickListener(v -> analyzeMessage());
    }

    private void analyzeMessage() {
        String message = etMessageInput.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(getContext(), "Please paste a message to analyze", Toast.LENGTH_SHORT).show();
            return;
        }

        String lowerMessage = message.toLowerCase();
        List<String> indicators = new ArrayList<>();
        int riskScore = 0;

        boolean hasUrgency = containsAny(lowerMessage, URGENCY_PHRASES);
        if (hasUrgency) {
            indicators.add("Creates a sense of urgency to pressure quick action");
            riskScore += 25;
        }

        boolean hasSensitiveRequest = containsAny(lowerMessage, SENSITIVE_REQUEST_PHRASES);
        if (hasSensitiveRequest) {
            indicators.add("Requests sensitive information (OTP, PIN, password, or card details)");
            riskScore += 30;
        }

        boolean hasPrizeBait = containsAny(lowerMessage, PRIZE_BAIT_PHRASES);
        if (hasPrizeBait) {
            indicators.add("Uses prize/lottery bait — a classic scam pattern");
            riskScore += 25;
        }

        boolean hasGenericGreeting = containsAny(lowerMessage, GENERIC_GREETINGS);
        if (hasGenericGreeting) {
            indicators.add("Uses an impersonal, generic greeting instead of your actual name");
            riskScore += 10;
        }

        boolean containsLink = URL_PATTERN.matcher(message).find();
        if (containsLink && (hasUrgency || hasSensitiveRequest || hasPrizeBait)) {
            indicators.add("Contains a link combined with other suspicious patterns");
            riskScore += 20;
        }

        riskScore = Math.min(100, riskScore);

        showResults(riskScore, indicators);
    }

    private boolean containsAny(String text, List<String> phrases) {
        for (String phrase : phrases) {
            if (text.contains(phrase)) return true;
        }
        return false;
    }

    private void showResults(int riskScore, List<String> indicators) {
        llResults.setVisibility(View.VISIBLE);
        llIndicators.removeAllViews();

        if (indicators.isEmpty()) {
            TextView noIssues = new TextView(getContext());
            noIssues.setText("✓ No common scam patterns detected");
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

        String recommendation;
        if (riskScore >= 50) {
            tvVerdict.setText("⚠ HIGH RISK - LIKELY SCAM");
            tvVerdict.setTextColor(getResources().getColor(R.color.Dangerred));
            recommendation = "Do NOT click any links, reply, or share personal information. " +
                    "Legitimate organizations never ask for your OTP or PIN via message.";
        } else if (riskScore >= 20) {
            tvVerdict.setText("⚠ MEDIUM RISK - BE CAUTIOUS");
            tvVerdict.setTextColor(getResources().getColor(R.color.accent));
            recommendation = "Some suspicious patterns found. Verify the sender through official channels before acting.";
        } else {
            tvVerdict.setText("✓ LOW RISK");
            tvVerdict.setTextColor(getResources().getColor(R.color.Safegreen));
            recommendation = "No major scam indicators found, but always stay cautious with unexpected messages.";
        }

        tvRecommendation.setText(recommendation);
    }
}