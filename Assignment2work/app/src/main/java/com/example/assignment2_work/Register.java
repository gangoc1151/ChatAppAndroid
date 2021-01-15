package com.example.assignment2_work;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.MoreObjects;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    EditText user_name;
    EditText email;
    EditText password;
    TextView back_to_login;
    ProgressBar loading;
    FirebaseAuth fAuth;
    Button signup;
    FirebaseFirestore firestore;
    String userID;
    public static final String TAG = "TAG";
    DatabaseReference documentReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        user_name = findViewById(R.id.user_name_register);
        email = findViewById(R.id.email_register);
        password = findViewById(R.id.password_register);
        back_to_login = findViewById(R.id.back_to_login);
        loading = findViewById(R.id.progressBar);
        signup = findViewById(R.id.signup);


        back_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, MainActivity.class));
            }
        });

        /** access to authentication in firebase storage */
        fAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              final String username=  user_name.getText().toString().trim();
               final String Email= email.getText().toString().trim();
                final String Password = password.getText().toString().trim();

                /** Checking user if they do not enter anything in */
                if(TextUtils.isEmpty(username)){
                    user_name.setError("User is empty");
                    return;
                }
                if (TextUtils.isEmpty(Email)){
                    email.setError("Email is empty");
                    return;
                }
                if (TextUtils.isEmpty(Password)){
                    password.setError("Password is empty");
                    return;
                }
                if (password.length() < 6){
                    password.setError("At least 6 characters");
                    return;
                }

                loading.setVisibility(View.VISIBLE);

                /** create user with parameter email and password input*/
                fAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){


                            FirebaseUser firebaseUser = fAuth.getCurrentUser();
                            firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Register.this, "Verification email has been sent. Please check email", Toast.LENGTH_LONG).show();
                                    userID = fAuth.getCurrentUser().getUid();



                                    documentReference = FirebaseDatabase.getInstance().getReference("users").child(userID);
                                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                        @Override
                                        public void onSuccess(InstanceIdResult instanceIdResult) {
                                            String deviceToken = instanceIdResult.getToken();
                                            documentReference.child("device_token")
                                                    .setValue(deviceToken);
                                        }
                                    });

                                    /** Record user information in Real Firebase in real time database storage for further functionality */

                                    Map<String,Object> user = new HashMap<>();
                                    user.put("Username", username);
                                    user.put("Email", Email);
                                    user.put("Password", Password);
                                    user.put("ImageURL", "https://firebasestorage.googleapis.com/v0/b/assignment2-work.appspot.com/o/Profile%20Image%2Fdefault.png?alt=media&token=b64149cc-5603-4a9b-a516-a3eb909bb471");
                                    user.put("id", userID);
                                    user.put("privacy", "public");
                                    documentReference.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {

                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: user is created for " + userID);
                                            CreateQRcode();

                                        }
                                    });


                                    /** loading is invisible and turn back to main activity */
                                   loading.setVisibility(View.GONE);
                                   Intent intent = new Intent(Register.this, MainActivity.class);
                                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK  | Intent.FLAG_ACTIVITY_NEW_TASK);
                                   startActivity(intent);
                                   finish();

                                }
                            }).addOnFailureListener(new OnFailureListener() {

                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Register.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    loading.setVisibility(View.GONE);
                                }
                            });



                        }
                        else {
                            Toast.makeText(Register.this, "Error: " +task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            loading.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

    }

    /** Create QR code for user, every users have different QR code from userID*/
    private void CreateQRcode(){
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(userID.toString(), BarcodeFormat.QR_CODE, 200, 200);
            Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);

            for (int x = 0; x <200; x++){
                for (int y =0; y <200; y++){
                    bitmap.setPixel(x, y, bitMatrix.get(x, y)? Color.BLACK : Color.WHITE);
                }
            }
           StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Profile QRcode");
            final StorageReference filepath = storageReference.child(userID +".jpg");
            filepath.putFile(getImageUri(this, bitmap)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    /**Get the URL from storage and pass in Realtime database*/
                    Task<Uri> firebaseUi = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseUi.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String downloadURL = uri.toString();
                            documentReference.child("QRcode").setValue(downloadURL)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        }
                                    });
                        }
                    });
                }
            });

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
