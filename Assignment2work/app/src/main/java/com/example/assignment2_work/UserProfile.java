package com.example.assignment2_work;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignment2_work.Adapter.PageAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity {

    TextView name;
    TextView  email;
    ProgressBar progressBar;
    CircleImageView userImage;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    Button update, logout;
    StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView back_setting;
    TabLayout tabLayout;
    TabItem setting, gallery;
    ViewPager viewPager;
    PageAdapter pageAdapter;
    Switch aSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_profile);
            name = findViewById(R.id.profile_name);
            email = findViewById(R.id.profile_email);
            progressBar = findViewById(R.id.progressBar4);
            update = findViewById(R.id.Update);
            userImage = findViewById(R.id.userImage);
            back_setting = findViewById(R.id.back_to_setting);
            logout = findViewById(R.id.logout);
            tabLayout= findViewById(R.id.tablayout);
            setting = findViewById(R.id.setting);
            gallery = findViewById(R.id.gallery);
            viewPager = findViewById(R.id.viewpaper);
            aSwitch = findViewById(R.id.switch1);

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid());
            storageReference = FirebaseStorage.getInstance().getReference().child("Profile Image");

            pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
            viewPager.setAdapter(pageAdapter);


            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                    if (tab.getPosition() == 1){

                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

            /** The switch is on, the privacy of user will be private and other can not see gallery */
            aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked == true){
                                reference.child("privacy").setValue("private")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(UserProfile.this, "Your gallery is private now", Toast.LENGTH_SHORT ).show();
                                            }
                                        });

                            }
                            else {
                                reference.child("privacy").setValue("public")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(UserProfile.this, "Your gallery is public now", Toast.LENGTH_SHORT ).show();
                                            }
                                        });
                            }
                        }
                    });




                    /**Set Onclick image listener*/
                    userImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            CharSequence option [] = new CharSequence[]
                                    {
                                            "Change profile Image",
                                            "My QR code",
                                            "Cancel"
                                    };
                            final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            builder.setTitle("Option");

                            /** access to gallery device and pick up image*/
                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0){
                                        Intent intent = new Intent();
                                        try {
                                            /**permission to access to gallery*/
                                            if (ActivityCompat.checkSelfPermission(UserProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                                ActivityCompat.requestPermissions(UserProfile.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                            } else {
                                                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                startActivityForResult(galleryIntent, 1);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (which==1){
                                        QRdialog();
                                    }


                                }
                            });
                            builder.show();

                        }

                    });
                    /** back button onclick to previous activity*/
                    back_setting.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            startActivity(new Intent(UserProfile.this, HomePage.class));
                            finish();
                        }
                    });

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Users user = dataSnapshot.child("users").getValue(Users.class);
                            String privacy = dataSnapshot.child("privacy").getValue().toString();

                            if (privacy.equals("private")){
                                aSwitch.setChecked(true);
                            }
                            else {
                                aSwitch.setChecked(false);
                            }
                            /**getting user Information and Image and put in textView and ImageView*/
                            String username = dataSnapshot.child("Username").getValue().toString();
                            String Email = dataSnapshot.child("Email").getValue().toString();
                            String imageurl = dataSnapshot.child("ImageURL").getValue(String.class);
                            name.setText(username);
                            email.setText(Email);
                            Picasso.get().load(imageurl).into(userImage);
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



    }

   @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /** crop image into squad that fit in image view  */
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null){

            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);


        }

       if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
           CropImage.ActivityResult result = CropImage.getActivityResult(data);
           if (resultCode == RESULT_OK)
           {
               Uri resultUri = result.getUri();

               /** send image view to firebase storage and get URL link */

               progressBar.setVisibility(View.VISIBLE);
               final StorageReference filepath = storageReference.child(firebaseUser.getUid() +".jpg");
               filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                       Task<Uri> firebaseUi = taskSnapshot.getStorage().getDownloadUrl();
                       firebaseUi.addOnSuccessListener(new OnSuccessListener<Uri>() {
                           @Override
                           public void onSuccess(Uri uri) {
                               /** get URL link and store it in ImageURL in firebase realtime database*/
                               final String downloadURL = uri.toString();
                               reference.child("ImageURL").
                                       setValue(downloadURL)
                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
                                               if (task.isSuccessful()){
                                                   Toast.makeText(UserProfile.this, "successful", Toast.LENGTH_SHORT).show();
                                               }
                                               else {
                                                   Toast.makeText(UserProfile.this, task.getException().getMessage().toString(), Toast.LENGTH_SHORT ).show();
                                               }
                                           }
                                       });
                           }
                       });
                   }
               });

           }
       }
    }


    /** Show dialog with QRcode */
    private void QRdialog(){
        final Dialog myDialog = new Dialog(UserProfile.this);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.qr_user_code);
        myDialog.setTitle("My QR code");
        final ImageView imageView = (ImageView)myDialog.findViewById(R.id.qr_user);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /**loading QR code and show in image View */
                String qrcode = dataSnapshot.child("QRcode").getValue().toString();
                Picasso.get().load(qrcode).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Button cancel =(Button)myDialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.cancel();
            }
        });
        myDialog.show();
    }



}
