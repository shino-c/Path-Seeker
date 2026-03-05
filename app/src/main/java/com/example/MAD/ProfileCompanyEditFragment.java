package com.example.MAD;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileCompanyEditFragment extends Fragment {
    private static final int CAMERA_REQUEST = 1888;
    private static final int GALLERY_REQUEST = 1889;

    private ImageView profilePhotoCom;
    private ImageButton cameraCom;
    private Bitmap image;
    private String imageType;
    private Button btnLink;
    private String insertedUrl;
    private Uri selectedFileUri;
    private EditText name, sector, bio;

    private DatabaseReference mDatabase;

    private View rootView;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    String userEmail, sanitizedEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile_company_edit, container, false);

        // Fetch the user's email from the session manager
        userEmail = UserSessionManager.getInstance().getUserEmail();

        // Check if the user email is null or empty
        if (TextUtils.isEmpty(userEmail)) {
            Log.e("ProfileCompanyEdit", "User email is null or empty.");
            return rootView; // Or navigate to login screen if necessary
        }

        sanitizedEmail = userEmail.replace(".", "_");
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Log the sanitized email for debugging purposes
        Log.d("ProfileCompanyEdit", "Sanitized email: " + sanitizedEmail);

        // Initialize the views
        initializeViews();

        // Check for existing data
        checkForExistingProfilePhoto();
        checkForExistingName();
        checkForExistingSector();
        checkForExistingBio();
        checkForExistingUrl();

        // Save button functionality
        Button btnSaveCom = rootView.findViewById(R.id.btnSaveCom);
        btnSaveCom.setOnClickListener(v -> {
            if (editBioToDatabase()) {
                editNameToDatabase();
                editSectorToDatabase();
                uploadImageToDatabase(image, "profile");
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup image pickers (gallery/camera)
        setupLaunchers();

        return rootView;
    }

    private void initializeViews() {
        cameraCom = rootView.findViewById(R.id.cameraCom);
        profilePhotoCom = rootView.findViewById(R.id.profilePhotoCom);
        name = rootView.findViewById(R.id.editNameCom);
        sector = rootView.findViewById(R.id.sectorText);
        bio = rootView.findViewById(R.id.editBioCom);
        btnLink = rootView.findViewById(R.id.btnLink);

        // Handle button clicks for taking a photo or selecting an image from the gallery
        cameraCom.setOnClickListener(v -> showImageSourceOptions());
        btnLink.setOnClickListener(v -> showUrlDialog());

        // Back button navigation
        ImageButton btnBackCom = rootView.findViewById(R.id.btnBackCom);
        btnBackCom.setOnClickListener(v -> {
            Navigation.findNavController(rootView).navigateUp();
        });
    }

    private void setupLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                            profilePhotoCom.setImageBitmap(bitmap);
                            image = bitmap;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        profilePhotoCom.setImageBitmap(photo);
                        image = photo;
                    }
                });
    }

    private void showImageSourceOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select an Image")
                .setItems(new String[]{"Take a Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        if (requireActivity().checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(cameraIntent);
        }
        imageType = "profile";
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
        imageType = "profile";
    }

    private void checkForExistingProfilePhoto() {
        if (TextUtils.isEmpty(sanitizedEmail)) {
            Log.e("ProfileCompanyEdit", "Sanitized email is empty");
            return;
        }
        DatabaseReference userRef = mDatabase.child("recruiter").child(sanitizedEmail);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profileImageBase64 = dataSnapshot.child("profile").getValue(String.class);
                    if (profileImageBase64 != null) {
                        byte[] imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT);
                        Bitmap profileImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        profilePhotoCom.setImageBitmap(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing profile photo: " + databaseError.getMessage());
            }
        });
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

        if (TextUtils.isEmpty(sanitizedEmail)) {
            Log.e("ProfileCompanyEdit", "Sanitized email is empty");
            return;
        }

        DatabaseReference userRef = mDatabase.child("recruiter").child(sanitizedEmail);

        userRef.updateChildren(imageData)
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), imageType + " image uploaded successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to upload " + imageType + " image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void checkForExistingName() {
        if (TextUtils.isEmpty(sanitizedEmail)) {
            Log.e("ProfileCompanyEdit", "Sanitized email is empty");
            return;
        }
        DatabaseReference userRef = mDatabase.child("recruiter").child(sanitizedEmail);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String existingName = dataSnapshot.child("companyName").getValue(String.class);
                    name.setText(existingName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing name: " + databaseError.getMessage());
            }
        });
    }

    private void editNameToDatabase() {
        if (name != null) {
            DatabaseReference userRef = mDatabase.child("recruiter").child(sanitizedEmail);
            userRef.child("companyName").setValue(name.getText().toString());
        }
    }

    private void checkForExistingSector() {
        DatabaseReference userRef = mDatabase.child("recruiter").child(sanitizedEmail);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String existingSector = dataSnapshot.child("sector").getValue(String.class);
                    sector.setText(existingSector);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing sector: " + databaseError.getMessage());
            }
        });
    }

    private void editSectorToDatabase() {
        if (sector != null) {
            DatabaseReference userRef = mDatabase.child("recruiter").child(sanitizedEmail);
            userRef.child("sector").setValue(sector.getText().toString());
        }
    }

    private void checkForExistingBio() {
        DatabaseReference userRef = mDatabase.child("recruiter").child(sanitizedEmail);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String existingBio = dataSnapshot.child("bio").getValue(String.class);
                    bio.setText(existingBio);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing bio: " + databaseError.getMessage());
            }
        });
    }

    private boolean editBioToDatabase() {
        if (bio != null) {
            DatabaseReference userRef = mDatabase.child("recruiter").child(sanitizedEmail);
            userRef.child("bio").setValue(bio.getText().toString());
            return true;
        }
        return false;
    }

    private void showUrlDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.insert_url);
        dialog.getWindow().setBackgroundDrawable(requireContext().getDrawable(R.drawable.dialog_box));
        dialog.setTitle("Insert URL");

        EditText insertLink = dialog.findViewById(R.id.insertLink);
        Button saveButton = dialog.findViewById(R.id.btnSaveLink);
        saveButton.setOnClickListener(v -> {
            String url = insertLink.getText().toString().trim();
            if (!TextUtils.isEmpty(url) && Patterns.WEB_URL.matcher(url).matches()) {
                btnLink.setText(url);
                insertedUrl = url;
                saveUrlToDatabase(insertedUrl);
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "Please enter a valid URL.", Toast.LENGTH_SHORT).show();
            }
        });

        Button cancel = dialog.findViewById(R.id.BtnCancel);
        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveUrlToDatabase(String url) {
        if (TextUtils.isEmpty(sanitizedEmail)) {
            Log.e("ProfileCompanyEdit", "Sanitized email is empty");
            return;
        }
        DatabaseReference linkRef = mDatabase.child("recruiter").child(sanitizedEmail).child("link");
        linkRef.setValue(url)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "URL saved successfully"))
                .addOnFailureListener(e -> Log.e("Firebase", "Error saving URL: " + e.getMessage()));
    }

    private void checkForExistingUrl() {
        DatabaseReference userRef = mDatabase.child("recruiter").child(sanitizedEmail);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String existingLink = dataSnapshot.child("link").getValue(String.class);
                    if (existingLink != null) {
                        btnLink.setText(existingLink);
                    } else {
                        btnLink.setText("Website Link"); // Default text if no link is set
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Error checking for existing URL: " + databaseError.getMessage());
            }
        });
    }
}