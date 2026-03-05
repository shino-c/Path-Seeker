package com.example.MAD;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class ProfileSelfSeekerFragment extends Fragment {

    private RecyclerView RVSkill, RVExpShow;
    private RecycleviewSkillAdapter adapter;
    private RecycleviewExperienceShowAdapter adapter2;
    private ArrayList<Skill> skillList = new ArrayList<>();
    private ArrayList<ExperienceShow> expShowList = new ArrayList<>();

    TextView nameSelfSeeker, statusSelfSeeker, numExpSelf, bioSelfSeeker;
    ImageView profilePhotoSelf;

    Button btnRetrievePDF2;

    TextView textView43;

    private DatabaseReference ratingRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_profile_seeker_self, container, false);
        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton btnSetting = view.findViewById(R.id.btnSetting2);
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile3);
        textView43 = view.findViewById(R.id.textView43);
        // Set click listeners
        btnSetting.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.seekerSettingFragment));

        btnEditProfile.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.seekerEditFragment));

        // Ensure userEmail is not null before sanitizing
        String userEmail = UserSessionManager.getInstance().getUserEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            String sanitizedEmail = userEmail.replace(".", "_");
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child("jobseeker").child(sanitizedEmail);
            ratingRef = userRef.child("rating");
            fetchAndDisplayAverageRating();
            // Initialize RecyclerView for Skills
            RVSkill = view.findViewById(R.id.RVSkill);
            adapter = new RecycleviewSkillAdapter(requireContext(), skillList);
            RVSkill.setAdapter(adapter);
            RVSkill.setLayoutManager(new LinearLayoutManager(requireContext()));
            setUpSkill(userRef);

            // Initialize RecyclerView for Experience
            RVExpShow = view.findViewById(R.id.RVExpShow);
            adapter2 = new RecycleviewExperienceShowAdapter(requireContext(), expShowList);
            RVExpShow.setAdapter(adapter2);
            RVExpShow.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            setUpExp(userRef);

            // Initialize Views
            nameSelfSeeker = view.findViewById(R.id.nameSelfSeeker);
            nameSelfSeeker.setText(UserSessionManager.getInstance().getUserName());

            statusSelfSeeker = view.findViewById(R.id.statusSelfSeeker);
            statusSelfSeeker.setText(UserSessionManager.getInstance().getWorkingStatus());

            bioSelfSeeker = view.findViewById(R.id.bioSelfSeeker);
            accessBio(userRef);

            profilePhotoSelf = view.findViewById(R.id.profilePhotoSelf);
            accessProfile(userRef);

            btnRetrievePDF2 = view.findViewById(R.id.btnRetrievePDF2);
            btnRetrievePDF2.setOnClickListener(v->retrievePDFFromDatabase(userRef));

            numExpSelf = view.findViewById(R.id.numExpSelf);


        } else {
            Log.e("ProfileSelfSeekerFragment", "User email is null or empty.");
        }

        return view;
    }


    private void setUpSkill(DatabaseReference userRef) {
        userRef.child("skills").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        skillList.clear();

                        for (DataSnapshot skillIdSnapshot : dataSnapshot.getChildren()) {
                            String skillId = skillIdSnapshot.getKey();
                            Skill skill = skillIdSnapshot.getValue(Skill.class);
                            if (skill != null) {
                                skill.setSkillID(skillId);
                                skillList.add(skill);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("setUpSkill", "No skills found for user.");
                    }
                } catch (Exception e) {
                    Log.e("Error", "Error fetching skill details: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to load skill.", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error loading skill: " + databaseError.getMessage());
            }
        });
    }

    private void setUpExp(DatabaseReference userRef) {
        userRef.child("experience").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        expShowList.clear();

                        for (DataSnapshot expIdSnapshot : dataSnapshot.getChildren()) {
                            String expId = expIdSnapshot.getKey();
                            ExperienceShow exp = expIdSnapshot.getValue(ExperienceShow.class);
                            if (exp != null) {
                                exp.setExpID(expId);
                                expShowList.add(exp);
                            }
                        }

                        adapter2.notifyDataSetChanged();
                        numExpSelf.setText(String.valueOf(expShowList.size()));

                    } else {
                        Log.d("setUpExp", "No experience found for user.");
                    }
                } catch (Exception e) {
                    Log.e("setUpExp", "Error fetching experience details: " + e.getMessage(), e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("setUpExp", "Error loading experience: " + databaseError.getMessage());
            }
        });
    }

    private void accessBio(DatabaseReference userRef) {
        userRef.child("bio").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String existingBio = dataSnapshot.getValue(String.class);
                    bioSelfSeeker.setText(existingBio);
                } else {
                    bioSelfSeeker.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing bio: " + databaseError.getMessage());
            }
        });
    }

    private void accessProfile(DatabaseReference userRef) {
        userRef.child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profileImageBase64 = dataSnapshot.getValue(String.class);
                    if (profileImageBase64 != null) {
                        byte[] imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT);
                        Bitmap profileImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        profilePhotoSelf.setImageBitmap(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing profile photo: " + databaseError.getMessage());
            }
        });
    }

    private void retrievePDFFromDatabase(DatabaseReference userRef) {

        // Reference to the user's 'resume' node in the database
        DatabaseReference userPdfRef = userRef.child("resume");

        // Get the data for the resume
        userPdfRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(requireContext(), "No PDF found for this user", Toast.LENGTH_SHORT).show();
            } else {
                // Retrieve the base64-encoded PDF string and fileName
                String base64EncodedPDF = snapshot.child("base64Pdf").getValue(String.class);
                String fileName = snapshot.child("fileName").getValue(String.class);

                if (base64EncodedPDF != null && fileName != null) {
                    // Decode the base64 string to get the PDF bytes
                    byte[] pdfBytes = Base64.decode(base64EncodedPDF, Base64.DEFAULT);

                    // Download the PDF with the appropriate fileName
                    downloadPDF(pdfBytes, fileName);
                } else {
                    Toast.makeText(requireContext(), "PDF data is incomplete", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error retrieving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void downloadPDF(byte[] pdfBytes, String fileName) {
        try {
            // Check if we are on Android 10 (API 29) or later
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore to save the file in the Downloads folder (Scoped Storage for Android 10+)
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/");

                Uri pdfUri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (pdfUri != null) {
                    // Open output stream to save the PDF file
                    try (OutputStream outputStream = requireContext().getContentResolver().openOutputStream(pdfUri)) {
                        if (outputStream != null) {
                            outputStream.write(pdfBytes);
                            Toast.makeText(requireContext(), "PDF downloaded successfully", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error downloading PDF", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // For devices below Android 10, write to external storage directly
                File downloadsFolder = new File(requireContext().getExternalFilesDir(null), "Download");
                if (!downloadsFolder.exists()) {
                    downloadsFolder.mkdirs();
                }

                // Save PDF in the downloads folder
                File file = new File(downloadsFolder, fileName);
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(pdfBytes);
                    Toast.makeText(requireContext(), "PDF downloaded successfully", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Error downloading PDF", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error saving PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAndDisplayAverageRating() {
        // Reference to the rating node under the user

        // Fetch ratings and calculate the average
        ratingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float totalScore = 0;
                int ratingCount = 0;

                // Loop through each rating entry
                for (DataSnapshot ratingSnapshot : dataSnapshot.getChildren()) {
                    Float score = ratingSnapshot.child("score").getValue(Float.class);
                    if (score != null) {
                        totalScore += score;
                        ratingCount++;
                    }
                }

                // Calculate and display the average rating
                displayAverageRating(totalScore, ratingCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Error fetching ratings: " + databaseError.getMessage());
            }
        });
    }

    private void displayAverageRating(float totalScore, int ratingCount) {
        if (ratingCount > 0) {
            // Calculate average score
            float averageScore = totalScore / ratingCount;
            textView43.setText(String.format("%.1f", averageScore));
        } else {
            textView43.setText("0");
        }
    }

}