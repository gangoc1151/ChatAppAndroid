package com.example.assignment2_work;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.assignment2_work.Adapter.MessageAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private String receiverid, receiverName, userImage, senderid, seen;
    private TextView Name, lastseen;
    private ImageView imageView;
    private Toolbar toolbar;
    private ImageView send, Back;
    private EditText input;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;
    private final List<Message>  messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter adapter;
    private RecyclerView recyclerView;
    String previouspage, files = " ", myUrl=" ", saveCurrentTime, SaveCurrentDate;
    ImageButton imageSend;
    private Uri  fileUri;
    private StorageTask uploadTask;
    private ProgressDialog loading;




    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        previouspage = getIntent().getExtras().get("previouspage").toString();
        receiverid = getIntent().getExtras().get("userid").toString();
        receiverName = getIntent().getExtras().get("username").toString();
        userImage = getIntent().getExtras().get("image").toString();


        send = findViewById(R.id.send_message1);
        input = findViewById(R.id.message_input);
        lastseen = findViewById(R.id.lastseen);
        imageSend = findViewById(R.id.imageupload);
        loading = new ProgressDialog(this);
        Name = findViewById(R.id.ReceiverName);
        imageView = findViewById(R.id.ReciverImage);
        Name.setText(receiverName);
        toolbar = findViewById(R.id.private_chat);
        setSupportActionBar(toolbar);

        /**Access to firebase database and get current userid */
        firebaseAuth = FirebaseAuth.getInstance();
        senderid = firebaseAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();



        adapter = new MessageAdapter(messageList);
        recyclerView = findViewById(R.id.message1);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        Back = findViewById(R.id.back_to_chatroom);

        /** back to previous activity */
        Back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                    HaveSeen();
                if (previouspage.equals("FriendList")) {
                    startActivity(new Intent(ChatActivity.this, FriendList.class));
                    finish();
                }else if (previouspage.equals("ChatList")){
                    startActivity(new Intent(ChatActivity.this, HomePage.class));
                    finish();
                }
                else if (previouspage.equals("Profile")){
                   Intent intent = new Intent(ChatActivity.this, FriendProfile.class);
                   intent.putExtra("userid", receiverid);
                   intent.putExtra("previous", "friendlist");
                   startActivity(intent);
                   finish();
                }
                else if (previouspage.equals("chat")){
                    startActivity(new Intent(ChatActivity.this, HomePage.class));
                    finish();
                }
            }
        });


        /** Get current time now */
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        SaveCurrentDate = currentDate.format(calendar.getTime());

        final SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        /** loading image */
        Picasso.get().load(userImage).into(imageView);
        displayuserstate();


        /** when user click on send message, message will sent to friend
         * and by the way message will be saved in Message folder with carefully details from who and to who*/
        send.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {


                String messageString = input.getText().toString();
                if (!messageString.isEmpty()){
                    String messageSendRef = "Message/" + senderid + "/" + receiverid;
                    String messageReceiverRef = "Message/" + receiverid + "/" + senderid;

                    DatabaseReference userMessageKey = reference.child("Message").child(senderid).child(receiverid).push();

                    String messageID = userMessageKey.getKey();
                    Map messageText = new HashMap();
                    messageText.put("message", messageString);
                    messageText.put("type", "text");
                    messageText.put("from", senderid);
                    messageText.put("to", receiverid);
                    messageText.put("MessageID", messageID);
                    messageText.put("time", saveCurrentTime);
                    messageText.put("date", SaveCurrentDate);
                    messageText.put("seen", "not seen yet");


                    Map messageBody = new HashMap();
                    messageBody.put(messageSendRef +"/" +messageID, messageText);
                    messageBody.put(messageReceiverRef +"/" +messageID, messageText);

                    reference.updateChildren(messageBody).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ChatActivity.this, "successfully", Toast.LENGTH_SHORT).show();

                            }
                            else {
                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                            input.setText("");
                        }
                    });



                }
            }
        });


        /** using dialog to display option to users */
        imageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence option [] = new CharSequence[]
                        {
                                "Image",
                                "Files",
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("File Categories");

                builder.setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            files = "Image";

                            try {
                                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                } else {
                                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(galleryIntent, 1);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                        if (which == 1){
                            files = "PDF";
                            Intent intent = new Intent();
                            intent.setType("application/pdf");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(intent.createChooser(intent, "Select pdf file"), 1);

                        }

                    }
                });
                builder.show();
            }
        });

        /** adding message into messageList and setup smooth scroll to position */
        reference.child("Message").child(senderid).child(receiverid)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        messageList.clear();
                        for(DataSnapshot child: dataSnapshot.getChildren())
                        {
                            Message message = child.getValue(Message.class);
                            messageList.add(message);
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                HaveSeen();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_menu, menu);
        return true;
    }

    /** set up navigation in toolbar if user wants to visit friend profile or remove him */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.user_profile:
                Intent intent = new Intent(ChatActivity.this, FriendProfile.class);
                intent.putExtra("userid", receiverid);
                intent.putExtra("username", receiverName);
                intent.putExtra("image", userImage);
                /** get previous activity key*/
                intent.putExtra("previous", "chatActivity");
                startActivity(intent);
                break;
            case R.id.remove_chat:
                final AlertDialog.Builder removechat = new AlertDialog.Builder(ChatActivity.this);
                removechat.setMessage("Do you want to remove this chat ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                    reference.child("Message")
                                            .child(senderid).child(receiverid).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(ChatActivity.this, "remove successfull", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(ChatActivity.this, HomePage.class));
                                                    finish();
                                                }
                                            });
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                removechat.show();
                break;
            case R.id.remove_friend:

                /** the dialog display to confirm that they want to remove friend */
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setMessage("Do you want to remove " + receiverName + " ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reference.child("Friend List").child(senderid).child(receiverid)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                reference.child("Friend List").child(receiverid).child(senderid)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Toast.makeText(ChatActivity.this, "remove successful", Toast.LENGTH_SHORT).show();
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
                break;
        }
        return true;
    }

    /** get user state if they are online or not, when user offline, the status will display offline about minutes ago or last day*/
    private void displayuserstate (){
        reference.child("users").child(receiverid).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("userState").hasChild("state"))
                            {

                                String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();


                                String Time2 = date + " " + time;

                                // date format
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
                                String Time3 = format.format(calendar.getTime());

                                Date d1 = null;
                                Date d2 = null;
                                try {
                                    d1 = format.parse(Time3);
                                    d2 = format.parse(Time2);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }


                                long diff = d1.getTime() - d2.getTime();
                                long diffMinutes = diff/(60 * 1000);
                                long diffHours = diff/(60*60*1000);
                                /** Calculate the time at current time and last time to appear the last time online */
                                if (state.equals("online")){
                                    lastseen.setText("Online");
                                }else if (state.equals("offline")){

                                    if (diffMinutes < 60){
                                        lastseen.setText("Last Online: " + diffMinutes + " minutes ago");
                                    }else if (diffMinutes > 60 && diffHours < 24){
                                        lastseen.setText("Last Online: " + diffHours + " hours ago");
                                    }
                                    else {
                                        lastseen.setText("Last Online: " + date + " " + time);
                                    }
                                }
                            }else {
                                lastseen.setText("Offline");
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        /** Display the message in recycle View when message is sent to Message Folder*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1 && resultCode == RESULT_OK && data != null && data.getData() != null){

            loading.setTitle("Loading");
            loading.setMessage("Please wait");
            loading.setCanceledOnTouchOutside(false);
            loading.show();
            fileUri = data.getData();

            if(!files.equals("Image")){

                /**Adding the message in firebase Realtime Database following the structure Message -> userid -> receiverid -> message */
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                final String messageSendRef = "Message/" + senderid + "/" + receiverid;
                final String messageReceiverRef = "Message/" + receiverid + "/" + senderid;

                DatabaseReference userMessageKey = reference.child("Message").child(senderid).child(receiverid).push();

                final String messageID = userMessageKey.getKey();

                /** saving in file store in Firebase with the message is URL and documentName is file name  */
                final StorageReference filePath = storageReference.child(getFileName(fileUri) + "." + files);

                filePath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> firebaseUi = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUi.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadURL = uri.toString();
                                Map messageText = new HashMap();
                                messageText.put("message", downloadURL);
                                messageText.put("documentName", getFileName(fileUri));
                                messageText.put("type", files);
                                messageText.put("from", senderid);
                                messageText.put("to", receiverid);
                                messageText.put("MessageID", messageID);
                                messageText.put("time", saveCurrentTime);
                                messageText.put("date", SaveCurrentDate);
                                messageText.put("seen", "not seen yet");

                                Map messageBody = new HashMap();
                                messageBody.put(messageSendRef +"/" +messageID, messageText);
                                messageBody.put(messageReceiverRef +"/" +messageID, messageText);
                                reference.updateChildren(messageBody);
                                loading.dismiss();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loading.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        /** showing the progress upload */
                        double p =(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        loading.setMessage((int) p + "% Uploading....");
                    }
                });

            }
            else if (files.equals("Image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ImageFile");
                final String messageSendRef = "Message/" + senderid + "/" + receiverid;
                final String messageReceiverRef = "Message/" + receiverid + "/" + senderid;

                DatabaseReference userMessageKey = reference.child("Message").child(senderid).child(receiverid).push();

                final String messageID = userMessageKey.getKey();

                final StorageReference filePath = storageReference.child(messageID + "." + "jpg");
                /** saving in file store in Firebase with the message is URL and documentName is file name  */
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        /** if the message is a image, it will setValue documentName*/
                        if (task.isSuccessful())
                        {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();
                            Map messageText = new HashMap();
                            messageText.put("message", myUrl);
                            messageText.put("documentName", getFileName(fileUri));
                            messageText.put("type", files);
                            messageText.put("from", senderid);
                            messageText.put("to", receiverid);
                            messageText.put("MessageID", messageID);
                            messageText.put("time", saveCurrentTime);
                            messageText.put("date", SaveCurrentDate);
                            messageText.put("seen", "not seen yet");

                            Map messageBody = new HashMap();
                            messageBody.put(messageSendRef +"/" +messageID, messageText);
                            messageBody.put(messageReceiverRef +"/" +messageID, messageText);

                            reference.updateChildren(messageBody).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        loading.dismiss();
                                        Toast.makeText(ChatActivity.this, "successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loading.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    input.setText("");
                                }
                            });


                        }
                    }
                });
            }
            else {
                loading.dismiss();
                Toast.makeText(this, "nothing", Toast.LENGTH_LONG).show();
            }

        }
    }


    /** Have seen function, when user have seen the message, it will not appear as a notification in Homepage activity*/
    private void HaveSeen (){

            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Message").child(senderid).child(receiverid);
            final Query lastQuery = databaseReference.orderByKey();
            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        /**the looping to get message child and display in the second row */

                        String from = child.child("from").getValue().toString();
                        if (from.equals(receiverid)){
                            String messageidd = child.child("MessageID").getValue().toString();

                                databaseReference.child(messageidd).child("seen").setValue("seen");
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    /** get the name of the file to save in documentName, the value could be image or pdf file*/
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
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
