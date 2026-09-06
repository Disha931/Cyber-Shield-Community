package com.disha.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fragment_SmsDetector extends Fragment {

    private static final int MAX_MESSAGES_TO_SCAN = 20;

    private static final List<String> URGENCY_PHRASES = Arrays.asList(
            "act now", "immediately", "within 24 hours", "account has been blocked",
            "account will be suspended", "urgent", "act fast"
    );

    private static final List<String> SENSITIVE_REQUEST_PHRASES = Arrays.asList(
            "otp", "pin", "password", "verify your account", "click here",
            "cvv", "card number", "update your details"
    );

    private static final List<String> PRIZE_BAIT_PHRASES = Arrays.asList(
            "winner", "lottery", "congratulations", "claim your prize",
            "you have won", "cash reward"
    );

    private LinearLayout llSmsResults;
    private TextView tvNoResults;
    private TextView tvScanSummary;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sms_detector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        llSmsResults = view.findViewById(R.id.llSmsResults);
        tvNoResults = view.findViewById(R.id.tvNoResults);
        tvScanSummary = view.findViewById(R.id.tvScanSummary);

        CardView cardScanInbox = view.findViewById(R.id.cardScanInbox);

        // Register the permission request BEFORE the fragment view is shown to the user —
        // this must happen in onViewCreated/onCreate, not inside a click listener.
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        scanInbox();
                    } else {
                        Toast.makeText(getContext(), "SMS permission is required to scan your inbox", Toast.LENGTH_SHORT).show();
                    }
                });

        cardScanInbox.setOnClickListener(v -> checkPermissionAndScan());
    }

    private void checkPermissionAndScan() {
        boolean alreadyGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED;

        if (alreadyGranted) {
            scanInbox();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_SMS);
        }
    }

    private void scanInbox() {
        List<String[]> messages = readInboxMessages(); // each entry: {sender, body}

        llSmsResults.removeAllViews();

        if (messages.isEmpty()) {
            tvScanSummary.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
            tvNoResults.setText("No messages found in inbox");
            return;
        }

        int suspiciousCount = 0;

        for (String[] message : messages) {
            String sender = message[0];
            String body = message[1];

            int riskScore = analyzeMessageRisk(body);
            if (riskScore > 0) {
                addResultRow(sender, body, riskScore);
                suspiciousCount++;
            }
        }

        tvScanSummary.setVisibility(View.VISIBLE);
        tvScanSummary.setText("Scanned " + messages.size() + " messages — " + suspiciousCount + " flagged as suspicious");

        if (suspiciousCount == 0) {
            tvNoResults.setVisibility(View.VISIBLE);
            tvNoResults.setText("No suspicious messages found");
        } else {
            tvNoResults.setVisibility(View.GONE);
        }
    }

    private List<String[]> readInboxMessages() {
        List<String[]> messages = new ArrayList<>();

        Uri inboxUri = Telephony.Sms.Inbox.CONTENT_URI;
        String[] projection = {Telephony.Sms.ADDRESS, Telephony.Sms.BODY};
        String sortOrder = Telephony.Sms.DATE + " DESC LIMIT " + MAX_MESSAGES_TO_SCAN;

        try (Cursor cursor = requireContext().getContentResolver()
                .query(inboxUri, projection, null, null, sortOrder)) {

            if (cursor != null) {
                int addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
                int bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY);

                while (cursor.moveToNext()) {
                    String address = addressIndex >= 0 ? cursor.getString(addressIndex) : "Unknown";
                    String body = bodyIndex >= 0 ? cursor.getString(bodyIndex) : "";
                    messages.add(new String[]{address, body});
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to read SMS inbox", Toast.LENGTH_SHORT).show();
        }

        return messages;
    }

    private int analyzeMessageRisk(String body) {
        if (body == null) return 0;

        String lower = body.toLowerCase();
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

    private void addResultRow(String sender, String body, int riskScore) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.item_sms_result, llSmsResults, false);

        TextView tvSender = row.findViewById(R.id.tvSmsSender);
        TextView tvBody = row.findViewById(R.id.tvSmsBody);
        TextView tvBadge = row.findViewById(R.id.tvSmsRiskBadge);

        tvSender.setText(sender != null ? sender : "Unknown");
        tvBody.setText(body);

        if (riskScore >= 50) {
            tvBadge.setText("High Risk");
            tvBadge.setBackgroundResource(R.drawable.bg_severity_high);
        } else {
            tvBadge.setText("Suspicious");
            tvBadge.setBackgroundResource(R.drawable.bg_severity_medium);
        }

        llSmsResults.addView(row);
    }
}