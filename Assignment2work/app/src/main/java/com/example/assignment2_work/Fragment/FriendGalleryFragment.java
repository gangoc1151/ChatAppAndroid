package com.example.assignment2_work.Fragment;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.assignment2_work.Adapter.FriendImageAdapter;
import com.example.assignment2_work.Adapter.ImageAdapter;
import com.example.assignment2_work.R;
import com.example.assignment2_work.Upload;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class FriendGalleryFragment extends Fragment {

    View view;
    String userID;
    RecyclerView recyclerView;
    FriendImageAdapter imageAdapter;
    List<Upload> mUpload;

    private DatabaseReference mDatabaseRef, reference;
    FirebaseUser firebaseAuth;
    TextView textView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_friend_gallery, container, false);
        userID = getActivity().getIntent().getExtras().get("userid").toString();
        textView = view.findViewById(R.id.error_message);

        firebaseAuth = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Gallery").child(userID);
        reference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        recyclerView = view.findViewById(R.id.image_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);
        mUpload = new ArrayList<>();

        /** checking public and private to gallery, if users do not want to share gallery, the friend can not see gallery*/
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String privacy = dataSnapshot.child("privacy").getValue().toString();
                if (privacy.equals("public")){
                    mDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            mUpload.clear();
                            for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                                Upload upload = dataSnapshot1.getValue(Upload.class);
                                mUpload.add(upload);
                            }
                            imageAdapter = new FriendImageAdapter(getContext(), mUpload);
                            recyclerView.setAdapter(imageAdapter);
                            textView.setVisibility(View.INVISIBLE);


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                /** if it is private, the message will appear as this is private, you can not see the content*/
                else if(privacy.equals("private")){

                        textView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }
}