package com.blank_learn.chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.blank_learn.dark.databinding.ChatItemviewBinding;

import com.blank_learn.loginandsignup.Users;
import com.blank_learn.profile.ProActivity;
//import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.viewholder> {
    ArrayList<Users> list;
    Context context;

    public MemberAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;

    }
    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.chat_itemview,parent,false);
        return new viewholder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        Users users = list.get(position);
        Picasso.get().load(users.getProfilepic())
                .placeholder(R.drawable.userprofile)
                        .into(holder.binding.profilePicture);
        holder.binding.userName.setText(users.getName());
//       holder.binding.textView27.setText(users.getEmail());
       holder.itemView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent=  new Intent(context,ProActivity.class);
               intent.putExtra("name",users.getUserID());
               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               context.startActivity(intent);
           }
       });
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
    public  class viewholder extends RecyclerView.ViewHolder{
        @NonNull
        ChatItemviewBinding binding;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            binding= ChatItemviewBinding.bind(itemView);
        }
    }
}