package com.example.assignment2_work.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment2_work.ImageViewActivity;
import com.example.assignment2_work.R;
import com.example.assignment2_work.Upload;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FriendImageAdapter extends RecyclerView.Adapter<FriendImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Upload> mUpload;
    /** Constructor of Image Adapter */
    public FriendImageAdapter(Context context, List<Upload> uploads){
        mContext = context;
        mUpload = uploads;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.image_activity, parent, false);
        return new FriendImageAdapter.ImageViewHolder(v);
    }

    /**Bind holder to get information in put in the existing layout */
    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, int position) {
        final Upload uploadcurrent = mUpload.get(position);
        holder.textView.setText(uploadcurrent.getmName());
        Picasso.get().load(uploadcurrent.getmImageUrl()).fit().centerCrop().into(holder.imageView);
        /**Set onclick to view all image */
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), ImageViewActivity.class);
                intent.putExtra("url", uploadcurrent.getmImageUrl());
                holder.itemView.getContext().startActivity(intent);
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
