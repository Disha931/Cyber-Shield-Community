package com.disha.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

public class fragment_Profile extends Fragment {

    private TextView tvProfileName;
    private TextView tvProfileEmail;

    private TextView tvStatTopics;
    private TextView tvStatReports;
    private TextView tvStatPosts;
    private TextView tvStatSecurityScore;

    private CardView cardLogout; // 🔵 NEW

    private String currentUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);

        tvStatTopics = view.findViewById(R.id.tvStatTopics);
        tvStatReports = view.findViewById(R.id.tvStatReports);
        tvStatPosts = view.findViewById(R.id.tvStatPosts);
        tvStatSecurityScore = view.findViewById(R.id.tvStatSecurityScore);

        cardLogout = view.findViewById(R.id.cardLogout); // 🔵 NEW

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = currentUser != null ? currentUser.getUid() : null;

        loadUserProfile();

        SecurityScoreCalculator.calculateSecurityScore(currentUid, score ->
                tvStatSecurityScore.setText(String.valueOf(score)));

        loadTopicsCompleted();
        loadReportsSubmitted();
        loadCommunityPostsCount();

        cardLogout.setOnClickListener(v -> showLogoutConfirmation()); // 🔵 NEW
    }

    private void loadUserProfile() {
        if (currentUid == null) {
            tvProfileName.setText("Not signed in");
            tvProfileEmail.setText("");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    tvProfileName.setText(name != null && !name.isEmpty() ? name : "User");
                    tvProfileEmail.setText(email != null ? email : "");
                } else {
                    tvProfileName.setText("User");
                    tvProfileEmail.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvProfileName.setText("Unable to load profile");
                tvProfileEmail.setText("");
            }
        });
    }

    private void loadTopicsCompleted() {
        if (currentUid == null) return;

        DatabaseReference progressRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(currentUid).child("learningProgress");

        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    Boolean done = child.getValue(Boolean.class);
                    if (Boolean.TRUE.equals(done)) count++;
                }
                tvStatTopics.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvStatTopics.setText("0");
            }
        });
    }

    private void loadReportsSubmitted() {
        if (currentUid == null) return;

        DatabaseReference reportsRef = FirebaseDatabase.getInstance()
                .getReference("Users").child(currentUid).child("myReports");

        reportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvStatReports.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvStatReports.setText("0");
            }
        });
    }

    private void loadCommunityPostsCount() {
        if (currentUid == null) return;

        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("CommunityPosts");

        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String authorUid = child.child("authorUid").getValue(String.class);
                    if (currentUid.equals(authorUid)) count++;
                }
                tvStatPosts.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvStatPosts.setText("0");
            }
        });
    }

    // 🔵 NEW
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // 🔵 NEW
    private void performLogout() {
        FirebaseAuth.getInstance().signOut();

        // 🔴 REPLACE "LoginActivityPlaceholder" WITH YOUR ACTUAL LOGIN CLASS NAME
        Intent intent = new Intent(requireContext(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}