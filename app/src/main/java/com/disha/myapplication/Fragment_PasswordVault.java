package com.disha.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Fragment_PasswordVault extends Fragment {

    private EditText etSiteName, etUsername, etVaultPassword;
    private LinearLayout llVaultEntries;
    private TextView tvNoEntries;

    private SharedPreferences encryptedPrefs;
    private String vaultKey; // scoped per logged-in user

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_password_vault, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSiteName = view.findViewById(R.id.etSiteName);
        etUsername = view.findViewById(R.id.etUsername);
        etVaultPassword = view.findViewById(R.id.etVaultPassword);
        llVaultEntries = view.findViewById(R.id.llVaultEntries);
        tvNoEntries = view.findViewById(R.id.tvNoEntries);

        CardView cardSaveEntry = view.findViewById(R.id.cardSaveEntry);

        setupEncryptedStorage();

        cardSaveEntry.setOnClickListener(v -> saveEntry());

        loadEntries();
    }

    private void setupEncryptedStorage() {
        try {
            MasterKey masterKey = new MasterKey.Builder(requireContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedPrefs = EncryptedSharedPreferences.create(
                    requireContext(),
                    "vault_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to set up secure storage", Toast.LENGTH_SHORT).show();
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser != null ? currentUser.getUid() : "guest";
        vaultKey = "vault_" + uid;
    }

    private void saveEntry() {
        String site = etSiteName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etVaultPassword.getText().toString();

        if (site.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        List<VaultEntry> entries = readEntries();
        entries.add(new VaultEntry(site, username, password));
        writeEntries(entries);

        etSiteName.setText("");
        etUsername.setText("");
        etVaultPassword.setText("");

        Toast.makeText(getContext(), "Saved to vault", Toast.LENGTH_SHORT).show();
        loadEntries();
    }

    private void loadEntries() {
        List<VaultEntry> entries = readEntries();
        llVaultEntries.removeAllViews();

        if (entries.isEmpty()) {
            tvNoEntries.setVisibility(View.VISIBLE);
            return;
        }

        tvNoEntries.setVisibility(View.GONE);

        for (int i = 0; i < entries.size(); i++) {
            addEntryRow(entries.get(i), i);
        }
    }

    private void addEntryRow(VaultEntry entry, int index) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.item_vault_entry, llVaultEntries, false);

        TextView tvSite = row.findViewById(R.id.tvVaultSite);
        TextView tvUsername = row.findViewById(R.id.tvVaultUsername);
        TextView tvPassword = row.findViewById(R.id.tvVaultPassword);
        ImageView ivToggleReveal = row.findViewById(R.id.ivToggleReveal);
        ImageView ivDeleteEntry = row.findViewById(R.id.ivDeleteEntry);

        tvSite.setText(entry.siteName);
        tvUsername.setText(entry.username);
        tvPassword.setText("••••••••");
        tvPassword.setTag(entry.password); // real password hidden in the tag until revealed

        final boolean[] isRevealed = {false};

        ivToggleReveal.setOnClickListener(v -> {
            isRevealed[0] = !isRevealed[0];
            if (isRevealed[0]) {
                tvPassword.setText((String) tvPassword.getTag());
            } else {
                tvPassword.setText("••••••••");
            }
        });

        ivDeleteEntry.setOnClickListener(v -> {
            List<VaultEntry> entries = readEntries();
            if (index < entries.size()) {
                entries.remove(index);
                writeEntries(entries);
                loadEntries(); // rebuild the whole list so indices stay correct
                Toast.makeText(getContext(), "Entry deleted", Toast.LENGTH_SHORT).show();
            }
        });

        llVaultEntries.addView(row);
    }

    // ---- JSON <-> List conversion helpers ----

    private List<VaultEntry> readEntries() {
        List<VaultEntry> entries = new ArrayList<>();
        if (encryptedPrefs == null) return entries;

        String json = encryptedPrefs.getString(vaultKey, "[]");

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                entries.add(new VaultEntry(
                        obj.getString("siteName"),
                        obj.getString("username"),
                        obj.getString("password")
                ));
            }
        } catch (JSONException e) {
            // corrupted or empty data — just return an empty list
        }

        return entries;
    }

    private void writeEntries(List<VaultEntry> entries) {
        if (encryptedPrefs == null) return;

        JSONArray array = new JSONArray();
        try {
            for (VaultEntry entry : entries) {
                JSONObject obj = new JSONObject();
                obj.put("siteName", entry.siteName);
                obj.put("username", entry.username);
                obj.put("password", entry.password);
                array.put(obj);
            }
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Error saving entry", Toast.LENGTH_SHORT).show();
            return;
        }

        encryptedPrefs.edit().putString(vaultKey, array.toString()).apply();
    }
}