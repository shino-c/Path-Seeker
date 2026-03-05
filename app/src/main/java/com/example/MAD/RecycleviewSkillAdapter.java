package com.example.MAD;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecycleviewSkillAdapter extends RecyclerView.Adapter<RecycleviewSkillAdapter.MyViewHolder> {
    private Context context;
    private List<Skill> skillList;



    public RecycleviewSkillAdapter(Context context, List<Skill> skillList) {
        this.context = context;
        this.skillList = skillList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycleview_skill, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Skill skill = skillList.get(position);
        String text = skill.getSkill();
        float rate = skill.getRating();

        holder.skillText.setText(text);
        holder.ratingText.setRating(rate);
    };


    @Override
    public int getItemCount() {
        return skillList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView skillText;
        RatingBar ratingText;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            skillText = itemView.findViewById(R.id.skilText);
            ratingText = itemView.findViewById(R.id.ratingText);
        }
    }
}