package com.disha.myapplication;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class Fragment_Protect extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_protect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CardView cardPasswordAnalyzer = view.findViewById(R.id.cardPasswordAnalyzer);
        CardView cardPasswordGenerator = view.findViewById(R.id.cardPasswordGenerator);
        CardView cardPasswordVault = view.findViewById(R.id.cardPasswordVault);
        CardView cardEmergencySos = view.findViewById(R.id.cardEmergencySos);

        // 🔵 CHANGED — Toast ki jagah ab Fragment_PasswordAnalyzer khulta hai
        cardPasswordAnalyzer.setOnClickListener(v ->
                ((Dashboard) requireActivity()).openFragment(new Fragment_PasswordAnalyzer(), "password_analyzer"));

        cardPasswordGenerator.setOnClickListener(v ->
                ((Dashboard) requireActivity()).openFragment(new Fragment_PasswordGenerator(), "password_generator"));
        cardPasswordVault.setOnClickListener(v -> showComingSoon("Password Vault"));
        cardEmergencySos.setOnClickListener(v -> showComingSoon("Emergency SOS"));
    }

    private void showComingSoon(String tool) {
        Toast.makeText(requireContext(), tool + " — coming soon", Toast.LENGTH_SHORT).show();
    }
}