package com.example.assignment2_work;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FriendRequest extends AppCompatActivity {
    RecyclerView recyclerView;
    DatabaseReference userRef, currentuserref, FriendListRef, receivedRef;
    FirebaseAuth firebaseAuth;
    String CurrentUserID;
    Button Accept, Cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        BottomNavigationView bottomNav = findViewById(R.id.navigation_bottom);
        bottomNav.setSelectedItemId(R.id.Request);
        /** Access data in firebase and realtime storage */
        userRef = FirebaseDatabase.getInstance().getReference().child("FriendRequest");

        currentuserref = FirebaseDatabase.getInstance().getReference().child("users");
        receivedRef = FirebaseDatabase.getInstance().getReference().child("Received Request");
        FriendListRef = FirebaseDatabase.getInstance().getReference().child("Friend List");
        firebaseAuth = FirebaseAuth.getInstance();
        CurrentUserID = firebaseAuth.getCurrentUser().getUid();
        bottomNav.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        recyclerView = findViewById(R.id.recycleView3);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

    }
    /** Bottom navigation bar set onclick to intent to other activities*/
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        /** changing between every activity  by using navigation bottom bar*/
            switch (item.getItemId()){
                case R.id.List:
                    startActivity(new Intent(FriendRequest.this, FriendList.class));
                    overridePendingTransition(0,0);
                    finish();
                    return true;
                case R.id.chat:
                    startActivity(new Intent(FriendRequest.this, HomePage.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;

            }

            return false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        /** display elements from firebase and display in recycleView*/
        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>().setQuery(receivedRef.child(CurrentUserID), Users.class)
                .build();
        FirebaseRecyclerAdapter<Users, Friends> adapter =
                new FirebaseRecyclerAdapter<Users, Friends>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final Friends friends, final int i, @NonNull Users users) {
                        /** for every elements displaying on screen, set up visible button for it to onclick*/
                        Accept = friends.itemView.findViewById(R.id.chat2);
                        Cancel = friends.itemView.findViewById(R.id.remove);
                        Accept.setVisibility(View.VISIBLE);
                        Cancel.setVisibility(View.VISIBLE);
                        Accept.setText("Accept");
                        Cancel.setText("Cancel");
                        final String userID = getRef(i).getKey();
                        DatabaseReference getListRef = getRef(i).child("request").getRef();
                        getListRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String type = dataSnapshot.getValue().toString();
                                    /** if request in ReceivedRequest folder in firebase is equals to received, get data from ReceivedRequest folder*/
                                    if (type.equals("received")){
                                        currentuserref.child(userID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){
                                                String name = dataSnapshot.child("Username").getValue().toString();
                                                String email = dataSnapshot.child("Email").getValue().toString();
                                                String image = dataSnapshot.child("ImageURL").getValue().toString();
                                                Picasso.get().load(image).into(friends.ImageURL);

                                                /** display data in recycle View */
                                                friends.Username.setText(name);
                                              receivedRef.child(CurrentUserID).child(userID).child("message").addValueEventListener(new ValueEventListener() {
                                                  @Override
                                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                      if (dataSnapshot.exists()) {
                                                          String message = dataSnapshot.getValue().toString();
                                                          friends.Email.setText(message);
                                                      }
                                                  }

                                                  @Override
                                                  public void onCancelled(@NonNull DatabaseError databaseError) {

                                                  }
                                              });


                                                }

                                                /** Onclick to intent to Friend Profile*/
                                                friends.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        String visit_user = getRef(i).getKey();
                                                        Intent intent = new Intent(FriendRequest.this, FriendProfile.class);
                                                        intent.putExtra("userid", visit_user);
                                                        /** get previous activity key*/
                                                        intent.putExtra("previous", "friendrequest");
                                                        startActivity(intent);
                                                    }
                                                });

                                                /** Accept button activates to remove data from Received Request and Friend Request folder
                                                 * and Adding new friend in friend list*/
                                                Accept.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        FriendListRef.child(CurrentUserID).child(userID)
                                                                .child("Friends").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    FriendListRef.child(userID).child(CurrentUserID)
                                                                            .child("Friends").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {

                                                                                /** when user accept friends, this method will remove data from request */
                                                                                userRef.child(CurrentUserID).child(userID)
                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            userRef.child(userID).child(CurrentUserID).
                                                                                                    removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()){
                                                                                                            receivedRef.child(CurrentUserID).child(userID).
                                                                                                                    removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                                                }
                                                                                                            });
                                                                                                            Toast.makeText(FriendRequest.this, "Accept Request", Toast.LENGTH_SHORT).show();
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
                                                });

                                                /** Decline the Request if users do not want to accep request friend
                                                 * and by the way, this request will remove data from Friend Request.*/
                                                Cancel.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                                    userRef.child(CurrentUserID).child(userID)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                userRef.child(userID).child(CurrentUserID).
                                                                                        removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            Toast.makeText(FriendRequest.this, "Decline Request", Toast.LENGTH_SHORT).show();
                                                                                            receivedRef.child(CurrentUserID).child(userID).
                                                                                                    removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            });




                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }

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
        recyclerView.setAdapter(adapter);
        adapter.startListening();


    }


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
