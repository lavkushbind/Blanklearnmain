package com.blank_learn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.blank_learn.loginandsignup.Users;
import com.bumptech.glide.Glide;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter_run extends RecyclerView.Adapter<UserAdapter_run.UserViewHolder> {

    private final Context context;
    private final List<Users> userList;

    public UserAdapter_run(Context context, List<Users> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item_layout_run, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Users currentUser = userList.get(position);

        // Set text data, with checks for null or empty values
        holder.userNameTextView.setText(currentUser.getName() != null ? currentUser.getName() : "No Name");
        holder.userEmailTextView.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "No Email");
        holder.userPhoneTextView.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "No Phone");
        holder.userBioTextView.setText(currentUser.getBio() != null ? currentUser.getBio() : "No Bio");

        // Use Glide to load the profile picture
        Glide.with(context)
                .load(currentUser.getProfilepic())
                .placeholder(R.drawable.userprofile) // A default image
                .error(R.drawable.userprofile)       // Image to show on error
                .into(holder.profileImageView);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImageView;
        TextView userNameTextView, userEmailTextView, userPhoneTextView, userBioTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            userEmailTextView = itemView.findViewById(R.id.userEmailTextView);
            userPhoneTextView = itemView.findViewById(R.id.userPhoneTextView);
            userBioTextView = itemView.findViewById(R.id.userBioTextView);
        }
    }
}