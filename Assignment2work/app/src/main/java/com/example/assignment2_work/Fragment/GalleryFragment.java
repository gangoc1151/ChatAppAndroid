package com.example.assignment2_work.Fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentProvider;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.assignment2_work.Adapter.ImageAdapter;
import com.example.assignment2_work.R;
import com.example.assignment2_work.Upload;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;


public class GalleryFragment extends Fragment {

   View view;
   Button upload;
   RecyclerView recyclerView;
   ImageAdapter imageAdapter;
    List<Upload> mUpload;
   private StorageReference storageReference;
   private DatabaseReference mDatabaseRef;
   FirebaseUser firebaseAuth;
   String current_usrid;
    Uri resultUri;
    String message;
    private ProgressDialog loading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_gallery, container, false);
        upload = view.findViewById(R.id.upload);
        storageReference = FirebaseStorage.getInstance().getReference("Gallery");
        firebaseAuth = FirebaseAuth.getInstance().getCurrentUser();
        current_usrid = firebaseAuth.getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Gallery").child(current_usrid);

        recyclerView = view.findViewById(R.id.recycleView4);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);
        loading = new ProgressDialog(getContext());

        mUpload = new ArrayList<>();

        /**Get image from firebase storage and add into list mUpload */
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mUpload.clear();
                    for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                        Upload upload = dataSnapshot1.getValue(Upload.class);

                        mUpload.add(upload);

                    }

                    imageAdapter = new ImageAdapter(getContext(), mUpload);
                    recyclerView.setAdapter(imageAdapter);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Name for the image");

                /** sending the message to friend with friend request */
                final EditText ImageName = new EditText(getActivity());
                ImageName.setHint("e.g My luxury car");
                builder.setView(ImageName);
                builder.setPositiveButton("Get Image", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        message = ImageName.getText().toString();
                        if (TextUtils.isEmpty(message)) {
                            Toast.makeText(getActivity(), "Please write a name for that image", Toast.LENGTH_LONG).show();
                        } else {
                            try {
                                /**permission to access to gallery*/
                                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                } else {
                                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(galleryIntent, 1);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            }
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();


            }
        });
        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    /** get Uri from picture and push to firebase storage*/
            resultUri = data.getData();
            uploadFile();
    }


    private void uploadFile(){
        loading.setTitle("Loading");
        loading.setMessage("Please wait");
        loading.setCanceledOnTouchOutside(false);
        loading.show();
        StorageReference fileReference =  storageReference.child(current_usrid).child(getFileName(resultUri));
        fileReference.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getActivity(), "Sucessfull", Toast.LENGTH_SHORT).show();
                Task<Uri> firebaseUi = taskSnapshot.getStorage().getDownloadUrl();
                firebaseUi.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        /** get URL link and store it in ImageURL in firebase realtime database*/
                        final String downloadURL = uri.toString();
                        mDatabaseRef.child(message).child("mImageUrl").
                                setValue(downloadURL)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            mDatabaseRef.child(message).child("mName")
                                                    .setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    mDatabaseRef.child(message).child("UserID")
                                                    .setValue(current_usrid).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(getActivity(), "successful", Toast.LENGTH_SHORT).show();
                                                            loading.dismiss();
                                                        }
                                                    });

                                                }
                                            });
                                        }
                                        else {
                                            Toast.makeText(getActivity(), task.getException().getMessage().toString(), Toast.LENGTH_SHORT ).show();
                                        }
                                    }
                                });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loading.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double p =(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                loading.setMessage((int) p + "% Uploading....");

            }
        });

    }
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }



}