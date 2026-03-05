package com.example.MAD;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecycleviewJobOfferedAdapter extends RecyclerView.Adapter<RecycleviewJobOfferedAdapter.MyViewHolder> {

    Context context;
    List<jobOffered> jobList;

    public RecycleviewJobOfferedAdapter(Context context, List<jobOffered> jobList) {
        this.context = context;
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycleview_company, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        jobOffered job = jobList.get(position);

        // Bind the job title to the TVJobPosition TextView
        holder.TVJobPosition.setText(job.getTitle());

        // Bind the job categories to TVJobCategory TextView
        // Assuming jobCategory is a list and you want to join them as a string
        StringBuilder jobCategories = new StringBuilder();
        for (String category : job.getJobCategory()) {
            jobCategories.append(category).append(", ");
        }

        // Remove last comma and space if there's any category
        if (jobCategories.length() > 0) {
            jobCategories.setLength(jobCategories.length() - 2);
        }

        holder.TVJobCategory.setText(jobCategories.toString());
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        Button btnDetails;
        TextView TVJobCategory, TVJobPosition;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            TVJobCategory = itemView.findViewById(R.id.TVJobCategory);
            TVJobPosition = itemView.findViewById(R.id.TVJobPosition);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}
