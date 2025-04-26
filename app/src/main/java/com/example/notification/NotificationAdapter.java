package com.example.notification;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dark.R;
import com.example.dark.databinding.Notification2sampleBinding;
import com.example.chat.GroupChat;
import com.example.loginandsignup.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
public class NotificationAdapter extends  RecyclerView.Adapter<NotificationAdapter.viewholder> {
    ArrayList<NotificationModel> list;
    Context contextl;
    public NotificationAdapter(ArrayList<NotificationModel> list, Context contextl)
    {
        this.list = list;
        this.contextl = contextl;
    }
    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(contextl).inflate(R.layout.notification2sample,parent,false);
        return new viewholder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {


            NotificationModel model = list.get(position);
            Picasso.get().load(model.getPaypic())
                    .into(holder.binding.imageView9);

            String type = model.getType();
            String userId= FirebaseAuth.getInstance().getUid();
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(model.getNotificationBy())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Users users = snapshot.getValue(Users.class);
                            Picasso.get().load(users.getProfilepic())
                                    .placeholder(R.drawable.lavkushbind);
                            if (type.equals("follow")) {
                                holder.binding.notification.setText("Starting following");
                            }

                            if (type.equals("Demo")) {
                                DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                                        .getReference("notification")
                                        .child(userId);

                                notificationRef.orderByChild("notificationAt")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    List<String> demoIds = new ArrayList<>();

                                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                        String demoId = childSnapshot.getKey(); // Get unique ID for each demo
                                                        if (demoId != null) {
                                                            demoIds.add(demoId);
                                                        }
                                                    }

                                                    // Fetch details from UserSchedules using demo IDs
                                                    fetchDemoDetails(demoIds, userId);
                                                } else {
                                                    holder.binding.notification.setText("No demo classes booked.");
                                                }
                                            }

                                            private void fetchDemoDetails(List<String> demoIds, String userId) {
                                                DatabaseReference scheduleRef = FirebaseDatabase.getInstance()
                                                        .getReference("UserSchedules")
                                                        .child(userId);

                                                scheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            StringBuilder demoClasses = new StringBuilder("Your demo classes:\n");

                                                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                                String demoId = childSnapshot.getKey(); // Match with the demoId list

                                                                if (demoIds.contains(demoId)) {
                                                                    String date = childSnapshot.child("date").getValue(String.class);
                                                                    String timeSlot = childSnapshot.child("timeSlot").getValue(String.class);
                                                                    String className = childSnapshot.child("class").getValue(String.class);

                                                                    if (date != null && timeSlot != null && className != null) {
                                                                        demoClasses.append(String.format("📅 %s | ⏰ %s | 📚 %s\n", date, timeSlot, className));
                                                                    }
                                                                }
                                                            }

                                                            if (demoClasses.length() > 0) {
                                                                holder.binding.notification.setText(demoClasses.toString().trim());
                                                            } else {
                                                                holder.binding.notification.setText("No demo classes found.");
                                                            }
                                                        } else {
                                                            holder.binding.notification.setText("No demo class records available.");
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.e("Firebase", "Failed to retrieve demo class details: " + error.getMessage());
                                                    }
                                                });
                                            }


                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.e("Firebase", "Failed to retrieve demo class notifications: " + error.getMessage());
                                            }
                                        });
                            }

















                            if (type.equals("post")) {
                                holder.binding.notification.setText("After reviewing, your course will be live");
                            }

                            if (type.equals("edit")) {
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("notify").child("massg");
                                databaseReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            String notificationText = dataSnapshot.getValue(String.class);
                                            holder.binding.notification.setText(notificationText);
                                        } else {
                                            holder.binding.notification.setText("Data not available");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        holder.binding.notification.setText("Error fetching data");
                                    }
                                });
                            }


                            if (type.equals("buy")) {
                                holder.binding.notification.setText("Congratulation you are join to a course");
                            }
                            if (type.equals("Payment received")) {
                                holder.binding.notification.setText("Your payment has been sent to your phone number" );
                            }

                            if (type.equals("blanklearn")) {
                                holder.binding.notification.setText("Blanklearn inform to you" );
                            }
                            else {
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNotificationPopup(contextl, model);
            }

            private void showNotificationPopup(Context context, NotificationModel notificationModel) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.notification2sample, null);
                dialogBuilder.setView(dialogView);
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }        });

            holder.binding.openNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    FirebaseDatabase.getInstance().getReference()
                            .child("notification")
                            .child(model.getNotificationBy())
                            .child(model.getNotificationId())
                            .child("checkopen")
                            .setValue(true);

                       holder.binding.notification.setTextColor(Color.parseColor("#100F0F"));

                      holder.binding.openNotification.setBackgroundColor(contextl.getResources().getColor(R.color.white));

                }
            });
            Boolean checkOpen = model.isCheckOpen();


        if (checkOpen == true)
        {
              holder.binding.notification.setTextColor(Color.parseColor("#100F0F"));
             holder.binding.openNotification.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        else {
        }

    }
    @Override
    public int getItemCount() {
        return list.size();
    }


    public  class viewholder extends RecyclerView.ViewHolder{
        Notification2sampleBinding binding;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            binding =Notification2sampleBinding.bind(itemView);
        }
    }
}