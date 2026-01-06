package com.wakey.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.wakey.R;
import com.wakey.database.UserProfileEntity;
import com.wakey.database.WakeyDatabase;

public class ProfileFragment extends Fragment {

    private EditText editName;
    private TextView textGreeting;
    private MaterialButton btnSave;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editName = view.findViewById(R.id.editName);
        textGreeting = view.findViewById(R.id.textGreeting);
        btnSave = view.findViewById(R.id.btnSaveProfile);

        // Save when user presses DONE on keyboard
        editName.setOnEditorActionListener((v, actionId, event) -> {
            saveProfile();
            return true;
        });

        // Save automatically when focus is lost
        editName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveProfile();
            }
        });


        loadProfile();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        new Thread(() -> {
            UserProfileEntity profile =
                    WakeyDatabase.getInstance(requireContext())
                            .userProfileDao()
                            .get();

            if (profile != null && profile.name != null && !profile.name.isEmpty()) {
                requireActivity().runOnUiThread(() -> {
                    editName.setText(profile.name);
                    textGreeting.setText(
                            getString(R.string.greeting, profile.name)
                    );
                });
            }
        }).start();
    }

    private void saveProfile() {
        String name = editName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(
                    getContext(),
                    getString(R.string.enter_name_warning),
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        new Thread(() -> {
            UserProfileEntity profile = new UserProfileEntity();
            profile.name = name;

            WakeyDatabase.getInstance(requireContext())
                    .userProfileDao()
                    .save(profile);

            requireActivity().runOnUiThread(() -> {
                textGreeting.setText(
                        getString(R.string.greeting, name)
                );
                Toast.makeText(
                        getContext(),
                        getString(R.string.profile_saved),
                        Toast.LENGTH_SHORT
                ).show();
            });
        }).start();
    }
}
