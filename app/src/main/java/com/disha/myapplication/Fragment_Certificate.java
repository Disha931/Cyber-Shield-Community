package com.disha.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class Fragment_Certificate extends Fragment {

    private TextView tvCertName;
    private TextView tvCertDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_certificate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCertName = view.findViewById(R.id.tvCertName);
        tvCertDate = view.findViewById(R.id.tvCertDate);

        loadCertificateDetails();
    }

    private void loadCertificateDetails() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                tvCertName.setText(name != null && !name.isEmpty() ? name : "User");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvCertName.setText("User");
            }
        });

        DatabaseReference certRef = userRef.child("certificate").child("dateEarned");
        certRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long timestamp = snapshot.getValue(Long.class);
                if (timestamp != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                    tvCertDate.setText("Awarded on " + sdf.format(timestamp));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // leave default text
            }
        });
    }
}