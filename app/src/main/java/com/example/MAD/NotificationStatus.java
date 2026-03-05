package com.example.MAD;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationStatus extends Fragment {

    private job_RecyclerViewAdapter adapterApplied, adapterSuccess, adapterRejected;
    private InterviewAdapter adapterInterview;
    private DatabaseReference databaseReference;
    private ArrayList<Job> appliedJobs = new ArrayList<>();
    private ArrayList<Job> successJobs = new ArrayList<>();
    private ArrayList<Job> rejectedJobs = new ArrayList<>();
    private ArrayList<Application> interviewJobs = new ArrayList<>();
    private RecyclerView recyclerViewApplied, recyclerViewSuccess, recyclerViewRejected, recyclerViewInterview;
    private TextView applied, success, rejected, interview;
    private String username,userEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notification_status, container, false);

        userEmail = UserSessionManager.getInstance().getUserEmail();
        username = userEmail.replace(".", "_");

        applied = rootView.findViewById(R.id.TVApplied);
        success = rootView.findViewById(R.id.TVSuccess);
        rejected = rootView.findViewById(R.id.TVRejected);
        interview = rootView.findViewById(R.id.TVInterview);

        // RecyclerView and Adapter initialization
        recyclerViewApplied = rootView.findViewById(R.id.RVApplied);
        adapterApplied = new job_RecyclerViewAdapter(getActivity(), username,true);
        recyclerViewApplied.setAdapter(adapterApplied); // Attach adapter
        recyclerViewApplied.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerViewSuccess = rootView.findViewById(R.id.RVSuccess);
        adapterSuccess = new job_RecyclerViewAdapter(getActivity(), username, true);
        recyclerViewSuccess.setAdapter(adapterSuccess); // Attach adapter
        recyclerViewSuccess.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerViewRejected = rootView.findViewById(R.id.RVRejected);
        adapterRejected = new job_RecyclerViewAdapter(getActivity(), username, true);
        recyclerViewRejected.setAdapter(adapterRejected); // Attach adapter
        recyclerViewRejected.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerViewInterview = rootView.findViewById(R.id.RVInterview);
        adapterInterview = new InterviewAdapter(getActivity(), username);
        recyclerViewInterview.setAdapter(adapterInterview); // Attach adapter
        recyclerViewInterview.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Firebase Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("applications").child("applicants").child(username);

        // Fetch jobs
        fetchAppliedJobsFromFirebase();
        fetchSuccessJobsFromFirebase();
        fetchRejectedJobsFromFirebase();
        fetchInterviewJobsFromFirebase();

        ImageButton btnBack = rootView.findViewById(R.id.IBback);
        btnBack.setOnClickListener(v -> {
            // Handle back button click
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        return rootView;
    }

    private void fetchAppliedJobsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                appliedJobs.clear(); // Clear the list before adding new items

                for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                    String jobId = jobSnapshot.getKey(); // Get jobId

                    // Check if the status is "Applied"
                    String status = jobSnapshot.child("status").getValue(String.class);
                    if ("Applied".equalsIgnoreCase(status)) {
                        fetchJobDetailsFromJobsNode(jobId, "Applied"); // Fetch job details if status is "Applied"
                    }
                }

                // After the loop, update the adapter with the full list
                adapterApplied.updateJobList(appliedJobs);

                // Update visibility of RecyclerView and TextView
                handleRecyclerViewVisibility(appliedJobs, recyclerViewApplied, applied);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load jobs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSuccessJobsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                successJobs.clear(); // Clear the list before adding new items

                for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                    String jobId = jobSnapshot.getKey(); // Get jobId

                    // Check if the status is "Applied"
                    String status = jobSnapshot.child("status").getValue(String.class);
                    if ("Success".equalsIgnoreCase(status)) {
                        fetchJobDetailsFromJobsNode(jobId, "Success"); // Fetch job details if status is "Applied"
                    }
                }

                // After the loop, update the adapter with the full list
                adapterSuccess.updateJobList(successJobs);

                // Update visibility of RecyclerView and TextView
                handleRecyclerViewVisibility(successJobs, recyclerViewSuccess, success);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load jobs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRejectedJobsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                rejectedJobs.clear(); // Clear the list before adding new items

                for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                    String jobId = jobSnapshot.getKey(); // Get jobId

                    // Check if the status is "Applied"
                    String status = jobSnapshot.child("status").getValue(String.class);
                    if ("Rejected".equalsIgnoreCase(status)) {
                        fetchJobDetailsFromJobsNode(jobId, "Rejected"); // Fetch job details if status is "Applied"
                    }
                }

                // After the loop, update the adapter with the full list
                adapterRejected.updateJobList(rejectedJobs);

                // Update visibility of RecyclerView and TextView
                handleRecyclerViewVisibility(rejectedJobs, recyclerViewRejected, rejected);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load jobs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchInterviewJobsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                interviewJobs.clear();

                for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                    // Get the job information
                    String status = jobSnapshot.child("status").getValue(String.class);

                    if ("Interview".equalsIgnoreCase(status)) {
                        Application app = new Application();
                        app.setPosition(jobSnapshot.child("title").getValue(String.class));
                        app.setCompanyName(jobSnapshot.child("companyName").getValue(String.class));
                        app.setInterviewDateTime(jobSnapshot.child("interviewDateTime").getValue(String.class));
                        app.setImageBase64(jobSnapshot.child("profile").getValue(String.class));
                        app.setStatus(status);
                        // You might want to store the job ID too
                        app.setJobId(jobSnapshot.getKey());

                        interviewJobs.add(app);
                    }
                }

                // Update the adapter after collecting all interview applications
                adapterInterview.updateJobList(interviewJobs);

                // Update visibility
                handleRecyclerViewVisibilityInterview(interviewJobs, recyclerViewInterview, interview);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load jobs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchApplicationDetailsFromJobsNode(String jobId, String status) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("applications").child("applicants").child(username);

        ref.child(jobId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Application app = new Application();
                    app.setCompanyName(dataSnapshot.child("companyName").getValue(String.class));
                    app.setInterviewDateTime(dataSnapshot.child("interviewDateTime").getValue(String.class));
                    app.setImageBase64(dataSnapshot.child("profile").getValue(String.class));
                    app.setPosition(dataSnapshot.child("title").getValue(String.class));
                    app.setStatus(dataSnapshot.child("status").getValue(String.class));

                    if ("Interview".equalsIgnoreCase(status)) {
                        interviewJobs.add(app);
                        adapterInterview.updateJobList(interviewJobs);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchJobDetailsFromJobsNode(String jobId, String status) {
        DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("jobs").child(jobId);

        jobsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Job job = dataSnapshot.getValue(Job.class);

                if (job != null) {
                    // Add job to the correct list based on status
                    if ("Applied".equalsIgnoreCase(status)) {
                        appliedJobs.add(job);
                    } else if ("Success".equalsIgnoreCase(status)) {
                        successJobs.add(job);
                    } else if ("Rejected".equalsIgnoreCase(status)) {
                        rejectedJobs.add(job);
                    }

                    // Notify the adapter to update the UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if ("Applied".equalsIgnoreCase(status)) {
                                adapterApplied.updateJobList(appliedJobs);
                                handleRecyclerViewVisibility(appliedJobs, recyclerViewApplied, applied);
                            } else if ("Success".equalsIgnoreCase(status)) {
                                adapterSuccess.updateJobList(successJobs);
                                handleRecyclerViewVisibility(successJobs, recyclerViewSuccess, success);
                            } else if ("Rejected".equalsIgnoreCase(status)) {
                                adapterRejected.updateJobList(rejectedJobs);
                                handleRecyclerViewVisibility(rejectedJobs, recyclerViewRejected, rejected);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void handleRecyclerViewVisibility(ArrayList<Job> list, RecyclerView recyclerView, TextView tv) {
        int visibility = list.isEmpty() ? View.GONE : View.VISIBLE;
        recyclerView.setVisibility(visibility);
        tv.setVisibility(visibility);
    }

    private void handleRecyclerViewVisibilityInterview(ArrayList<Application> list, RecyclerView recyclerView, TextView tv) {
        int visibility = list.isEmpty() ? View.GONE : View.VISIBLE;
        recyclerView.setVisibility(visibility);
        tv.setVisibility(visibility);
    }

}