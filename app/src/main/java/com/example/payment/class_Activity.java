package com.example.payment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.blank_learn.dark.R;
import com.blank_learn.dark.databinding.ActivityClassBinding;
import com.blank_learn.dark.databinding.ActivityGroupChatBinding;
import com.example.chat.GroupChat;
import com.example.chat.MemberAdapter;
import com.example.chat.chatAdapter;
import com.example.chat.chatmodel;
import com.example.dark.clasmodel;
import com.example.home.MainActivity;
import com.example.loginandsignup.Users;
import com.example.loginandsignup.signup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class class_Activity extends AppCompatActivity {
    ActivityClassBinding binding;
    FirebaseAuth auth;
    ArrayList<Users> list;
    Context context;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String name;
    Intent intent;
    String Postid;
    String postpic;
    String title;
    private com.example.chat.chatAdapter chatAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        list = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        intent = getIntent();
        postpic= intent.getStringExtra("postpic");
        name = intent.getStringExtra("name");
        Postid = intent.getStringExtra("Postid");
        title= intent.getStringExtra("topic");

        if (postpic != null && !postpic.isEmpty()) {
            Picasso.get().load(postpic).into(binding.imageView30);
        }else {
        }
        binding.textView79.setText(title);


        binding.imageView30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(class_Activity.this, GroupChat.class);
                intent.putExtra("Postid",Postid);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        binding.imageView31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(class_Activity.this, GroupChat.class);
                intent.putExtra("Postid",Postid);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        MemberAdapter memberAdapter = new MemberAdapter(list, getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, true);

        binding.recyclerView3.setLayoutManager(linearLayoutManager);
        binding.recyclerView3.setAdapter(memberAdapter);
        binding.recyclerView3.scrollToPosition(memberAdapter.getItemCount() - 1);
        linearLayoutManager.setStackFromEnd(true);



        database.getReference().child("Group")
                .child(name)
                .child("member")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            String memberId = snapshot1.getKey();
                            database.getReference().child("Users").child(memberId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                            if (userSnapshot.exists()) {
                                                Users user = userSnapshot.getValue(Users.class);
                                                user.setUserID(memberId);
                                                list.add(user);
                                                memberAdapter.notifyDataSetChanged();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

    }
}