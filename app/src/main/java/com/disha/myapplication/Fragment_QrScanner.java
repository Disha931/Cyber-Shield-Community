package com.disha.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Fragment_QrScanner extends Fragment {

    private LinearLayout llScanResult;
    private TextView tvScannedContent;
    private TextView tvSafetyStatus;
    private CardView cardOpenLink;

    private String scannedUrl = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        llScanResult = view.findViewById(R.id.llScanResult);
        tvScannedContent = view.findViewById(R.id.tvScannedContent);
        tvSafetyStatus = view.findViewById(R.id.tvSafetyStatus);
        cardOpenLink = view.findViewById(R.id.cardOpenLink);

        CardView cardStartScan = view.findViewById(R.id.cardStartScan);
        cardStartScan.setOnClickListener(v -> startScan());

        cardOpenLink.setOnClickListener(v -> openScannedLink());
    }

    private void startScan() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Point your camera at a QR code");
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {
                handleScannedContent(result.getContents());
            }
        }
    }

    private void handleScannedContent(String content) {
        scannedUrl = content;
        llScanResult.setVisibility(View.VISIBLE);
        tvScannedContent.setText(content);

        boolean looksLikeUrl = content.startsWith("http://") || content.startsWith("https://");

        if (!looksLikeUrl) {
            tvSafetyStatus.setText("ℹ Not a URL — just plain text/data");
            tvSafetyStatus.setTextColor(getResources().getColor(R.color.txtSecond));
            cardOpenLink.setVisibility(View.GONE);
            return;
        }

        int riskScore = analyzeUrlRisk(content);

        if (riskScore >= 50) {
            tvSafetyStatus.setText("⚠ WARNING — This link looks unsafe");
            tvSafetyStatus.setTextColor(getResources().getColor(R.color.Dangerred));
            cardOpenLink.setVisibility(View.GONE);
        } else {
            tvSafetyStatus.setText("✓ Looks safe to open");
            tvSafetyStatus.setTextColor(getResources().getColor(R.color.Safegreen));
            cardOpenLink.setVisibility(View.VISIBLE);
        }
    }

    // A lightweight, self-contained safety check for scanned URLs.
    // Deliberately separate from Fragment_UrlChecker's logic to avoid touching that already-tested code.
    private int analyzeUrlRisk(String url) {
        String lowerUrl = url.toLowerCase();
        int score = 0;

        if (!lowerUrl.startsWith("https://")) score += 25;
        if (lowerUrl.matches(".*https?://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*")) score += 30;
        if (url.contains("@")) score += 25;
        if (lowerUrl.contains("bit.ly") || lowerUrl.contains("tinyurl.com")) score += 20;

        return Math.min(100, score);
    }

    private void openScannedLink() {
        if (scannedUrl.isEmpty()) return;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(scannedUrl));
        startActivity(browserIntent);
    }
}