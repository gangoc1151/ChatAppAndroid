package com.example.assignment2_work;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.assignment2_work.Adapter.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFriend extends AppCompatActivity {

    private RecyclerView recyclerView;
    FirebaseAuth firebaseAuth;

    private UserAdapter userAdapter;
    private List<Users> mUsers;
    private EditText editText;
    DatabaseReference reference;
    ImageView back;
    String friendId, currentUserID;
    ImageView qr_code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        recyclerView = findViewById(R.id.recycleView2);
        editText = findViewById(R.id.search_friend);
        reference = FirebaseDatabase.getInstance().getReference().child("users");

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        back = findViewById(R.id.back_to_FriendList);
        recyclerView.setHasFixedSize(true);
        firebaseAuth = FirebaseAuth.getInstance();
        qr_code = findViewById(R.id.qr_code);

        /** get userID are using the app */
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        mUsers = new ArrayList<>();

        ReadUsers();

        qr_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchFriend.this, QRcodeScanner.class));

            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchUser(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchFriend.this, FriendList.class));
            }
        });
    }

    /**Searching friend method using loop to get user information*/
    private void searchUser(final String search){
       final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                 final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                 Query query = FirebaseDatabase.getInstance().getReference("users").orderByChild("Email")
                         .startAt(search)
                         .endAt(search +"\uf8ff");
                 query.addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         mUsers.clear();

                            for (final DataSnapshot child1: dataSnapshot.getChildren()){

                                friendId = child1.getValue().toString();
                                Users user = child1.getValue(Users.class);
                                if(!user.getId().equals(firebaseUser.getUid())) {
                                    mUsers.add(user);
                                }
                                userAdapter = new UserAdapter(getApplicationContext(), mUsers);
                                recyclerView.setAdapter(userAdapter);

                            }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });


             }

    /**Read Users methods to display existing user using the applicaiton */
    private void ReadUsers(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
         DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(editText.getText().toString().equals("")) {

                    mUsers.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Users user = child.getValue(Users.class);
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            mUsers.add(user);

                        }
                        userAdapter = new UserAdapter(getApplicationContext(), mUsers);
                        recyclerView.setAdapter(userAdapter);

                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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





