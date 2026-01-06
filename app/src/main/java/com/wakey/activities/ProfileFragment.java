package com.wakey.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
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

        View c1 = view.findViewById(R.id.challengeNoSnooze);
        View c2 = view.findViewById(R.id.challengeEarlyRiser);

        View b1 = view.findViewById(R.id.badgeNoSnooze);
        View b2 = view.findViewById(R.id.badgeEarlyRiser);

        loadChallengeProgress(c1, c2, b1, b2);

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


    private long getTodaySixAmMillis() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 6);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void loadChallengeProgress(View c1, View c2, View b1, View b2) {
        new Thread(() -> {
            WakeyDatabase db = WakeyDatabase.getInstance(requireContext());

            long start7days = daysAgo(6);
            long sixAm = getTodaySixAmMillis();

            int noSnoozeDays = db.wakeHistoryDao().countNoSnoozeDays(start7days);
            int earlyDays = db.wakeHistoryDao().countEarlyRiserDays(start7days, sixAm);

            requireActivity().runOnUiThread(() -> {
                setupChallenge(
                        c1,
                        getString(R.string.challenge_no_snooze_title),
                        getString(R.string.challenge_no_snooze_desc),
                        noSnoozeDays,
                        7
                );

                setupChallenge(
                        c2,
                        getString(R.string.challenge_early_title),
                        getString(R.string.challenge_early_desc),
                        earlyDays,
                        5
                );

                // badges unlock only if challenge completed
                setBadge(b1, getString(R.string.badge_no_snooze), noSnoozeDays >= 7,
                        "Complete the 7-Day No Snooze challenge.");

                setBadge(b2, getString(R.string.badge_morning_person), earlyDays >= 5,
                        "Wake up before 6:00 AM on 5 days.");
            });
        }).start();
    }


    private void setupChallenge(View challengeView, String title, String desc, int current, int total) {
        TextView tvTitle = challengeView.findViewById(R.id.tvChallengeTitle);
        TextView tvDesc = challengeView.findViewById(R.id.tvChallengeDesc);
        TextView tvProgress = challengeView.findViewById(R.id.tvProgressText);
        android.widget.ProgressBar progressBar = challengeView.findViewById(R.id.progressBar);

        tvTitle.setText(title);
        tvDesc.setText(desc);

        tvProgress.setText(current + " / " + total + " days");

        int percent = (int) ((current / (float) total) * 100f);
        progressBar.setProgress(percent);
    }


    private void setBadge(View badgeView, String title, boolean unlocked, String howToUnlock) {
        TextView tv = badgeView.findViewById(R.id.tvBadge);
        tv.setText(title);

        badgeView.setAlpha(unlocked ? 1f : 0.3f);

        badgeView.setOnClickListener(v -> {
            String msg = unlocked
                    ? "Unlocked ✅"
                    : howToUnlock;

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    private long daysAgo(int days) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -days);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }




}
