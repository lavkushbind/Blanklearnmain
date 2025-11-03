package com.blank_learn.home;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.blank_learn.Booking.BookingWizardActivity;
import com.blank_learn.dark.R;
import com.blank_learn.dark.databinding.FragmentHomeBinding;
import com.blank_learn.chat.Chat_list_Activity;
import com.blank_learn.dark.Search_course_adapter;
import com.blank_learn.dark.databinding.Home2Binding;
import com.blank_learn.demo.AllocationAdapter;
import com.blank_learn.demo.AllocationData;
import com.blank_learn.demoActivity3;
import com.blank_learn.loginandsignup.Teacher_form_Activity;
import com.blank_learn.loginandsignup.Users;
//import com.example.payment.SliderAdapter;
import com.blank_learn.payment.SliderData;
import com.blank_learn.payment.one_adapter;
import com.blank_learn.payment.postmodel;
import com.blank_learn.review.ReviewAdapter;
import com.blank_learn.review.ReviewItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
//import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomFragment extends Fragment {
    FragmentHomeBinding binding;
    private boolean isTeacher=true;

     private ActivityResultLauncher<Intent> demoActivityLauncher;
    private DatabaseReference userQuestRef;
    private DatabaseReference dailyQuestInfoRef;
    private CountDownTimer countDownTimer;
    private static final String CHANNEL_ID = "media_notification_channel";

    private FAQAdapter faqAdapter;
    private List<FAQ> faqList;
    FirebaseAuth auth;
    private DatabaseReference reviewsRef; // Specific reference for reviews

    FirebaseStorage storage;

    FirebaseDatabase database;
    ArrayList<postmodel> list;
    ArrayList<Users> listT;

    ArrayList<ReviewItem> list_review;

    ArrayList<postmodel> allPosts;
    ArrayList<appmodel> app_list;
    ArrayList<Story_model> story_list;
    Context context;
    private AllocationAdapter allocationAdapter;
    private List<AllocationData> allocationList = new ArrayList<>();
    private String currentUserId;
    private ReviewAdapter reviewAdapter; // Changed from homeadapter3 for clarity

    private Context mContext;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
    ArrayList<Teacher_model> Teacher_list;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        context= getContext();
        auth = FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();
        database=FirebaseDatabase.getInstance();
        list= new ArrayList<>();
        list_review = new ArrayList<>();
        listT= new ArrayList<>();
        story_list= new ArrayList<>();
        app_list= new ArrayList<>();
        Teacher_list= new ArrayList<>();
        allPosts = new ArrayList<>();




        demoActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Check if the result code is OK, which means the user booked successfully.
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                        Log.d("HomFragment", "Result OK received from demoActivity. Calling navigateToAllocations.");
                        // Call the public method in MainActivity to switch fragments.
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).navigateToAllocations();
                        }
                    } else {
                        // This block runs if the user presses the back button without booking.
                        Log.d("HomFragment", "Result was not OK from demoActivity. User may have cancelled.");
                    }
                });

    }









    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.postnow.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
              FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            currentUserId = user.getUid();
            loadAllocations();
        } else
            userQuestRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("dailyQuest");
        dailyQuestInfoRef = FirebaseDatabase.getInstance().getReference("dailyQuestInfo");

        database.getReference().child("Poster").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
