package com.example.MAD;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileSelfCompanyFragment extends Fragment {
    private RecyclerView RVJob;
    private RecycleviewJobOfferedAdapter adapter;

    private ImageView profilePhotoSelfCom;
    private TextView nameSelfCom, sectorSelfCom, numJobSelf, bioSelfCom;
    private Button btnRetrieveLinkSelf;

    private Uri url;
    private final ArrayList<jobOffered> jobList = new ArrayList<>();

    private TextView rateComp;

    String userEmail = UserSessionManager.getInstance().getUserEmail();
    String sanitizedEmail = userEmail.replace(".", "_");

    DatabaseReference ratingRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_self_company, container, false);

        // Handle window insets for system bars
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rateComp = view.findViewById(R.id.rateComp);

        // Initialize buttons and their actions
        ImageButton btnSetting = view.findViewById(R.id.btnSettingCompany);
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile2);

        btnSetting.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.companySettingFragment));
        btnEditProfile.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.companyEditFragment));
        // Initialize RecyclerView and adapter
        RVJob = view.findViewById(R.id.RVJob);
        adapter = new RecycleviewJobOfferedAdapter(getContext(), jobList);
        RVJob.setAdapter(adapter);
        RVJob.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child("recruiter").child(sanitizedEmail);
        ratingRef = userRef.child("rating");
        fetchAndDisplayAverageRating();
        nameSelfCom = view.findViewById(R.id.nameSelfCom);
        nameSelfCom.setText(UserSessionManager.getInstance().getUserName());

        sectorSelfCom = view.findViewById(R.id.sectorSelfCom);
        sectorSelfCom.setText(UserSessionManager.getInstance().getSector());

        bioSelfCom = view.findViewById(R.id.bioSelfCom);
        accessBio(userRef);

        profilePhotoSelfCom = view.findViewById(R.id.profilePhotoSelfCom);
        accessProfile(userRef);

        numJobSelf = view.findViewById(R.id.numJobSelf);
        // Load data into RecyclerView
        setUpJob();

        btnRetrieveLinkSelf = view.findViewById(R.id.btnRetrieveLinkSelf);
        accessLink(userRef);

        btnRetrieveLinkSelf.setOnClickListener(v -> {
            // Validate the URL format
            if (url != null && Patterns.WEB_URL.matcher(url.toString()).matches()) {
                // If the URL is valid, launch it in a browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, url);
                startActivity(browserIntent);
            } else {
                // If the URL is invalid, show a toast message
                Toast.makeText(requireContext(), "Invalid URL. Please check the URL and try again.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void accessBio(DatabaseReference userRef) {
        userRef.child("bio").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String existingBio = dataSnapshot.getValue(String.class);
                    bioSelfCom.setText(existingBio);
                } else {
                    bioSelfCom.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing bio: " + databaseError.getMessage());
            }
        });
    }

    private void accessProfile(DatabaseReference userRef) {
        userRef.child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profileImageBase64 = dataSnapshot.getValue(String.class);
                    if (profileImageBase64 != null) {
                        byte[] imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT);
                        Bitmap profileImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        profilePhotoSelfCom.setImageBitmap(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing profile photo: " + databaseError.getMessage());
            }
        });
    }

    private void setUpJob() {
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("users")
                .child("recruiter")
                .child(sanitizedEmail)
                .child("postedJobs");

        // Clear the job list before fetching new data to avoid duplication
        jobList.clear();
        adapter.notifyDataSetChanged();

        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot jobIdSnapshot : dataSnapshot.getChildren()) {
                        String jobId = jobIdSnapshot.getValue(String.class);

                        if (jobId != null) {
                            fetchJobDetails(jobId);
                        } else {
                            Log.w("ProfileSelfCompany", "Invalid job ID encountered.");
                        }
                    }
                } else {
                    Log.d("ProfileSelfCompany", "No jobs found for this user.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ProfileSelfCompany", "Error loading posted jobs: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Failed to load posted jobs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchJobDetails(String jobId) {
        DatabaseReference jobRef = FirebaseDatabase.getInstance().getReference("jobs").child(jobId);

        jobRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot jobSnapshot) {
                if (jobSnapshot.exists()) {
                    jobOffered job = jobSnapshot.getValue(jobOffered.class);

                    if (job != null) {
                        jobList.add(job);
                        adapter.notifyItemInserted(jobList.size() - 1); // Notify adapter about the new item
                        numJobSelf.setText(String.valueOf(jobList.size()));
                    } else {
                        Log.w("ProfileSelfCompany", "Job data is null for ID: " + jobId);
                    }
                } else {
                    Log.w("ProfileSelfCompany", "Job not found for ID: " + jobId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ProfileSelfCompany", "Error fetching job details: " + databaseError.getMessage());
            }
        });
    }

    private void accessLink(DatabaseReference userRef) {
        userRef.child("link").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String existingLink = dataSnapshot.getValue(String.class);
                    if (existingLink != null) {
                        // Convert the String to Uri
                        url = Uri.parse(existingLink);
                        btnRetrieveLinkSelf.setText(existingLink);
                    }
                } else {
                    btnRetrieveLinkSelf.setText("No Website");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing link: " + databaseError.getMessage());
            }
        });
    }

    private void fetchAndDisplayAverageRating() {
        // Reference to the rating node under the user

        // Fetch ratings and calculate the average
        ratingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float totalScore = 0;
                int ratingCount = 0;

                // Loop through each rating entry
                for (DataSnapshot ratingSnapshot : dataSnapshot.getChildren()) {
                    Float score = ratingSnapshot.child("score").getValue(Float.class);
                    if (score != null) {
                        totalScore += score;
                        ratingCount++;
                    }
                }

                // Calculate and display the average rating
                displayAverageRating(totalScore, ratingCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error fetching ratings: " + databaseError.getMessage());
            }
        });
    }

    private void displayAverageRating(float totalScore, int ratingCount) {
        if (ratingCount > 0) {
            // Calculate average score
            float averageScore = totalScore / ratingCount;
            rateComp.setText(String.format("%.1f", averageScore));
        } else {
            rateComp.setText("0");
        }
    }

}