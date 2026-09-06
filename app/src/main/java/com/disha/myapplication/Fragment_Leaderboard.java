package com.disha.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Fragment_Leaderboard extends Fragment {

    private TextView tvLoadingLeaderboard;
    private LinearLayout llLeaderboardRows;

    private String currentUid;

    private final List<LeaderboardEntry> entries = new ArrayList<>();
    private int totalUsers = 0;
    private int scoresReceived = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLoadingLeaderboard = view.findViewById(R.id.tvLoadingLeaderboard);
        llLeaderboardRows = view.findViewById(R.id.llLeaderboardRows);

        currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) return;

                totalUsers = (int) snapshot.getChildrenCount();

                if (totalUsers == 0) {
                    tvLoadingLeaderboard.setText("No users found");
                    return;
                }

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String uid = userSnapshot.getKey();
                    String name = userSnapshot.child("name").getValue(String.class);
                    String displayName = (name != null && !name.isEmpty()) ? name : "User";

                    if (uid == null) {
                        onOneScoreReceived(); // still count it so we don't wait forever
                        continue;
                    }

                    SecurityScoreCalculator.calculateSecurityScore(uid, score -> {
                        entries.add(new LeaderboardEntry(uid, displayName, score));
                        onOneScoreReceived();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvLoadingLeaderboard.setText("Unable to load leaderboard");
            }
        });
    }

    private void onOneScoreReceived() {
        scoresReceived++;
        if (scoresReceived >= totalUsers) {
            renderLeaderboard();
        }
    }

    private void renderLeaderboard() {
        tvLoadingLeaderboard.setVisibility(View.GONE);
        llLeaderboardRows.removeAllViews();

        entries.sort((a, b) -> b.score - a.score); // descending

        int rank = 1;
        for (LeaderboardEntry entry : entries) {
            addLeaderboardRow(rank, entry);
            rank++;
        }
    }

    private void addLeaderboardRow(int rank, LeaderboardEntry entry) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.item_leaderboard_row, llLeaderboardRows, false);

        CardView cardRow = row.findViewById(R.id.cardRow);
        TextView tvRank = row.findViewById(R.id.tvRank);
        TextView tvName = row.findViewById(R.id.tvLeaderboardName);
        TextView tvScore = row.findViewById(R.id.tvLeaderboardScore);

        tvRank.setText(String.valueOf(rank));
        tvName.setText(entry.name);
        tvScore.setText(String.valueOf(entry.score));

        boolean isCurrentUser = entry.uid.equals(currentUid);
        if (isCurrentUser) {
            cardRow.setCardBackgroundColor(getResources().getColor(R.color.surface));
            tvName.setText(entry.name + " (You)");
        } else {
            cardRow.setCardBackgroundColor(getResources().getColor(R.color.card_background));
        }

        llLeaderboardRows.addView(row);
    }

    // Simple holder for one leaderboard entry
    private static class LeaderboardEntry {
        String uid;
        String name;
        int score;

        LeaderboardEntry(String uid, String name, int score) {
            this.uid = uid;
            this.name = name;
            this.score = score;
        }
    }
}