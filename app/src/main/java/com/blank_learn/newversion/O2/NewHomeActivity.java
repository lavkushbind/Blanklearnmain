package com.blank_learn.newversion.O2;

import android.content.Intent;
import android.net.Uri; // <-- यह इम्पोर्ट जोड़ें
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blank_learn.dark.databinding.ActivityNewHomeBinding;
import com.blank_learn.home.StoryAdapter;
import com.blank_learn.home.Story_model;
import com.google.firebase.auth.FirebaseAuth; // <-- यह इम्पोर्ट जोड़ें
import com.google.firebase.auth.FirebaseUser; // <-- यह इम्पोर्ट जोड़ें
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NewHomeActivity extends AppCompatActivity {

    private ActivityNewHomeBinding binding;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth; // Firebase Auth के लिए वेरिएबल
    private FirebaseUser currentUser; // वर्तमान यूज़र के लिए वेरिएबल

    // Stories
    private ArrayList<Story_model> storyList;
    private StoryAdapter storyAdapter;

    // Reviews (Commented out as in original code)
    // private List<ReviewModel> reviewList;
    // private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase Initialization
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Lists Initialization
        storyList = new ArrayList<>();
        // reviewList = new ArrayList<>(); // Commented out

        // Setup RecyclerViews
        setupStoriesRecyclerView();
        // setupReviewsRecyclerView(); // Commented out

        // Setup Click Listeners
        setupClickListeners();
    }

    private void setupStoriesRecyclerView() {
        storyAdapter = new StoryAdapter(storyList, this);
        binding.storiesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.storiesRecyclerView.setAdapter(storyAdapter);

        // Fetch stories data from Firebase
        databaseReference.child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Story_model storyModel = dataSnapshot.getValue(Story_model.class);
                    if (storyModel != null) {
                        storyModel.setStoryid(dataSnapshot.getKey());
                        storyList.add(storyModel);
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NewHomeActivity.this, "Failed to load stories.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void setupClickListeners() {
        // 1. "Join Class" Card Listener - अब यह सब्सक्रिप्शन जांचेगा
        binding.cardJoinClass.setOnClickListener(v -> {
            checkSubscriptionAndJoinClass();
        });
binding.btnJoinClass.setOnClickListener(v -> {
    checkSubscriptionAndJoinClass();
});


        // 2. Share App Card Listener
        binding.btnShare.setOnClickListener(v -> {
            shareApp();
        });

        // 3. Locked Features Listeners
        // 4. Navigate to Doubt Fragment (UPDATED)
        binding.cardAskDoubt.setOnClickListener(v -> {
            // Toast hata diya, Fragment load kar rahe hain
//            loadFragment(new DoubtSolveFragment());
        });

        // 5. Navigate to Quiz Fragment (UPDATED)
        binding.cardPlayQuiz.setOnClickListener(v -> {
            // Toast hata diya, Fragment load kar rahe hain
//            loadFragment(new QuizFragment());
        });

    }

//    private void loadFragment(Fragment fragment) {
//        if (getParentFragmentManager() != null) {
//            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
//            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
//            // NOTE: 'R.id.container' must match the ID in activity_main.xml
//            transaction.replace(R.id.container, fragment);
//            transaction.addToBackStack(null); // Back button will return here
//            transaction.commit();
//        }
//    }



    /**
     * यह मेथड यूज़र के सब्सक्रिप्शन की जांच करता है।
     * अगर सब्सक्रिप्शन मौजूद है, तो यह गूगल मीट लिंक खोलता है।
     * अगर नहीं, तो यह यूज़र को प्लान खरीदने के लिए PlanBuilderActivity पर भेजता है।
     */
    private void checkSubscriptionAndJoinClass() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        if (currentUser == null) {
            Toast.makeText(this, "Please log in to join a class.", Toast.LENGTH_SHORT).show();
            // Optional: Redirect to login activity
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference userSubscriptionRef = databaseReference.child("user_subscriptions").child(userId);

        userSubscriptionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // सब्सक्रिप्शन डेटा मौजूद है, अब लिंक और समय निकालें
                    String meetLink = snapshot.child("class").getValue(String.class);
                    String selectedSlot = snapshot.child("selectedSlot").getValue(String.class); // क्लास का समय स्लॉट

                    // जांचें कि लिंक और समय दोनों मौजूद हैं
                    if (meetLink != null && !meetLink.isEmpty() && selectedSlot != null && !selectedSlot.isEmpty()) {

                        // ===== टाइम चेक करने का लॉजिक यहाँ शुरू होता है =====
                        try {
                            // 1. टाइम स्लॉट को Start और End टाइम में अलग करें
                            // उदा. "5:00 PM - 6:00 PM" -> ["5:00 PM", "6:00 PM"]
                            String[] times = selectedSlot.split(" - ");
                            if (times.length != 2) {
                                Toast.makeText(NewHomeActivity.this, "Invalid time slot format.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String startTimeStr = times[0].trim();
                            String endTimeStr = times[1].trim();

                            // 2. टाइम को पार्स करने के लिए एक फॉर्मेट बनाएं (12-घंटे का फॉर्मेट AM/PM के साथ)
                            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());

                            // 3. क्लास के Start/End और मौजूदा समय को Date ऑब्जेक्ट में बदलें
                            Date classStartTime = sdf.parse(startTimeStr);
                            Date classEndTime = sdf.parse(endTimeStr);

                            // मौजूदा समय को भी उसी फॉर्मेट में लाकर पार्स करें ताकि सिर्फ समय की तुलना हो, तारीख की नहीं
                            String currentTimeStr = sdf.format(new Date());
                            Date currentTime = sdf.parse(currentTimeStr);

                            // 4. जांचें कि क्या मौजूदा समय क्लास के समय के बीच में है
                            // !currentTime.before(classStartTime) का मतलब है कि मौजूदा समय स्टार्ट टाइम पर या उसके बाद है
                            if (!currentTime.before(classStartTime) && currentTime.before(classEndTime)) {
                                // सही समय है, क्लास ज्वाइन करें
                                Toast.makeText(NewHomeActivity.this, "Joining class...", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(meetLink));
                                startActivity(intent);
                            } else {
                                // क्लास का समय नहीं है
                                Toast.makeText(NewHomeActivity.this, "Class is scheduled for " + selectedSlot + ". Please join at the correct time.", Toast.LENGTH_LONG).show();
                            }

                        } catch (ParseException e) {
                            // अगर selectedSlot का फॉर्मेट गलत है तो यह एरर आएगा
                            e.printStackTrace();
                            Toast.makeText(NewHomeActivity.this, "Could not read the class time. Please contact support.", Toast.LENGTH_SHORT).show();
                        }
                        // ===== टाइम चेक करने का लॉजिक यहाँ खत्म होता है =====

                    } else {
                        // सब्सक्रिप्शन है लेकिन लिंक या टाइम स्लॉट नहीं मिला
                        Toast.makeText(NewHomeActivity.this, "Please wait, the educators will be available in some time. ", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // सब्सक्रिप्शन नहीं है, प्लान खरीदने के लिए भेजें
                    Toast.makeText(NewHomeActivity.this, "You need an active plan to join the class.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(NewHomeActivity.this, PlanBuilderActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NewHomeActivity.this, "Failed to check subscription. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "Hey! I'm using this amazing learning app. You should try it too!\n\n";
            // यहाँ अपने ऐप का Play Store लिंक डालें
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=com.blank_learn.dark";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share app via"));
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing app.", Toast.LENGTH_SHORT).show();
        }
    }
}