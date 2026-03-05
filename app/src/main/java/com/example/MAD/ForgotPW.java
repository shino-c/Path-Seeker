package com.example.MAD;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ForgotPW extends AppCompatActivity {

    private EditText emailInput;
    private Button resetPWBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pw);

        initializeUI();
    }

    /**
     * Initializes UI components and sets up listeners.
     */
    private void initializeUI() {
        // Back button functionality
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());

        // Cancel button functionality
        Button cancelBtn = findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(v -> finish());

        emailInput = findViewById(R.id.emailInput);
        resetPWBtn = findViewById(R.id.resetPWBtn);

        // Email input validation listener
        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(emailInput);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Reset password button functionality
        resetPWBtn.setOnClickListener(v -> {
            if (validateEmail(emailInput)) {
                checkUser();
            }
        });
    }

    /**
     * Validates the email input.
     *
     * @param email The email input field.
     * @return True if the email is valid, false otherwise.
     */
    private boolean validateEmail(EditText email) {
        String emailText = email.getText().toString().trim();

        if (emailText.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.setError("Enter a valid email address");
            email.requestFocus();
            return false;
        } else {
            email.setError(null);
            email.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,
                    getResources().getDrawable(R.drawable.done_icon, null), null);
            return true;
        }
    }

    /**
     * Checks if the user exists in the database.
     */
    private void checkUser() {
        String userEmail = emailInput.getText().toString().trim().toLowerCase().replace(".", "_");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        // Check "jobseeker" path
        Query jobseekerQuery = reference.child("jobseeker").child(userEmail);
        jobseekerQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    handleUserFound("jobseeker", userEmail);
                } else {
                    // Check "recruiter" path if "jobseeker" not found
                    Query recruiterQuery = reference.child("recruiter").child(userEmail);
                    recruiterQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                handleUserFound("recruiter", userEmail);
                            } else {
                                emailInput.setError("User email does not exist");
                                emailInput.requestFocus();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            handleDatabaseError(error);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
            }
        });
    }


    /**
     * Handles a found user by generating and sending a verification code.
     *
     * @param userType The type of user (jobseeker or recruiter).
     * @param userEmail The user's email.
     */
    private void handleUserFound(String userType, String userEmail) {
        String verificationCode = generateVerificationCode();
        sendChangePasswordVerificationCode(userEmail.replace("_", "."), verificationCode);

        // Show the dialog with the verification code and pass the user type
        showVerificationCodeDialog(verificationCode, userType);
    }

    /**
     * Generates a random 5-digit verification code.
     *
     * @return The generated verification code.
     */
    private String generateVerificationCode() {
        return String.valueOf((int) (Math.random() * 90000) + 10000);
    }

    /**
     * Sends the verification code to the user's email.
     *
     * @param email The user's email.
     * @param verificationCode The verification code.
     */
    private void sendChangePasswordVerificationCode(String email, String verificationCode) {
        String userEmailKey = email.toLowerCase().replace(".", "_");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        // Attempt to retrieve the user's name from the database
        reference.child("jobseeker").child(userEmailKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    sendEmail(name, email, verificationCode);
                } else {
                    reference.child("recruiter").child(userEmailKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String name = snapshot.child("name").getValue(String.class);
                                sendEmail(name, email, verificationCode);
                            } else {
                                sendEmail("User", email, verificationCode);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            handleDatabaseError(error);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error);
            }
        });
    }

    /**
     * Sends the email with the verification code.
     *
     * @param name            The user's name.
     * @param email           The user's email.
     * @param verificationCode The verification code.
     */
    private void sendEmail(String name, String email, String verificationCode) {
        String senderEmail = "ooiruizhe@gmail.com";
        String senderPassword = "jolahdelotzfzogm";
        String host = "smtp.gmail.com";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Verification Code to Change Password");

            String emailContent = "<html><body>"
                    + "<p>Dear <b>" + name + "</b>,</p>"
                    + "<p>Your verification code is: <b style=\"font-size: 18px; color: #4CAF50;\">" + verificationCode + "</b></p>"
                    + "<p>Please use this code to change your password.</p>"
                    + "<p style=\"margin-top: 20px;\">Best Regards,</p>"
                    + "<p><b>Ooi Rui Zhe</b><br>"
                    + "<i><b>System Administrator</b></i><br>"
                    + "<b>PathSeeker</b></p>"
                    + "</body></html>";

            message.setContent(emailContent, "text/html");

            // Run email-sending in a separate thread
            new Thread(() -> {
                try {
                    Transport.send(message);
                    runOnUiThread(() -> Toast.makeText(ForgotPW.this, "Verification code sent to your email.", Toast.LENGTH_SHORT).show());
                } catch (MessagingException e) {
                    Log.e("ForgotPW", "Error sending email: " + e.getMessage(), e);
                    runOnUiThread(() -> Toast.makeText(ForgotPW.this, "Failed to send verification email. Please try again.", Toast.LENGTH_SHORT).show());
                }
            }).start();

        } catch (MessagingException e) {
            Log.e("ForgotPW", "Error creating email message: " + e.getMessage(), e);
            Toast.makeText(ForgotPW.this, "An error occurred while preparing the email.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays the verification code dialog.
     *
     * @param verificationCode The verification code.
     */
    private void showVerificationCodeDialog(String verificationCode, String userType) {
        LayoutInflater inflater = getLayoutInflater();
        View customView = inflater.inflate(R.layout.changepw_confirmation_layout, null);

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(ForgotPW.this)
                .setView(customView)
                .setCancelable(false) // Prevent dismissal by back button or outside touch
                .create();

        EditText verificationCodeInput = customView.findViewById(R.id.verificationCode); // User's input field
        Button proceedToChangePasswordBtn = customView.findViewById(R.id.submitVerificationBtn);
        Button cancelVerificationBtn = customView.findViewById(R.id.cancelVerificationBtn);

        proceedToChangePasswordBtn.setOnClickListener(v -> {
            String enteredCode = verificationCodeInput.getText().toString().trim();

            if (!enteredCode.equals(verificationCode)) {
                verificationCodeInput.setError("Incorrect verification code, please try again");
                verificationCodeInput.requestFocus();
            } else {
                dialog.dismiss(); // Dismiss the current dialog

                // Show the change password dialog and pass the user type
                showChangePasswordDialog(userType);
            }
        });

        cancelVerificationBtn.setOnClickListener(v -> dialog.dismiss()); // Close the dialog

        dialog.show(); // Ensure the dialog is shown before any navigation
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // Declare dialog references at the class level
    private android.app.AlertDialog changePasswordDialog;
    private android.app.AlertDialog successDialog;

    private void showChangePasswordDialog(String userType) {
        LayoutInflater inflater = getLayoutInflater();
        View changePasswordView = inflater.inflate(R.layout.changepw_layout, null);

        // Initialize the password visibility toggle
        initializePasswordVisibility(changePasswordView);

        changePasswordDialog = new android.app.AlertDialog.Builder(ForgotPW.this)
                .setView(changePasswordView)
                .setCancelable(false)
                .create();

        EditText newPasswordInput = changePasswordView.findViewById(R.id.newPasswordInput);
        EditText confirmPasswordInput = changePasswordView.findViewById(R.id.confirmNewPasswordInput);
        Button confirmChangePasswordBtn = changePasswordView.findViewById(R.id.submitNewPasswordBtn);
        Button cancelChangePWBtn = changePasswordView.findViewById(R.id.cancelChangePWBtn);

        confirmChangePasswordBtn.setOnClickListener(v -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (newPassword.isEmpty()) {
                newPasswordInput.setError("Password is required");
                newPasswordInput.requestFocus();
            } else if (newPassword.length() < 6) {
                newPasswordInput.setError("Password must be at least 6 characters");
                newPasswordInput.requestFocus();
            } else if (!newPassword.equals(confirmPassword)) {
                confirmPasswordInput.setError("Passwords do not match");
                confirmPasswordInput.requestFocus();
            } else {
                // Proceed with password change (e.g., update in the database)
                changePassword(newPassword, userType); // Pass the user type here
                dismissDialog(changePasswordDialog); // Dismiss the current dialog
                showSuccessDialog(); // Show the success dialog
            }
        });

        cancelChangePWBtn.setOnClickListener(v -> {
            dismissDialog(changePasswordDialog); // Dismiss the current dialog
            navigateToLogin(); // Navigate back to login
        });

        changePasswordDialog.show();
        if (changePasswordDialog.getWindow() != null) {
            changePasswordDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void showSuccessDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View successView = inflater.inflate(R.layout.changepwsuccess_layout, null);

        successDialog = new android.app.AlertDialog.Builder(ForgotPW.this)
                .setView(successView)
                .setCancelable(false)
                .create();

        Button backToLoginBtn = successView.findViewById(R.id.backToLoginBtn);

        backToLoginBtn.setOnClickListener(v -> {
            dismissDialog(successDialog); // Dismiss the success dialog
            navigateToLogin(); // Navigate back to login
        });

        successDialog.show();
        if (successDialog.getWindow() != null) {
            successDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // Utility method to dismiss a dialog if it's showing
    private void dismissDialog(android.app.AlertDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    // Utility method to navigate to the login page
    private void navigateToLogin() {
        Intent intent = new Intent(ForgotPW.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initializePasswordVisibility(View view) {
        EditText passwordNewInput = view.findViewById(R.id.newPasswordInput);
        EditText confirmNewPasswordInput = view.findViewById(R.id.confirmNewPasswordInput);

        passwordNewInput.setOnTouchListener(getPasswordVisibilityToggleListener(passwordNewInput));
        confirmNewPasswordInput.setOnTouchListener(getPasswordVisibilityToggleListener(confirmNewPasswordInput));
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

    private void changePassword(String newPassword, String userType) {
        String hashedPassword = hashPassword(newPassword); // Hash the password
        String userEmailKey = emailInput.getText().toString().trim().toLowerCase().replace(".", "_");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

        // Update the password based on the user type
        reference.child(userType).child(userEmailKey).child("password").setValue(hashedPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                    } else {
                        Toast.makeText(ForgotPW.this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
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

    /**
     * Handles database errors.
     *
     * @param error The database error.
     */
    private void handleDatabaseError(DatabaseError error) {
        Log.e("ForgotPW", "Database error: " + error.getMessage());
        Toast.makeText(ForgotPW.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
    }
}
