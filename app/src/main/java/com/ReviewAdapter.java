package com;

import java.util.ArrayList;

import com.example.tryone.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private ArrayList<ReviewItem> reviewList;
    private StorageReference profileStorageRef;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserRef;

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private ImageView profilePicture;
        private TextView reviewerName;
        private TextView reviewDate;
        private TextView review;

        /**
         * Constructor for the ReviewViewHolder class
         *
         * @param itemView View of the item to be displayed
         */
        public ReviewViewHolder(View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.imageViewReview);
            reviewerName = itemView.findViewById(R.id.author);
            reviewDate = itemView.findViewById(R.id.date);
            review = itemView.findViewById(R.id.review_description);
        }
    }

    /**
     * Constructor of the ReviewAdapter class
     *
     * @param reviewList List of reviews for a particular user
     */
    public ReviewAdapter(ArrayList<ReviewItem> reviewList) {
        this.reviewList = reviewList;
        profileStorageRef = FirebaseStorage.getInstance()
                .getReference("profile picture uploads");
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);
        ReviewViewHolder rvh = new ReviewViewHolder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewItem currentItem = reviewList.get(position);
        final ReviewViewHolder holder1 = holder;
        String reviewerID = currentItem.getUserID();
        StorageReference profileStorageRefIndiv = profileStorageRef.child(reviewerID);
        profileStorageRefIndiv.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder1.profilePicture);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                holder1.profilePicture.setImageResource(R.drawable.fake_user_dp);
            }
        });
        holder.reviewerName.setText(currentItem.getReviewerdisplayName());
        holder.reviewDate.setText(currentItem.getDate());
        holder.review.setText(currentItem.getReview());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }
}
