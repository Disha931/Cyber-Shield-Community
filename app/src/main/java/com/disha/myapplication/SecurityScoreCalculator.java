package com.disha.myapplication;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SecurityScoreCalculator {

    private static final int BASE_SCORE = 50;
    private static final int POINTS_PER_TOPIC = 5;
    private static final int POINTS_PER_REPORT = 3;
    private static final int MAX_SCORE = 100;

    public interface ScoreCallback {
        void onScoreReady(int score);
    }

    // Reads topics first, then reports, then computes the final score
    public static void calculateSecurityScore(String uid, ScoreCallback callback) {
        if (uid == null) {
            callback.onScoreReady(BASE_SCORE);
            return;
        }

        DatabaseReference progressRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(uid).child("learningProgress");

        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot progressSnapshot) {
                int topicsCompleted = 0;
                for (DataSnapshot child : progressSnapshot.getChildren()) {
                    Boolean done = child.getValue(Boolean.class);
                    if (Boolean.TRUE.equals(done)) topicsCompleted++;
                }

                // Make topicsCompleted effectively final for inner class
                final int finalTopicsCompleted = topicsCompleted;

                // now read reports, using topicsCompleted captured above
                DatabaseReference reportsRef = FirebaseDatabase.getInstance()
                        .getReference("Users").child(uid).child("myReports");

                reportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot reportsSnapshot) {
                        long reportsSubmitted = reportsSnapshot.getChildrenCount();

                        int score = BASE_SCORE
                                + (finalTopicsCompleted * POINTS_PER_TOPIC)
                                + ((int) reportsSubmitted * POINTS_PER_REPORT);

                        score = Math.min(MAX_SCORE, score);
                        callback.onScoreReady(score);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onScoreReady(BASE_SCORE);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onScoreReady(BASE_SCORE);
            }
        });
    }
}
