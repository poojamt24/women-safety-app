package com.example.womensafetyapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {
    private EditText mobileNoEditText;

    private EditText nameEditText;
    private CheckBox termCheckBox;
    private TextView backupbutton;
    private Button signUpButton;

    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase authentication instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        nameEditText = findViewById(R.id.name);
        termCheckBox = findViewById(R.id.term);
        mobileNoEditText = findViewById(R.id.mobileno);
        signUpButton = findViewById(R.id.signup);
        backupbutton = findViewById(R.id.backup);
        // Set click listener for the "Sign up" button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (nameEditText.getText().toString().equals("")) {
                    Toast.makeText(Login.this, "Enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!termCheckBox.isChecked()) {
                    Toast.makeText(Login.this, "Please accept the term and conditions.", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", nameEditText.getText().toString());
                editor.putString("phonenumber", mobileNoEditText.getText().toString());
                editor.apply();

                //redirecting to main page
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
