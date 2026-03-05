package com.example.MAD;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class JobTypeAdapter extends RecyclerView.Adapter<JobTypeAdapter.MyViewHolder> {

    Context context;
    List<String> jobTypeList; // List of job type strings
    List<String> selectedJobTypes; // Store selected job types

    public JobTypeAdapter(Context context, List<String> jobTypeList) {
        this.context = context;
        this.jobTypeList = jobTypeList != null ? new ArrayList<>(jobTypeList) : new ArrayList<>();
        this.selectedJobTypes = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_checkbox, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String jobType = jobTypeList.get(position);

        // Bind the job type to the CheckBox
        holder.checkBox.setText(jobType);

        // Set the current selection state
        holder.checkBox.setChecked(selectedJobTypes.contains(jobType));

        // Handle CheckBox clicks
        holder.checkBox.setOnClickListener(v -> {
            if (holder.checkBox.isChecked()) {
                selectedJobTypes.add(jobType); // Add to selected list
            } else {
                selectedJobTypes.remove(jobType); // Remove from selected list
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobTypeList.size();
    }

    // Update the adapter's data
    public void updateJobTypeList(List<String> newJobTypeList) {
        this.jobTypeList.clear();
        this.jobTypeList.addAll(newJobTypeList);
        notifyDataSetChanged();
    }

    // Get selected job types
    public List<String> getSelectedJobTypes() {
        return selectedJobTypes;
    }

    public void setJobTypeList(List<String> jobTypeList) {
        this.jobTypeList = jobTypeList;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.CBJobType);
        }
    }
}

