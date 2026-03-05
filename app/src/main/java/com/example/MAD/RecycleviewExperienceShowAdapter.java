package com.example.MAD;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecycleviewExperienceShowAdapter extends RecyclerView.Adapter<RecycleviewExperienceShowAdapter.MyViewHolder> {
    private Context context;
    private List<ExperienceShow> expShowList;



    public RecycleviewExperienceShowAdapter(Context context, List<ExperienceShow> expShowList) {
        this.context = context;
        this.expShowList = expShowList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycleview_experience_show, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ExperienceShow expShow = expShowList.get(position);
        holder.poText.setText(expShow.getPosition());
        holder.orText.setText(expShow.getOrganization());
        holder.deText.setText(expShow.getDetails());
    }



    @Override
    public int getItemCount() {
        return expShowList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView poText, orText, deText;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            poText = itemView.findViewById(R.id.poText);
            orText = itemView.findViewById(R.id.orText);
            deText = itemView.findViewById(R.id.deText);

        }
    }
}