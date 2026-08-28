
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

public class Fragment_UrlChecker extends Fragment {

    private static final List<String> SHORTENER_DOMAINS = Arrays.asList(
            "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly", "is.gd", "buff.ly"
    );

    private static final List<String> SUSPICIOUS_TLDS = Arrays.asList(
            ".xyz", ".tk", ".top", ".click", ".gq", ".ml", ".cf", ".work", ".loan"
    );

    private static final List<String> SENSITIVE_KEYWORDS = Arrays.asList(
            "verify", "login", "secure", "bank", "account", "update", "confirm", "signin"
    );

    private EditText etUrlInput;
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
        return inflater.inflate(R.layout.fragment_url_checker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etUrlInput = view.findViewById(R.id.etUrlInput);
        llResults = view.findViewById(R.id.llResults);
        llIndicators = view.findViewById(R.id.llIndicators);
        tvVerdict = view.findViewById(R.id.tvVerdict);
        tvRiskScore = view.findViewById(R.id.tvRiskScore);
        tvRecommendation = view.findViewById(R.id.tvRecommendation);

        CardView cardAnalyzeUrl = view.findViewById(R.id.cardAnalyzeUrl);
        cardAnalyzeUrl.setOnClickListener(v -> analyzeUrl());
    }

    private void analyzeUrl() {
        String url = etUrlInput.getText().toString().trim();

        if (url.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a URL to check", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> indicators = new ArrayList<>();
        int riskScore = 0;

        String lowerUrl = url.toLowerCase();

        // Check 1: not using HTTPS
        if (!lowerUrl.startsWith("https://")) {
            indicators.add("Not using a secure HTTPS connection");
            riskScore += 20;
        }

        // Check 2: raw IP address instead of a domain name
        if (Pattern.compile("https?://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}").matcher(lowerUrl).find()) {
            indicators.add("Uses a raw IP address instead of a domain name");
            riskScore += 25;
        }

        // Check 3: @ symbol trick
        if (url.contains("@")) {
            indicators.add("Contains '@' — a common phishing trick to hide the real destination");
            riskScore += 25;
        }

        // Check 4: known URL shortener
        for (String shortener : SHORTENER_DOMAINS) {
            if (lowerUrl.contains(shortener)) {
                indicators.add("Uses a link shortener (" + shortener + ") — real destination can't be verified");
                riskScore += 15;
                break;
            }
        }

        // Check 5: suspicious/cheap TLD
        for (String tld : SUSPICIOUS_TLDS) {
            if (lowerUrl.contains(tld)) {
                indicators.add("Uses a domain ending (" + tld + ") commonly abused for scams");
                riskScore += 15;
                break;
            }
        }

        // Check 6: sensitive keyword combined with non-standard domain
        boolean hasSensitiveKeyword = false;
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (lowerUrl.contains(keyword)) {
                hasSensitiveKeyword = true;
                break;
            }
        }
        boolean looksLikeMajorBrandDomain = lowerUrl.contains("google.com")
                || lowerUrl.contains("microsoft.com")
                || lowerUrl.contains("apple.com")
                || lowerUrl.contains("amazon.com");

        if (hasSensitiveKeyword && !looksLikeMajorBrandDomain) {
            indicators.add("Contains sensitive keywords (login/verify/bank/etc.) on an unfamiliar domain");
            riskScore += 20;
        }

        // Check 7: excessive hyphens (common in fake lookalike domains)
        long hyphenCount = lowerUrl.chars().filter(ch -> ch == '-').count();
        if (hyphenCount >= 3) {
            indicators.add("Domain contains an unusually high number of hyphens");
            riskScore += 10;
        }

        riskScore = Math.min(100, riskScore);

        showResults(riskScore, indicators);
    }

    private void showResults(int riskScore, List<String> indicators) {
        llResults.setVisibility(View.VISIBLE);
        llIndicators.removeAllViews();

        if (indicators.isEmpty()) {
            TextView noIssues = new TextView(getContext());
            noIssues.setText("✓ No suspicious patterns detected");
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
        if (riskScore >= 60) {
            tvVerdict.setText("⚠ HIGH RISK - Avoid This Link");
            tvVerdict.setTextColor(getResources().getColor(R.color.Dangerred));
            recommendation = "Do NOT click this link or enter any personal information. It shows strong signs of being unsafe.";
        } else if (riskScore >= 30) {
            tvVerdict.setText("⚠ MEDIUM RISK - Be Cautious");
            tvVerdict.setTextColor(getResources().getColor(R.color.accent));
            recommendation = "Proceed carefully. Verify the sender and destination before entering any sensitive information.";
        } else {
            tvVerdict.setText("✓ LOW RISK");
            tvVerdict.setTextColor(getResources().getColor(R.color.Safegreen));
            recommendation = "No major red flags found, but always stay alert online.";
        }

        tvRecommendation.setText(recommendation);
    }
}