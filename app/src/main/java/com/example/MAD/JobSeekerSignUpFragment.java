package com.example.MAD;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class JobSeekerSignUpFragment extends Fragment {

    // Arguments
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    FirebaseDatabase database;
    DatabaseReference reference;

    public JobSeekerSignUpFragment() {
        // Required empty public constructor
    }

    public static JobSeekerSignUpFragment newInstance(String param1, String param2) {
        JobSeekerSignUpFragment fragment = new JobSeekerSignUpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_job_seeker_sign_up, container, false);

        initializeBackButton(view);
        initializeOccupationSpinner(view);
        initializeSignInLink(view);
        initializePasswordVisibility(view);

        EditText emailInput = view.findViewById(R.id.emailInput);
        EditText passwordInput = view.findViewById(R.id.passwordInput);
        EditText confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
        EditText nameInput = view.findViewById(R.id.name);
        EditText dateOfBirthInput = view.findViewById(R.id.dateOfBirth);

        Spinner occupationSpinner = view.findViewById(R.id.occupationSpinner);
        TextView occupationErrorText = view.findViewById(R.id.occupationErrorText);
        Button signUpBtn = view.findViewById(R.id.signUpBtnJS);
        occupationErrorText.setVisibility(View.GONE);


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFieldsAndShowSuccess(view);
            }
        });
        occupationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isFirstTime = true;
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // If it's the first time, do not show the error message
                if (isFirstTime) {
                    isFirstTime = false;  // Set the flag to false after the first selection
                    return;  // Don't show the error message
                }
                if (position > 0) { // Assuming the default "Select" option is at position 0
                    occupationErrorText.setVisibility(View.GONE);
                    occupationErrorText.setText("");
                    isFirstTime = true;
                }

                // Check if the default "Select" option is selected
                if (position == 0) { // Assuming the default "Select" option is at position 0
                    occupationErrorText.setText("Please select a valid working status");
                    occupationErrorText.setVisibility(View.VISIBLE);
                } else {
                    occupationErrorText.setVisibility(View.GONE);
                    occupationErrorText.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                occupationErrorText.setVisibility(View.VISIBLE); // Show error if nothing is selected
            }
        });

        dateOfBirthInput.setOnClickListener(v -> showDatePickerDialog(dateOfBirthInput));


        emailInput.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);


        // Email validation in real-time
        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validateEmail(emailInput);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Password and confirm password validation in real-time
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validatePasswords(passwordInput, confirmPasswordInput);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        confirmPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validatePasswords(passwordInput, confirmPasswordInput);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        nameInput.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validateName(nameInput);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        dateOfBirthInput.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                validateAge(dateOfBirthInput);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return view;
    }

    private void showDatePickerDialog(EditText dateOfBirthInput) {
        // Get current date to set as default in the date picker
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the selected date
                    String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    // Set the selected date in the EditText
                    dateOfBirthInput.setText(selectedDate);
                }, year, month, day);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }


    // Initializes the back button
    private void initializeBackButton(View view) {
        ImageView backIcon = view.findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    // Initializes the occupation Spinner with adapter and listener
    private void initializeOccupationSpinner(View view) {
        Spinner occupationSpinner = view.findViewById(R.id.occupationSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.occupation_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        occupationSpinner.setAdapter(adapter);
        occupationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Initializes the Sign In link with underline effect
    private void initializeSignInLink(View view) {
        TextView signin = view.findViewById(R.id.signin);
        signin.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_HOVER_ENTER:
                    signin.setPaintFlags(signin.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_HOVER_EXIT:
                    signin.setPaintFlags(signin.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                    Intent intent = new Intent(requireContext(), Login.class);
                    startActivity(intent);
                    break;
            }
            return true;
        });
    }

    // Initializes password visibility toggle for password and confirm password
    private void initializePasswordVisibility(View view) {
        EditText passwordInput = view.findViewById(R.id.passwordInput);
        EditText confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);

        passwordInput.setOnTouchListener(getPasswordVisibilityToggleListener(passwordInput));
        confirmPasswordInput.setOnTouchListener(getPasswordVisibilityToggleListener(confirmPasswordInput));
    }

    // Returns a listener to toggle password visibility on touch
    private View.OnTouchListener getPasswordVisibilityToggleListener(EditText passwordInput) {
        return (v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable drawableEnd = passwordInput.getCompoundDrawablesRelative()[2];
                if (drawableEnd != null && event.getRawX() >= (passwordInput.getRight() - drawableEnd.getBounds().width())) {
                    togglePasswordVisibility(passwordInput);
                    return true;
                }
            }
            return false;
        };
    }

    // Toggle password visibility and change icon
    private void togglePasswordVisibility(EditText passwordInput) {
        Typeface currentTypeface = passwordInput.getTypeface();
        if (passwordInput.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordInput.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,
                    getResources().getDrawable(R.drawable.password_icon, null), null); // Open eye icon
        } else {
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordInput.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,
                    getResources().getDrawable(R.drawable.eyeclose, null), null); // Closed eye icon
        }
        passwordInput.setTypeface(currentTypeface);
        passwordInput.setSelection(passwordInput.getText().length());
    }

    // Validate passwords match and meet criteria
    private void validatePasswords(EditText password, EditText confirmPassword) {
        String passwordText = password.getText().toString();
        String confirmPasswordText = confirmPassword.getText().toString();

        // Validate password length and emptiness first
        if (passwordText.isEmpty()) {
            password.setError("Password is required");
        } else if (passwordText.length() < 6) {
            password.setError("Password must be at least 6 characters");
        } else {
            password.setError(null); // Clear password error if valid
        }

        // Check if confirm password matches and handle password strength
        if (confirmPasswordText.isEmpty()) {
            confirmPassword.setError("This field cannot be empty");
        } else if (!passwordText.equals(confirmPasswordText)) {
            confirmPassword.setError("Passwords do not match");
        } else if (passwordText.length() < 6) {
            // Check for weak password (less than 6 characters) even if they match
            confirmPassword.setError("Please make sure your password is more than 6 characters");
        } else {
            confirmPassword.setError(null); // Clear confirm password error if valid
        }
    }

    // Validate email format
    private void validateEmail(EditText email) {
        String emailText = email.getText().toString();

        if (emailText.isEmpty()) {
            email.setError("Email is required");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.setError("Invalid email address");
        } else {
            email.setError(null);
            email.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,
                    getResources().getDrawable(R.drawable.done_icon, null), null);
        }
    }

    private void validateName(EditText name){
        String nameText = name.getText().toString();

        if (nameText.isEmpty()){
            name.setError("This field cannot be empty.");
        } else {
            name.setError(null);
        }
    }
    private void validateAge(EditText ageInput) {
        String dob = ageInput.getText().toString();
        int age = countAge(dob);

        if (age == -1) {
            ageInput.setError("Invalid date format or invalid date");
        } else if (age < 16) {
            ageInput.setError("You must be at least 16 years old");
        } else {
            ageInput.setError(null);
        }
    }

    private int countAge(String dob) {
        // Parse the date of birth string into a Date object
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        simpleDateFormat.setLenient(false); // This ensures strict parsing

        try {
            Date birthDate = simpleDateFormat.parse(dob);

            // Check if the year is below 1900, which is an invalid year
            Calendar birthCalendar = Calendar.getInstance();
            birthCalendar.setTime(birthDate);
            int year = birthCalendar.get(Calendar.YEAR);
            if (year < 1900) {
                return -1; // Return -1 if the year is invalid (below 1900)
            }

            // Check if the birthDate is valid (not in the future)
            if (birthDate.after(new Date())) {
                return -1; // Return -1 if the birthdate is in the future
            }

            // Validate the day/month combination
            int maxDay = birthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (birthCalendar.get(Calendar.DAY_OF_MONTH) > maxDay) {
                return -1; // Return -1 if the day exceeds the valid days in the month
            }

            // Get the current date
            Calendar currentCalendar = Calendar.getInstance();

            // Calculate the difference in years
            int age = currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);

            // Check if the current date has passed the birth date this year
            if (currentCalendar.get(Calendar.MONTH) < birthCalendar.get(Calendar.MONTH) ||
                    (currentCalendar.get(Calendar.MONTH) == birthCalendar.get(Calendar.MONTH) &&
                            currentCalendar.get(Calendar.DAY_OF_MONTH) < birthCalendar.get(Calendar.DAY_OF_MONTH))) {
                age--; // Subtract one year if the birthday hasn't passed yet
            }

            return age;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 in case of invalid date format or parsing issues
    }

    private void checkFieldsAndShowSuccess(View view) {
        EditText emailInput = view.findViewById(R.id.emailInput);
        EditText passwordInput = view.findViewById(R.id.passwordInput);
        EditText confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
        EditText nameInput = view.findViewById(R.id.name);
        EditText dateOfBirthInput = view.findViewById(R.id.dateOfBirth);
        Spinner occupationSpinner = view.findViewById(R.id.occupationSpinner);
        TextView occupationErrorText = view.findViewById(R.id.occupationErrorText);

        // Validate each field
        boolean hasError = false;

        // Validate Email
        if (emailInput.getText().toString().isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            hasError = true;
        }

        // Validate Name
        else if (nameInput.getText().toString().isEmpty()) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            hasError = true;
        }

        // Validate Date of Birth
        else if (dateOfBirthInput.getText().toString().isEmpty()) {
            dateOfBirthInput.setError("Date of birth is required");
            dateOfBirthInput.requestFocus();
            hasError = true;
        }

        // Validate Occupation
        else if (occupationSpinner.getSelectedItemPosition() == 0) {
            occupationErrorText.setText("Please select a valid working status");
            occupationErrorText.setVisibility(View.VISIBLE);
            occupationSpinner.requestFocus();
            hasError = true;
        }

        // Validate Password
        else if (passwordInput.getText().toString().isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            hasError = true;
        }

        // Validate Confirm Password
        else if (confirmPasswordInput.getText().toString().isEmpty()) {
            confirmPasswordInput.setError("Confirm password is required");
            confirmPasswordInput.requestFocus();
            hasError = true;
        }
        // Check if passwords match
        else if (!passwordInput.getText().toString().equals(confirmPasswordInput.getText().toString())) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            hasError = true;
        }

        // If no errors, proceed to check if email already exists in Firebase
        if (!hasError) {
            String email = emailInput.getText().toString();
            String sanitizedEmail = email.replace(".", "_").replace("#", "_");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Check if email exists in "jobseeker" node
            databaseReference.child("users").child("jobseeker").child(sanitizedEmail)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot jobseekerSnapshot) {
                            if (jobseekerSnapshot.exists()) {
                                // If email exists in "jobseeker", show error and return
                                emailInput.setError("Email is already registered as a jobseeker");
                                emailInput.requestFocus();
                            } else {
                                // Check if email exists in "recruiter" node
                                databaseReference.child("users").child("recruiter").child(sanitizedEmail)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot recruiterSnapshot) {
                                                if (recruiterSnapshot.exists()) {
                                                    // If email exists in "recruiter", show error and return
                                                    emailInput.setError("Email is already registered as a recruiter");
                                                    emailInput.requestFocus();
                                                } else {
                                                    // If email does not exist in either node, proceed with user registration
                                                    proceedWithSignUp(view);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                // Handle errors during the recruiter email check
                                                Toast.makeText(view.getContext(), "Error checking recruiter email. Please try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle errors during the jobseeker email check
                            Toast.makeText(view.getContext(), "Error checking jobseeker email. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void proceedWithSignUp(View view) {
        // If email does not exist, proceed with the original sign-up logic
        EditText emailInput = view.findViewById(R.id.emailInput);
        EditText passwordInput = view.findViewById(R.id.passwordInput);
        EditText nameInput = view.findViewById(R.id.name);
        EditText dateOfBirthInput = view.findViewById(R.id.dateOfBirth);
        Spinner occupationSpinner = view.findViewById(R.id.occupationSpinner);

        String name = nameInput.getText().toString();
        String dob = dateOfBirthInput.getText().toString();
        String workStatus = occupationSpinner.getSelectedItem().toString();
        String email = emailInput.getText().toString();
        String sanitizedEmail = email.replace(".", "_").replace("#", "_");
        String password = passwordInput.getText().toString();

        new Thread(() -> {
            try {
                // Hash the password before storing it
                String hashedPassword = hashPassword(password);

                JobSeekerHelperClass jobSeekerHelperClass = new JobSeekerHelperClass(email, name, dob, workStatus, hashedPassword);

                // Store the user data in the database
                FirebaseDatabase.getInstance().getReference("users").child("jobseeker").child(sanitizedEmail).setValue(jobSeekerHelperClass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                getActivity().runOnUiThread(this::showSignUpSuccessDialog); // Run on main thread to show dialog
                            } else {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(view.getContext(), "Error saving user data.", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
            } catch (RuntimeException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(view.getContext(), "Error hashing password. Please try again.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Hash password using SHA-256
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: SHA-256 algorithm not available", e);
        }
    }

    // Convert bytes to a hexadecimal string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Show a dialog or frame for successful sign-up
    private void showSignUpSuccessDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View customView = inflater.inflate(R.layout.signupsuccess_dialog_layout, null);

        // Create the dialog
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(customView)
                .setCancelable(false)
                .create();

        Button proceedToLoginBtn = customView.findViewById(R.id.proceedToLoginBtn);
        proceedToLoginBtn.setOnClickListener(v -> {
            // Redirect to the login activity
            Intent intent = new Intent(requireContext(), Login.class);
            startActivity(intent);
            dialog.dismiss(); // Close dialog
            clearFormFields();
            clearAllErrors();
        });

        // Show the dialog
        dialog.show();

        // Set the background of the dialog to transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // Clear form fields after successful sign-up
    private void clearFormFields() {
        View view = getView(); // Get the current fragment view
        if (view != null) {
            EditText emailInput = view.findViewById(R.id.emailInput);
            EditText passwordInput = view.findViewById(R.id.passwordInput);
            EditText confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
            EditText nameInput = view.findViewById(R.id.name);
            EditText dateOfBirthInput = view.findViewById(R.id.dateOfBirth);
            Spinner occupationSpinner = view.findViewById(R.id.occupationSpinner);
            TextView occupationErrorText = view.findViewById(R.id.occupationErrorText);

            // Clear the EditText fields
            emailInput.setText("");
            passwordInput.setText("");
            confirmPasswordInput.setText("");
            nameInput.setText("");
            dateOfBirthInput.setText("");

            // Reset spinner to default position
            occupationSpinner.setSelection(0);
            occupationErrorText.setVisibility(View.GONE);
            occupationErrorText.setText("");
        }
    }

    // Clear all field errors
    private void clearAllErrors() {
        View view = getView();
        if (view != null) {
            // Find the input fields
            EditText emailInput = view.findViewById(R.id.emailInput);
            EditText passwordInput = view.findViewById(R.id.passwordInput);
            EditText confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
            EditText nameInput = view.findViewById(R.id.name);
            EditText dateOfBirthInput = view.findViewById(R.id.dateOfBirth);
            Spinner occupationSpinner = view.findViewById(R.id.occupationSpinner);
            TextView occupationErrorText = view.findViewById(R.id.occupationErrorText);

            // Clear errors on EditText fields
            emailInput.setError(null);
            passwordInput.setError(null);
            confirmPasswordInput.setError(null);
            nameInput.setError(null);
            dateOfBirthInput.setError(null);

            // Hide occupation spinner error message
            occupationErrorText.setVisibility(View.GONE);
            occupationErrorText.setText("");
        }
    }
}

