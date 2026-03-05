package com.example.MAD;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class jobPostedAdapter extends RecyclerView.Adapter<jobPostedAdapter.MyViewHolder> {

    Context context;
    List<Job> jobList;
    private AlertDialog dialog;


    public jobPostedAdapter(Context context, List<Job> jobList) {
        this.context = context;
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_job, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Job job = jobList.get(position);


        DatabaseReference refCompany = FirebaseDatabase.getInstance().getReference("users").child("recruiter").child(job.getCompanyName()).child("companyName");

        refCompany.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve the value of the companyName key
                    String companyName = snapshot.getValue(String.class);
                    if (companyName != null) {
                        Log.d("Firebase", "Company Name: " + companyName);
                        // Do something with the company name, e.g., display it
                        holder.TVcompany.setText(companyName);
                    } else {
                        Log.e("Firebase", "Company Name is null.");
                    }
                } else {
                    Log.e("Firebase", "Company key not found in the database.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.TVjobTitle.setText(job.getTitle());
        holder.TVlocation.setText(job.getLocation());
        holder.TVremote.setText(job.getRemoteOptions().toString());
//        holder.IBdelete.setImageResource(R.drawable.baseline_delete_24);
        decodeBase64(job.getImageBase64(), holder.imageView);
        holder.IBdelete.setVisibility(View.GONE);

//        holder.IBdelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showPopUp();
//            }
//        });
    }

//    private void showPopUp() {
//        // Inflate the custom layout
//        LayoutInflater inflater = LayoutInflater.from(context);
//
//
//        View dialogview = inflater.inflate(R.layout.dialog_delete, null);
//
//        // Build the AlertDialog
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setView(dialogview);
//        dialog = builder.create();
//
//        // Apply custom background to dialog
//        dialog.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.dialog_box));
//;
//
//        // Set up the close button
//        ImageButton btnCancel = dialogview.findViewById(R.id.btnCancel);
//        btnCancel.setContentDescription("Close the dialog");
//        btnCancel.setOnClickListener(v -> dialog.dismiss());
//
//        // Set up the close button
//        ImageButton btnYes = dialogview.findViewById(R.id.btnYes);
//        btnYes.setContentDescription("Confirm Delete");
//        btnYes.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Implement the delete action here
//                Toast.makeText(context, "Job Deleted", Toast.LENGTH_SHORT).show();
//                dialog.dismiss();  // Close the dialog after action
//            }
//        });
//
//
//        // Optional: Allow dismissal on outside touch
//        dialog.setCanceledOnTouchOutside(true);
//
//        // Show the dialog
//        dialog.show();
//    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public void updateJobList(List<Job> newJobList) {
        this.jobList.clear();
        this.jobList.addAll(newJobList);
        notifyDataSetChanged();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView TVjobTitle, TVcompany, TVlocation, TVremote;
        ImageButton IBdelete;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.IVgoogle);
            TVjobTitle = itemView.findViewById(R.id.TVJobTitle);
            TVcompany = itemView.findViewById(R.id.TVCompanyName);
            TVlocation = itemView.findViewById(R.id.TVLocation);
            TVremote = itemView.findViewById(R.id.TVRemote);
            IBdelete = itemView.findViewById(R.id.IBbookmark);
        }
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

