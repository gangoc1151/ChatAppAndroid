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

import com.example.assignment2_work.FriendProfile;
import com.example.assignment2_work.R;
import com.example.assignment2_work.Users;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<Users> mUsers;

    public UserAdapter(Context mContext, List<Users> mUsers){

        this.mUsers = mUsers;
        this.mContext = mContext;
    }

    /** create the new view (invoked by  the layout manager*/
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.users_list, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        /** get elements from data set at it position*/
        /** replace the contents  of the view with that element*/
            final Users user = mUsers.get(position);
            holder.Username.setText(user.getUsername());
            holder.Email.setText(user.getEmail());
            String image = user.getImageURL();
            Picasso.get().load(image).into(holder.ImageURL);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String visit_user = user.getId();
                    Intent intent = new Intent(mContext, FriendProfile.class);
                    intent.putExtra("userid", visit_user);
                    intent.putExtra("previous", "searchfriend");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });
    }

    /** return the size of  data set */
    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView Username, Email;
        ImageView ImageURL;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Username = itemView.findViewById(R.id.user1);
            Email = itemView.findViewById(R.id.user2);
            ImageURL = itemView.findViewById(R.id.user_pics);
        }
    }

}
