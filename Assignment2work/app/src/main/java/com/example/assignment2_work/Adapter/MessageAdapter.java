package com.example.assignment2_work.Adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment2_work.ImageViewActivity;
import com.example.assignment2_work.Message;
import com.example.assignment2_work.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** provide access to all the views for a data item in a view holder*/
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> usermessagelist;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef, timeRef;
    String userid, username, userimage;



    /**provide suitable constructor with specified value */
    public MessageAdapter(List<Message> usermessagelist){
        this.usermessagelist = usermessagelist;

    }
    public class MessageViewHolder extends RecyclerView.ViewHolder {


        public TextView senderMessage, receiverMessage, time;
        public ImageView receiverImage, imageSender, imageReceiver;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessage = itemView.findViewById(R.id.textviewsendermessage);
            receiverMessage = itemView.findViewById(R.id.textviewreceivermessage);
            receiverImage = itemView.findViewById(R.id.message_receiver_image);
            imageSender = itemView.findViewById(R.id.imagesender);
            imageReceiver = itemView.findViewById(R.id.imagereceiver);
            time = itemView.findViewById(R.id.time);
        }
    }



    @NonNull
    @Override

    /** create the new view (invoked by  the layout manager*/
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custome_mesage_layout,parent,false);
        firebaseAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {

        /** get elements from data set at it position*/
        /** replace the contents  of the view with that element*/
        String messagesenderId = firebaseAuth.getCurrentUser().getUid();
        final Message messages = usermessagelist.get(position);
        String fromUserID = messages.getFrom();
        String type = messages.getType();

        /** get date and time */
        String timeline  = messages.getTime();
        String dateline = messages.getDate();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        String Time1 = format.format(calendar.getTime());
        String TIme2 = dateline + " " + timeline;
        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(Time1);
            d2 = format.parse(TIme2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        /**calculate time in different moment and convert to hours and minutes */
        long diff = d1.getTime() - d2.getTime();
        long diffMinutes = diff/(60 * 1000);
        long diffHours = diff/(60*60*1000);



        /**getting the image from firebase */
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String receiverImage = dataSnapshot.child("ImageURL").getValue().toString();
                    Picasso.get().load(receiverImage).into(holder.receiverImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /** set up message in text view, set up invisible messages */
        holder.receiverMessage.setVisibility(View.GONE);
        holder.senderMessage.setVisibility(View.GONE);
        holder.receiverImage.setVisibility(View.GONE);
        holder.imageSender.setVisibility(View.GONE);
        holder.imageReceiver.setVisibility(View.GONE);
        holder.time.setVisibility(View.GONE);
        /**Condition when the type is equal to text */
        if (type.equals("text")){


            if (fromUserID.equals(messagesenderId)){
                holder.senderMessage.setVisibility(View.VISIBLE);
                holder.senderMessage.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessage.setTextColor(Color.BLACK);
                holder.senderMessage.setText(messages.getMessage());
                /** if time > 5 minutes and less than 24 hours, the message display time only but when time > 1 day, the message will display date and time */
                if ( diffMinutes >5 && diffHours < 24){
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(timeline);
                }
                if (diffHours > 24){
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(timeline + " - " + dateline);
                }

            }


            else {
                holder.receiverMessage.setVisibility(View.VISIBLE);
                holder.receiverImage.setVisibility(View.VISIBLE);

                holder.receiverMessage.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessage.setTextColor(Color.BLACK);
                holder.receiverMessage.setText(messages.getMessage());

                /** if time > 5 minutes and less than 24 hours, the message display time only but when time > 1 day, the message will display date and time */
                if ( diffMinutes >5 && diffHours < 24){
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(timeline);
                }
                if (diffHours > 24){
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(timeline + " - " + dateline);
                }

            }
        }
        /** condition when type in firebase is equal to Image */
        else if (type.equals("Image"))
        {
            if (fromUserID.equals(messagesenderId))
            {

                holder.imageSender.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.imageSender);
                /** if time > 5 minutes and less than 24 hours, the message display time only but when time > 1 day, the message will display date and time */
                if ( diffMinutes >5 && diffHours < 24){
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(timeline);
                }
                if (diffHours > 24){
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(timeline + " - " + dateline);
                }

            }else {

                holder.receiverImage.setVisibility(View.VISIBLE);
                holder.imageReceiver.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.imageReceiver);
                /** if time > 5 minutes and less than 24 hours, the message display time only but when time > 1 day, the message will display date and time */
                if ( diffMinutes >5 && diffHours < 24){
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(timeline);
                }
                if (diffHours > 24){
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(timeline + " - " + dateline);
                }
            }
        }
        /** else type is equal to PDF file*/
        else {
            if (fromUserID.equals(messagesenderId))
            {
                holder.senderMessage.setVisibility(View.VISIBLE);
                holder.senderMessage.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessage.setTextColor(Color.BLACK);
                holder.senderMessage.setText(messages.getDocumentName());
                holder.senderMessage.setPaintFlags(holder.senderMessage.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
                /** if time > 5 minutes and less than 24 hours, the message display time only but when time > 1 day, the message will display date and time */
                if ( diffMinutes >5 && diffHours < 24){
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(timeline);
                }
                if (diffHours > 24){
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(timeline + " - " + dateline);
                }


            }else {

                holder.receiverMessage.setVisibility(View.VISIBLE);
                holder.receiverImage.setVisibility(View.VISIBLE);

                holder.receiverMessage.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessage.setTextColor(Color.BLACK);
                holder.receiverMessage.setText(messages.getDocumentName());
                holder.receiverMessage.setPaintFlags(holder.receiverMessage.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
                /** if time > 5 minutes and less than 24 hours, the message display time only but when time > 1 day, the message will display date and time */
                if ( diffMinutes >5 && diffHours < 24){
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(timeline);
                }
                if (diffHours > 24){
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(timeline + " - " + dateline);
                }

            }
        }

        if (fromUserID.equals(messagesenderId)){
            /** When users hold the itemView, a dialog appears with options*/
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (usermessagelist.get(position).getType().equals("PDF"))
                    {
                        CharSequence option [] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Undo",
                                        "Download",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Option");
                        /** Onclick item and get different methods */
                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    DeleteSentMessage(position, holder);

                                }
                                else if (which==1){
                                    DeleteMessageForEveryone(position, holder);
                                }
                                else if (which==2){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(usermessagelist.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }

                    /** Onclick item and get different methods */
                    if (usermessagelist.get(position).getType().equals("Image"))
                    {
                        CharSequence option [] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "View this Image",
                                        "Undo",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Option");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    DeleteSentMessage(position, holder);
                                }
                                else if (which==1){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("url", usermessagelist.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if (which == 2){
                                    DeleteMessageForEveryone(position, holder);
                                }

                            }
                        });
                        builder.show();
                    }

                    /** Onclick item and get different methods */
                    if (usermessagelist.get(position).getType().equals("text"))
                    {
                        CharSequence option [] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Undo",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Option");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    DeleteSentMessage(position, holder);

                                }
                                else if (which==1){
                                    DeleteMessageForEveryone(position, holder);
                                }

                            }
                        });
                        builder.show();
                    }
                    return false;
                }
            });
        }
        else {

            /** LongClick item and get different methods */
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (usermessagelist.get(position).getType().equals("PDF"))
                    {
                        CharSequence option [] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Download",
                                        "Cancel"
                                };
                        final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Option");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                        DeleteReceivedMessage(position, holder);
                                }
                                if(which ==1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(usermessagelist.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }


                            }
                        });
                        builder.show();
                    }
                    if (usermessagelist.get(position).getType().equals("Image"))
                    {
                        CharSequence option [] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "View this Image",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Option");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    DeleteReceivedMessage(position, holder);
                                }
                                else if (which==1){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                                    intent.putExtra("url", usermessagelist.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }


                            }
                        });
                        builder.show();
                    }
                    /** the option if user want to delete the message */
                    if (usermessagelist.get(position).getType().equals("text"))
                    {
                        CharSequence option [] = new CharSequence[]
                                {
                                        "Delete For me",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Option");

                        builder.setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0){
                                    DeleteReceivedMessage(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }
                    return false;
                }
            });
        }

    }

    @Override
    /** return the size of  data set */
    public int getItemCount() {
        return usermessagelist.size();
    }

    /** Delete sent message method from sender side */
    private void DeleteSentMessage (final int position, final MessageViewHolder holder){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("users").child(usermessagelist.get(position).getTo()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userid = dataSnapshot.child("id").getValue().toString();
                username = dataSnapshot.child("Username").getValue().toString();
                userimage = dataSnapshot.child("ImageURL").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /**Remove the value in Realtime database and refresh the chat activity*/
        reference.child("Message")
                .child(usermessagelist.get(position).getFrom())
                .child(usermessagelist.get(position).getTo()).
                child(usermessagelist.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Sucessfully", Toast.LENGTH_SHORT).show();


                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /** Delete received message from receiver side */
    private void  DeleteReceivedMessage(final int position, final MessageViewHolder holder){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("users").child(usermessagelist.get(position).getFrom()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userid = dataSnapshot.child("id").getValue().toString();
                username = dataSnapshot.child("Username").getValue().toString();
                userimage = dataSnapshot.child("ImageURL").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        /** this function will delete message in firebase database based on user id*/
        reference.child("Message")
                .child(usermessagelist.get(position).getTo())
                .child(usermessagelist.get(position).getFrom()).
                child(usermessagelist.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Sucessfully", Toast.LENGTH_SHORT).show();


                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /** Delete message from both side */
    private void  DeleteMessageForEveryone(final int position, final MessageViewHolder holder){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("users").child(usermessagelist.get(position).getTo()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userid = dataSnapshot.child("id").getValue().toString();
                username = dataSnapshot.child("Username").getValue().toString();
                userimage = dataSnapshot.child("ImageURL").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        /**Delete message from both user when they want to undo */
        reference.child("Message")
                .child(usermessagelist.get(position).getTo())
                .child(usermessagelist.get(position).getFrom()).
                child(usermessagelist.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    reference.child("Message")
                            .child(usermessagelist.get(position).getFrom())
                            .child(usermessagelist.get(position).getTo()).
                            child(usermessagelist.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                Toast.makeText(holder.itemView.getContext(), "Deleted Sucessfully", Toast.LENGTH_SHORT).show();

                            }
                            else {
                                Toast.makeText(holder.itemView.getContext(), "Error Occured" + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Error Occured" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }





}
