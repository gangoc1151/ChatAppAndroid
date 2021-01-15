package com.example.assignment2_work;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FriendList extends AppCompatActivity {


    RecyclerView contactList;
    ImageView find;
    DatabaseReference reference, userref;
    FirebaseAuth firebaseAuth;
    private String currentUserID;
    ProgressBar loading;
    Button Remove, Chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        BottomNavigationView bottomNav = findViewById(R.id.navigation_bottom);
        bottomNav.setSelectedItemId(R.id.List);

        bottomNav.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        contactList = findViewById(R.id.recycleView1);
        find = findViewById(R.id.find_friend);
        contactList.setHasFixedSize(true);
        contactList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        loading = findViewById(R.id.loadingFriendList);
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Friend List");
        userref = FirebaseDatabase.getInstance().getReference().child("users");

        /**find friend onclick*/
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    startActivity(new Intent(FriendList.this, SearchFriend.class));

            }
        });

    }
    /** set up on click for each element in navigation bottom bar */
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()){

                case R.id.chat:
                    startActivity(new Intent(FriendList.this, HomePage.class));
                    overridePendingTransition(0,0);
                    finish();
                    return true;
                case R.id.Request:
                    startActivity(new Intent(FriendList.this, FriendRequest.class));
                    overridePendingTransition(0,0);
                    finish();
                    return true;

            }

            return false;
        }
    };

   @Override
   protected void onStart() {
        super.onStart();
        /** display friend list after they accept the request from sender */
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().
                setQuery(reference.child(currentUserID), Users.class).build();
        FirebaseRecyclerAdapter<Users, Friends> adapter =
                new FirebaseRecyclerAdapter<Users, Friends>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final Friends friends, final int position, @NonNull Users users) {
                            final String userID = getRef(position).getKey();
                            /** access to friend list folder and link to current user ID friend list*/
                            userref.child(userID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        /** get data from users folder to display on screen the information of user's friend list*/
                                        final String name = dataSnapshot.child("Username").getValue().toString();
                                        String email = dataSnapshot.child("Email").getValue().toString();
                                        final String image = dataSnapshot.child("ImageURL").getValue().toString();

                                        Remove = friends.itemView.findViewById(R.id.remove);
                                        Chat = friends.itemView.findViewById(R.id.chat2);
                                        Chat.setVisibility(View.VISIBLE);
                                        Remove.setVisibility(View.VISIBLE);
                                        friends.Username.setText(name);
                                        friends.Email.setText(email);
                                        Picasso.get().load(image).into(friends.ImageURL);
                                        loading.setVisibility(View.INVISIBLE);
                                        friends.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                /** lead to file chat */
                                                Intent intent = new Intent(FriendList.this, FriendProfile.class);
                                                intent.putExtra("userid", userID);
                                                intent.putExtra("username", name);
                                                intent.putExtra("image", image);
                                                /** get previous activity key*/
                                                intent.putExtra("previous", "friendlist");
                                                startActivity(intent);
                                            }
                                        });
                                        Chat.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(FriendList.this, ChatActivity.class);
                                                intent.putExtra("userid", userID);
                                                intent.putExtra("username", name);
                                                intent.putExtra("image", image);
                                                intent.putExtra("previouspage", "FriendList");
                                                startActivity(intent);
                                            }
                                        });

                                        /** set onclick remove button*/
                                        Remove.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(FriendList.this);
                                                builder.setMessage("Do you want to remove " + name + " ?")
                                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                reference.child(currentUserID).child(userID)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                reference.child(userID).child(currentUserID)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                Toast.makeText(FriendList.this, "remove successful", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });
                                                                            }
                                                                        });
                                                            }
                                                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                });
                                                builder.show();

                                            }
                                        });


                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });




                    }
                    /** create the new view (invoked by  the layout manager*/
                    @NonNull
                    @Override
                    public Friends onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_list, parent, false);
                        Friends viewHolder = new Friends(view);
                        return viewHolder;
                    }
                };
        contactList.setAdapter(adapter);
        adapter.startListening();

    }

    /** Initialise friend information name, email, image*/
    public static class Friends extends RecyclerView.ViewHolder{

        TextView Username, Email;
        ImageView ImageURL;
        public Friends(@NonNull View itemView) {
            super(itemView);
            Username = itemView.findViewById(R.id.user1);
            Email = itemView.findViewById(R.id.user2);
            ImageURL = itemView.findViewById(R.id.user_pics);
        }
    }

}
