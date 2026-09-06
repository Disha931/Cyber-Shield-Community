package com.disha.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class Fragment_EmergencySos extends Fragment {

    private static final String PREFS_NAME = "emergency_contact_prefs";
    private static final String KEY_CONTACT_NAME = "contact_name";
    private static final String KEY_CONTACT_NUMBER = "contact_number";

    private EditText etContactName, etContactNumber;
    private CardView cardCallContact;
    private TextView tvCallContactLabel;

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emergency_sos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etContactName = view.findViewById(R.id.etContactName);
        etContactNumber = view.findViewById(R.id.etContactNumber);
        cardCallContact = view.findViewById(R.id.cardCallContact);
        tvCallContactLabel = view.findViewById(R.id.tvCallContactLabel);

        CardView cardCallHelpline = view.findViewById(R.id.cardCallHelpline);
        CardView cardReportOnline = view.findViewById(R.id.cardReportOnline);
        CardView cardSaveContact = view.findViewById(R.id.cardSaveContact);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        cardCallHelpline.setOnClickListener(v -> dialNumber("1930"));

        cardReportOnline.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://cybercrime.gov.in"));
            startActivity(intent);
        });

        cardSaveContact.setOnClickListener(v -> saveEmergencyContact());

        loadEmergencyContact();
    }

    private void dialNumber(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        startActivity(intent);
    }

    private void saveEmergencyContact() {
        String name = etContactName.getText().toString().trim();
        String number = etContactNumber.getText().toString().trim();

        if (name.isEmpty() || number.isEmpty()) {
            Toast.makeText(getContext(), "Please enter both name and number", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit()
                .putString(KEY_CONTACT_NAME, name)
                .putString(KEY_CONTACT_NUMBER, number)
                .apply();

        Toast.makeText(getContext(), "Emergency contact saved", Toast.LENGTH_SHORT).show();
        loadEmergencyContact();
    }

    private void loadEmergencyContact() {
        String name = prefs.getString(KEY_CONTACT_NAME, null);
        String number = prefs.getString(KEY_CONTACT_NUMBER, null);

        if (name != null && number != null) {
            etContactName.setText(name);
            etContactNumber.setText(number);

            cardCallContact.setVisibility(View.VISIBLE);
            tvCallContactLabel.setText("Call " + name);
            cardCallContact.setOnClickListener(v -> dialNumber(number));
        } else {
            cardCallContact.setVisibility(View.GONE);
        }
    }
}