package com.blank_learn.demo;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.chat.ChatAA;
import com.blank_learn.dark.R;
import com.blank_learn.loginandsignup.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.widget.Toast;

public class AllocationAdapter extends RecyclerView.Adapter<AllocationAdapter.AllocationViewHolder>
{

    private List<AllocationData> allocationList;
    private Context context;
    private Map<String, Users> userMap; // BEST PRACTICE: Pre-load user data (ID -> User object with phone)

    private boolean isTeacher;

    public AllocationAdapter(List<AllocationData> allocationList, Context context, boolean isTeacher) {
        this.allocationList = allocationList;
        this.context = context;
        this.isTeacher = isTeacher;
    }


    @NonNull
    @Override
    public AllocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.allocation_item, parent, false);
        return new AllocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllocationViewHolder holder, int position) {
        AllocationData allocation = allocationList.get(position);
        holder.classNameTextView.setText("Class:" +allocation.getClassName());
        holder.dateTextView.setText("Date: " + allocation.getDate());
        holder.timeTextView.setText("Time: " + allocation.getTimeSlot());

        String otherUserId;
        String otherUserRole;

        if (isTeacher) {
            otherUserId = allocation.getStudentID();
            otherUserRole = "Student";
        } else {
            otherUserId = allocation.getTeacherID();
            otherUserRole = "Teacher";

        }
//        final String finalOtherUserRole = otherUserRole;

        String finalOtherUserId = otherUserId;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    if (finalOtherUserId != null && !finalOtherUserId.isEmpty()) {
                        Intent intent = new Intent(context, ChatAA.class);
                        intent.putExtra("name", finalOtherUserId);
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Error: User ID not found", Toast.LENGTH_SHORT).show();
                    }}
            }
        });

        if (isTeacher) {
            if (allocation.getStudentID() != null) {
                fetchStudentDetails(holder, allocation.getStudentID());
            } else {
                holder.teacherIdTextView.setText("Student: ID not found");
            }
        } else {
            if (allocation.getTeacherID() != null) {
                fetchTeacherDetails(holder, allocation.getTeacherID());
            } else {
                holder.teacherIdTextView.setText("Teacher: ID not found");
            }
        }
        String allocationId = allocation.getDemoID();


        holder.call.setEnabled(false);
        holder.call.setAlpha(0.5f);

        if (finalOtherUserId != null && !finalOtherUserId.isEmpty()) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override

                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Users targetUser = dataSnapshot.child(finalOtherUserId).getValue(Users.class);

                    if (targetUser != null && targetUser.getPhone() != null && !targetUser.getPhone().trim().isEmpty()) {
                        final String phoneNumber = targetUser.getPhone().trim();

                        holder.call.setEnabled(true);
                        holder.call.setAlpha(1.0f);

                        holder.call.setOnClickListener(v -> {
                            Toast.makeText(context, "calling...", Toast.LENGTH_SHORT).show();
                            initiatePhoneCall(context, phoneNumber);
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            });
        }



        holder.whatsapp.setEnabled(false); // Disable initially
        holder.whatsapp.setAlpha(0.5f);

        if (finalOtherUserId != null && !finalOtherUserId.isEmpty()) {
            DatabaseReference usersRef1 = FirebaseDatabase.getInstance().getReference("Users");

            usersRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Users targetUser = dataSnapshot.child(finalOtherUserId).getValue(Users.class);

                    if (targetUser != null && targetUser.getPhone() != null && !targetUser.getPhone().trim().isEmpty()) {
                        final String phoneNumber = targetUser.getPhone().trim();

                        holder.whatsapp.setEnabled(true);
                        holder.whatsapp.setAlpha(1.0f);

                        holder.whatsapp.setOnClickListener(v -> {
                            Toast.makeText(context, "whatsapp...", Toast.LENGTH_SHORT).show();
                            openWhatsAppChat(context, phoneNumber);
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            });
        }




        holder.enrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Main_next_demo_Activity.class);
                intent.putExtra("allocationId", allocationId);
                intent.putExtra("price", 450000);

                context.startActivity(intent);
            }


        });
        holder.bookAnotherDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PaymentActivity_demo.class);
                intent.putExtra("allocationId", allocationId);
                intent.putExtra("price", 10000);

                context.startActivity(intent);
            }
        });

        holder.no_demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allocation.setPaymentStatus("done");

                updatePaymentStatusInDatabase(allocation.getDemoID(), "booked");

                notifyDataSetChanged();            }

            private void updatePaymentStatusInDatabase(String demoID, String newStatus) {
                DatabaseReference allocationRef = FirebaseDatabase.getInstance().getReference("allocated_classes").child(demoID);

                allocationRef.child("paymentStatus").setValue(newStatus)
                        .addOnSuccessListener(aVoid -> {
                            // Update successful
                            Log.d("PaymentUpdate", "Payment status updated successfully for demoID: " + demoID);
                        })
                        .addOnFailureListener(e -> {
                            // Handle the error
                            Log.e("PaymentUpdate", "Failed to update payment status for demoID: " + demoID + ": " + e.getMessage());
                        });
            }
        });


        holder.demo_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allocation.setPaymentStatus("done");

                updatePaymentStatusInDatabase(allocation.getDemoID(), "done");

                notifyDataSetChanged();            }

            private void updatePaymentStatusInDatabase(String demoID, String newStatus) {
                DatabaseReference allocationRef = FirebaseDatabase.getInstance().getReference("allocated_classes").child(demoID);

                allocationRef.child("paymentStatus").setValue(newStatus)
                        .addOnSuccessListener(aVoid -> {
                            // Update successful
                            Log.d("PaymentUpdate", "Payment status updated successfully for demoID: " + demoID);
                        })
                        .addOnFailureListener(e -> {
                            // Handle the error
                            Log.e("PaymentUpdate", "Failed to update payment status for demoID: " + demoID + ": " + e.getMessage());
                        });
            }
        });

        handlePaymentStatus(holder, allocation);
    }

    private void initiatePhoneCall(Context context, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL); // Opens Dialer (safer)

        intent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {

        } catch (SecurityException e) {
        }
    }

    private void openWhatsAppChat(Context context, String phoneNumber) {
       String number = phoneNumber.replace("+", "").replace(" ", "");
        Uri uri = Uri.parse("https://wa.me/" + number);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);


        try {
            Log.i("Adapter", "Attempting to open WhatsApp chat with: " + number);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("Adapter", "WhatsApp not installed or cannot handle intent.", e);
            // Try the older 'smsto:' URI method as a fallback or just show error
            try {
                Uri smsUri = Uri.parse("smsto:" + number);
                Intent waIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
                waIntent.setPackage("com.whatsapp");
                context.startActivity(waIntent);
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(context, "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void fetchTeacherDetails(AllocationViewHolder holder, String teacherID) {
        if (teacherID == null) {
            holder.teacherIdTextView.setText("Teacher: ID not found");
            return;
        }

        DatabaseReference teacherRef = FirebaseDatabase.getInstance().getReference("Users").child(teacherID);
        teacherRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String teacherName = dataSnapshot.child("name").getValue(String.class);
                    String contact = dataSnapshot.child("phone").getValue(String.class);
                    holder.teacherIdTextView.setText("Teacher: " + teacherName);
                    holder.contactNumberTextView.setText("Contact Number:"+ contact);
                } else {
                    holder.teacherIdTextView.setText("Teacher: Name not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                holder.teacherIdTextView.setText("Teacher: Error loading name");
            }
        });
    }
    private void fetchStudentDetails(AllocationViewHolder holder, String studentID) {
        if (studentID == null) {
            holder.teacherIdTextView.setText("Student: ID not found");
            return;
        }

        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("Users").child(studentID);
        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String studentName = dataSnapshot.child("name").getValue(String.class);
                    String studentNumber = dataSnapshot.child("phone").getValue(String.class);
                    holder.teacherIdTextView.setText("Student Name: " + studentName);
                    holder.contactNumberTextView.setText("Contact Number: " + studentNumber);
                } else {
                    holder.teacherIdTextView.setText("Student: Name not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                holder.teacherIdTextView.setText("Student: Error loading name");
            }
        });
    }
    private void handlePaymentStatus(AllocationViewHolder holder, AllocationData allocation) {
        String paymentStatus = allocation.getPaymentStatus();
        if (paymentStatus == null) {
            paymentStatus = "unpaid";
        }

        if (isTeacher) {
            switch (paymentStatus.toLowerCase()) {
                case "booked":
                    holder.paymentStatusTextView.setText("Student: Demo Booked");
                    holder.paymentReminderTextView.setVisibility(View.GONE);
                    holder.buttonContainer.setVisibility(View.GONE);
                    holder.demoOptionsMessage.setVisibility(View.GONE);
                    holder.teachermsg.setVisibility(View.VISIBLE);
                    holder.no_demo.setVisibility(View.VISIBLE);

                    holder.demo_yes.setVisibility(View.VISIBLE);
                    holder.demoCompleteMessage.setVisibility(View.GONE);
                    holder.countdownTextView.setVisibility(View.GONE);
                    holder.bookAnotherDemoButton.setVisibility(View.GONE);


                    break;
                case "done":
                    holder.paymentStatusTextView.setText("Student: Demo Completed");
                    holder.paymentReminderTextView.setVisibility(View.GONE);
                    holder.buttonContainer.setVisibility(View.GONE);
                    holder.demo_yes.setVisibility(View.GONE);
                    holder.no_demo.setVisibility(View.VISIBLE);
                    holder.teachermsg.setVisibility(View.VISIBLE);
                    holder.demoOptionsMessage.setVisibility(View.GONE);
                    holder.demoCompleteMessage.setVisibility(View.GONE);
                    holder.countdownTextView.setVisibility(View.GONE);
                    holder.bookAnotherDemoButton.setVisibility(View.GONE);
                    break;
                case "unpaid":
                    holder.paymentStatusTextView.setText("Student: Payment Due");
                    holder.paymentReminderTextView.setVisibility(View.GONE);
                    holder.demo_yes.setVisibility(View.GONE);
                    holder.no_demo.setVisibility(View.GONE);
                    holder.teachermsg.setVisibility(View.GONE);
                    holder.buttonContainer.setVisibility(View.GONE);
                    holder.demo_yes.setVisibility(View.GONE);
                    holder.demoOptionsMessage.setVisibility(View.GONE);
                    holder.demoCompleteMessage.setVisibility(View.GONE);
                    holder.countdownTextView.setVisibility(View.GONE);
                    holder.bookAnotherDemoButton.setVisibility(View.GONE);
                    break;
                case "paid":
                    holder.paymentStatusTextView.setText("Student: Payment Received");
                    holder.paymentReminderTextView.setVisibility(View.GONE);
                    holder.buttonContainer.setVisibility(View.GONE);
                    holder.demo_yes.setVisibility(View.GONE);
                    holder.no_demo.setVisibility(View.GONE);
                    holder.teachermsg.setVisibility(View.GONE);
                    holder.demoOptionsMessage.setVisibility(View.GONE);
                    holder.demoCompleteMessage.setVisibility(View.GONE);
                    holder.countdownTextView.setVisibility(View.GONE);
                    holder.bookAnotherDemoButton.setVisibility(View.GONE);
                    break;
                case "complete":
                    holder.paymentStatusTextView.setText("Student: Course Completed");
                    holder.paymentReminderTextView.setVisibility(View.GONE);
                    holder.buttonContainer.setVisibility(View.GONE);
                    holder.demo_yes.setVisibility(View.GONE);
                    holder.no_demo.setVisibility(View.GONE);
                    holder.teachermsg.setVisibility(View.GONE);
                    holder.demoOptionsMessage.setVisibility(View.GONE);
                    holder.demoCompleteMessage.setVisibility(View.GONE);
                    holder.countdownTextView.setVisibility(View.GONE);
                    holder.bookAnotherDemoButton.setVisibility(View.GONE);
                    break;
                default:
                    holder.paymentStatusTextView.setText("Student Payment Status: Unknown");
                    holder.paymentReminderTextView.setVisibility(View.GONE);
                    holder.buttonContainer.setVisibility(View.GONE);
                    holder.demoOptionsMessage.setVisibility(View.GONE);
                    holder.demo_yes.setVisibility(View.GONE);
                    holder.no_demo.setVisibility(View.GONE);
                    holder.teachermsg.setVisibility(View.GONE);
                    holder.demoCompleteMessage.setVisibility(View.GONE);
                    holder.countdownTextView.setVisibility(View.GONE);
                    holder.bookAnotherDemoButton.setVisibility(View.GONE);
                    break;
            }
        } else {
            switch (paymentStatus.toLowerCase()) {
                case "booked":
                    holder.paymentStatusTextView.setText("Status: Demo Booked");
                    holder.paymentReminderTextView.setVisibility(View.GONE);
                    holder.buttonContainer.setVisibility(View.GONE);
                    holder.demo_yes.setVisibility(View.GONE);
                    holder.no_demo.setVisibility(View.GONE);
                    holder.teachermsg.setVisibility(View.GONE);
                    holder.demoOptionsMessage.setVisibility(View.GONE);
                    holder.demoCompleteMessage.setVisibility(View.GONE);
                    holder.countdownTextView.setVisibility(View.GONE);
                    holder.bookAnotherDemoButton.setVisibility(View.GONE);
                    holder.textViewInformation.setVisibility(View.VISIBLE);

                    break;
                case "done":
                    holder.paymentStatusTextView.setText("Status: Demo Completed");
                    holder.paymentReminderTextView.setVisibility(View.GONE);
                    holder.buttonContainer.setVisibility(View.VISIBLE);
                    holder.demoOptionsMessage.setVisibility(View.VISIBLE);
                    holder.demoCompleteMessage.setVisibility(View.VISIBLE);
                    holder.countdownTextView.setVisibility(View.GONE);
                    holder.demo_yes.setVisibility(View.GONE);
                    holder.no_demo.setVisibility(View.GONE);
                    holder.teachermsg.setVisibility(View.GONE);
                    holder.bookAnotherDemoButton.setVisibility(View.VISIBLE);
                    break;
                case "unpaid":
                    holder.paymentStatusTextView.setText("Status: Payment Due. Please pay to continue.");
                    holder.paymentReminderTextView.setVisibility(View.GONE);
                    holder.buttonContainer.setVisibility(View.VISIBLE);
                    holder.demoOptionsMessage.setVisibility(View.VISIBLE);
                    holder.demo_yes.setVisibility(View.GONE);
                    holder.no_demo.setVisibility(View.GONE);
                    holder.teachermsg.setVisibility(View.GONE);
                    holder.demoCompleteMessage.setVisibility(View.VISIBLE);
                    holder.countdownTextView.setVisibility(View.GONE);
                    holder.bookAnotherDemoButton.setVisibility(View.VISIBLE);
                    break;
                case "paid":
                    holder.paymentStatusTextView.setText("Status: Payment Received");
                    holder.paymentReminderTextView.setVisibility(View.GONE);
                    holder.buttonContainer.setVisibility(View.GONE);
                    holder.demo_yes.setVisibility(View.GONE);
                    holder.no_demo.setVisibility(View.GONE);
                    holder.teachermsg.setVisibility(View.GONE);
                    holder.demoOptionsMessage.setVisibility(View.GONE);
                    holder.demoCompleteMessage.setVisibility(View.GONE);
                    holder.countdownTextView.setVisibility(View.GONE);
                    holder.bookAnotherDemoButton.setVisibility(View.GONE);
                    break;
                case "complete":
                    holder.paymentStatusTextView.setText("Status: Course Completed");
                    holder.paymentReminderTextView.setVisibility(View.VISIBLE);
                    holder.buttonContainer.setVisibility(View.VISIBLE);
                    holder.demo_yes.setVisibility(View.GONE);
                    holder.no_demo.setVisibility(View.GONE);
                    holder.teachermsg.setVisibility(View.GONE);
                    holder.demoOptionsMessage.setVisibility(View.GONE);
                    holder.demoCompleteMessage.setVisibility(View.GONE);
                    holder.countdownTextView.setVisibility(View.GONE);
                    holder.bookAnotherDemoButton.setVisibility(View.VISIBLE);

                    break;
                default:
                    holder.paymentStatusTextView.setText("Status: Unknown");
                    holder.paymentReminderTextView.setVisibility(View.GONE);
                    holder.buttonContainer.setVisibility(View.VISIBLE);
                    holder.demoOptionsMessage.setVisibility(View.GONE);
                    holder.demoCompleteMessage.setVisibility(View.GONE);
                    holder.countdownTextView.setVisibility(View.GONE);
                    holder.demo_yes.setVisibility(View.GONE);
                    holder.no_demo.setVisibility(View.GONE);
                    holder.teachermsg.setVisibility(View.GONE);
                    holder.bookAnotherDemoButton.setVisibility(View.GONE);
                    break;
            }
        }
        holder.paymentStatusTextView.setVisibility(View.GONE);
    }


    private void startCountdown(AllocationViewHolder holder, AllocationData allocation) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh-mm a", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String[] timeSlotParts = allocation.getTimeSlot().split("-");
        String startTime = timeSlotParts[0].trim();
        String endTime = timeSlotParts[1].trim();

        String amPm = startTime.contains("AM") || endTime.contains("AM") ? "AM" : "PM";
        int hour = 0;

        try {
            hour = Integer.parseInt(startTime.split(" ")[0]);
            String dateTimeString = allocation.getDate() + " " + hour + "-" + "00 " + amPm;
            Date allocationDate = sdf.parse(dateTimeString);
            long timeDifference = allocationDate.getTime() - System.currentTimeMillis();

            if (timeDifference > 0) {
                holder.countdownTextView.setVisibility(View.VISIBLE);
                new CountDownTimer(timeDifference, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                        long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24;
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

                        String countdownText = String.format("%d days, %02d:%02d:%02d", days, hours, minutes, seconds);
                        holder.countdownTextView.setText("Remaining Time: " + countdownText);
                    }

                    @Override
                    public void onFinish() {
                        holder.countdownTextView.setText("Demo Time!");
                    }
                }.start();
            } else {
                holder.countdownTextView.setText("Demo already started/passed!");
            }
        } catch (ParseException | NumberFormatException e) {
            holder.countdownTextView.setText("Error parsing date/time");
            Log.e("Countdown", "Error parsing date/time", e);
        }
    }

    @Override
    public int getItemCount() {
        return allocationList.size();
    }

    public static class AllocationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewInformation, teachermsg,classNameTextView, dateTextView, timeTextView, teacherIdTextView, countdownTextView, demoCompleteMessage, demoOptionsMessage, paymentReminderTextView, paymentStatusTextView, contactNumberTextView;
        Button no_demo,enrollButton, bookAnotherDemoButton, call,whatsapp, demo_yes;
        LinearLayout buttonContainer;



        public AllocationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewInformation=itemView.findViewById(R.id.textViewInformation);
            classNameTextView = itemView.findViewById(R.id.class_name_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            no_demo=itemView.findViewById(R.id.demo_no);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            demo_yes= itemView.findViewById(R.id.demo_yes);
            teachermsg= itemView.findViewById(R.id.demo_complete_message_teacher);
            teacherIdTextView = itemView.findViewById(R.id.teacher_id_text_view);
            countdownTextView = itemView.findViewById(R.id.countdown_text_view);
            demoCompleteMessage = itemView.findViewById(R.id.demo_complete_message);
            demoOptionsMessage = itemView.findViewById(R.id.demo_options_message);
            enrollButton = itemView.findViewById(R.id.enroll_button);
            bookAnotherDemoButton = itemView.findViewById(R.id.book_another_demo_button);
            paymentReminderTextView = itemView.findViewById(R.id.payment_reminder_text_view);
            paymentStatusTextView = itemView.findViewById(R.id.payment_status_text_view);
            contactNumberTextView = itemView.findViewById(R.id.contact_number);
            buttonContainer = itemView.findViewById(R.id.button_container);
            call=itemView.findViewById(R.id.call);
            whatsapp=itemView.findViewById(R.id.whatsapp);
        }
    }
}
