package com.disha.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.util.HashMap;
import java.util.Map;

public class fragment_learn extends Fragment {

    private static final String[] TOPIC_KEYS = {
            "awareness", "phishing", "passwords", "privacy", "malware", "webSecurity"
    };

    private TextView tvLearnProgress;

    // topicKey -> its checkmark ImageView, so we can update it after marking complete
    private final Map<String, ImageView> checkViews = new HashMap<>();
    // topicKey -> completed?
    private final Map<String, Boolean> progressMap = new HashMap<>();

    private DatabaseReference progressRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_learn, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLearnProgress = view.findViewById(R.id.tvLearnProgress);

        CardView cardAwareness = view.findViewById(R.id.cardAwareness);
        CardView cardPhishing = view.findViewById(R.id.cardPhishing);
        CardView cardPasswords = view.findViewById(R.id.cardPasswords);
        CardView cardPrivacy = view.findViewById(R.id.cardPrivacy);
        CardView cardMalware = view.findViewById(R.id.cardMalware);
        CardView cardWebSecurity = view.findViewById(R.id.cardWebSecurity);

        checkViews.put("awareness", view.findViewById(R.id.ivCheckAwareness));
        checkViews.put("phishing", view.findViewById(R.id.ivCheckPhishing));
        checkViews.put("passwords", view.findViewById(R.id.ivCheckPasswords));
        checkViews.put("privacy", view.findViewById(R.id.ivCheckPrivacy));
        checkViews.put("malware", view.findViewById(R.id.ivCheckMalware));
        checkViews.put("webSecurity", view.findViewById(R.id.ivCheckWebSecurity));

        for (String key : TOPIC_KEYS) {
            progressMap.put(key, false);
        }

        cardAwareness.setOnClickListener(v -> onTopicTapped("awareness", "Cyber Awareness Basics"));
        cardPhishing.setOnClickListener(v -> onTopicTapped("phishing", "Phishing & Scams"));
        cardPasswords.setOnClickListener(v -> onTopicTapped("passwords", "Password Security"));
        cardPrivacy.setOnClickListener(v -> onTopicTapped("privacy", "Privacy Protection"));
        cardMalware.setOnClickListener(v -> onTopicTapped("malware", "Malware & Viruses"));
        cardWebSecurity.setOnClickListener(v -> onTopicTapped("webSecurity", "Web Security & Safe Practices"));

        setupFirebaseRef();
        loadProgress();
    }

    private void setupFirebaseRef() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            progressRef = null;
            return;
        }
        String uid = currentUser.getUid();
        progressRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("learningProgress");
    }

    private void loadProgress() {
        if (progressRef == null) {
            updateProgressText();
            return;
        }

        progressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (String key : TOPIC_KEYS) {
                    Boolean done = snapshot.child(key).getValue(Boolean.class);
                    boolean isDone = done != null && done;
                    progressMap.put(key, isDone);

                    ImageView checkView = checkViews.get(key);
                    if (checkView != null) {
                        checkView.setVisibility(isDone ? View.VISIBLE : View.GONE);
                    }
                }
                updateProgressText();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // fail safely — just show 0/6, don't crash
                updateProgressText();
            }
        });
    }

    private void onTopicTapped(String key, String displayName) {
        boolean alreadyDone = Boolean.TRUE.equals(progressMap.get(key));

        if (alreadyDone) {
            Toast.makeText(getContext(), displayName + " — lessons coming soon", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressRef == null) {
            Toast.makeText(getContext(), displayName + " — lessons coming soon", Toast.LENGTH_SHORT).show();
            return;
        }

        progressRef.child(key).setValue(true);

        // optimistic local update — no need to re-read from Firebase
        progressMap.put(key, true);
        ImageView checkView = checkViews.get(key);
        if (checkView != null) {
            checkView.setVisibility(View.VISIBLE);
        }
        updateProgressText();

        Toast.makeText(getContext(), displayName + " marked as completed!", Toast.LENGTH_SHORT).show();
    }

    private void updateProgressText() {
        int completed = 0;
        for (Boolean done : progressMap.values()) {
            if (Boolean.TRUE.equals(done)) completed++;
        }
        tvLearnProgress.setText(completed + "/" + TOPIC_KEYS.length + " topics completed");
    }
}