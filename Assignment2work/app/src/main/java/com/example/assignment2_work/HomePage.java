package com.example.assignment2_work;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class HomePage extends AppCompatActivity {


    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    DatabaseReference reference, UserRef, databaseReference;
    String currentuserID;
    Button add, remove;
    String userID;
    ImageView online, profile;
    String messageID;
    Toolbar toolbar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page2);

        /**Access into firebase realtime database */
        reference = FirebaseDatabase.getInstance().getReference().child("Message");
        UserRef = FirebaseDatabase.getInstance().getReference().child("users");
        firebaseAuth = FirebaseAuth.getInstance();


        databaseReference = FirebaseDatabase.getInstance().getReference();
        /** get userID are using the app */
        currentuserID = firebaseAuth.getCurrentUser().getUid();
        BottomNavigationView bottomNav = findViewById(R.id.navigation_bottom);
        bottomNav.setSelectedItemId(R.id.chat);
        recyclerView = findViewById(R.id.recycleViewhomepage);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        bottomNav.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        add = findViewById(R.id.chat2);
        remove = findViewById(R.id.remove);
        toolbar =findViewById(R.id.chat_room);
        setSupportActionBar(toolbar);
        profile = findViewById(R.id.link_to_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, UserProfile.class);
                intent.putExtra("previouspage", "Homepage");
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        /** Set status for user if they are online */
        FirebaseUser CurrentUser = firebaseAuth.getCurrentUser();
        if (CurrentUser != null) {
            updateStatus("online");
        }
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(reference.child(currentuserID), Users.class).build();
        /**Recycle View display every elements from firebase server */
        FirebaseRecyclerAdapter<Users, Friends> adapter =
                new FirebaseRecyclerAdapter<Users, Friends>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final Friends friends, int i, @NonNull Users users) {
                        final String friendID = getRef(i).getKey();



                        UserRef.child(friendID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){

                                    /** initialise variables for chat list information */
                                    final String image = dataSnapshot.child("ImageURL").getValue().toString();
                                    final String name = dataSnapshot.child("Username").getValue().toString();
                                    String email = dataSnapshot.child("Email").getValue().toString();
                                    Picasso.get().load(image).into(friends.ImageURL);
                                    friends.Username.setText(name);
                                    friends.Username.setTypeface(null, Typeface.BOLD);
                                    friends.Username.setTextSize(25);

                                    /** get the last message of from message database and display as the last message in chat list */
                                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Message").child(currentuserID).child(friendID);
                                    final Query lastQuery = databaseReference.orderByKey().limitToLast(1);
                                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot child: dataSnapshot.getChildren()) {
                                                /**the looping to get message child and display in the second row */
                                                Log.d("User val", child.child("message").getValue().toString());
                                                String message = child.child("message").getValue().toString();
                                                String from = child.child("from").getValue().toString();
                                                String seen = child.child("seen").getValue().toString();
                                                String type = child.child("type").getValue().toString();
                                                messageID = child.getValue().toString();
                                                /**Condition when the sender id is current userID, this message will not be bold and display as current user send */
                                                if (from.equals(currentuserID))
                                                {
                                                        if (type.equals("PDF")){
                                                            friends.Email.setText("You sent you an attachment");

                                                        }
                                                        if (type.equals("Image")){
                                                            friends.Email.setText("You sent you a picture");

                                                        }else if (type.equals("text")){
                                                            friends.Email.setText("you: " + message);

                                                        }
                                                }
                                                /** if seen variable is equal to 2 different seen situation, this message will be display based on those situation */
                                                else if (seen.equals("not seen yet") ){
                                                        if (type.equals("PDF")){
                                                            friends.Email.setText(name + " sent you an attachment");
                                                            friends.Email.setTypeface(null, Typeface.BOLD);
                                                        }
                                                        if (type.equals("Image")){
                                                            friends.Email.setText(name + " sent you a picture");
                                                            friends.Email.setTypeface(null, Typeface.BOLD);
                                                        }else if (type.equals("text")){
                                                            friends.Email.setText(name + ": " + message);
                                                            friends.Email.setTypeface(null, Typeface.BOLD);
                                                        }
                                                }else if (seen.equals("seen")){
                                                        if (type.equals("PDF")){
                                                            friends.Email.setText(name + " sent you an attachment");

                                                        }
                                                        if (type.equals("Image")){
                                                            friends.Email.setText(name + " sent you a picture");

                                                            }else if (type.equals("text")){
                                                            friends.Email.setText(name + ": " + message);

                                                        }

                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            friends.Email.setText("");
                                        }
                                    });

                                    /** get countable for messages which are not seen yet if users have not seen 3 message, the count will be visible and equal to 3*/

                                    final Query countable = databaseReference.orderByKey();
                                    countable.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Integer count = 0;
                                            for (DataSnapshot child: dataSnapshot.getChildren()){
                                                String seen = child.child("seen").getValue().toString();
                                                String from = child.child("from").getValue().toString();
                                            /** counting the message have not seen yet*/
                                                if (seen.equals("not seen yet")&& from.equals(friendID)){
                                                    count = count +1;
                                                    String number = count.toString();
                                                    friends.HaveSeen.setText(number);
                                                    friends.HaveSeen.setVisibility(View.VISIBLE);

                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });



                                    /** Set state for users when they are online or offline */
                                    if (dataSnapshot.child("userState").hasChild("state")){
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();


                                        /** Condition when users are online*/
                                        if (state.equals("online")){
                                            online = findViewById(R.id.online);
                                            online.setVisibility(View.VISIBLE);

                                        }
                                        /** Condition when users are offline*/
                                        if (state.equals("offline")){
                                            online = findViewById(R.id.online);
                                            online.setVisibility(View.INVISIBLE);

                                        }
                                    }else {
                                        friends.Email.setText("offline");
                                    }

                                    /**On click listener intent to ChatActivity and pass data to other activity */
                                    friends.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(HomePage.this, ChatActivity.class);
                                            intent.putExtra("userid", friendID);
                                            intent.putExtra("username", name);
                                            intent.putExtra("image", image);
                                            intent.putExtra("previouspage", "ChatList");
                                            intent.putExtra("seen", "seen");
                                            intent.putExtra("messageID", messageID);
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

    /** Initialise friend information name, email, image*/
    public static class Friends extends RecyclerView.ViewHolder{

        TextView Username, Email, HaveSeen;
        ImageView ImageURL;
        public Friends(@NonNull View itemView) {
            super(itemView);
            Username = itemView.findViewById(R.id.user1);
            Email = itemView.findViewById(R.id.user2);
            ImageURL = itemView.findViewById(R.id.user_pics);
            HaveSeen = itemView.findViewById(R.id.have_seen);
        }
    }

    /** Update status method when users are offline, display last time online */
    private void updateStatus (String state){
        String saveCurrentTime, SaveCurrentDate;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        SaveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("time", saveCurrentTime);
        onlineState.put("date", SaveCurrentDate);
        onlineState.put("state", state);

        userID = firebaseAuth.getCurrentUser().getUid();
        UserRef.child(userID).child("userState").
                updateChildren(onlineState);

    }
    /** bottom navigation onclick to intent to other activity, there are 3 activities totally*/
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()){
                case R.id.List:
                    startActivity(new Intent(HomePage.this, FriendList.class));
                    overridePendingTransition(0,0);
                    finish();
                    return true;

                case R.id.Request:
                    startActivity(new Intent(HomePage.this, FriendRequest.class));
                    overridePendingTransition(0,0);
                    finish();
                    return true;
            }

            return false;
        }
    };




}
