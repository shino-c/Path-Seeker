package com.example.MAD;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class jobDetails extends Fragment {

    private AlertDialog applyDialog;
    private AlertDialog infoDialog;
    private ArrayList<String> jobType;
    private String compPic;
    private String jobId;
    private String title;
    private TextView jobTitle;
    private TextView companyName;
    private String JobType;
    private String Remote;
    private String cname;
    private TextView timePosted;
    private TextView salary;
    private TextView description;
    private TextView skills;
    private ShapeableImageView pic;
    private ShapeableImageView companyPic;
    private TextView companyNameinCard;
    private String companyId;
    private JobTypeAdapter adapter;
    private String username,userEmail;

    private CardView CVClickCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job_details, container, false);

        userEmail = UserSessionManager.getInstance().getUserEmail();
        username = userEmail.replace(".", "_");

        // Initialize UI elements
        jobTitle = view.findViewById(R.id.TVJobTitle);
        companyName = view.findViewById(R.id.TVCompanyName);
        timePosted = view.findViewById(R.id.TVTimePosted);
        salary = view.findViewById(R.id.TVSalary);
        description = view.findViewById(R.id.TVJobDescription);
        skills = view.findViewById(R.id.TVSkills);
        pic = view.findViewById(R.id.IVcompanyPic);
        companyPic = view.findViewById(R.id.IVcompanyPicinCard);
        companyNameinCard = view.findViewById(R.id.TVCompanyNameinCard);
        CVClickCard = view.findViewById(R.id.CVClickCard);

        // Back button to navigate to previous fragment or activity
        ImageButton btnBack = view.findViewById(R.id.IBback);
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());


        // Get job ID from arguments
        if (getArguments() != null) {
            jobId = getArguments().getString("jobId");
        }

        if (jobId == null) {
            Toast.makeText(requireContext(), "Job ID is missing", Toast.LENGTH_SHORT).show();
            return view; // Exit if no job ID is found
        }
        fetchJobDetails(jobId);
        CVClickCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("rateUser", companyId); // Pass the company ID

                Navigation.findNavController(requireView())
                        .navigate(R.id.profileCompanyViewRate, bundle); // Pass the bundle to navigate()
            }
        });

        TextView seeJobType = view.findViewById(R.id.TVJobType);
        seeJobType.setOnClickListener(v -> showPopUp("Job Types", JobType));

        TextView seeRemote = view.findViewById(R.id.TVRemote);
        seeRemote.setOnClickListener(v -> showPopUp("Remote Types", Remote));

        Button btnApplyJob = view.findViewById(R.id.btnApplyJob);
        btnApplyJob.setOnClickListener(v -> showApplyDialog());

        return view;
    }

    private void showPopUp(String header, String content) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_job_info, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        infoDialog = builder.create();

        infoDialog.getWindow().setBackgroundDrawable(requireContext().getDrawable(R.drawable.dialog_box));

        TextView tvHeader = dialogView.findViewById(R.id.TVHeader);
        tvHeader.setText(header);

        if (content == null || content.isEmpty()) {
            content = "No information available";
        }
        String formattedContent = content.replace("[", "").replace("]", "").replace(", ", "\n");
        TextView tvInfo = dialogView.findViewById(R.id.TVinfo);
        tvInfo.setText(formattedContent);

        ImageButton closeButton = dialogView.findViewById(R.id.IBClose);
        closeButton.setOnClickListener(v -> infoDialog.dismiss());

        infoDialog.setCanceledOnTouchOutside(true);
        infoDialog.show();
    }

    private void showApplyDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_apply, null);

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(dialogView);
        dialog.getWindow().setBackgroundDrawable(requireContext().getDrawable(R.drawable.dialog_box));

        RecyclerView recyclerView = dialogView.findViewById(R.id.RVJobType);
        JobTypeAdapter adapter = new JobTypeAdapter(requireContext(), jobType);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> {
            List<String> selectedJobTypes = adapter.getSelectedJobTypes();
            saveApplicantToDatabase(jobId, username, selectedJobTypes, title);
            dialog.dismiss();
            Toast.makeText(requireContext(), "Successfully Applied!", Toast.LENGTH_SHORT).show();
        });

        ImageButton closeButton = dialogView.findViewById(R.id.IBClose);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void saveApplicantToDatabase(String jobId, String username, List<String> selectedJobTypes, String jobTitle) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child("jobseeker").child(username);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch user details
                    String name = snapshot.child("name").getValue(String.class);
                    String imageBase64 = snapshot.child("profile").getValue(String.class);

                    // Now fetch company details
                    DatabaseReference compRef = FirebaseDatabase.getInstance().getReference().child("jobs").child(jobId);
                    compRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String compName = snapshot.child("companyName").getValue(String.class);
                                compPic = snapshot.child("imageBase64").getValue(String.class);

                                DatabaseReference nameRef = FirebaseDatabase.getInstance().getReference().child("users").child("recruiter").child(compName);
                                nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            cname = snapshot.child("companyName").getValue(String.class);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                // Now we can perform the update after both user and company details are fetched
                                performApplicationUpdate(jobId, username, selectedJobTypes, jobTitle, name, imageBase64, cname, compPic);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle the error
                            Toast.makeText(getContext(), "Error fetching company details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // This method is called after both user and company details are fetched
    private void performApplicationUpdate(String jobId, String username, List<String> selectedJobTypes, String jobTitle, String name, String imageBase64, String cname, String compPic) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("applications");

        // Create a map to hold updates
        Map<String, Object> updates = new HashMap<>();

        // Define paths and values for "jobs"
        String jobPath = "jobs/" + jobId + "/" + username;
        updates.put(jobPath + "/jobType", selectedJobTypes);
        updates.put(jobPath + "/status", "Applied");
        updates.put(jobPath + "/title", jobTitle);
        updates.put(jobPath + "/name", name);
        updates.put(jobPath + "/profile", imageBase64);
        updates.put(jobPath + "/companyName", cname);

        // Define paths and values for "applicants"
        String applicantPath = "applicants/" + username + "/" + jobId;
        updates.put(applicantPath + "/status", "Applied");
        updates.put(applicantPath + "/title", jobTitle);
        updates.put(applicantPath + "/profile", compPic);
        updates.put(applicantPath + "/companyName", cname);

        // Perform the updates atomically
        ref.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Application saved successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to save application: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchJobDetails(String id) {
        DatabaseReference refJob = FirebaseDatabase.getInstance().getReference("jobs").child(id);

        refJob.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Job job = snapshot.getValue(Job.class);  // Deserialize the job object

                    if (job != null) {
                        title = job.getTitle();
                        // Set the job details in the UI elements
                        jobTitle.setText(job.getTitle());
                        companyId = job.getCompanyName();
                        jobType = job.getJobTypes();
                        JobType = jobType.toString();
                        Remote = job.getRemoteOptions().toString();

                        // Calculate and set the time posted
                        long currentTimeMillis = System.currentTimeMillis();
                        long timeDifference = currentTimeMillis - job.getTimePosted(); // Get time difference in milliseconds
                        String timePostedString = getTimeAgo(timeDifference);
                        timePosted.setText(timePostedString);// Add this field to your Job class if not present
                        salary.setText("RM" + String.format("%.2f", job.getSalary()) + "/month"); // Add this field to your Job class if not present
                        description.setText(job.getDescription());  // Add this field to your Job class if not present
                        skills.setText(job.getSkills());  // Add this field to your Job class if not present

                        // Optionally set the company picture if available
                        if (job.getImageBase64() != null) {
                            decodeBase64(job.getImageBase64(), pic);  // Method to decode and set image
                        }

                        fetchCompanyDetails(companyId);
                    }
                } else {
                    Toast.makeText(getContext(), "Job not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("JobDetailsFragment", "Error fetching details", error.toException());
            }
        });
    }

    private void fetchCompanyDetails(String companyId) {
        if (TextUtils.isEmpty(companyId)) {
            Log.e("jobDetails", "Company ID is null or empty.");
            return;
        }

        DatabaseReference refCompany = FirebaseDatabase.getInstance()
                .getReference("users")
                .child("recruiter")
                .child(companyId);

        refCompany.child("companyName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String companyname = snapshot.getValue(String.class);
                    if (companyname != null) {
                        companyName.setText(companyname);
                        companyNameinCard.setText(companyname);
                    } else {
                        Log.e("Firebase", "Company name is null.");
                    }
                } else {
                    Log.e("Firebase", "Company ID not found in the database.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("jobDetails", "Error fetching company details", error.toException());
            }
        });

        refCompany.child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imageBase64 = snapshot.getValue(String.class);
                    if (imageBase64 != null) {
                        decodeBase64(imageBase64, companyPic);
                    } else {
                        Log.e("Firebase", "Company name is null.");
                    }
                } else {
                    Log.e("Firebase", "Company ID not found in the database.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void decodeBase64(String base64String, ShapeableImageView imageView) {
        try {
            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTimeAgo(long milliseconds) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long days = TimeUnit.MILLISECONDS.toDays(milliseconds);

        if (seconds < 60) {
            return seconds + " seconds ago";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            return days + " days ago";
        }
    }
}