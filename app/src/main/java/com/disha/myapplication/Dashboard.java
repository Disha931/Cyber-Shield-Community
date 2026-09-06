package com.disha.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.Query;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessaging;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    private TextView tvUserName;
    private TextView tvSecurityScore;
    private ProgressBar progressSecurityScore;

    private CardView cardLearn, cardDetect, cardReport, cardCommunity;
    private CardView cardProtect;
    private LinearLayout llThreatFeed;
    private TextView tvNoThreats;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private BottomNavigationView bottomNavigation;

    private NestedScrollView dashboardContent;
    private LinearLayout llNewsFeed;
    private TextView tvNoNews;
    private FrameLayout fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    // whether granted or not, we proceed silently — notifications are optional, not blocking
                });

        requestNotificationPermissionIfNeeded();
        saveFcmToken();

        initViews();
        setupSecurityScore();
        setupQuickActions();
        setupBottomNavigation();
        loadNewsFeed();


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
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean alreadyGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;

            if (!alreadyGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        // on Android 12 and below, no runtime permission is needed for notifications
    }

    private void saveFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) return;

            String token = task.getResult();
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && token != null) {
                FirebaseDatabase.getInstance().getReference("Users")
                        .child(currentUser.getUid())
                        .child("fcmToken")
                        .setValue(token);
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


        // 🔵 FIXED — was a broken line before, now a clean findViewById
        llNewsFeed = findViewById(R.id.llNewsFeed);
        tvNoNews = findViewById(R.id.tvNoNews);
        // 🔵 NEW
        llThreatFeed = findViewById(R.id.llThreatFeed);
        tvNoThreats = findViewById(R.id.tvNoThreats);

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

    private void setupSecurityScore() {
        progressSecurityScore.setMax(100);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : null;

        SecurityScoreCalculator.calculateSecurityScore(uid, score -> {
            progressSecurityScore.setProgress(score);
            String scoreText = score + "\n/100";
            tvSecurityScore.setText(scoreText);
        });
    }
    private void loadThreatAlerts() {
        DatabaseReference threatReportsRef = FirebaseDatabase.getInstance().getReference("ThreatReports");

        threatReportsRef.orderByChild("timestamp").limitToLast(3)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        llThreatFeed.removeAllViews();

                        if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                            tvNoThreats.setVisibility(View.VISIBLE);
                            return;
                        }

                        tvNoThreats.setVisibility(View.GONE);

                        List<ThreatAlertEntry> alerts = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String type = child.child("type").getValue(String.class);
                            Long timestamp = child.child("timestamp").getValue(Long.class);
                            if (type != null && timestamp != null) {
                                alerts.add(new ThreatAlertEntry(type, timestamp));
                            }
                        }

                        // limitToLast() returns oldest-first among the last N — reverse for newest-first
                        Collections.reverse(alerts);

                        for (ThreatAlertEntry alert : alerts) {
                            addThreatAlertRow(alert);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvNoThreats.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void addThreatAlertRow(ThreatAlertEntry alert) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_threat_alert, llThreatFeed, false);

        TextView tvTitle = row.findViewById(R.id.tvAlertTitle);
        TextView tvDescription = row.findViewById(R.id.tvAlertDescription);
        TextView tvTime = row.findViewById(R.id.tvAlertTime);
        TextView tvBadge = row.findViewById(R.id.tvAlertBadge);

        tvTitle.setText(alert.type + " Reported");
        tvDescription.setText(descriptionForType(alert.type));
        tvTime.setText(timeAgo(alert.timestamp));

        if (isHighSeverity(alert.type)) {
            tvBadge.setText("High");
            tvBadge.setBackgroundResource(R.drawable.bg_severity_high);
            tvTitle.setTextColor(getResources().getColor(R.color.Dangerred));
        } else {
            tvBadge.setText("Medium");
            tvBadge.setBackgroundResource(R.drawable.bg_severity_medium);
            tvTitle.setTextColor(getResources().getColor(R.color.accent));
        }

        llThreatFeed.addView(row);
    }

    private String descriptionForType(String type) {
        switch (type) {
            case "Phishing":
                return "A phishing attempt was reported by a community member.";
            case "Scam Call":
                return "A scam phone call was reported nearby.";
            case "Fake Website":
                return "A fraudulent website was flagged by the community.";
            case "Financial Fraud":
                return "A financial fraud incident was reported.";
            default:
                return "A security incident was reported by a community member.";
        }
    }

    private boolean isHighSeverity(String type) {
        return type.equals("Phishing") || type.equals("Financial Fraud");
    }

    private String timeAgo(long timestamp) {
        long diffMillis = System.currentTimeMillis() - timestamp;
        long minutes = diffMillis / (60 * 1000);
        long hours = minutes / 60;
        long days = hours / 24;

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        return days + (days == 1 ? " day ago" : " days ago");
    }

    // Simple holder — reused just for rendering, not stored anywhere new
    private static class ThreatAlertEntry {
        String type;
        long timestamp;

        ThreatAlertEntry(String type, long timestamp) {
            this.type = type;
            this.timestamp = timestamp;
        }
    }

    private void setupQuickActions() {
        cardLearn.setOnClickListener(v -> openFragment(new fragment_learn(), "learn"));
        cardDetect.setOnClickListener(v -> openFragment(new fragment_Detect(), "detect"));
        cardProtect.setOnClickListener(v -> openFragment(new Fragment_Protect(), "protect"));
        cardReport.setOnClickListener(v -> openFragment(new fragment_Report(), "report"));
        cardCommunity.setOnClickListener(v -> openFragment(new Fragment_Community(), "community"));
    }

    private void loadNewsFeed() {
        DatabaseReference newsRef = FirebaseDatabase.getInstance().getReference("CyberNews");

        newsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                llNewsFeed.removeAllViews();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    tvNoNews.setVisibility(View.VISIBLE);
                    return;
                }

                tvNoNews.setVisibility(View.GONE);

                List<NewsItem> newsList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    NewsItem item = child.getValue(NewsItem.class);
                    if (item != null) newsList.add(item);
                }

                // newest first — Firebase push() keys come back oldest-first by default
                Collections.reverse(newsList);

                for (NewsItem item : newsList) {
                    addNewsRow(item);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvNoNews.setVisibility(View.VISIBLE);
            }
        });
    }

    private void addNewsRow(NewsItem item) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_news, llNewsFeed, false);

        TextView tvTitle = row.findViewById(R.id.tvNewsTitle);
        TextView tvDescription = row.findViewById(R.id.tvNewsDescription);
        TextView tvDate = row.findViewById(R.id.tvNewsDate);

        tvTitle.setText(item.title);
        tvDescription.setText(item.description);
        tvDate.setText(item.date);

        llNewsFeed.addView(row);
    }

    // Data holder — Firebase converts this to/from a database node automatically
    public static class NewsItem {
        public String title;
        public String description;
        public String date;

        public NewsItem() {} // required by Firebase

        public NewsItem(String title, String description, String date) {
            this.title = title;
            this.description = description;
            this.date = date;
        }
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