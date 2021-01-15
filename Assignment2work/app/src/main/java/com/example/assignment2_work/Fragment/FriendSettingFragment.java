package com.example.assignment2_work.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.assignment2_work.ChatActivity;
import com.example.assignment2_work.FriendList;
import com.example.assignment2_work.FriendProfile;
import com.example.assignment2_work.FriendRequest;
import com.example.assignment2_work.R;
import com.example.assignment2_work.SearchFriend;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class FriendSettingFragment extends Fragment {

   View view;
    private String userID, senderID, Current_State, previous_activity;

    private DatabaseReference reference, senderReference, friendRef, ReceivedRequestRef, NotificationRef;
    private FirebaseAuth firebaseAuth;
    Button addFriend, cancel, chat;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_friend_setting, container, false);
        userID = getActivity().getIntent().getExtras().get("userid").toString();
        addFriend = view.findViewById(R.id.addfiend);
        cancel = view.findViewById(R.id.cancel_button1);
        firebaseAuth = FirebaseAuth.getInstance();

        previous_activity = getActivity().getIntent().getExtras().get("previous").toString();
        /** get the ID of users using the app */
        senderID = firebaseAuth.getCurrentUser().getUid();
        /** get information name, email of users */
        reference  = FirebaseDatabase.getInstance().getReference().child("users");
        /** access to Received request in firebase */
        ReceivedRequestRef = FirebaseDatabase.getInstance().getReference().child("Received Request");
        senderReference = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friend List");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notification");
        Current_State = "new";

        chat = view.findViewById(R.id.chat1);

        FriendInfor();

        return view;
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

                    requesManagement();

                    /** chat onclick and intent to Chat Activity class */
                    chat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra("userid", userID);
                            intent.putExtra("username", userName);
                            intent.putExtra("image", userImage);
                            intent.putExtra("previouspage", "Profile");

                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                        addFriend.setText("Undo Request");
                    }
                    if (request.equals("received"))
                    {
                        Current_State="request_received";
                        addFriend.setText("Accept");

                        cancel.setVisibility(View.VISIBLE);
                        cancel.setEnabled(true);
                        /** cancel function if they decline to be friends */
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Cancel_request();
                            }
                        });
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
                        Current_State = "friends";
                        addFriend.setText("Remove Friend");

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /** condition in case user click on their own profile, the button of chat and add friend will disappear*/
        if(!senderID.equals(userID)){
            addFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFriend.setEnabled(false);
                    if (Current_State.equals("new")){
                        addFriend();
                    }

                    if (Current_State.equals("request_sent")){
                        Cancel_request();
                    }
                    if (Current_State.equals("request_received")){
                        Accept_request();
                    }
                    if (Current_State.equals("friends")){
                        Remove_friend();

                    }

                }
            });
        }
        else {
            addFriend.setVisibility(View.INVISIBLE);

        }

    }
    private void Remove_friend(){
        /** access to real time firebase storage and remove the value */
        friendRef.child(senderID).child(userID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        friendRef.child(userID).child(senderID)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        addFriend.setEnabled(true);
                                        addFriend.setText("Add Friend");
                                        /** set Current_State back to new status */
                                        Current_State ="new";
                                        cancel.setVisibility(View.INVISIBLE);
                                        cancel.setEnabled(false);

                                    }
                                });
                    }
                });
    }
    private void Accept_request(){
        /** accept user request to make friend */
        friendRef.child(senderID).child(userID)
                .child("Friends").setValue("saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            friendRef.child(userID).child(senderID).child("Friends").setValue("saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                /** if user accept the request, system will remove request from request folder in realtime firebase and add friend into user folder in FriendList Folder*/
                                                senderReference.child(userID).child(senderID)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        senderReference.child(senderID).child(userID).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        addFriend.setEnabled(true);
                                                                        addFriend.setText("Remove Friend");
                                                                        cancel.setVisibility(View.INVISIBLE);
                                                                        cancel.setEnabled(false);
                                                                        Current_State = "friends";
                                                                        ReceivedRequestRef.child(senderID).child(userID)
                                                                                .removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                    }
                                                                                });
                                                                    }
                                                                })  ;
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    /** add friend function and send request to friend */
    private void addFriend (){

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Send a message");

        /** sending the message to friend with friend request */
        final EditText sendMessage = new EditText(getActivity());
        sendMessage.setHint("e.g Hi my friend");
        builder.setView(sendMessage);

        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String message = sendMessage.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(getActivity(), "Please write a message", Toast.LENGTH_LONG).show();
                } else {
                    senderReference.child(senderID).child(userID).child("request").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        senderReference.child(userID).child(senderID).child("request").setValue("received")
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            /** adding sender information in current user to display on request list */
                                                            ReceivedRequestRef.child(userID).child(senderID).
                                                                    child("request").setValue("received")
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                ReceivedRequestRef.child(userID).child(senderID)
                                                                                        .child("message").setValue(message);
                                                                                HashMap<String, String> chatnotification = new HashMap<>();
                                                                                chatnotification.put("from", senderID);
                                                                                chatnotification.put("to", userID);
                                                                                chatnotification.put("type", "request");
                                                                                NotificationRef.child(userID).push()
                                                                                        .setValue(chatnotification)
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    addFriend.setEnabled(true);
                                                                                                    Current_State = "request_sent";
                                                                                                    addFriend.setText("Undo Request");
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });

                                                        }
                                                    }

                                                });
                                    }
                                }
                            });
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

    private void Cancel_request (){
        /** cancel request and delete value from request folder in firebase realtime database */
        senderReference.child(senderID).child(userID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    senderReference.child(userID).child(senderID)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        addFriend.setEnabled(true);
                                        Current_State = "new";
                                        addFriend.setText("Add Friend");
                                        cancel.setVisibility(View.INVISIBLE);
                                        cancel.setEnabled(false);

                                        /** Remove information from Received Requested of users in Request List*/
                                        ReceivedRequestRef.child(senderID).child(userID)
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        ReceivedRequestRef.child(userID).child(senderID)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });
    }
}