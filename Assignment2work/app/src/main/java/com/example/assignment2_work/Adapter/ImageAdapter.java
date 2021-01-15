package com.example.assignment2_work.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment2_work.ImageViewActivity;
import com.example.assignment2_work.Message;
import com.example.assignment2_work.R;
import com.example.assignment2_work.Upload;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Upload> mUpload;
    private DatabaseReference databaseReference;
    /** Constructor of Image Adapter */
    public ImageAdapter(Context context, List<Upload> uploads){
        mContext = context;
        mUpload = uploads;
    }
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_activity, parent, false);
        return new ImageViewHolder(v);
    }

    /**Bind holder to get information in put in the existing layout */
    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, int position) {

        final Upload uploadcurrent = mUpload.get(position);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Gallery");
        holder.textView.setText(uploadcurrent.getmName());
        Picasso.get().load(uploadcurrent.getmImageUrl()).fit().centerCrop().into(holder.imageView);
        /**Set LongClick to view all image */
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CharSequence option[] = new CharSequence[]
                        {
                                "See the picture",
                                "Delete",
                                "Cancel"
                        };
                final AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                builder.setTitle("Option");

                builder.setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            /** when user choose see the picture or Delete it immediately*/
                            Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                            intent.putExtra("url", uploadcurrent.getmImageUrl());
                            holder.itemView.getContext().startActivity(intent);
                        }
                        if (which == 1) {
                            databaseReference.child(uploadcurrent.getUserID()).child(uploadcurrent.getmName()).
                                    removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                        }


                    }
                });
                builder.show();
                return false;
            }



        });
    }

    /** return the size of  data set */
    @Override
    public int getItemCount() {
        return mUpload.size();
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder{
        public TextView textView;
        public ImageView imageView;

        public ImageViewHolder(View view){
            super(view);

            textView = view.findViewById(R.id.image_name);
            imageView =view.findViewById(R.id.image_view_layout);

        }
    }

}
