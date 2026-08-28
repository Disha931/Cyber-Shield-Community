package com.disha.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Dashboard extends AppCompatActivity {

    private TextView tvUserName;
    private TextView tvSecurityScore;
    private ProgressBar progressSecurityScore;

    private CardView cardLearn, cardDetect, cardReport, cardCommunity;
    private CardView cardProtect;
    private CardView cardThreat1, cardThreat2;
    private CardView cardNews1, cardNews2;

    private BottomNavigationView bottomNavigation;

    private NestedScrollView dashboardContent;
    private FrameLayout fragmentContainer;

    // ❌ REMOVED — private static final int CURRENT_SECURITY_SCORE = 78;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        setupSecurityScore();
        setupQuickActions();
        setupThreatAlerts();
        setupNewsCards();
        setupBottomNavigation();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    fragmentContainer.setVisibility(View.GONE);
                    dashboardContent.setVisibility(View.VISIBLE);
                    bottomNavigation.setSelectedItemId(R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvSecurityScore = findViewById(R.id.tvSecurityScore);
        progressSecurityScore = findViewById(R.id.progressSecurityScore);

        cardLearn = findViewById(R.id.cardLearn);
        cardDetect = findViewById(R.id.cardDetect);
        cardReport = findViewById(R.id.cardReport);
        cardCommunity = findViewById(R.id.cardCommunity);
        cardProtect = findViewById(R.id.cardProtect);

        cardThreat1 = findViewById(R.id.cardThreat1);
        cardThreat2 = findViewById(R.id.cardThreat2);

        cardNews1 = findViewById(R.id.cardNews1);
        cardNews2 = findViewById(R.id.cardNews2);

        CardView cardSecurityTip = findViewById(R.id.cardSecurityTip);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        dashboardContent = findViewById(R.id.dashboardContent);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        loadUserName();
    }

    private void loadUserName() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            tvUserName.setText("Hello, User 👋");
            return;
        }

        String uid = currentUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null && !name.isEmpty()) {
                        tvUserName.setText("Hello, " + name + " 👋");
                    } else {
                        tvUserName.setText("Hello, User 👋");
                    }
                } else {
                    tvUserName.setText("Hello, User 👋");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvUserName.setText("Hello, User 👋");
            }
        });
    }

    // 🔵 CHANGED — ab Firebase se calculate hota hai, hardcoded value nahi
    private void setupSecurityScore() {
        progressSecurityScore.setMax(100);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : null;

        SecurityScoreCalculator.calculateSecurityScore(uid, score -> {
            progressSecurityScore.setProgress(score);
            tvSecurityScore.setText(score + "\n/100");
        });
    }

    private void setupQuickActions() {
        cardLearn.setOnClickListener(v -> openFragment(new fragment_learn(), "learn"));
        cardDetect.setOnClickListener(v -> openFragment(new fragment_Detect(), "detect"));
        cardProtect.setOnClickListener(v -> openFragment(new Fragment_Protect(), "protect"));
        cardReport.setOnClickListener(v -> openFragment(new fragment_Report(), "report"));
        cardCommunity.setOnClickListener(v -> openFragment(new Fragment_Community(), "community"));
    }

    private void setupThreatAlerts() {
        cardThreat1.setOnClickListener(v ->
                Toast.makeText(this, "Threat detail: Suspicious Link Detected", Toast.LENGTH_SHORT).show());

        cardThreat2.setOnClickListener(v ->
                Toast.makeText(this, "Threat detail: Unusual Login Attempt", Toast.LENGTH_SHORT).show());
    }

    private void setupNewsCards() {
        cardNews1.setOnClickListener(v ->
                Toast.makeText(this, "Opening article: 5 Signs of a Phishing Email", Toast.LENGTH_SHORT).show());

        cardNews2.setOnClickListener(v ->
                Toast.makeText(this, "Opening article: New UPI Fraud Pattern Reported", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener((@NonNull android.view.MenuItem item) -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true; // already on Dashboard
            } else if (id == R.id.nav_learn) {
                openFragment(new fragment_learn(), "learn");
                return true;
            } else if (id == R.id.nav_report) {
                openFragment(new fragment_Report(), "report");
                return true;
            } else if (id == R.id.nav_community) {
                openFragment(new Fragment_Community(), "community");
                return true;
            } else if (id == R.id.nav_profile) {
                openFragment(new fragment_Profile(), "profile");
                return true;
            }
            return false;
        });
    }

    public void openFragment(Fragment fragment, String tag) {
        dashboardContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(tag)
                .commit();
    }
}