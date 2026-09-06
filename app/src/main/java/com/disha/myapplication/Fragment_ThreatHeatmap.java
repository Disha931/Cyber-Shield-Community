
package com.disha.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Fragment_ThreatHeatmap extends Fragment {

    private TextView tvTotalReports;
    private TextView tvMostCommon;
    private TextView tvNoData;
    private LinearLayout llBars;

    private DatabaseReference threatReportsRef;
    private ValueEventListener threatListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_threat_heatmap, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalReports = view.findViewById(R.id.tvTotalReports);
        tvMostCommon = view.findViewById(R.id.tvMostCommon);
        tvNoData = view.findViewById(R.id.tvNoData);
        llBars = view.findViewById(R.id.llBars);

        threatReportsRef = FirebaseDatabase.getInstance().getReference("ThreatReports");

        attachListener();
    }

    private void attachListener() {
        threatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) return;

                Map<String, Integer> countsByType = new LinkedHashMap<>();
                int total = 0;

                for (DataSnapshot child : snapshot.getChildren()) {
                    String type = child.child("type").getValue(String.class);
                    if (type == null) continue;

                    countsByType.put(type, countsByType.getOrDefault(type, 0) + 1);
                    total++;
                }

                renderResults(countsByType, total);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // fail safely — leave the screen showing 0 reports
            }
        };

        threatReportsRef.addValueEventListener(threatListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (threatReportsRef != null && threatListener != null) {
            threatReportsRef.removeEventListener(threatListener);
        }
    }

    private void renderResults(Map<String, Integer> countsByType, int total) {
        llBars.removeAllViews();

        tvTotalReports.setText(total + (total == 1 ? " report" : " reports") + " submitted community-wide");

        if (countsByType.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            tvMostCommon.setText("Most reported: —");
            return;
        }

        tvNoData.setVisibility(View.GONE);

        // sort types by count, descending, so the biggest bar shows first
        List<Map.Entry<String, Integer>> sortedEntries = countsByType.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .collect(Collectors.toList());

        int maxCount = sortedEntries.get(0).getValue();
        String topType = sortedEntries.get(0).getKey();
        tvMostCommon.setText("Most reported: " + topType);

        for (Map.Entry<String, Integer> entry : sortedEntries) {
            addBarRow(entry.getKey(), entry.getValue(), maxCount);
        }
    }

    private void addBarRow(String type, int count, int maxCount) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.item_threat_bar, llBars, false);

        TextView tvLabel = row.findViewById(R.id.tvBarLabel);
        TextView tvCount = row.findViewById(R.id.tvBarCount);
        View barFill = row.findViewById(R.id.viewBarFill);
        View barSpacer = row.findViewById(R.id.viewBarSpacer);

        tvLabel.setText(type);
        tvCount.setText(String.valueOf(count));

        LinearLayout.LayoutParams fillParams = (LinearLayout.LayoutParams) barFill.getLayoutParams();
        fillParams.weight = count;
        barFill.setLayoutParams(fillParams);

        LinearLayout.LayoutParams spacerParams = (LinearLayout.LayoutParams) barSpacer.getLayoutParams();
        spacerParams.weight = Math.max(0, maxCount - count);
        barSpacer.setLayoutParams(spacerParams);

        llBars.addView(row);
    }
}