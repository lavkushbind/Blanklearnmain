package com.example.home;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.dark.R;
import com.example.dark.databinding.FragmentHomeBinding;
import com.example.chat.Chat_list_Activity;
import com.example.dark.Search_course_adapter;
import com.example.demo.AllocationAdapter;
import com.example.demo.AllocationData;
import com.example.loginandsignup.Teacher_form_Activity;
import com.example.loginandsignup.Users;
//import com.example.payment.SliderAdapter;
import com.example.payment.SliderData;
import com.example.payment.postmodel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
//import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomFragment extends Fragment {
    FragmentHomeBinding binding;
    private static final String CHANNEL_ID = "media_notification_channel";

    private FAQAdapter faqAdapter;
    private List<FAQ> faqList;
    FirebaseAuth auth;
    FirebaseStorage storage;

    FirebaseDatabase database;
    ArrayList<postmodel> list;
    ArrayList<Users> listT;

    ArrayList<postmodel> allPosts;
     ArrayList<appmodel> app_list;
    ArrayList<Story_model> story_list;
    Context context;
    private AllocationAdapter allocationAdapter;
    private List<AllocationData> allocationList = new ArrayList<>();
    private String currentUserId;
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
        listT= new ArrayList<>();
        story_list= new ArrayList<>();
        app_list= new ArrayList<>();
        Teacher_list= new ArrayList<>();
        allPosts = new ArrayList<>();
    }








    private void showWelcomeDialog() {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_home, null);

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();
        dialogView.findViewById(R.id.btnDismiss).setOnClickListener(v -> {
            alertDialog.dismiss();
            Intent intent = new Intent(getActivity(), demoActivity.class);
            startActivity(intent);
//            showCustomDialog();

        });
        dialogView.findViewById(R.id.imageView40).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

            }
        });
        alertDialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.demoRV.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

//        binding.demoRV.setLayoutManager(new LinearLayoutManager(mContext));
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            currentUserId = user.getUid();
            loadAllocations();
        } else
        {
            Log.e("TeacherAllocationFragment", "No user is currently signed in.");
        }


        database.getReference().child("Poster").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imageUrl = snapshot.getValue(String.class);

                    Picasso.get().load(imageUrl).into(binding.poster);
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

        homeadapter homeadapter3 = new homeadapter(list, getContext());
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        binding.postRV.setLayoutManager(layoutManager2);
        binding.postRV.setAdapter(homeadapter3);
       binding.postRV.scrollToPosition(homeadapter3.getItemCount() - 1);
         layoutManager2.setStackFromEnd(true);
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
         binding.poster.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(getActivity(), demoActivity.class);
                 startActivity(intent);
             }
         });
         binding.greetingTextView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(getActivity(), Teacher_form_Activity.class);
                 startActivity(intent);
             }
         });
        binding.studentPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        database.getReference().child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    postmodel postmodel = dataSnapshot.getValue(postmodel.class);
                    postmodel.setPostid(dataSnapshot.getKey());
                    if (postmodel.getPostVideo() != null) {
                        list.add(postmodel);
                    }

                }
                homeadapter3.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        homeadapter homeadapter2 = new homeadapter(list, getContext());
        LinearLayoutManager layoutManager4 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        binding.TopTeacherRv.setLayoutManager(layoutManager4);
       binding.TopTeacherRv .setAdapter(homeadapter2);
        binding.TopTeacherRv.scrollToPosition(homeadapter2.getItemCount() - 1);
        layoutManager4.setStackFromEnd(true);



        database.getReference().child("App").child("top courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String value = snapshot.getValue(String.class);

//                    binding.textView11.setText(value);
                }
            }

            @Override
            public void onCancelled(@NonNull     DatabaseError error) {

            }
        });
        database.getReference().child("App").child("new upload").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String value = snapshot.getValue(String.class);

//                    binding.textView18.setText(value);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

        useradapter useradapter= new useradapter(listT,getContext());
        LinearLayoutManager linearLayoutManagerT = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,true);
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

                        // Get the user object and set the userID
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



        Search_course_adapter homeadapter = new Search_course_adapter(list, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        binding.postnow.setLayoutManager(layoutManager);
        binding.postnow.setAdapter(homeadapter);
        binding.postnow.scrollToPosition(homeadapter.getItemCount() - 1);
        layoutManager.setStackFromEnd(true);

        database.getReference().child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("postVideo").getValue() == null) {
                        continue;
                    }
                    postmodel postmodel = dataSnapshot.getValue(postmodel.class);
                    postmodel.setPostid(dataSnapshot.getKey());
                        list.add(postmodel);
                    }
                Collections.shuffle(list);

                homeadapter.notifyDataSetChanged();
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
        DatabaseReference allocationsRef = FirebaseDatabase.getInstance().getReference("allocated_classes");
        allocationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allocationList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AllocationData allocation = snapshot.getValue(AllocationData.class);
                    if (allocation != null) {
                        // Teachers see only their own allocations.
                        if (allocation.getTeacherID() != null && allocation.getTeacherID().equals(currentUserId)) {
                            allocationList.add(allocation);
                        }
                    }
                }

                if (mContext != null) {
                    allocationAdapter = new AllocationAdapter(allocationList, mContext, true);  // isTeacher = true
                    binding.demoRV.setAdapter(allocationAdapter);
                    allocationAdapter.notifyDataSetChanged(); // Refresh the adapter
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TeacherAllocationFragment", "Database error: " + databaseError.getMessage());
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
            showWelcomeDialog();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isDialogShown", true);
            editor.apply();
        }




        DatabaseReference sliderReference = database.getReference().child("Slider");
        sliderReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getActivity() != null && isAdded()) {
                    if (snapshot.exists()) {
                        String url1 = snapshot.child("url1").getValue(String.class);
                        String url2 = snapshot.child("url2").getValue(String.class);
                        String url3 = snapshot.child("url3").getValue(String.class);
                        if (url1 != null && url2 != null && url3 != null) {
                            ArrayList<SliderData> sliderDataArrayList = new ArrayList<>();
                            sliderDataArrayList.add(new SliderData(url1));
                            sliderDataArrayList.add(new SliderData(url2));
                            sliderDataArrayList.add(new SliderData(url3));
//                            SliderAdapter adapter = new SliderAdapter(requireContext(), sliderDataArrayList);
//                            binding.slider.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
//                            binding.slider.setSliderAdapter(adapter);
//                            binding.slider.setScrollTimeInSec(3);
//                            binding.slider.setAutoCycle(true);
//                            binding.slider.startAutoCycle();
//                        } else {
                        }
                    } else {
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


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





