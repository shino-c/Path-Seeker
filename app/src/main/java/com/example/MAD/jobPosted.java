package com.example.MAD;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class jobPosted extends Fragment {

    private ArrayList<Job> jobList = new ArrayList<>();
    private jobPostedAdapter adapter;
    private DatabaseReference databaseReference;
    private String username;
    private TextView welcome;
    private ShapeableImageView profilePic;

    private String userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job_posted, container, false);
        userEmail = UserSessionManager.getInstance().getUserEmail();
        username =  userEmail.replace(".", "_");

        // Button to go to notifications
        ImageButton btnNoti = view.findViewById(R.id.IBnoti);
        btnNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(requireView()).navigate(R.id.notiRequestFragment);
            }
        });

        // Floating action button to create a new job
        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(requireView()).navigate(R.id.createNewJobFragment);
            }
        });

        // Initialize RecyclerView and Adapter
        RecyclerView recyclerView = view.findViewById(R.id.RVjobPosted);
        adapter = new jobPostedAdapter(getContext(), jobList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch jobs from Firebase
        fetchJobsFromFirebase(view);

        return view;
    }

    private void fetchJobsFromFirebase(View view) {
        // Reference to the user
        DatabaseReference postedJobsRef = FirebaseDatabase.getInstance().getReference("users")
                .child("recruiter")
                .child(username);

        profilePic = view.findViewById(R.id.IVProfilePic);
        welcome = view.findViewById(R.id.TVWelcome);

        postedJobsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageBase64 = snapshot.child("profile").getValue(String.class);
                decodeBase64(imageBase64, profilePic);
                String welcomeText = snapshot.child("companyName").getValue(String.class);
                welcome.setText(welcomeText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postedJobsRef.child("postedJobs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                jobList.clear();  // Clear the previous list before adding new data

                // Check if postedJobs exists
                if (dataSnapshot.exists()) {
                    // Loop through each job ID in the postedJobs list
                    for (DataSnapshot jobIdSnapshot : dataSnapshot.getChildren()) {
                        String jobId = jobIdSnapshot.getValue(String.class);  // Get the job ID

                        // Reference to the job details using the jobId
                        DatabaseReference jobRef = FirebaseDatabase.getInstance().getReference("jobs").child(jobId);

                        jobRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot jobDataSnapshot) {
                                // Get job details from the job node
                                Job job = jobDataSnapshot.getValue(Job.class);
                                if (job != null) {
                                    jobList.add(job);  // Add the job to the list
                                    adapter.notifyDataSetChanged();  // Notify the adapter to update the RecyclerView
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getContext(), "Error loading job details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(getContext(), "No posted jobs found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load posted jobs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void decodeBase64(String base64String, ImageView imageView) {
        try {
            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedBitmap);  // Set decoded Bitmap to Image View
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
