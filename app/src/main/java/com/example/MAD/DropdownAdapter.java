package com.example.MAD;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DropdownAdapter extends RecyclerView.Adapter<DropdownAdapter.ViewHolder> {
    private List<String> items;
    private List<Boolean> selections;
    private Runnable updateHeaderText;

    public DropdownAdapter(List<String> items, List<Boolean> selections, Runnable updateHeaderText) {
        this.items = items;
        this.selections = selections;
        this.updateHeaderText = updateHeaderText;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.checkBox.setText(items.get(position));
        holder.checkBox.setChecked(selections.get(position));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selections.set(position, isChecked);
            updateHeaderText.run();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}


