package com.example.MAD;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class job_RecyclerViewAdapter extends RecyclerView.Adapter<job_RecyclerViewAdapter.JobViewHolder> {

    private final Context context;
    private final List<Job> jobList = new ArrayList<>();
    private final String username;
    private final boolean isSavedJobsScreen;
    private OnBookmarkChangeListener bookmarkChangeListener;

    public interface OnBookmarkChangeListener {
        void onBookmarkChanged(String jobId, boolean isBookmarked);
    }

    public job_RecyclerViewAdapter(Context context, String username, boolean isSavedJobsScreen) {
        this.context = context;
        this.username = username;
        this.isSavedJobsScreen = isSavedJobsScreen;
    }

    public void setOnBookmarkChangeListener(OnBookmarkChangeListener listener) {
        this.bookmarkChangeListener = listener;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);

        if (username == null || username.isEmpty()) {
            Log.e("JobAdapter", "Username is null or empty.");
            return;
        }

        if (job == null || job.getJobID() == null || job.getJobID().isEmpty()) {
            Log.e("JobAdapter", "Job or Job ID is null or empty.");
            return;
        }

        populateJobDetails(holder, job);
        handleBookmarking(holder, job);

        holder.itemView.setOnClickListener(v -> openJobDetails(v, job.getJobID()));
    }

    private void populateJobDetails(JobViewHolder holder, Job job) {
        fetchCompanyName(job.getCompanyName(), holder.companyTextView);
        holder.jobTitleTextView.setText(job.getTitle());
        holder.locationTextView.setText(job.getLocation());
        holder.remoteTextView.setText(job.getRemoteOptions().toString());
        decodeBase64AndSetImage(job.getImageBase64(), holder.jobImageView);
    }

    private void fetchCompanyName(String companyId, TextView companyTextView) {
        DatabaseReference companyRef = FirebaseDatabase.getInstance()
                .getReference("users/recruiter/")
                .child(companyId)
                .child("companyName");

        companyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    companyTextView.setText(snapshot.getValue(String.class));
                } else {
                    Log.e("JobAdapter", "Company name not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("JobAdapter", "Failed to fetch company name", error.toException());
            }
        });
    }

    private void handleBookmarking(JobViewHolder holder, Job job) {
        DatabaseReference savedJobsRef = FirebaseDatabase.getInstance()
                .getReference("users/jobseeker/")
                .child(username)
                .child("savedJobs");

        savedJobsRef.child(job.getJobID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isBookmarked = snapshot.exists();
                updateBookmarkIcon(holder.bookmarkButton, isBookmarked);

                holder.bookmarkButton.setOnClickListener(v -> {
                    holder.bookmarkButton.setEnabled(false);
                    if (isBookmarked) {
                        removeBookmark(savedJobsRef, job, holder);
                    } else {
                        addBookmark(savedJobsRef, job, holder);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("JobAdapter", "Failed to check bookmark status", error.toException());
            }
        });
    }

    private void addBookmark(DatabaseReference ref, Job job, JobViewHolder holder) {
        ref.child(job.getJobID()).setValue(job, (error, ref1) -> {
            holder.bookmarkButton.setEnabled(true);
            if (error == null) {
                Toast.makeText(context, "Added to saved jobs", Toast.LENGTH_SHORT).show();
                updateBookmarkIcon(holder.bookmarkButton, true);
                notifyBookmarkChanged(job.getJobID(), true);
            } else {
                Toast.makeText(context, "Failed to add bookmark", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeBookmark(DatabaseReference ref, Job job, JobViewHolder holder) {
        ref.child(job.getJobID()).removeValue((error, ref1) -> {
            holder.bookmarkButton.setEnabled(true);
            if (error == null) {
                Toast.makeText(context, "Removed from saved jobs", Toast.LENGTH_SHORT).show();
                updateBookmarkIcon(holder.bookmarkButton, false);
                notifyBookmarkChanged(job.getJobID(), false);

                if (isSavedJobsScreen) {
                    int position = jobList.indexOf(job);
                    if (position != -1) {
                        jobList.remove(position);
                        notifyItemRemoved(position);
                    }
                }
            } else {
                Toast.makeText(context, "Failed to remove bookmark", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyBookmarkChanged(String jobId, boolean isBookmarked) {
        if (bookmarkChangeListener != null) {
            bookmarkChangeListener.onBookmarkChanged(jobId, isBookmarked);
        }
    }

    private void updateBookmarkIcon(ImageButton bookmarkButton, boolean isBookmarked) {
        bookmarkButton.setImageResource(isBookmarked ?
                R.drawable.baseline_bookmark_24 :
                R.drawable.baseline_bookmark_border_24);
    }

    private void openJobDetails(View view, String jobId) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("jobId", jobId); // Pass the jobId to the next fragment
            Navigation.findNavController(view).navigate(R.id.jobDetailsFragment, bundle);
        } catch (Exception e) {
            Log.e("JobAdapter", "Navigation failed", e);
        }
    }

    private void decodeBase64AndSetImage(String base64String, ImageView imageView) {
        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imageView.setImageBitmap(decodedBitmap);
        } catch (Exception e) {
            Log.e("JobAdapter", "Failed to decode base64 image", e);
        }
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public void updateJobList(List<Job> newJobList) {
        jobList.clear();
        jobList.addAll(newJobList);
        notifyDataSetChanged();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {
        final ImageView jobImageView;
        final TextView jobTitleTextView, companyTextView, locationTextView, remoteTextView;
        final ImageButton bookmarkButton;

        JobViewHolder(@NonNull View itemView) {
            super(itemView);
            jobImageView = itemView.findViewById(R.id.IVgoogle);
            jobTitleTextView = itemView.findViewById(R.id.TVJobTitle);
            companyTextView = itemView.findViewById(R.id.TVCompanyName);
            locationTextView = itemView.findViewById(R.id.TVLocation);
            remoteTextView = itemView.findViewById(R.id.TVRemote);
            bookmarkButton = itemView.findViewById(R.id.IBbookmark);
        }
    }
}