//                    String imageUrl = snapshot.getValue(String.class);
//
//                    Picasso.get().load(imageUrl).into(binding.poster);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        database.getReference().child("Poster").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imageUrl = snapshot.getValue(String.class);

                    Picasso.get().load(imageUrl).into(binding.studentPost);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        binding.imageView22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = "9235044520";
                String message = "how does Blanklearn work:";
                String url = "https://wa.me/" + phoneNumber + "?text=" + message;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.textView67.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BookingWizardActivity.class);
                demoActivityLauncher.launch(intent);
            }
        });
        binding.poster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BookingWizardActivity.class);
                demoActivityLauncher.launch(intent);
            }
        });


        homeadapter homeadapter2 = new homeadapter(list, getContext());
        LinearLayoutManager layoutManager4 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        binding.TopTeacherRv.setLayoutManager(layoutManager4);
        binding.TopTeacherRv .setAdapter(homeadapter2);
        binding.TopTeacherRv.scrollToPosition(homeadapter2.getItemCount() - 1);
        layoutManager4.setStackFromEnd(true);




        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users user = snapshot.getValue(Users.class);
                    binding.textView25.setText(user.getName());
                    Picasso.get().load(user.getProfilepic())
                            .into(binding.profilepic2);

                }
                else {
                    binding.profilepic2.setVisibility(View.GONE);
                    Log.d("ProfilePic", "Snapshot does not exist");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        binding.imageView15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Chat_list_Activity.class);
                startActivity(intent);
            }
        });

        one_adapter useradapter= new one_adapter(listT,getContext());
        LinearLayoutManager linearLayoutManagerT = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,true);
        binding.recyclerView2.setLayoutManager(linearLayoutManagerT);
        binding.recyclerView2.setAdapter(useradapter);
        binding.postnow.scrollToPosition(useradapter.getItemCount() - 1);
        linearLayoutManagerT.setStackFromEnd(true);
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("charge").getValue() != null &&
                            dataSnapshot.child("bio").getValue() != null &&
                            dataSnapshot.child("storyid").getValue() != null) {

                        Users users = dataSnapshot.getValue(Users.class);
                        if (users != null) { // Ensure users object is not null
                            users.setUserID(dataSnapshot.getKey());
                            listT.add(users); // Add user to the list
                        }
                    }
                }
                Collections.shuffle(list);

                useradapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });





        StoryAdapter storyAdapter = new StoryAdapter(story_list, getContext());
        LinearLayoutManager layoutManagers = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        binding.StoryRV.setLayoutManager(layoutManagers);
        binding.StoryRV.setAdapter(storyAdapter);
        binding.StoryRV.scrollToPosition(storyAdapter.getItemCount() - 1);
        layoutManagers.setStackFromEnd(true);
        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                story_list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Story_model storyModel = dataSnapshot.getValue(Story_model.class);
                    storyModel.setStoryid(dataSnapshot.getKey());
                    story_list.add(storyModel);
                }
                Collections.shuffle(list);

                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return binding.getRoot();
    }




    private void loadAllocations() {
        DatabaseReference allocationsRef = FirebaseDatabase.getInstance().
                getReference("allocated_classes");
        allocationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allocationList.clear();

                // STEP 1: LOG THE STATE BEFORE THE LOOP
                Log.d("DEBUG_ALLOCATION", "--- Starting Check ---");
                Log.d("DEBUG_ALLOCATION", "isTeacher flag is: " + isTeacher);
                Log.d("DEBUG_ALLOCATION", "The currentUserId is: '" + currentUserId + "'");
                Log.d("DEBUG_ALLOCATION", "----------------------");


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AllocationData allocation = snapshot.getValue(AllocationData.class);
                    if (allocation != null) {

                        // STEP 2: LOG THE DATA FROM FIREBASE FOR EACH ITEM
                        Log.d("DEBUG_ALLOCATION", "Processing item. DB TeacherID: '" + allocation.getTeacherID() + "'");

                        if (isTeacher) {
                            // This block should be running for teachers
                            if (allocation.getTeacherID() != null && allocation.getTeacherID().equals(currentUserId)) {
                                Log.d("DEBUG_ALLOCATION", "MATCH FOUND! Adding to list.");
                                allocationList.add(allocation);
                            } else {
                                // This log will tell us why it failed
                                Log.w("DEBUG_ALLOCATION", "NO MATCH. The DB teacherID does not equal the currentUserId.");
                            }
                        } else {
                            // If this block runs when a teacher is logged in, 'isTeacher' is false
                            Log.w("DEBUG_ALLOCATION", "SKIPPING teacher check because isTeacher is FALSE.");
                            if (allocation.getStudentID() != null && allocation.getStudentID().equals(currentUserId)) {
                                allocationList.add(allocation);
                            }
                        }
                    }
                }

                Log.d("DEBUG_ALLOCATION", "--- Finished ---. Final list size: " + allocationList.size());

                if (mContext != null) {
                    // Using notifyDataSetChanged() is more efficient if the adapter already exists
                    if (allocationAdapter == null) {
                        allocationAdapter = new AllocationAdapter(allocationList, mContext, isTeacher);
                        binding.postnow.setAdapter(allocationAdapter);
                    } else {
                        allocationAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AllocationFragment", "Database error: " + databaseError.getMessage());
            }
        });
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        SharedPreferences preferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isDialogShown = preferences.getBoolean("isDialogShown", false);
        if (!isDialogShown) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isDialogShown", true);
            editor.apply();
        }







        faqList = new ArrayList<>();
        faqList.add(new FAQ("How does Blanklearn work?","Blanklearn provides personalized live online classes with small groups of 2, 5, 10, or 20 students. Teachers create courses by uploading a 2-minute introduction video and detailed course information. Students and parents can browse these courses, watch the videos, and read the details to find the right class. Upon enrollment, students are added to a small, private group with the teacher and other enrolled students, ensuring a focused and engaging learning experience."));
        faqList.add(new FAQ("How can I become a teacher on Blanklearn?","To become a teacher on Blanklearn, you need to create a professional video introduction and upload detailed course information. Our team will then contact you for a verification call, followed by an interview. We will also check all necessary documents, such as certifications and qualifications. Once these steps are completed and approved, your course will go live on the platform."));
        faqList.add(new FAQ("What is the purpose of the small group?", "The small group allows for direct interaction between the teacher and students. It facilitates the sharing of materials, resources, and assignments, and enables students to ask questions directly to the teacher."));
        faqList.add(new FAQ("How are live classes conducted within the small group?", "Live classes are conducted within the small group, allowing for real-time discussions, lectures, and activities. Only the students enrolled in the course and the teacher can participate, ensuring a secure and intimate learning experience."));
        faqList.add(new FAQ("Is my information safe and secure in the small group?", "Yes, Blanklearn prioritizes your privacy and security. The small group setting is designed to be a secure space where only the teacher and enrolled students can interact. Your information is protected in accordance with our privacy policy."));
        faqAdapter = new FAQAdapter(getContext(), faqList);
        LinearLayoutManager layoutManagerfaq = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        binding.faqRV.setLayoutManager(layoutManagerfaq);
        binding.faqRV.setAdapter(faqAdapter);
    }


}