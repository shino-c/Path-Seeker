package com.example.MAD;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecycleviewExperienceAdapter extends RecyclerView.Adapter<RecycleviewExperienceAdapter.MyViewHolder> {
    private Context context;
    private List<Experience> experienceList;

    String userEmail = UserSessionManager.getInstance().getUserEmail();

    String sanitizedEmail = userEmail.replace(".","_");

    // Constructor to initialize context and the experience list
    public RecycleviewExperienceAdapter(Context context, List<Experience> experienceList) {
        this.context = context;
        this.experienceList = experienceList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycleview_my_experience, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Get the experience object at the current position
        Experience experience = experienceList.get(position);
        String positionText = experience.getPosition(); // Use the position from the experience object
        holder.btnExp.setText(positionText);

        // Set up the button click to show details in a dialog
        holder.btnExp.setOnClickListener(v -> {
            showExperienceDialog(experience, position);  // Pass position to update list after changes
        });

        holder.btnDeteleExp.setOnClickListener(v -> {
            String expIdToDelete = experience.getExpID();  // Get the ID of the experience to delete
            DatabaseReference expRef = FirebaseDatabase.getInstance().getReference("users")
                    .child("jobseeker")
                    .child(sanitizedEmail)
                    .child("experience")
                    .child(expIdToDelete);

            expRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    experienceList.remove(position);  // Remove from list
                    notifyItemRemoved(position);  // Notify adapter of the removal

                    Toast.makeText(context, "Experience deleted!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(context, "Failed to delete experience", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    @Override
    public int getItemCount() {
        return experienceList.size();
    }

    // ViewHolder to hold references to the views
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Button btnExp;
        ImageButton btnDeteleExp;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            btnExp = itemView.findViewById(R.id.btnExp);
            btnDeteleExp = itemView.findViewById(R.id.btnDeleteExp);
        }
    }

    private DatabaseReference mDatabase;

    private void showExperienceDialog(Experience experience, int position) {
        // Initialize Firebase Database reference
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Create a new Dialog to display
        Dialog dialog = new Dialog(context);

        // Set the custom layout for the dialog
        dialog.setContentView(R.layout.my_experience);
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.dialog_boxs));

        // Show the dialog
        dialog.show();

        // Set up cancel button functionality to dismiss the dialog
        Button BtnCancel = dialog.findViewById(R.id.BtnCancel);
        BtnCancel.setOnClickListener(v -> dialog.dismiss());

        // Find the input fields and the save button
        Button btnSave = dialog.findViewById(R.id.btnSaveExp);
        EditText positionField = dialog.findViewById(R.id.position);
        EditText organization = dialog.findViewById(R.id.organization);
        EditText details = dialog.findViewById(R.id.details);

        // Pre-fill the dialog fields if editing an existing experience
        if (experience != null) {
            positionField.setText(experience.getPosition());
            organization.setText(experience.getOrganization());
            details.setText(experience.getDetails());
        }

        // Set save button click listener to update existing experience in Firebase
        btnSave.setOnClickListener(v -> {
            String positionText = positionField.getText().toString().trim();
            String organizationText = organization.getText().toString().trim();
            String detailsText = details.getText().toString().trim();

            // Validate the input fields
            if (!positionText.isEmpty() && !organizationText.isEmpty() && !detailsText.isEmpty()) {

                // Prepare data to upload
                Map<String, String> expData = new HashMap<>();
                expData.put("position", positionText);
                expData.put("organization", organizationText);
                expData.put("details", detailsText);

                // Specify the user's path in Firebase
                DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail).child("experience");

                // Update existing experience
                if (experience != null) {
                    // Use the expID to update the existing experience in Firebase
                    userRef.child(experience.getExpID()).setValue(expData).addOnSuccessListener(aVoid -> {
                        // Update the local experience object with the new values
                        experience.setPosition(positionText);
                        experience.setOrganization(organizationText);
                        experience.setDetails(detailsText);

                        // Notify RecyclerView to update this item
                        notifyItemChanged(position);

                        dialog.dismiss();
                        Toast.makeText(context, "Experience updated successfully!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update experience", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                // If any field is empty, show a toast message
                Toast.makeText(context, "Please fill in all fields!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}