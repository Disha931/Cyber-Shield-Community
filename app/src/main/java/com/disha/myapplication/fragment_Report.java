package com.disha.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class fragment_Report extends Fragment {

    private static final String[] REPORT_TYPES = {
            "Phishing", "Scam Call", "Fake Website", "Financial Fraud", "Other"
    };

    private Spinner spinnerReportType;
    private EditText etReportDescription;
    private LinearLayout llMyReports;
    private TextView tvNoReports;

    private DatabaseReference myReportsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerReportType = view.findViewById(R.id.spinnerReportType);
        etReportDescription = view.findViewById(R.id.etReportDescription);
        llMyReports = view.findViewById(R.id.llMyReports);
        tvNoReports = view.findViewById(R.id.tvNoReports);
        CardView cardSubmitReport = view.findViewById(R.id.cardSubmitReport);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, REPORT_TYPES);
        spinnerReportType.setAdapter(adapter);

        setupFirebaseRef();

        cardSubmitReport.setOnClickListener(v -> submitReport());

        loadMyReports();
    }

    private void setupFirebaseRef() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            myReportsRef = null;
            return;
        }
        String uid = currentUser.getUid();
        myReportsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("myReports");
    }

    private void submitReport() {
        if (myReportsRef == null) {
            Toast.makeText(getContext(), "You must be logged in to submit a report", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = spinnerReportType.getSelectedItem().toString();
        String description = etReportDescription.getText().toString().trim();

        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Please describe what happened", Toast.LENGTH_SHORT).show();
            return;
        }

        String reportId = myReportsRef.push().getKey();
        if (reportId == null) {
            Toast.makeText(getContext(), "Something went wrong. Try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = System.currentTimeMillis();

        ReportItem report = new ReportItem(type, description, "Submitted", timestamp);
        myReportsRef.child(reportId).setValue(report)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Report submitted successfully", Toast.LENGTH_SHORT).show();
                    etReportDescription.setText("");
                    loadMyReports(); // refresh the list to show the new report
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadMyReports() {
        if (myReportsRef == null) {
            tvNoReports.setVisibility(View.VISIBLE);
            return;
        }

        myReportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                llMyReports.removeAllViews(); // clear before rebuilding

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    tvNoReports.setVisibility(View.VISIBLE);
                    return;
                }

                tvNoReports.setVisibility(View.GONE);

                for (DataSnapshot child : snapshot.getChildren()) {
                    ReportItem report = child.getValue(ReportItem.class);
                    if (report != null) {
                        addReportRow(report);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvNoReports.setVisibility(View.VISIBLE);
            }
        });
    }

    private void addReportRow(ReportItem report) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.item_report, llMyReports, false);

        TextView tvType = row.findViewById(R.id.tvReportType);
        TextView tvStatus = row.findViewById(R.id.tvReportStatus);
        TextView tvDescription = row.findViewById(R.id.tvReportDescription);
        TextView tvDate = row.findViewById(R.id.tvReportDate);

        tvType.setText(report.type);
        tvStatus.setText(report.status);
        tvDescription.setText(report.description);

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault());
        tvDate.setText(sdf.format(report.timestamp));

        llMyReports.addView(row);
    }

    // Simple data holder — Firebase can automatically convert this to/from a database node
    public static class ReportItem {
        public String type;
        public String description;
        public String status;
        public long timestamp;

        // Empty constructor required by Firebase for deserialization
        public ReportItem() {}

        public ReportItem(String type, String description, String status, long timestamp) {
            this.type = type;
            this.description = description;
            this.status = status;
            this.timestamp = timestamp;
        }
    }
}