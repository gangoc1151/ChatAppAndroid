package com.example.assignment2_work;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignment2_work.Adapter.FriendPageAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendProfile extends AppCompatActivity {

    private String userID, senderID, Current_State, previous_activity;
    private ImageView  backbutton;
    CircleImageView friendImage;
    private TextView friendName, friendEmail, status;
    Button addFriend, cancel, chat;
    private DatabaseReference reference, senderReference, friendRef, ReceivedRequestRef, NotificationRef;
    private FirebaseAuth firebaseAuth;
    TabLayout tabLayout;
    TabItem setting, gallery;
    ViewPager viewPager;
    FriendPageAdapter pageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        /** get the id of friend from real time database in firebase */
        userID = getIntent().getExtras().get("userid").toString();
        friendImage = findViewById(R.id.friendImage);
        friendName = findViewById(R.id.friend_name);
        friendEmail = findViewById(R.id.friend_email);
        addFriend = findViewById(R.id.addfiend);
        backbutton = findViewById(R.id.back_to_friendlist);
        cancel = findViewById(R.id.cancel_button1);
        firebaseAuth = FirebaseAuth.getInstance();
        status = findViewById(R.id.status);
        previous_activity = getIntent().getExtras().get("previous").toString();
;        /** get the ID of users using the app */
        senderID = firebaseAuth.getCurrentUser().getUid();
        /** get information name, email of users */
        reference  = FirebaseDatabase.getInstance().getReference().child("users");
        /** access to Received request in firebase */
        ReceivedRequestRef = FirebaseDatabase.getInstance().getReference().child("Received Request");
        senderReference = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friend List");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notification");
        Current_State = "new";

        /**tab layout when to change to other fragment when user wipe left or right */
        tabLayout= findViewById(R.id.tablayout);
        setting = findViewById(R.id.setting);
        gallery = findViewById(R.id.gallery);
        viewPager = findViewById(R.id.viewpaper);

        pageAdapter = new FriendPageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pageAdapter);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


        chat = findViewById(R.id.chat1);

        backbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /** check previous activity key and get back to previous activity*/
                if(previous_activity.equals("friendlist")){
                startActivity(new Intent(FriendProfile.this, FriendList.class));
                finish();
                }
                if (previous_activity.equals("searchfriend")){
                    startActivity(new Intent(FriendProfile.this, SearchFriend.class));
                    finish();
                }
                if (previous_activity.equals("friendrequest")){
                    startActivity(new Intent(FriendProfile.this, FriendRequest.class));
                    finish();
                }
                if (previous_activity.equals("chatActivity")){
                    Intent intent = new Intent(FriendProfile.this, ChatActivity.class);
                    intent.putExtra("previouspage", "FriendProfile");
                    finish();
                }

            }
        });
        FriendInfor();

    }

    private void requesManagement() {
        /**  link to real time database named FriendRequest in firebase with sender ID*/
        senderReference.child(senderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userID)){
                    /** get value from request  */
                    String request = dataSnapshot.child(userID).child("request").getValue().toString();

                    /** check value if it is equal to sent or received*/
                    if (request.equals("sent"))
                    {
                        Current_State = "request_sent";
                    }
                    if (request.equals("received"))
                    {

                        status.setText("Accept to have a cool conversation with me");

                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        /** access into FriendList folder in Realtime database*/
        friendRef.child(senderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userID)){
                    String request = dataSnapshot.child(userID).child("Friends").getValue().toString();
                    if (request.equals("saved"))
                    {
                        /**chang current state and set button text remove friend */

                        status.setText("Let start a conversation, my friend !");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void FriendInfor() {
            /** retrieve user information from firebase when you click on some one on the list */
        reference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    final String userImage = dataSnapshot.child("ImageURL").getValue().toString();
                    final String userName = dataSnapshot.child("Username").getValue().toString();
                    final String userEmail = dataSnapshot.child("Email").getValue().toString();

                    Picasso.get().load(userImage).into(friendImage);
                    friendName.setText(userName);
                    friendEmail.setText(userEmail);

                    requesManagement();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
