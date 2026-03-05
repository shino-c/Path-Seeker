package com.example.MAD;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context context;
    private List<String> titles;
    private List<Integer> images;
    private List<String> descriptions;
    private List<Mentor> mentors;  // Add this to store complete mentor data

    public MyAdapter(Context context, List<String> titles, List<Integer> images, List<String> descriptions, List<Mentor> mentors) {
        this.context = context;
        this.titles = titles;
        this.images = images;
        this.descriptions = descriptions;
        this.mentors = mentors;
    }

    public void setFilteredList(List<String> filteredTitles, List<Integer> filteredImages,
                                List<String> filteredDescriptions, List<Mentor> filteredMentors) {
        this.titles = filteredTitles;
        this.images = filteredImages;
        this.descriptions = filteredDescriptions;
        this.mentors = filteredMentors;  // Add this
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.mTextView.setText(titles.get(position));
        holder.mImageView.setImageResource(images.get(position));
        holder.mDescription.setText(descriptions.get(position));

        Mentor mentor = mentors.get(position);
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("profileImage", mentor.mentor_profilepic);
            bundle.putString("profileName", mentor.mentor_name);
            bundle.putString("profileTitle", mentor.mentor_title);
            bundle.putInt("profileYearsExperience", mentor.mentor_year_experience);
            bundle.putString("profileLivingArea", mentor.mentor_living_area);
            bundle.putString("profileBio", mentor.mentor_description);

            Navigation.findNavController(v).navigate(R.id.scheduleFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;
        TextView mTextView;
        TextView mDescription;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.imageview);
            mTextView = itemView.findViewById(R.id.textview);
            mDescription = itemView.findViewById(R.id.description);
        }
    }
}