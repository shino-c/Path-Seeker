package com.example.MAD;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class Login extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;

    TextView userName;

    CheckBox rmbMeCheckBox; // Remember Me Checkbox

    private static final String PREFS_NAME = "LoginPrefs";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        rmbMeCheckBox = findViewById(R.id.RmbMeCheckBox);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load saved login state
        loadSavedLoginState();
        initializePasswordVisibility(password);

        TextView forgotPW = findViewById(R.id.forgotPW);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validateEmail() | !validatePassword()){

                }else{
                    checkUser();
                }
            }
        });


        forgotPW.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: // Finger or mouse down
                case MotionEvent.ACTION_HOVER_ENTER: // Mouse enters
                    forgotPW.setPaintFlags(forgotPW.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    break;

                case MotionEvent.ACTION_UP: // Finger or mouse up
                case MotionEvent.ACTION_HOVER_EXIT: // Mouse exits
                    forgotPW.setPaintFlags(forgotPW.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));

                    Intent intent = new Intent(getApplicationContext(), ForgotPW.class);
                    startActivity(intent);
                    break;
            }
            return true;
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void loadSavedLoginState() {
        // Check if email and password are saved
        String savedEmail = sharedPreferences.getString("email", "");
        String savedPassword = sharedPreferences.getString("password", "");

        if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            // Pre-fill the login fields
            email.setText(savedEmail);
            password.setText(savedPassword);
            rmbMeCheckBox.setChecked(true); // Set the checkbox as checked
        }
    }

    private void saveLoginState(String email, String password) {
        // Save email and password if Remember Me is checked
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (rmbMeCheckBox.isChecked()) {
            editor.putString("email", email);
            editor.putString("password", password);
        } else {
            editor.clear(); // Clear saved login state if Remember Me is unchecked
        }
        editor.apply(); // Apply changes
    }

    public Boolean validateEmail(){
        String val = email.getText().toString();
        if(val.isEmpty()){
            email.setError("Username cannot be empty");
            return false;
        }else{
            return true;
        }
    }

    public Boolean validatePassword(){
        String val = password.getText().toString();
        if(val.isEmpty()){
            password.setError("Password cannot be empty");
            return false;
        }else{
            return true;
        }
    }

    public void checkUser() {
        String userEmail = email.getText().toString().trim().toLowerCase().replace(".", "_");
        String userPassword = password.getText().toString().trim();
        String hashPassword = hashPassword(userPassword);

        Log.d("LoginDebug", "Checking user: " + userEmail);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        // First, check if the user is a jobseeker
        reference.child("jobseeker").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    handleUserAuthentication(snapshot, hashPassword, "jobseeker");
                } else {
                    // If not found, check if the user is a recruiter
                    reference.child("recruiter").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                handleUserAuthentication(snapshot, hashPassword, "recruiter");
                            } else {
                                Log.d("LoginDebug", "User not found in both categories");
                                email.setError("User does not exist");
                                email.requestFocus();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("LoginDebug", "Error checking recruiter: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LoginDebug", "Error checking jobseeker: " + error.getMessage());
            }
        });
    }

    private void handleUserAuthentication(DataSnapshot userSnapshot, String hashPassword, String userType) {
        String passwordFromDB = userSnapshot.child("password").getValue(String.class);

        if (passwordFromDB != null && passwordFromDB.equals(hashPassword)) {
            // Extract normalized fields
            saveLoginState(email.getText().toString().trim(), password.getText().toString().trim());
            String userEmail = userSnapshot.child(userType.equals("recruiter") ? "companyMail" : "email").getValue(String.class);
            String userName = userSnapshot.child(userType.equals("recruiter") ? "companyName" : "name").getValue(String.class);

            String dob = userType.equals("jobseeker") ? userSnapshot.child("dob").getValue(String.class) : null;
            String workingStatus = userType.equals("jobseeker") ? userSnapshot.child("workingStatus").getValue(String.class) : null;
            String sector = userType.equals("recruiter") ? userSnapshot.child("sector").getValue(String.class) : null;

            // Pass all data to the next activity via Intent
            Intent intent = new Intent(Login.this, AppHomePage.class);
            intent.putExtra("userEmail", userEmail); // Unified email field
            intent.putExtra("userName", userName);   // Unified name field
            intent.putExtra("dob", dob);             // Jobseeker-specific
            intent.putExtra("workingStatus", workingStatus); // Jobseeker-specific
            intent.putExtra("sector", sector);       // Recruiter-specific
            startActivity(intent);
        } else {
            password.setError("Invalid credentials");
            password.requestFocus();
        }
    }


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

    // Helper method to validate credentials
    private void validateCredentials(DataSnapshot userSnapshot, String userPassword) {
        String passwordFromDB = userSnapshot.child("password").getValue(String.class);

        if (passwordFromDB != null && passwordFromDB.equals(userPassword)) {
            String userType = userSnapshot.getRef().getParent().getKey(); // Check if it's 'jobseeker' or 'recruiter'

            String name;
            String userEmail; // Store the email retrieved from the database

            if ("recruiter".equals(userType)) {
                name = userSnapshot.child("companyName").getValue(String.class); // Recruiter uses companyName
                userEmail = userSnapshot.child("companyMail").getValue(String.class); // Recruiter email is stored in 'companyMail'
            } else {
                name = userSnapshot.child("name").getValue(String.class); // Jobseeker uses name
                userEmail = userSnapshot.child("email").getValue(String.class); // Jobseeker email is stored in 'email'
            }

            // Create an intent and pass the email
            Intent intent = new Intent(Login.this, AppHomePage.class);
            intent.putExtra("userName", name); // Pass the username or companyName
            intent.putExtra("email", userEmail); // Pass the email or companyMail
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            password.setError("Invalid credentials");
            password.requestFocus();
        }
    }




    private void initializePasswordVisibility(View view) {
        EditText passwordInput = view.findViewById(R.id.passwordInput);

        passwordInput.setOnTouchListener(getPasswordVisibilityToggleListener(passwordInput));
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
}