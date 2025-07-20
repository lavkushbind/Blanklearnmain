package com.blank_learn.home;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;
import com.blank_learn.dark.databinding.ActivityPost2Binding;
import com.blank_learn.loginandsignup.Users;
import com.blank_learn.payment.postmodel;
import com.blank_learn.payment.razorpayActivity;
import com.blank_learn.profile.ProActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Locale;

public class post2Activity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseStorage storage;
    String randomKey;
    FirebaseDatabase database;
    Intent intent;
    ExoPlayer exoPlayer;
    SimpleExoPlayer player;
    ActivityPost2Binding binding;
    PlayerView playerView;
    Context context;
//    String paylink;
   String postid;
   String price;
   String stand;
    String price2;
    String seats;
   String name;
   String uri;

   String postpic;
   long  priceLong;

   String topic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPost2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        intent = getIntent();
        postpic= intent.getStringExtra("postPic");
        stand= intent.getStringExtra("stand");


        postid = intent.getStringExtra("postid");

        seats= intent.getStringExtra("seats");
        price2 = intent.getStringExtra("price2");
        name = intent.getStringExtra("postedBy");
        uri = intent.getStringExtra("video");
        topic = intent.getStringExtra("title");

        auth = FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();

        database=FirebaseDatabase.getInstance();
        exoPlayer = new ExoPlayer.Builder(this ).build();
        binding.videoView.setPlayer(exoPlayer);
        MediaItem mediaItem= MediaItem.fromUri(uri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();

        binding.videoView.setUseController(false);
        binding.videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exoPlayer.isPlaying()) {
                    exoPlayer.pause();
                } else {
                    exoPlayer.play();
                }
            }
        });
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });


        Users currentUser = new Users();
        currentUser.setName(name);
        currentUser.setProfilepic("default_profile_image_url");

        database.getReference()
                .child("Users")
                .child(name)
                .addListenerForSingleValueEvent(new ValueEventListener() {




                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Users user= snapshot.getValue(Users.class);
                            Picasso.get().load(user.getProfilepic())
                                    .placeholder(R.drawable.profileuser)
                                    .into(binding.profilepic);
                            binding.usernm.setText(user.getName());
                            if (user.isVerify()) {
                                binding.imageView24.setVisibility(View.VISIBLE);
                            } else {
                                binding.imageView24.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });





        binding.profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=  new Intent(post2Activity.this,ProActivity.class);
                intent.putExtra("name",name);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        binding.usernm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=  new Intent(post2Activity.this,ProActivity.class);
                intent.putExtra("name",name);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });



        binding.paybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(post2Activity.this, razorpayActivity.class);
                intent.putExtra("Price", priceLong);
                intent.putExtra("Topic", topic);
                intent.putExtra("postpic",postpic);
                intent.putExtra("Postid", postid);

                startActivity(intent);
            }
        });




        if (postid != null) {
            database.getReference().child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(postid)) {
                        // Only proceed if postid exists in the database
                        database.getReference().child("posts").child(postid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                postmodel postmodel = snapshot.getValue(postmodel.class);
                                if (postmodel != null) {
                                    binding.NameID.setText(postmodel.getPostdescription());
//                                    binding.PriceID.setText(postmodel.getPrice());
//                                    binding.textView68.setText(String.valueOf(postmodel.getPrice2()));


                                    binding.PriceID.setText(String.valueOf(postmodel.getPrice()));

//                                    binding.PriceID.setText(formatPriceAccordingToLocale(Double.parseDouble((postmodel.getPrice()));
                                    binding.textView68.setText(formatPriceAccordingToLocale(postmodel.getPrice2()));
                                    binding.textview73.setText(String.valueOf(postmodel.getSeats()));


                                    binding.AboutID.setText(postmodel.getAbout());
                                    binding.DurationID.setText(postmodel.getDuration());
                                    binding.LanguageID.setText(postmodel.getLanguage());
                                    binding.TimeID.setText(postmodel.getTime());
                                } else {
                                    Log.e("post2Activity", "postmodel is null");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    } else {
                        // Handle the case where postid doesn't exist
                        Log.e("post2Activity", "postid does not exist in the database");
                        // You may want to show a toast or handle this situation appropriately
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            // Handle the case where postid is null
//            Log.e("post2Activity", "postid is null");
//            Toast.makeText(this, "Unable to retrieve post details. Post ID is null.", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exoPlayer != null) {
            exoPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private String formatPriceAccordingToLocale(double price) {
        Locale indianLocale = new Locale("en", "IN");
        NumberFormat format = NumberFormat.getCurrencyInstance(indianLocale);
        return format.format(price);
    }




}