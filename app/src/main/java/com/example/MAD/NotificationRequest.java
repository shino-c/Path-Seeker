package com.example.MAD;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationRequest extends Fragment {

    private DatabaseReference ref;
    private ValueEventListener valueEventListener;

    private RecyclerView recyclerView;
    private ApplicationAdapter adapter;
    private List<Application> applicationList;
    private String username,userEmail;

    public NotificationRequest() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_notification, container, false);

        userEmail = UserSessionManager.getInstance().getUserEmail();
        username = userEmail.replace(".", "_");

        // Initialize and set the click listener for the ImageButton
        ImageButton btnBack = view.findViewById(R.id.IBback);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        applicationList = new ArrayList<>();

        // Initialize RecyclerView and set up the adapter
        recyclerView = view.findViewById(R.id.RVapplicant);
        adapter = new ApplicationAdapter(getActivity(), username, applicationList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        fetchApplicants();

        return view;
    }

    private void fetchApplicants() {
        ref = FirebaseDatabase.getInstance().getReference("applications/jobs");

        ref.addValueEventListener(new ValueEventListener() {  // Changed to addValueEventListener to listen for real-time updates
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                applicationList.clear();  // Clear the existing list

                if (snapshot.exists()) {
                    for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                        String jobId = jobSnapshot.getKey();

                        // Iterate through users under a specific job
                        for (DataSnapshot userSnapshot : jobSnapshot.getChildren()) {
                            // First check the status - if it's Success or Rejected, skip this application
                            String status = userSnapshot.child("status").getValue(String.class);
                            if ("Success".equals(status) || "Rejected".equals(status)) {
                                continue;  // Skip this application
                            }

                            Application application = new Application();
                            application.setJobId(jobId);

                            // Extract userId from userSnapshot
                            String userId = userSnapshot.getKey();
                            application.setUserId(userId);

                            // Extract other fields
                            String name = userSnapshot.child("name").getValue(String.class);
                            application.setApplicantName(name);

                            String interviewDateTime = userSnapshot.child("interviewDateTime").getValue(String.class);
                            application.setInterviewDateTime(interviewDateTime);

                            String imageBase64 = userSnapshot.child("profile").getValue(String.class);
                            application.setImageBase64(imageBase64);

                            String jobTitle = userSnapshot.child("title").getValue(String.class);
                            application.setPosition(jobTitle);

                            ArrayList<String> jobTypeList = userSnapshot.child("jobType")
                                    .getValue(new GenericTypeIndicator<ArrayList<String>>() {});
                            application.setJobTypeList(jobTypeList);

                            application.setStatus(status);

                            // Add to list only if all required data is present
                            if (name != null && jobTitle != null) {
                                applicationList.add(application);
                            }
                        }
                    }

                    if (applicationList.isEmpty()) {
                        Toast.makeText(getActivity(), "No pending applications found", Toast.LENGTH_SHORT).show();
                    }

                    adapter.setApplicationList(applicationList);
                } else {
                    Toast.makeText(getActivity(), "No applications found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching applicants: " + error.getMessage());
                Toast.makeText(getActivity(), "Error loading applications: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ref != null && valueEventListener != null) {
            ref.removeEventListener(valueEventListener);  // Remove the listener when fragment is destroyed
        }
    }
}