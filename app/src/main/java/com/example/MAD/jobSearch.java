package com.example.MAD;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class jobSearch extends Fragment implements job_RecyclerViewAdapter.OnBookmarkChangeListener {

    private static final int EARTH_RADIUS_KM = 6371;
    private static final int FILTER_REQUEST_CODE = 1001;
    private ArrayList<Job> jobList = new ArrayList<>();
    private ArrayList<Job> filteredJobs = new ArrayList<>();
    private job_RecyclerViewAdapter adapter;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    private ArrayList<String> selectedJobTypes = new ArrayList<>();
    private ArrayList<String> selectedRemoteOptions = new ArrayList<>();
    private ArrayList<String> selectedExperienceLevels = new ArrayList<>();
    private ArrayList<String> selectedJobCategory = new ArrayList<>();
    private double lat, lng;
    private int radius;

    private String username;
    private TextView welcome;
    private ShapeableImageView profilePic;

    private View view;

    private String userEmail;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_job_search, container, false);

//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Location", MODE_PRIVATE);
//        lat = sharedPreferences.getFloat("LATITUDE", 0f);
//        lng = sharedPreferences.getFloat("LONGITUDE", 0f);
//        radius = sharedPreferences.getInt("RADIUS", 100);
        userEmail = UserSessionManager.getInstance().getUserEmail();
        username = userEmail.replace(".", "_");



        // Initialize RecyclerView and Adapter
        RecyclerView recyclerView = view.findViewById(R.id.RVjob);
        adapter = new job_RecyclerViewAdapter(requireContext(), username, false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("jobs");

        // Fetch jobs from Firebase
        fetchJobsFromFirebase();

        // Handle incoming filter data from Bundle
        if (getArguments() != null) {
            applyFiltersFromBundle(getArguments());
        }

        // Filter button to navigate to filter page
        ImageButton btnFilter = view.findViewById(R.id.IBfilter);
        btnFilter.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putDouble("LATITUDE", lat);
            bundle.putDouble("LONGITUDE", lng);
            bundle.putInt("RADIUS", radius);
            Navigation.findNavController(requireView()).navigate(R.id.jobFilterFragment, bundle);
        });

        // Button to go to notifications
        ImageButton btnNoti = view.findViewById(R.id.IBnoti);
        btnNoti.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.notificationStatus);
        });

        // Button to go to bookmarks
        ImageButton btnBookmarks = view.findViewById(R.id.IBbookmarks);
        btnBookmarks.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.savedFragment);
        });

        // Clear filter
        TextView clearFilter = view.findViewById(R.id.TVClearFilter);
        clearFilter.setOnClickListener(v -> {
            selectedJobTypes.clear();
            selectedRemoteOptions.clear();
            selectedExperienceLevels.clear();
            selectedJobCategory.clear();
            radius = 1000;

            EditText etUserInput = view.findViewById(R.id.ETsearch);
            etUserInput.setText("");

            ChipGroup chipGroup = view.findViewById(R.id.CGfilter);
            chipGroup.removeAllViews();
            filterJobs();
        });

        // EditText for search functionality
        EditText etUserInput = view.findViewById(R.id.ETsearch);
        etUserInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterJobs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void applyFiltersFromBundle(Bundle bundle) {
        // Extract filters from the Bundle
        selectedJobTypes = bundle.getStringArrayList("selectedJobTypes");
        selectedRemoteOptions = bundle.getStringArrayList("selectedRemoteOptions");
        selectedExperienceLevels = bundle.getStringArrayList("selectedExperienceLevels");
        selectedJobCategory = bundle.getStringArrayList("selectedJobCategory");
        radius = bundle.getInt("RADIUS", 100);
        lat = bundle.getDouble("LATITUDE", 0.0);
        lng = bundle.getDouble("LONGITUDE", 0.0);

        // Update ChipGroup with the selected filters
        updateChipGroup(selectedJobTypes, selectedRemoteOptions, selectedExperienceLevels, selectedJobCategory);

        // Filter jobs based on the passed filters
        filterJobs();
    }

    private void fetchJobsFromFirebase() {
        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                jobList.clear();
                int totalJobs = (int) dataSnapshot.getChildrenCount();
                final int[] processedJobs = {0};

                for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                    Job job = jobSnapshot.getValue(Job.class);
                    if (job != null) {
                        sanitizeJobData(job);
                        DatabaseReference bookmarkRef = FirebaseDatabase.getInstance().getReference("users")
                                .child("jobseeker")
                                .child(username)
                                .child("savedJobs")
                                .child(job.getJobID());

                        bookmarkRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                job.setBookmarked(snapshot.exists());
                                jobList.add(job);
                                processedJobs[0]++;

                                // Only filter jobs after all jobs have been processed
                                if (processedJobs[0] == totalJobs) {
                                    filterJobs();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                jobList.add(job);
                                processedJobs[0]++;
                                if (processedJobs[0] == totalJobs) {
                                    filterJobs();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load jobs.", Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference personalInfoRef = FirebaseDatabase.getInstance().getReference("users")
                .child("jobseeker")
                .child(username);

        profilePic = view.findViewById(R.id.IVProfilePic);
        welcome = view.findViewById(R.id.TVWelcome);

        personalInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageBase64 = snapshot.child("profile").getValue(String.class);
                decodeBase64(imageBase64, profilePic);
                String welcomeText = snapshot.child("name").getValue(String.class);
                welcome.setText(welcomeText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
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

    private void sanitizeJobData(Job job) {
        if (job.getTitle() == null) job.setTitle("");
        if (job.getCompanyName() == null) job.setCompanyName("");
        if (job.getLocation() == null) job.setLocation("");
        if (job.getJobTypes() == null) job.setJobTypes(new ArrayList<>());
        if (job.getRemoteOptions() == null) job.setRemoteOptions(new ArrayList<>());
        if (job.getExperienceLevels() == null) job.setExperienceLevels(new ArrayList<>());
        if (job.getJobCategory() == null) job.setJobCategory(new ArrayList<>());
    }

    // Filter jobs based on both user input and selected filters
    private void filterJobs() {
        // Get user input for search
        String userInput = ((EditText) view.findViewById(R.id.ETsearch)).getText().toString().toLowerCase();
        filteredJobs.clear();


        for (Job job : jobList) {
            boolean withinRadius = isWithinRadius(job, lat, lng, radius);
            System.out.println("Job: " + job.getTitle() + " at location (" +
                    job.getLatitude() + "," + job.getLongitude() + ") within radius: " + withinRadius);

            if (!withinRadius) continue;

            // Check if the job matches the search keyword
            if (!matchesKeyword(job, userInput)) continue;

            // Check if the job matches the selected filters
            if (!matchesFilters(job)) continue;

            // If the job matches both the keyword and the filters, add it to the filtered list
            filteredJobs.add(job);
        }

        // Update the RecyclerView adapter with the filtered job list
        adapter.updateJobList(filteredJobs);

        // Show the number of jobs found
        TextView tvnumJob = view.findViewById(R.id.TVJobFound);
        tvnumJob.setVisibility(View.VISIBLE);
        tvnumJob.setText(filteredJobs.isEmpty() ? "No jobs found" : filteredJobs.size() + " job(s) found");
    }

    private boolean isWithinRadius(Job job, double latitude, double longitude, int radius) {
        // If no location filter is set (lat and lng are 0), return true
        if (latitude == 0 && longitude == 0) {
            return true;
        }

        // If job has no location data, return false
        if (job.getLatitude() == 0 && job.getLongitude() == 0) {
            return false;
        }

        double distance = calculateDistance(latitude, longitude, job.getLatitude(), job.getLongitude());
        return distance <= radius;
    }

    private boolean matchesKeyword(Job job, String keyword) {
        return keyword.isEmpty() ||
                job.getTitle().toLowerCase().contains(keyword) ||
                job.getCompanyName().toLowerCase().contains(keyword) ||
                job.getLocation().toLowerCase().contains(keyword);
    }

    private boolean matchesFilters(Job job) {
        return (selectedJobTypes.isEmpty() || hasIntersection(selectedJobTypes, job.getJobTypes())) &&
                (selectedRemoteOptions.isEmpty() || hasIntersection(selectedRemoteOptions, job.getRemoteOptions())) &&
                (selectedExperienceLevels.isEmpty() || hasIntersection(selectedExperienceLevels, job.getExperienceLevels())) &&
                (selectedJobCategory.isEmpty() || hasIntersection(selectedJobCategory, job.getJobCategory()));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedJobTypes = data.getStringArrayListExtra("selectedJobTypes");
            selectedRemoteOptions = data.getStringArrayListExtra("selectedRemoteOptions");
            selectedExperienceLevels = data.getStringArrayListExtra("selectedExperienceLevels");
            selectedJobCategory = data.getStringArrayListExtra("selectedJobCategory");

            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Location", MODE_PRIVATE);
            float latitude = sharedPreferences.getFloat("LATITUDE", 0f);
            float longitude = sharedPreferences.getFloat("LONGITUDE", 0f);

            lat = (double) latitude;
            lng = (double) longitude;

            radius = data.getIntExtra("RADIUS", 0);

            updateChipGroup(selectedJobTypes, selectedRemoteOptions, selectedExperienceLevels, selectedJobCategory);
            filterJobs();
        }

    }

    private Chip createChip(String text, ArrayList<String> filterCategory) {
        Chip chip = new Chip(getContext());
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.black);
        chip.setChipStrokeColorResource(R.color.orange);
        chip.setChipStrokeWidth(2);
        chip.setChipCornerRadius(16);
        chip.setCloseIconTint(getResources().getColorStateList(R.color.brown));
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.judson);
        chip.setTypeface(typeface);
        chip.setTextColor(getResources().getColor(android.R.color.white));
        chip.setCloseIconVisible(true);

        // Handle chip removal and filter update
        chip.setOnCloseIconClickListener(v -> {
            ((ChipGroup) chip.getParent()).removeView(chip); // Remove chip from view
            filterCategory.remove(text); // Remove the corresponding filter value
            filterJobs(); // Refresh the job list
        });

        return chip;
    }

    private void updateChipGroup(ArrayList<String> jobTypes, ArrayList<String> remoteOptions,
                                 ArrayList<String> experienceLevels, ArrayList<String> jobCategory) {

        ChipGroup chipGroup = view.findViewById(R.id.CGfilter);
        chipGroup.removeAllViews(); // Clear existing chips

        chipGroup.setVisibility(jobTypes.isEmpty() && remoteOptions.isEmpty() &&
                experienceLevels.isEmpty() && jobCategory.isEmpty() ? View.GONE : View.VISIBLE);

        if (jobTypes != null) {
            for (String jobType : jobTypes) {
                chipGroup.addView(createChip(jobType, selectedJobTypes));
            }
        }
        if (remoteOptions != null) {
            for (String remoteOption : remoteOptions) {
                chipGroup.addView(createChip(remoteOption, selectedRemoteOptions));
            }
        }
        if (experienceLevels != null) {
            for (String experienceLevel : experienceLevels) {
                chipGroup.addView(createChip(experienceLevel, selectedExperienceLevels));
            }
        }
        if (jobCategory != null) {
            for (String category : jobCategory) {
                chipGroup.addView(createChip(category, selectedJobCategory));
            }
        }
    }

    private boolean hasIntersection(ArrayList<String> list1, ArrayList<String> list2) {
        for (String item : list1) {
            if (list2.contains(item)) {
                return true;
            }
        }
        return false;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public void onBookmarkChanged(String jobId, boolean isBookmarked) {
        DatabaseReference bookmarkRef = FirebaseDatabase.getInstance().getReference("users")
                .child("jobseeker")
                .child(username)
                .child("savedJobs")
                .child(jobId);

        if (isBookmarked) {
            bookmarkRef.setValue(true);
        } else {
            bookmarkRef.removeValue();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }
}