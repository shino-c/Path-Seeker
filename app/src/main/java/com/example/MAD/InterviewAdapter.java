package com.example.MAD;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InterviewAdapter extends RecyclerView.Adapter<InterviewAdapter.ViewHolder> {

    private Context context;
    private List<Application> interviewList;
    String username;

    public InterviewAdapter(Context context, String username) {
        this.context = context;
        this.username = username;
        this.interviewList = new ArrayList<>(); // Ensure it is not null
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_interview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Application application = interviewList.get(position);

        holder.jobTitle.setText(application.getPosition());
        holder.companyName.setText(application.getCompanyName());

        String interviewDateTime = application.getInterviewDateTime();

        // Call the method to separate the date and time
        separateDateAndTime(interviewDateTime, holder.date, holder.time);

        decodeBase64(application.getImageBase64(), holder.profile);
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
        return interviewList.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView jobTitle, companyName, date, time;
        ShapeableImageView profile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.TVJobTitle);
            companyName = itemView.findViewById(R.id.TVCompanyName);
            date = itemView.findViewById(R.id.TVDate);
            time = itemView.findViewById(R.id.TVTime);
            profile = itemView.findViewById(R.id.IVgoogle);
        }
    }

    // Separate date and time from interviewDateTime string and set to TextViews
    private void separateDateAndTime(String interviewDateTime, TextView dateTextView, TextView timeTextView) {
        try {
            // Define the format of your interviewDateTime string
            SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());

            // Parse the date and time string into a Date object
            Date date = dateFormat.parse(interviewDateTime);

            // Now, separate the date and time components
            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            String dateString = dateOnlyFormat.format(date);  // Date as a string

            SimpleDateFormat timeOnlyFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String timeString = timeOnlyFormat.format(date);  // Time as a string

            // Set the date and time to the corresponding TextViews
            dateTextView.setText(dateString);  // Set the formatted date
            timeTextView.setText(timeString);  // Set the formatted time

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateJobList(List<Application> newJobList) {
        this.interviewList.clear();
        this.interviewList.addAll(newJobList);
        notifyDataSetChanged();
    }
}

