package com;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import org.w3c.dom.Text;

public class UserProfile extends AppCompatActivity {
    private TextView displayName;
    private TextView residence;
    private ImageView displayPicture;
    private TextView telegramTitle;
    private TextView emailTitle;
    private TextView phoneTitle;
    private TextView telegramHandle;
    private TextView emailAddress;
    private TextView phoneNumber;
    private TextView IDBox;
    private TextView actualRating;
    private ImageView ratingStar;
    private TextView rateThisUser;
    private RatingBar ratingBar;

    private StorageReference mProfileStorageRef;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private String currUserId = MainActivity.getCurrentUser().getId();
    private String creatorId;

    float sumOfRatings = 0;
    float numOfRatings = 0;
    float averageRatingGlobal;

    private static final String[] residentials = {"I don't stay on campus",
            "Cinnamon", "Tembusu", "CAPT", "RC4", "RVRC",
            "Eusoff", "Kent Ridge", "King Edward VII", "Raffles",
            "Sheares", "Temasek", "PGP House", "PGP Residences", "UTown Residence",
            "Select Residence"};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        mProfileStorageRef = FirebaseStorage.getInstance()
                .getReference("profile picture uploads");
        displayName = findViewById(R.id.TextViewDisplayName);
        residence = findViewById(R.id.TextViewResidenceName);
        displayPicture = findViewById(R.id.ViewProfilePicture);
        telegramTitle = findViewById(R.id.DisplayTelegramTitle);
        telegramHandle = findViewById(R.id.DisplayTelegramHandle);
        emailTitle = findViewById(R.id.DisplayEmailTitle);
        emailAddress = findViewById(R.id.DisplayEmailAddress);
        phoneTitle = findViewById(R.id.DisplayPhoneTitle);
        phoneNumber = findViewById(R.id.DisplayPhoneNumber);
        IDBox = findViewById(R.id.IDBox);
        actualRating = findViewById(R.id.actual_score);
        ratingStar = findViewById(R.id.rating_star);
        rateThisUser = findViewById(R.id.rate_this_user);
        ratingBar = findViewById(R.id.UserRating);

        Intent intent = getIntent();
        String creatorIdCopy = intent.getStringExtra("creatorid");
        if (creatorIdCopy == null) {
            creatorId = currUserId;
        } else {
            creatorId = creatorIdCopy;
        }
        final String name = intent.getStringExtra("name");
        int residenceIndex = intent.getIntExtra("residence", 0);
        String telegram = intent.getStringExtra("telegram");
        String email = intent.getStringExtra("email");
        Long phone = intent.getLongExtra("phone", 0);

        displayName.setText(name);
        residence.setText(residentials[residenceIndex]);
        telegramHandle.setText(telegram);
        emailAddress.setText(email);
        phoneNumber.setText(phone.toString());
        StorageReference mProfileStorageRefIndiv = mProfileStorageRef.child(creatorId);
        mProfileStorageRefIndiv.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(displayPicture);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                displayPicture.setImageResource(R.drawable.fake_user_dp);
            }
        });


        //pulling the rating from the database
        db.getReference().child("Users").child(creatorId).child("Ratings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child :dataSnapshot.getChildren()) {
                        sumOfRatings += Float.parseFloat(child.getValue().toString());
                        numOfRatings++;
                        if (child.getKey().equals(currUserId)) {
                            ratingBar.setRating(Float.parseFloat(child.getValue().toString()));
                        }
                    }
                }
                float averageRating = sumOfRatings / numOfRatings;
                averageRatingGlobal = averageRating;
                actualRating.setText(String.format("%.2f", averageRating));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        //when the rating is changed.
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                db.getReference().child("Users").child(currUserId).child("Ratings").child(creatorId).setValue(rating);
                db.getReference().child("Users").child(creatorId).child("Ratings").child(currUserId).setValue(rating);
            }
        });

    }
}
