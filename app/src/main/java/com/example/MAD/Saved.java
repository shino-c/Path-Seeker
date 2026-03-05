package com.example.MAD;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Saved extends Fragment implements job_RecyclerViewAdapter.OnBookmarkChangeListener {

    private job_RecyclerViewAdapter adapter;
    private DatabaseReference database;
    private ArrayList<Job> jobList = new ArrayList<>();
    private String username,userEmail;

    public Saved() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_saved, container, false);

        userEmail = UserSessionManager.getInstance().getUserEmail();
        username = userEmail.replace(".", "_");

        // RecyclerView setup
        RecyclerView recyclerView = rootView.findViewById(R.id.RVjob);
        adapter = new job_RecyclerViewAdapter(getContext(), username,true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch saved jobs from Firebase
        fetchJobsFromFirebase(username);

        // Back button handling
        ImageButton btnBack = rootView.findViewById(R.id.IBback);
        btnBack.setOnClickListener(v -> {
            // Handle back button click
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        return rootView;
    }

    private void fetchJobsFromFirebase(String username) {
        // Set reference to savedJobs node
        database = FirebaseDatabase.getInstance().getReference("users").child("jobseeker").child(username).child("savedJobs");

        // Query the savedJobs under the user
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear previous job list before populating with new data
                jobList.clear();

                // Check if savedJobs exists
                if (dataSnapshot.exists()) {
                    // Loop through each jobId in the savedJobs list
                    for (DataSnapshot jobIdSnapshot : dataSnapshot.getChildren()) {
                        // Get the job details for each saved job
                        Job job = jobIdSnapshot.getValue(Job.class);
                        if (job != null) {
                            jobList.add(job);  // Add each job to the list
                        }
                    }

                    // Notify the adapter once all jobs are fetched
                    adapter.updateJobList(jobList);
                } else {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load saved jobs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBookmarkChanged(String jobId, boolean isBookmarked) {
        if (!isBookmarked) {
            // Refresh the list immediately when a bookmark is removed
            fetchJobsFromFirebase(username);
        }
    }
}