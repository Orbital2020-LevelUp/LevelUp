package com.Events.LevelUp.ui.events;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ActivityOccasionItem;
import com.LikeOccasionItem;
import com.MainActivity;
import com.UserItem;
import com.example.LevelUp.ui.events.EventsFragment;
import com.example.LevelUp.ui.mylist.MylistFragment;
import com.example.tryone.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class EventPage extends AppCompatActivity {

    private ImageView mImageView;
    private ToggleButton mAddButton;
    private ToggleButton mLikeButton;
    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;
    private TextView mTextView4;
    private TextView mTextView5;
    private TextView mTextView6;

    private int position;
    private ArrayList<EventsItem> eventsItemArrayList = EventsFragment.getEventsItemList();
    private Context mContext = this;
    private StorageReference mProfileStorageRef;
    private FirebaseDatabase mFirebaseDatabase;

    private boolean changes = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.occasion_page);
        mProfileStorageRef = FirebaseStorage.getInstance()
                .getReference("profile picture uploads");
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mImageView = findViewById(R.id.event_page_image);
        mAddButton = findViewById(R.id.events_page_image_add);
        mLikeButton = findViewById(R.id.events_page_image_like);
        mTextView1 = findViewById(R.id.event_page_title);
        mTextView2 = findViewById(R.id.event_page_date);
        mTextView3 = findViewById(R.id.event_page_time);
        mTextView4 = findViewById(R.id.event_page_location);
        mTextView5 = findViewById(R.id.event_page_description);
        mTextView6 = findViewById(R.id.event_page_creator);

        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");
        String creatorName = intent.getStringExtra("creatorName");
        String title = intent.getStringExtra("title");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        String location = intent.getStringExtra("location");
        String description = intent.getStringExtra("description");
        boolean isChecked = intent.getBooleanExtra("stateChecked", true);
        boolean isLiked = intent.getBooleanExtra("stateLiked", true);
        final String eventID = intent.getStringExtra("eventID");
        position = intent.getIntExtra("position", 0);
        final String userID = MainActivity.currUser.getId();

        StorageReference mProfileStorageRefIndiv = mProfileStorageRef.child(uid);
        mProfileStorageRefIndiv.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(mImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mImageView.setImageResource(R.drawable.fake_user_dp);
            }
        });
        // mImageView.setImageResource(R.mipmap.ic_launcher_round);
        // mAddButton.setImageResource(R.drawable.ic_add_black_24dp);
        mTextView1.setText(title);
        mTextView2.setText(date);
        mTextView3.setText(time);
        mTextView4.setText(location);
        mTextView5.setText(description);
        mTextView6.setText(creatorName);

//        // add plus then add to my list
//        mAddButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Toast.makeText(mContext, "Event added to your list!", Toast.LENGTH_SHORT).show();
////                EventsItem ei = eventsItemArrayList.get(position);
////                int index = EventsFragment.getEventsItemListCopy().indexOf(ei);
////                MylistFragment.setNumberEvents(index);
//
//                EventsItem ei = eventsItemArrayList.get(position);
//
////                int index = EventsFragment.getEventsItemListCopy().indexOf(ei);
////                MylistFragment.setNumberEvents(index);
//
//                // add to ActivityEvent firebase
//                UserItem user = MainActivity.currUser;
//                String eventID = ei.getEventID();
//                String userID = user.getId();
//                DatabaseReference mActivityEventRef = mFirebaseDatabase.getReference("ActivityEvent");
//                ActivityOccasionItem activityOccasionItem = new ActivityOccasionItem(eventID, userID);
//                mActivityEventRef.push().setValue(activityOccasionItem);
//
//
//                Toast.makeText(mContext, "Event added to your list!", Toast.LENGTH_SHORT).show();
//            }
//        });

        if (isChecked) {
            mAddButton.setBackgroundResource(R.drawable.ic_done_black_24dp);
        } else {
            mAddButton.setBackgroundResource(R.drawable.ic_add_black_24dp);
        }

        mAddButton.setChecked(isChecked);

        mAddButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changes = true;
                if (isChecked) {
                    // change to tick
                    mAddButton.setBackgroundResource(R.drawable.ic_done_black_24dp);

                    DatabaseReference mActivityEventRef = mFirebaseDatabase.getReference("ActivityEvent");
                    ActivityOccasionItem activityOccasionItem = new ActivityOccasionItem(eventID, userID);
                    mActivityEventRef.push().setValue(activityOccasionItem);

                    Toast.makeText(mContext, "Event added to your list!", Toast.LENGTH_SHORT).show();
                } else {
                    // change back to plus
                    mAddButton.setBackgroundResource(R.drawable.ic_add_black_24dp);

                    // delete the entry from activity DB
                    final DatabaseReference mActivityEventRef = mFirebaseDatabase.getReference("ActivityEvent");
                    mActivityEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                ActivityOccasionItem selected = snapshot.getValue(ActivityOccasionItem.class);
                                if (eventID.equals(selected.getOccasionID()) && userID.equals(selected.getUserID())) {
                                    String key = snapshot.getKey();
                                    mActivityEventRef.child(key).removeValue();
                                    Toast.makeText(mContext, "Event removed from your list", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    MainActivity.mEventIDs.remove(eventID);
                }
            }
        });

        if (isLiked) {
            mLikeButton.setBackgroundResource(R.drawable.ic_favorite_red_24dp);
        } else {
            mLikeButton.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
        }

        mLikeButton.setChecked(isLiked);


        mLikeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changes = true;
                if (isChecked) {
                    mLikeButton.setBackgroundResource(R.drawable.ic_favorite_red_24dp);

                    DatabaseReference mLikeEventRef = mFirebaseDatabase.getReference("LikeEvent");
                    LikeOccasionItem likeOccasionItem = new LikeOccasionItem(eventID, userID);
                    mLikeEventRef.push().setValue(likeOccasionItem);

                } else {
                    mLikeButton.setBackgroundResource(R.drawable.ic_favorite_black_24dp);

                    // delete the entry from like DB
                    final DatabaseReference mLikeEventRef = mFirebaseDatabase.getReference("LikeEvent");
                    mLikeEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                LikeOccasionItem selected = snapshot.getValue(LikeOccasionItem.class);
                                if (eventID.equals(selected.getOccasionID()) && userID.equals(selected.getUserID())) {
                                    String key = snapshot.getKey();
                                    mLikeEventRef.child(key).removeValue();
                                    Toast.makeText(mContext, "Unliked", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    MainActivity.mLikeEventIDs.remove(eventID);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (changes) {
            EventsFragment.setRefresh(true);
        }
        MylistFragment.setRefreshList(true);
        EventsMyListFragment.setRefreshList(true);
        super.onBackPressed();
    }
}
