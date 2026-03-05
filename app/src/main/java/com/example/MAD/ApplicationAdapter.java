package com.example.MAD;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private Context context;
    private List<Application> applicationList;
    private List<Job> job;
    String username;

    public ApplicationAdapter(Context context, String username, List<Application> applicationList) {
        this.context = context;
        this.username = username;
        this.applicationList = applicationList != null ? applicationList : new ArrayList<>(); // Ensure it is not null
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_requests, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Application application = applicationList.get(position);

        holder.nameTextView.setText(application.getApplicantName());
        holder.positionTextView.setText(application.getPosition());
        holder.jobTypeTextView.setText("Job Type Applied: " + application.getJobTypeList());
        decodeBase64(application.getImageBase64(), holder.profile);

        // Check if interviewDateTime is already set, and change button text if it is
        if (application.getInterviewDateTime() != null && !application.getInterviewDateTime().isEmpty()) {
            holder.scheduleInterview.setText(application.getInterviewDateTime());
            holder.scheduleInterview.setEnabled(false); // Disable the button once the interview is scheduled
        } else {
            holder.scheduleInterview.setText("Schedule Interview");
            holder.scheduleInterview.setEnabled(true); // Enable the button if no interview is scheduled
        }

        // Handle "Schedule Interview" Button click (set status to "Interview")
        holder.scheduleInterview.setOnClickListener(v -> {
            showDialog(application);
        });

        // Handle "Accept" Button click (set status to "Success")
        holder.acceptButton.setOnClickListener(v -> {
            updateApplicationStatus(application, "Success");
            applicationList.remove(position); // Remove the application from the list
            notifyItemRemoved(position); // Notify adapter that item has been removed
        });

        // Handle "Reject" Button click (set status to "Rejected")
        holder.rejectButton.setOnClickListener(v -> {
            updateApplicationStatus(application, "Rejected");
            applicationList.remove(position); // Remove the application from the list
            notifyItemRemoved(position); // Notify adapter that item has been removed
        });

        holder.viewProfile.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("rateSeeker", application.getUserId());
            Log.d("Debug",application.getApplicantName());

            Navigation.findNavController(v)
                    .navigate(R.id.profileSeekerViewRate, bundle);
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

    @Override
    public int getItemCount() {
        return applicationList.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, positionTextView, jobTypeTextView,viewProfile;
        ShapeableImageView profile;
        Button acceptButton, rejectButton, scheduleInterview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.TVapplicantName);
            profile = itemView.findViewById(R.id.IVprofile);
            positionTextView = itemView.findViewById(R.id.TVAppliedPosition);
            jobTypeTextView = itemView.findViewById(R.id.TVAppliedJobType);
            acceptButton = itemView.findViewById(R.id.btnAccept);
            rejectButton = itemView.findViewById(R.id.btnReject);
            scheduleInterview = itemView.findViewById(R.id.btnScheduleIV);
            viewProfile=itemView.findViewById(R.id.TVviewProfile);
        }
    }

    // Show date picker for scheduling interview
    private void showDialog(Application application) {
        openDatePicker(application);
    }

    // Open date picker dialog
    private void openDatePicker(Application application) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                R.style.CustomDatePickerDialog, // Use the custom style
                (datePicker, selectedYear, selectedMonth, selectedDay) ->
                        confirmDate(selectedYear, selectedMonth, selectedDay, application),
                year, month, day);

        datePickerDialog.show();
        datePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void confirmDate(int year, int month, int day, Application application) {
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Confirm Date")
                .setMessage("Do you want to schedule the interview on " + day + "/" + (month + 1) + "/" + year + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    openTimePicker(year, month, day, application);
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Open time picker after confirming the date
    private void openTimePicker(int year, int month, int day, Application application) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(context, R.style.DialogTheme,
                (timePicker, selectedHour, selectedMinute) -> {
                    String selectedDateTime = day + "/" + (month + 1) + "/" + year + " " + selectedHour + ":" + selectedMinute;
                    storeInterviewDateTime(selectedDateTime, application); // Pass application object here
                    updateApplicationStatus(application, "Interview"); // Update status after both date and time are selected
                }, hour, minute, false);

        timePickerDialog.show();
    }

    // Store interview date and time in Firebase
    private void storeInterviewDateTime(String selectedDateTime, Application application) {
        application.setInterviewDateTime(selectedDateTime);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("applications").child("applicants").child(application.getUserId()).child(application.getJobId()).child("interviewDateTime");
        databaseReference.setValue(selectedDateTime) // Store selected date-time as a string
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Interview scheduled for: " + selectedDateTime, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error scheduling interview: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("applications").child("jobs").child(application.getJobId()).child(application.getUserId()).child("interviewDateTime");
        db.setValue(selectedDateTime); // Store selected date-time as a string
    }


    // Update the status of the application (Accepted or Rejected)
    private void updateApplicationStatus(Application application, String status) {
        // Define the reference for both applicants and jobs
        DatabaseReference applicantsReference = FirebaseDatabase.getInstance().getReference("applications")
                .child("applicants").child(application.getUserId()).child(application.getJobId()).child("status");

        DatabaseReference jobsReference = FirebaseDatabase.getInstance().getReference("applications")
                .child("jobs").child(application.getJobId()).child(application.getUserId()).child("status");

        // Update the status for applicants
        applicantsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If status exists for applicants, overwrite it
                applicantsReference.setValue(status)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Applicant status updated to " + status, Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Error updating applicant status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error checking applicant status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Update the status for jobs
        jobsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // If status exists for jobs, overwrite it
                jobsReference.setValue(status)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Job status updated to " + status, Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Error updating job status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error checking job status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void setApplicationList(List<Application> applicationList) {
        this.applicationList = applicationList;
        notifyDataSetChanged();
    }

}