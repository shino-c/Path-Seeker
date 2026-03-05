package com.example.MAD;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.ProgramViewHolder> {

    private final List<Program> programs;
    private final Context context;

    public ProgramAdapter(List<Program> programs, Context context) {
        this.programs = programs;
        this.context = context;
    }

    @NonNull
    @Override
    public ProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_program, parent, false);
        return new ProgramViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramViewHolder holder, int position) {
        Program program = programs.get(position);
        holder.programTitle.setText(program.getTitle());
        holder.programImage.setImageResource(program.getImageResId());
        holder.joinButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(program.getLink()));
            context.startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return programs.size();
    }

    public static class ProgramViewHolder extends RecyclerView.ViewHolder {
        TextView programTitle;
        ImageView programImage;
        Button joinButton;

        public ProgramViewHolder(@NonNull View itemView) {
            super(itemView);
            programTitle = itemView.findViewById(R.id.program_title);
            programImage = itemView.findViewById(R.id.program_image);
            joinButton = itemView.findViewById(R.id.join_button);
        }
    }
}

