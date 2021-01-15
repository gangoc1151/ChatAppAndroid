package com.example.assignment2_work;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity {
    EditText email;
    FirebaseAuth firebaseAuth;
    Button reset;
    TextView back_to_login;
    ProgressBar loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        email = findViewById(R.id.email_forget);
        firebaseAuth = FirebaseAuth.getInstance();
        reset = findViewById(R.id.button);
        loading = findViewById(R.id.progressBar3);
        back_to_login = findViewById(R.id.back_to_login);
        back_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgetPassword.this, MainActivity.class));
                finish();
            }
        });
        /** set Onclick listener when user select reset Button */
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = email.getText().toString();
                loading.setVisibility(View.VISIBLE);
                /** send request to user email to get reset password */
                firebaseAuth.sendPasswordResetEmail(Email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        /** if it is successful, display message ask user to check their email */
                        Toast.makeText(ForgetPassword.this, "Reset email is sent to your email", Toast.LENGTH_SHORT).show();
                        loading.setVisibility(View.GONE);
                        startActivity(new Intent(ForgetPassword.this, MainActivity.class));
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ForgetPassword.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
}
