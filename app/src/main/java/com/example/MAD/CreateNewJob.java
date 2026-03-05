package com.example.MAD;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateNewJob extends Fragment {

    private static final int CAMERA_REQUEST = 1888;
    private static final int GALLERY_REQUEST = 1889;

    private ImageView IVPreview;
    private EditText ETJobTitle, ETLocation, ETSalary, ETJobDesc, ETJobSkills;
    private Uri imageUri;
    String username;
    private String userEmail;

    public CreateNewJob() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_new_job, container, false);

        userEmail = UserSessionManager.getInstance().getUserEmail();
        username = userEmail.replace(".", "_");

        IVPreview = view.findViewById(R.id.IVPreview);
        ETJobTitle = view.findViewById(R.id.ETJobTitle);
        ETLocation = view.findViewById(R.id.ETLocation);
        ETSalary = view.findViewById(R.id.ETSalary);
        ETJobDesc = view.findViewById(R.id.ETJobDesc);
        ETJobSkills = view.findViewById(R.id.ETJobSkills);

        ImageButton btnBack = view.findViewById(R.id.IBback);
        btnBack.setOnClickListener(v->requireActivity().onBackPressed());

        ImageButton IBaddMedia = view.findViewById(R.id.IBaddMedia);
        IBaddMedia.setOnClickListener(v -> showImageSourceOptions());

        Button btnSaveJob = view.findViewById(R.id.btnSubmit);
        btnSaveJob.setOnClickListener(v -> {
            if (validateInputs()) { // Add this method for input validation
                saveJobToDatabase();
                Navigation.findNavController(requireView()).navigate(R.id.jobPostedFragment);
            } else {
                Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private boolean validateInputs() {
        String jobTitle = ETJobTitle.getText().toString().trim();
        String location = ETLocation.getText().toString().trim();
        String salaryString = ETSalary.getText().toString().trim();
        String description = ETJobDesc.getText().toString().trim();
        String skills = ETJobSkills.getText().toString().trim();

        // Ensure fields are not empty
        if (jobTitle.isEmpty() || location.isEmpty() || salaryString.isEmpty() ||
                description.isEmpty() || skills.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Ensure salary is numeric
        try {
            Double.parseDouble(salaryString);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Salary must be a valid number.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check for valid image
        if (imageUri == null) {
            Toast.makeText(getContext(), "Please upload an image.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Ensure at least one job type is selected
        ArrayList<String> jobTypes = getSelectedOptions(new int[]{
                R.id.CBFullTime, R.id.CBPartTime, R.id.CBContract, R.id.CBTemporary, R.id.CBInternship
        }, new String[]{"Full-Time", "Part-Time", "Contract", "Temporary", "Internship"});

        if (jobTypes.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one job type.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Ensure at least one remote option is selected
        ArrayList<String> remoteOptions = getSelectedOptions(new int[]{
                R.id.CBRemote, R.id.CBOnSite, R.id.CBHybrid
        }, new String[]{"Remote", "On-Site", "Hybrid"});

        if (remoteOptions.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one work type (Remote/On-Site/Hybrid).", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Ensure at least one experience level is selected
        ArrayList<String> experienceLevels = getSelectedOptions(new int[]{
                R.id.CBInternshipExp, R.id.CBEntryLevel, R.id.CBMidLevel, R.id.CBSeniorLevel, R.id.CBManagerial, R.id.CBExecutive, R.id.CBFreelance
        }, new String[]{"Internship", "Entry-Level", "Mid-Level", "Senior-Level", "Managerial", "Executive", "Freelance"});

        if (experienceLevels.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one experience level.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Ensure at least one job category is selected
        ArrayList<String> jobCategories = getSelectedOptions(new int[]{
                R.id.CBTech, R.id.CBEngineering, R.id.CBHealth, R.id.CBBusiness, R.id.CBEdu, R.id.CBCreative, R.id.CBRetail, R.id.CBFood,
                R.id.CBTransport, R.id.CBAdmin, R.id.CBLaw, R.id.CBScience, R.id.CBOthers
        }, new String[]{"Technology", "Engineering", "Healthcare", "Business", "Education", "Creative", "Retail", "Food",
                "Transportation", "Administrative", "Law", "Science", "Others"});

        if (jobCategories.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one job category.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showImageSourceOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        } else {
            File photoFile = null;
            try {
                photoFile = createImageFile(); // Create a temporary image file
            } catch (IOException ex) {
                Log.e("CreateNewJob", "Error creating file", ex);
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(), "com.example.MAD.fileprovider", photoFile);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI); // Save the image to this URI
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        imageUri = Uri.fromFile(image); // Set the image URI for later use
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                IVPreview.setImageURI(imageUri); // Preview the image using the URI
            } else if (requestCode == GALLERY_REQUEST) {
                imageUri = data.getData();
                IVPreview.setImageURI(imageUri);
            }
        } else {
            Toast.makeText(getContext(), "No image selected.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveJobToDatabase() {
        String jobTitle = ETJobTitle.getText().toString().trim();
        String location = ETLocation.getText().toString().trim();
        String salaryString = ETSalary.getText().toString().trim();
        double salary = Double.parseDouble(salaryString);
        String description = ETJobDesc.getText().toString().trim();
        String skills = ETJobSkills.getText().toString().trim();

        if (jobTitle.isEmpty() || location.isEmpty() || description.isEmpty() || skills.isEmpty() || salaryString.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng jobLatLng = getLatLngFromAddress(location);
        if (jobLatLng == null) {
            Toast.makeText(getContext(), "Invalid address provided.", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude = jobLatLng.latitude;
        double longitude = jobLatLng.longitude;

        ArrayList<String> jobTypes = getSelectedOptions(new int[]{
                R.id.CBFullTime, R.id.CBPartTime, R.id.CBContract, R.id.CBTemporary, R.id.CBInternship
        }, new String[]{"Full-Time", "Part-Time", "Contract", "Temporary", "Internship"});

        ArrayList<String> remoteOptions = getSelectedOptions(new int[]{
                R.id.CBRemote, R.id.CBOnSite, R.id.CBHybrid
        }, new String[]{"Remote", "On-Site", "Hybrid"});

        ArrayList<String> experienceLevels = getSelectedOptions(new int[]{
                R.id.CBInternshipExp, R.id.CBEntryLevel, R.id.CBMidLevel, R.id.CBSeniorLevel, R.id.CBManagerial, R.id.CBExecutive, R.id.CBFreelance
        }, new String[]{"Internship", "Entry-Level", "Mid-Level", "Senior-Level", "Managerial", "Executive", "Freelance"});

        ArrayList<String> jobCategories = getSelectedOptions(new int[]{
                R.id.CBTech, R.id.CBEngineering, R.id.CBHealth, R.id.CBBusiness, R.id.CBEdu, R.id.CBCreative, R.id.CBRetail, R.id.CBFood,
                R.id.CBTransport, R.id.CBAdmin, R.id.CBLaw, R.id.CBScience, R.id.CBOthers
        }, new String[]{"Technology", "Engineering", "Healthcare", "Business", "Education", "Creative", "Retail", "Food",
                "Transportation", "Administrative", "Law", "Science", "Others"});

        Job newJob = new Job("", jobTitle, location, salary, description, skills, jobTypes, remoteOptions, experienceLevels, jobCategories, "", latitude, longitude, 0);
        newJob.setCompanyName(username);
        newJob.setTimePosted(System.currentTimeMillis());

        if (imageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String base64ImageString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                newJob.setImageBase64(base64ImageString);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No image uploaded for the job.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Saving job...");
        progressDialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("jobs");
        String jobId = databaseReference.push().getKey();
        newJob.setJobID(jobId);

        DatabaseReference companyRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference postedJobsRef = companyRef.child("recruiter").child(username).child("postedJobs");

        postedJobsRef.get().addOnSuccessListener(dataSnapshot -> {
            ArrayList<String> jobIds = new ArrayList<>();
            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    jobIds.add(snapshot.getValue(String.class));
                }
            }
            jobIds.add(jobId);
            postedJobsRef.setValue(jobIds);
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error saving job IDs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        DatabaseReference jobRef = databaseReference.child(jobId);
        jobRef.setValue(newJob)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Job saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Error saving job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private ArrayList<String> getSelectedOptions(int[] checkBoxIds, String[] optionNames) {
        ArrayList<String> selectedOptions = new ArrayList<>();
        for (int i = 0; i < checkBoxIds.length; i++) {
            CheckBox checkBox = getView().findViewById(checkBoxIds[i]);
            if (checkBox.isChecked()) {
                selectedOptions.add(optionNames[i]);
            }
        }
        return selectedOptions;
    }

    public LatLng getLatLngFromAddress(String address) {
        Geocoder geocoder = new Geocoder(getContext());
        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null && addressList.size() > 0) {
                Address location = addressList.get(0);
                return new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}