package com.example.MAD;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.widget.TextView;

public class SignUp extends AppCompatActivity {

    private TextView jobSeekerButton;
    private TextView recruiterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize toggle buttons
        jobSeekerButton = findViewById(R.id.jobSeekerButton);
        recruiterButton = findViewById(R.id.recruiterButton);

        // Load JobSeekerSignUpFragment by default
        switchFragment(new JobSeekerSignUpFragment());

        // Set initial background for buttons (Job Seeker is selected)
        setButtonBackground(jobSeekerButton, recruiterButton);

        // Switch to Job Seeker Fragment
        jobSeekerButton.setOnClickListener(v -> {
            // Switch the fragment
            switchFragment(new JobSeekerSignUpFragment());

            // Update button backgrounds
            setButtonBackground(jobSeekerButton, recruiterButton);
        });

        // Switch to Recruiter Fragment
        recruiterButton.setOnClickListener(v -> {
            // Switch the fragment
            switchFragment(new RecruiterSignUpFragment());

            // Update button backgrounds
            setButtonBackground(recruiterButton, jobSeekerButton);
        });
    }

    // Helper method to switch fragments
    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .commit();
    }

    // Helper method to update the button backgrounds
    private void setButtonBackground(TextView selectedButton, TextView unselectedButton) {
        // Set the selected button to have the segment_selected background
        selectedButton.setBackgroundResource(R.drawable.segment_selected);

        // Set the unselected button's background to null
        unselectedButton.setBackground(null);
    }
}