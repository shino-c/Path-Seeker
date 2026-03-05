package com.example.MAD;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileSeekerEditFragment extends Fragment {
    private static final int PICK_PDF_REQUEST = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int GALLERY_REQUEST = 1889;

    private View rootView;
    private ImageView profilePhoto;
    private ImageButton camera;
    private Bitmap image;
    private String imageType;
    private Button btnUploadPDF;
    private DatabaseReference mDatabase;
    private Uri selectedFileUri;
    private EditText name, status, bio;
    private RecyclerView RVExp;
    private EditText skill1, skill2, skill3;
    private RatingBar ratingBarS1, ratingBarS2, ratingBarS3;
    private ArrayList<Experience> experienceList = new ArrayList<>();
    private RecycleviewExperienceAdapter adapter;

    String userEmail = UserSessionManager.getInstance().getUserEmail();

    String sanitizedEmail = userEmail.replace(".","_");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile_seeker_edit, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews();
        setupListeners();
        initializeFirebase();
        checkExistingData();
        setupRecyclerView();
    }

    private void initializeViews() {
        camera = rootView.findViewById(R.id.camera);
        profilePhoto = rootView.findViewById(R.id.profilePhoto);
        btnUploadPDF = rootView.findViewById(R.id.btnUploadPDF);
        name = rootView.findViewById(R.id.editName);
        status = rootView.findViewById(R.id.statusText);
        bio = rootView.findViewById(R.id.editBio);
        RVExp = rootView.findViewById(R.id.RVExp);
        skill1 = rootView.findViewById(R.id.skill1);
        skill2 = rootView.findViewById(R.id.skill2);
        skill3 = rootView.findViewById(R.id.skill3);
        ratingBarS1 = rootView.findViewById(R.id.ratingBarS1);
        ratingBarS2 = rootView.findViewById(R.id.ratingBarS2);
        ratingBarS3 = rootView.findViewById(R.id.ratingBarS3);
    }

    private void setupListeners() {
        camera.setOnClickListener(v -> showImageSourceOptions());
        btnUploadPDF.setOnClickListener(v -> openFileChooser());

        Button btnAdd = rootView.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> showExpDialog());

        Button btnSave = rootView.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            if(editBioToDatabase()) {
                uploadImageToDatabase(image, "profile");
                uploadPDFToDatabase(selectedFileUri);
                editNameToDatabase();
                editStatusToDatabase();
                uploadSkillToDatabase();
                Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton btnBack = rootView.findViewById(R.id.btnBackProf);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void initializeFirebase() {
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
    }

    private void checkExistingData() {
        checkForExistingResume();
        checkForExistingProfilePhoto();
        checkForExistingName();
        checkForExistingStatus();
        checkForExistingBio();
        checkForExistingSkill();
    }

    private void setupRecyclerView() {
        adapter = new RecycleviewExperienceAdapter(getContext(), experienceList);
        RVExp.setAdapter(adapter);
        RVExp.setLayoutManager(new LinearLayoutManager(getContext()));
        setUpExperience();
    }

    private void setUpExperience() {
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("users")
                .child("jobseeker")
                .child(sanitizedEmail)
                .child("experience");

        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        experienceList.clear();

                        for (DataSnapshot expIdSnapshot : dataSnapshot.getChildren()) {
                            String expId = expIdSnapshot.getKey();
                            Log.d("Experience ID", "Fetched expId: " + expId);

                            DatabaseReference expRef = FirebaseDatabase.getInstance().getReference("users")
                                    .child("jobseeker")
                                    .child(sanitizedEmail)
                                    .child("experience")
                                    .child(expId);

                            expRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot expDataSnapshot) {
                                    try {
                                        Experience exp = expDataSnapshot.getValue(Experience.class);
                                        if (exp != null) {
                                            exp.setExpID(expId);
                                            experienceList.add(exp);
                                        }
                                        adapter.notifyDataSetChanged();
                                    } catch (Exception e) {
                                        Log.e("Error", "Error fetching experience details: " + e.getMessage());
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("FirebaseError", "Error loading experience details: " + databaseError.getMessage());
                                }
                            });
                        }
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    Log.e("Error", "Error in onDataChange: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load experience.", Toast.LENGTH_SHORT).show();
                }
                Log.e("FirebaseError", "Error loading experience: " + databaseError.getMessage());
            }
        });
    }

    private void showImageSourceOptions() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select an Image")
                .setItems(new String[]{"Take a Photo", "Choose from Gallery"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openCamera();
                            break;
                        case 1:
                            openGallery();
                            break;
                    }
                })
                .show();
    }

    private void openCamera() {
        if (getContext() == null || getActivity() == null) return;

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
        imageType = "profile";
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
        imageType = "profile";
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getContext() == null || resultCode != getActivity().RESULT_OK || data == null) return;

        try {
            if (requestCode == CAMERA_REQUEST) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                profilePhoto.setImageBitmap(photo);
                image = photo;
            } else if (requestCode == GALLERY_REQUEST) {
                Uri selectedImageUri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                profilePhoto.setImageBitmap(bitmap);
                image = bitmap;
            } else if (requestCode == PICK_PDF_REQUEST) {
                selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    long fileSize = getFileSize(selectedFileUri);
                    if (fileSize > 10 * 1024 * 1024) {
                        Toast.makeText(getContext(), "File size exceeds 10MB. Please upload a smaller PDF.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        String fileName = getFileName(selectedFileUri);
                        btnUploadPDF.setText(fileName);
                    }
                } else {
                    Toast.makeText(getContext(), "No file selected", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getFileSize(Uri uri) {
        if (getContext() == null) return 0;

        long fileSize = 0;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    fileSize = cursor.getLong(sizeIndex);
                }
                cursor.close();
            }
        }
        return fileSize;
    }

    private String getFileName(Uri uri) {
        if (getContext() == null) return null;

        String fileName = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        }
        return fileName;
    }

    private void uploadImageToDatabase(Bitmap image, String imageType) {
        if (image == null) {
            Log.e("UploadImage", "Image is null, cannot upload.");
            return;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String base64EncodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        Map<String, Object> imageData = new HashMap<>();
        imageData.put(imageType, base64EncodedImage);

        DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail);

        userRef.updateChildren(imageData)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), imageType + " image uploaded successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to upload " + imageType + " image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPDFToDatabase(Uri fileUri) {
        if (fileUri == null || getContext() == null) {
            Log.e("UploadPDF", "File URI is null or context is null, cannot upload.");
            return;
        }

        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            byte[] pdfBytes = byteArrayOutputStream.toByteArray();
            String base64EncodedPDF = Base64.encodeToString(pdfBytes, Base64.DEFAULT);

            String fileName = getFileName(fileUri);

            Map<String, Object> pdfData = new HashMap<>();
            pdfData.put("fileName", fileName);
            pdfData.put("base64Pdf", base64EncodedPDF);

            DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail).child("resume");

            userRef.setValue(pdfData)
                    .addOnSuccessListener(aVoid -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Resume uploaded successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to upload resume: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error uploading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkForExistingProfilePhoto() {
        DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profileImageBase64 = dataSnapshot.child("profile").getValue(String.class);
                    if (profileImageBase64 != null) {
                        byte[] imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT);
                        Bitmap profileImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        profilePhoto.setImageBitmap(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing profile photo: " + databaseError.getMessage());
            }
        });
    }

    private void checkForExistingResume() {
        DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail).child("resume");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String existingFileName = dataSnapshot.child("fileName").getValue(String.class);
                    btnUploadPDF.setText(existingFileName);
                } else {
                    btnUploadPDF.setText("Upload CV pdf.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing resume: " + databaseError.getMessage());
            }
        });
    }

    private void editNameToDatabase() {
        if (name == null) {
            Log.e("Name Update", "Name is null, cannot upload.");
            return;
        }

        DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail).child("name");
        userRef.setValue(name.getText().toString().trim());
    }

    private void checkForExistingBio() {
        DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String existingBio = dataSnapshot.child("bio").getValue(String.class);
                    bio.setText(existingBio);
                } else {
                    bio.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing bio: " + databaseError.getMessage());
            }
        });
    }

    private void editStatusToDatabase() {
        if (status == null) {
            Log.e("Status Update", "Status is null, cannot upload.");
            return;
        }

        DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail).child("workingStatus");
        userRef.setValue(status.getText().toString().trim());
    }

    private void checkForExistingName() {
        DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String existingName = dataSnapshot.child("name").getValue(String.class);
                    name.setText(existingName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing name: " + databaseError.getMessage());
            }
        });
    }

    private void checkForExistingStatus() {
        DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String existingStatus = dataSnapshot.child("workingStatus").getValue(String.class);
                    status.setText(existingStatus);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing status: " + databaseError.getMessage());
            }
        });
    }

    private boolean editBioToDatabase() {
        if (bio == null) {
            Log.e("Bio Update", "Bio is null, cannot upload.");
            return false;
        }

        DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail).child("bio");

        int words = countWords(bio.getText().toString().trim());

        if (words <= 50) {
            userRef.setValue(bio.getText().toString().trim());
            return true;
        } else {
            if (getContext() != null) {
                Toast.makeText(getContext(), "About Me cannot exceed 50 words.", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }

    private int countWords(String text) {
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    private void checkForExistingSkill() {
        DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail).child("skills");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String existingSkill1 = dataSnapshot.child("skill1").child("skill").getValue(String.class);
                    if (existingSkill1 != null) {
                        skill1.setText(existingSkill1);
                    }

                    Float existingRateS1 = dataSnapshot.child("skill1").child("rating").getValue(Float.class);
                    if (existingRateS1 != null) {
                        ratingBarS1.setRating(existingRateS1);
                    }

                    String existingSkill2 = dataSnapshot.child("skill2").child("skill").getValue(String.class);
                    if (existingSkill2 != null) {
                        skill2.setText(existingSkill2);
                    }

                    Float existingRateS2 = dataSnapshot.child("skill2").child("rating").getValue(Float.class);
                    if (existingRateS2 != null) {
                        ratingBarS2.setRating(existingRateS2);
                    }

                    String existingSkill3 = dataSnapshot.child("skill3").child("skill").getValue(String.class);
                    if (existingSkill3 != null) {
                        skill3.setText(existingSkill3);
                    }

                    Float existingRateS3 = dataSnapshot.child("skill3").child("rating").getValue(Float.class);
                    if (existingRateS3 != null) {
                        ratingBarS3.setRating(existingRateS3);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing skills: " + databaseError.getMessage());
            }
        });
    }

    private void uploadSkillToDatabase() {
        String skill1Text = skill1.getText().toString();
        String skill2Text = skill2.getText().toString();
        String skill3Text = skill3.getText().toString();

        float rating1 = ratingBarS1.getRating();
        float rating2 = ratingBarS2.getRating();
        float rating3 = ratingBarS3.getRating();

        Map<String, Object> skillData1 = new HashMap<>();
        skillData1.put("skill", skill1Text);
        skillData1.put("rating", rating1);

        Map<String, Object> skillData2 = new HashMap<>();
        skillData2.put("skill", skill2Text);
        skillData2.put("rating", rating2);

        Map<String, Object> skillData3 = new HashMap<>();
        skillData3.put("skill", skill3Text);
        skillData3.put("rating", rating3);

        DatabaseReference userRef = mDatabase.child("jobseeker").child(sanitizedEmail).child("skills");

        Map<String, Object> allSkillsData = new HashMap<>();

        if (skill1Text != null && !skill1Text.isEmpty() && rating1 != 0) {
            allSkillsData.put("skill1", skillData1);
        } else {
            userRef.child("skill1").removeValue();
        }

        if (skill2Text != null && !skill2Text.isEmpty() && rating2 != 0) {
            allSkillsData.put("skill2", skillData2);
        } else {
            userRef.child("skill2").removeValue();
        }

        if (skill3Text != null && !skill3Text.isEmpty() && rating3 != 0) {
            allSkillsData.put("skill3", skillData3);
        } else {
            userRef.child("skill3").removeValue();
        }

        userRef.setValue(allSkillsData);
    }

    public void showExpDialog() {
        if (getContext() == null) return;

        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.my_experience);
        dialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.dialog_boxs));
        dialog.show();

        Button btnCancel = dialog.findViewById(R.id.BtnCancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        Button btnSave = dialog.findViewById(R.id.btnSaveExp);
        EditText position = dialog.findViewById(R.id.position);
        EditText organization = dialog.findViewById(R.id.organization);
        EditText details = dialog.findViewById(R.id.details);

        btnSave.setOnClickListener(v -> {
            if (!position.getText().toString().isEmpty() &&
                    !organization.getText().toString().isEmpty() &&
                    !details.getText().toString().isEmpty()) {

                Map<String, String> expData = new HashMap<>();
                expData.put("position", position.getText().toString());
                expData.put("organization", organization.getText().toString());
                expData.put("details", details.getText().toString());

                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                        .child("jobseeker").child(sanitizedEmail).child("experience");

                String experienceId = userRef.push().getKey();
                if (experienceId != null) {
                    userRef.child(experienceId).setValue(expData)
                            .addOnSuccessListener(aVoid -> {
                                Experience newExperience = new Experience(
                                        position.getText().toString(),
                                        organization.getText().toString(),
                                        details.getText().toString(),
                                        experienceId
                                );

                                experienceList.add(newExperience);
                                adapter.notifyItemInserted(experienceList.size() - 1);

                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Experience added successfully!",
                                            Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Failed to add experience.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to add experience.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Please complete all fields.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}