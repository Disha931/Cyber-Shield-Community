package com.disha.myapplication;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

    public class fragment_Detect extends Fragment {

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_detect, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            CardView cardUrlChecker = view.findViewById(R.id.cardUrlChecker);
            CardView cardQrScanner = view.findViewById(R.id.cardQrScanner);
            CardView cardScamDetector = view.findViewById(R.id.cardScamDetector);
            CardView cardScreenshotScanner = view.findViewById(R.id.cardScreenshotScanner);
            CardView cardSmsDetector = view.findViewById(R.id.cardSmsDetector);
            CardView cardThreatHeatmap = view.findViewById(R.id.cardThreatHeatmap);

            cardUrlChecker.setOnClickListener(v ->
                    ((Dashboard) requireActivity()).openFragment(new Fragment_UrlChecker(), "url_checker"));            cardQrScanner.setOnClickListener(v -> showComingSoon("QR Code Scanner"));
            cardScamDetector.setOnClickListener(v -> showComingSoon("AI Scam Detector"));
            cardScreenshotScanner.setOnClickListener(v -> showComingSoon("Screenshot Scanner"));
            cardSmsDetector.setOnClickListener(v -> showComingSoon("SMS Phishing Detector"));
            cardThreatHeatmap.setOnClickListener(v -> showComingSoon("Threat Heatmap"));
        }

        private void showComingSoon(String tool) {
            Toast.makeText(getContext(), tool + " — coming soon", Toast.LENGTH_SHORT).show();
        }
    }
