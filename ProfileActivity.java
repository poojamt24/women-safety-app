package com.example.womensafetyapp;

import static com.example.womensafetyapp.R.id.nameEditText;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ProfileActivity extends AppCompatActivity {
    private EditText nameEditText;
    private EditText phoneNumberEditText;
    private Button updateButton;
    private EditText otpEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        updateButton = findViewById(R.id.updateButton);

        // Retrieve user data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String phoneNumber = sharedPreferences.getString("phonenumber", "");

        // Set the retrieved data to the EditText fields
        nameEditText.setText(username);
        phoneNumberEditText.setText(phoneNumber);

        // Set click listener for the "Update" button
        updateButton.setOnClickListener(view -> {
            // Get updated data from EditText fields
            String updatedUsername = nameEditText.getText().toString();
            String updatedPhoneNumber = phoneNumberEditText.getText().toString();

            // Check if the phone number has changed
            if (!updatedPhoneNumber.equals(phoneNumber)) {
                // If the phone number has changed, send OTP for verification
                if (updatedPhoneNumber.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Please enter the New Phone number", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateProfile(updatedUsername, updatedPhoneNumber);
            } else {
                // If the phone number has not changed, update the profile directly
                if (updatedPhoneNumber.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Please enter the New Phone number", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateProfile(updatedUsername, updatedPhoneNumber);
            }
        });

    }



    private void updateProfile(String updatedUsername, String updatedPhoneNumber) {
        // Store the updated data in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", updatedUsername);
        editor.putString("phonenumber", updatedPhoneNumber);
        editor.apply();

        // Display a toast message to indicate successful update
        Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        otpEditText.setText("");
    }
}
