package com.example.assignment2_work;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {

    private static int SPASH_TIME_OUT = 3000;
    TextView register;
    Button login;
    CheckBox remember;
    TextView forget;
    EditText email;
    EditText password;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        register = findViewById(R.id.Register);
        login = findViewById(R.id.Login);
        remember = findViewById(R.id.remember);
        forget = findViewById(R.id.forget_password);
        email = findViewById(R.id.Email_login);
        password = findViewById(R.id.Password_login);
        progressBar = findViewById(R.id.progressBar2);
        firebaseAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("users");



        /** create remember me checkbox to save account and password */
        sharedPreferences = getSharedPreferences("Login",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        checkPreferences();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Register.class));

            }
        });

        /** when login is successfull, keep staying sign in when user shut down the application */
       if (firebaseAuth.getCurrentUser() != null){
            /** if email is verified, stay sign in */
           if (firebaseAuth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(MainActivity.this, HomePage.class));
            finish();
            }

        }



        /** login function checking user from firebase authentication */
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email= email.getText().toString().trim();
                String Password = password.getText().toString().trim();

                /** condition to avoid empty email input and password input*/
                if (TextUtils.isEmpty(Email)){
                    email.setError("Email is empty");
                    return;
                }
                if (TextUtils.isEmpty(Password)){
                    password.setError("Password is empty");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                /** authenticate users from firebase storage */

                firebaseAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            /** boolean checkbox if it is true, and check email and password if it null or not*/
                            if (remember.isChecked()) {
                                editor.putString("email",email.getText().toString());
                                editor.putString("password",password.getText().toString());
                                editor.commit();
                            }
                            else {
                                editor.putString("email","");
                                editor.putString("password","");
                                editor.commit();
                            }
                                editor.putBoolean("checkbox",remember.isChecked());
                                editor.commit();

                            /** if users have not identify email, they will not allowed to login */
                            if (firebaseAuth.getCurrentUser().isEmailVerified()){

                                final String userID = firebaseAuth.getCurrentUser().getUid();
                                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                    @Override
                                    public void onSuccess(InstanceIdResult instanceIdResult) {
                                        String deviceToken = instanceIdResult.getToken();
                                        reference.child(userID).child("device_token")
                                                .setValue(deviceToken)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            Intent intent = new Intent(MainActivity.this, HomePage.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                });
                                    }
                                });

                            }
                            else {
                                /**if users do not verify email, they could not login to the app */

                                Toast.makeText(MainActivity.this, "please verify your email", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Error! : " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });

            }
        });

        /** intent to forget password */
        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ForgetPassword.class));
            }
        });

    }

    /** save email and password if check box is marked*/
        public void checkPreferences() {
            String mail = sharedPreferences.getString("email","");
            String pass = sharedPreferences.getString("password","");
            email.setText(mail);
            password.setText(pass);
            boolean valueChecked = sharedPreferences.getBoolean("checkbox",false);
            remember.setChecked(valueChecked);
    }

}
