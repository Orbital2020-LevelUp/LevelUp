package com.Jios.LevelUp.ui.jios;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ActivityOccasionItem;
import com.Events.LevelUp.ui.events.EventsItem;
import com.LikeOccasionItem;
import com.MainActivity;
import com.UserItem;
import com.UserProfile;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JiosAdapter extends RecyclerView.Adapter<JiosAdapter.JiosViewHolder> implements Filterable {
    //ArrayList is passed in from JiosItem.java
    private ArrayList<JiosItem> mJiosList;
    private ArrayList<JiosItem> mJiosListFull;
    private StorageReference mProfileStorageRef;
    private DatabaseReference mUserRef;
    private FirebaseDatabase mFirebaseDatabase;

    private Context mContext;
    private DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);

    //the ViewHolder holds the content of the card
    public static class JiosViewHolder extends RecyclerView.ViewHolder {
        public String creatorUid;
        public String creatorName;
        public int creatorResidence;
        public String profilePictureUri;
        public String email;
        public long phone;
        public String telegram;

        public String jioID;
        public boolean isChecked;
        public boolean isLiked;
        public int numLikes;

        public ToggleButton mAddButton;
        public ToggleButton mLikeButton;
        public ImageView mImageView;
        public TextView mTextView1;
        public TextView mTextView2;
        public TextView mTextView3;
        public TextView mTextView4;
        public TextView mTextView5;
        public TextView mTextView6;
        public TextView mNumLikes;

        public JiosViewHolder(final Context context, View itemView) {
            super(itemView);
            final Context context1 = context;
            mImageView = itemView.findViewById(R.id.imageView);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UserProfile.class);
                    intent.putExtra("creatorfid", creatorUid);
                    intent.putExtra("name", creatorName);
                    intent.putExtra("residence", creatorResidence);
                    intent.putExtra("dpUri", profilePictureUri);
                    intent.putExtra("telegram", telegram);
                    intent.putExtra("email", email);
                    intent.putExtra("phone", phone);
                    context.startActivity(intent);
                }
            });
            mAddButton = itemView.findViewById(R.id.image_add);
            mLikeButton = itemView.findViewById(R.id.image_like);
            mTextView1 = itemView.findViewById(R.id.title);
            mTextView2 = itemView.findViewById(R.id.event_description);
            mTextView3 = itemView.findViewById(R.id.date);
            mTextView4 = itemView.findViewById(R.id.location);
            mTextView5 = itemView.findViewById(R.id.time);
            mTextView6 = itemView.findViewById(R.id.eventCreator);
            mTextView6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, UserProfile.class);
                    intent.putExtra("creatorfid", creatorUid);
                    intent.putExtra("name", creatorName);
                    intent.putExtra("residence", creatorResidence);
                    intent.putExtra("dpUri", profilePictureUri);
                    intent.putExtra("telegram", telegram);
                    intent.putExtra("email", email);
                    intent.putExtra("phone", phone);
                    context.startActivity(intent);
                }
            });
            mNumLikes = itemView.findViewById(R.id.numlikes_textview);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Toast.makeText(context, "Button Clicked", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, JiosPage.class);
                    intent.putExtra("uid", creatorUid);
                    intent.putExtra("creatorName", creatorName);
                    intent.putExtra("title", mTextView1.getText().toString());
                    intent.putExtra("description", mTextView2.getText().toString());
                    intent.putExtra("date", mTextView3.getText().toString());
                    intent.putExtra("location", mTextView4.getText().toString());
                    intent.putExtra("time", mTextView5.getText().toString());
                    intent.putExtra("position", getAdapterPosition());
                    intent.putExtra("stateChecked", isChecked);
                    intent.putExtra("stateLiked", isLiked);
                    intent.putExtra("numLikes", numLikes);
                    intent.putExtra("jioID", jioID);
                    intent.putExtra("residence", creatorResidence);
                    intent.putExtra("dpUri", profilePictureUri);
                    intent.putExtra("telegram", telegram);
                    intent.putExtra("email", email);
                    intent.putExtra("phone", phone);
                    context.startActivity(intent);
                }
            });
        }

        public void setCreatorUid(String newUID) {
            this.creatorUid = newUID;
        }

        public void setCreatorName(String creatorName) {
            this.creatorName = creatorName;
        }

        public void setCreatorResidence(int creatorResidence) {this.creatorResidence = creatorResidence;}

        public void setProfilePictureUri(String profilePictureUri) { this.profilePictureUri = profilePictureUri;}

        public void setEmail(String email) {this.email = email;}

        public void setTelegram(String telegram) { this.telegram = telegram;}

        public void setPhone(long phone) {this.phone = phone;}

        public void setChecked(boolean toSet) {this.isChecked = toSet; }

        public void setJioID(String jioID) {
            this.jioID = jioID;
        }

        public void setLiked(boolean liked) {
            isLiked = liked;
        }

        public void setNumLikes(int numLikes) {
            this.numLikes = numLikes;
        }
    }

    //Constructor for JiosAdapter class. This ArrayList contains the
    //complete list of items that we want to add to the View.
    public JiosAdapter(Context context, ArrayList<JiosItem> JiosList) {
        mContext = context;
        mJiosList = JiosList;
        mJiosListFull = new ArrayList<>(JiosList); // copy of JiosList for SearchView
        mProfileStorageRef = FirebaseStorage.getInstance()
                .getReference("profile picture uploads");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserRef = mFirebaseDatabase.getReference("Users");
    }

    //inflate the items in a JiosViewHolder
    @NonNull
    @Override
    public JiosAdapter.JiosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.occ_item, parent, false);
        JiosAdapter.JiosViewHolder evh = new JiosAdapter.JiosViewHolder(mContext, v);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull JiosAdapter.JiosViewHolder holder, final int position) {
        JiosItem currentItem = mJiosList.get(position);
//        holder.mImageView.setImageResource(currentItem.getProfilePicture());
        final JiosViewHolder holder1 = holder;
        final String creatorUID = currentItem.getCreatorID();
        holder1.setCreatorUid(creatorUID);
        StorageReference mProfileStorageRefIndiv = mProfileStorageRef.child(creatorUID);
        mProfileStorageRefIndiv.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder1.mImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                holder1.mImageView.setImageResource(R.drawable.fake_user_dp);
            }
        });

        holder1.mTextView1.setText(currentItem.getTitle());
        holder1.mTextView2.setText(currentItem.getDescription());
        holder1.mTextView3.setText(df.format(currentItem.getDateInfo()));
        holder1.mTextView4.setText(currentItem.getLocationInfo());
        holder1.mTextView5.setText(currentItem.getTimeInfo());
        holder1.mNumLikes.setText(Integer.toString(currentItem.getNumLikes()));

        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserItem selected = snapshot.getValue(UserItem.class);
                    String id = selected.getId();

                    if (creatorUID.equals(id)) {
                        String name = selected.getName();
                        holder1.mTextView6.setText(name);
                        holder1.setCreatorName(name);
                        int res = selected.getResidential();
                        String telegram = selected.getTelegram();
                        String email = selected.getEmail();
                        String dpUri = selected.getProfilePictureUri();
                        long phone = selected.getPhone();
                        holder1.setCreatorName(name);
                        holder1.setCreatorResidence(res);
                        holder1.setTelegram(telegram);
                        holder1.setEmail(email);
                        holder1.setProfilePictureUri(dpUri);
                        holder1.setPhone(phone);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String jioID = currentItem.getJioID();
        holder1.setJioID(jioID);
        if (MainActivity.mJioIDs.contains(jioID)) {
            holder1.mAddButton.setBackgroundResource(R.drawable.ic_done_black_24dp);
            holder1.setChecked(true);
            holder1.mAddButton.setChecked(true);
        } else {
            holder1.mAddButton.setBackgroundResource(R.drawable.ic_add_black_24dp);
            holder1.setChecked(false);
            holder1.mAddButton.setChecked(false);
        }

        holder1.mAddButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // change to tick
                    holder1.mAddButton.setBackgroundResource(R.drawable.ic_done_black_24dp);
                    holder1.setChecked(true);

                    JiosItem ji = mJiosList.get(position);

//                int index = EventsFragment.getEventsItemListCopy().indexOf(ei);
//                MylistFragment.setNumberEvents(index);

                    // add to ActivityEvent firebase
                    UserItem user = MainActivity.currUser;
                    String eventID = ji.getJioID();
                    String userID = user.getId();
                    DatabaseReference mActivityJioRef = mFirebaseDatabase.getReference("ActivityJio");
                    ActivityOccasionItem activityOccasionItem = new ActivityOccasionItem(eventID, userID);
                    mActivityJioRef.push().setValue(activityOccasionItem);

                    Toast.makeText(mContext, "Jio added to your list!", Toast.LENGTH_SHORT).show();
                } else {
                    // change back to plus
                    holder1.mAddButton.setBackgroundResource(R.drawable.ic_add_black_24dp);
                    holder1.setChecked(false);

                    // delete the entry from activity DB
                    JiosItem ji = mJiosList.get(position);
                    UserItem user = MainActivity.currUser;
                    final String jioID = ji.getJioID();
                    final String userID = user.getId();
                    final DatabaseReference mActivityJioRef = mFirebaseDatabase.getReference("ActivityJio");
                    mActivityJioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                ActivityOccasionItem selected = snapshot.getValue(ActivityOccasionItem.class);
                                if (jioID.equals(selected.getOccasionID()) && userID.equals(selected.getUserID())) {
                                    String key = snapshot.getKey();
                                    mActivityJioRef.child(key).removeValue();
                                    Toast.makeText(mContext, "Jio removed from your list", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    MainActivity.mJioIDs.remove(jioID);
                }
            }
        });

        if (MainActivity.mLikeJioIDs.contains(jioID)) {
            holder1.mLikeButton.setBackgroundResource(R.drawable.ic_favorite_red_24dp);
            holder1.setLiked(true);
            holder1.mLikeButton.setChecked(true);
        } else {
            holder1.mLikeButton.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
            holder1.setLiked(false);
            holder1.mLikeButton.setChecked(false);
        }

        holder1.setNumLikes(currentItem.getNumLikes());

        holder1.mLikeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    holder1.mLikeButton.setBackgroundResource(R.drawable.ic_favorite_red_24dp);
                    holder1.setLiked(true);

                    // send to LikeDatabase
                    JiosItem ji = mJiosList.get(position);
                    UserItem user = MainActivity.currUser;
                    final String eventID = ji.getJioID();
                    final String userID = user.getId();
                    DatabaseReference mLikeJioRef = mFirebaseDatabase.getReference("LikeJio");
                    LikeOccasionItem likeOccasionItem = new LikeOccasionItem(eventID, userID);
                    mLikeJioRef.push().setValue(likeOccasionItem);

                    // +1 to the Likes on the eventItem
                    int currLikes = ji.getNumLikes();
                    DatabaseReference mJioRef = mFirebaseDatabase.getReference("Jios");
                    mJioRef.child(eventID).child("numLikes").setValue(currLikes + 1);
                    ji.setNumLikes(currLikes + 1);
                    holder1.setNumLikes(currLikes + 1);

                    // for display only
                    holder1.mNumLikes.setText(Integer.toString(currLikes + 1));
                } else {
                    holder1.mLikeButton.setBackgroundResource(R.drawable.ic_favorite_black_24dp);
                    holder1.setLiked(false);

                    // Delete the entry from LikeDatabse
                    JiosItem ji = mJiosList.get(position);
                    UserItem user = MainActivity.currUser;
                    final String eventID = ji.getJioID();
                    final String userID = user.getId();
                    final DatabaseReference mLikeJioRef = mFirebaseDatabase.getReference("LikeJio");
                    mLikeJioRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                LikeOccasionItem selected = snapshot.getValue(LikeOccasionItem.class);
                                if (eventID.equals(selected.getOccasionID()) && userID.equals(selected.getUserID())) {
                                    String key = snapshot.getKey();
                                    mLikeJioRef.child(key).removeValue();
                                    Toast.makeText(mContext, "Unliked", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    // -1 to the Likes on the eventItem
                    int currLikes = ji.getNumLikes();
                    DatabaseReference mJioRef = mFirebaseDatabase.getReference("Jios");
                    mJioRef.child(eventID).child("numLikes").setValue(currLikes - 1);
                    ji.setNumLikes(currLikes - 1);
                    holder1.setNumLikes(currLikes - 1);

                    // for display only
                    holder1.mNumLikes.setText(Integer.toString(currLikes - 1));

                    MainActivity.mLikeJioIDs.remove(jioID);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mJiosList.size();
    }

    @Override
    public Filter getFilter() { // for the 'implements Filterable'
        return jiosFilter;
    }

    private Filter jiosFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<JiosItem> filteredList = new ArrayList<>(); // initially empty list

            if (constraint == null || constraint.length() == 0) { // search input field empty
                filteredList.addAll(mJiosListFull); // to show everything
            } else {
                String userSearchInput = constraint.toString().toLowerCase().trim();

                for (JiosItem item : mJiosListFull) {
                    // contains can be changed to StartsWith
                    if (item.getTitle().toLowerCase().contains(userSearchInput)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mJiosList.clear();
            mJiosList.addAll((List) results.values); // data list contains filtered items
            notifyDataSetChanged(); // tell adapter list has changed

        }
    };

    public void resetAdapter() {
        this.mJiosList = mJiosListFull;
    }
}