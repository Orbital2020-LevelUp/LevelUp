package com.Events.LevelUp.ui.events;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ActivityOccasionItem;
import com.MainActivity;
import com.UserItem;
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

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> implements Filterable {
    private ArrayList<EventsItem> mEventsList;
    private ArrayList<EventsItem> mEventsListFull;
    private StorageReference mProfileStorageRef;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserRef;

    private FragmentActivity mContext;
    private DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.UK);


    // ViewHolder holds the content of the card
    public static class EventsViewHolder extends RecyclerView.ViewHolder {
        public String uid;
        public String creatorName;

        public ImageView mImageView;
        public ImageView mAddButton;
        public ImageView mLikeButton;
        public TextView mTextView1;
        public TextView mTextView2;
        public TextView mTextView3;
        public TextView mTextView4;
        public TextView mTextView5;
        public TextView mTextView6;

        public View itemView;

        public EventsViewHolder(final Context context, View itemView) {
            super(itemView);
            this.itemView = itemView;
            mImageView = itemView.findViewById(R.id.imageView);
            mAddButton = itemView.findViewById(R.id.image_add);
            mLikeButton = itemView.findViewById(R.id.image_like);
            mTextView1 = itemView.findViewById(R.id.title);
            mTextView2 = itemView.findViewById(R.id.event_description);
            mTextView3 = itemView.findViewById(R.id.date);
            mTextView4 = itemView.findViewById(R.id.location);
            mTextView5 = itemView.findViewById(R.id.time);
            mTextView6 = itemView.findViewById(R.id.eventCreator);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     // Toast.makeText(context, "Button Clicked", Toast.LENGTH_SHORT).show();
                     Intent intent = new Intent(context, EventPage.class);
                     intent.putExtra("uid", uid);
                     intent.putExtra("creatorName", creatorName);
                     intent.putExtra("title", mTextView1.getText().toString());
                     intent.putExtra("description", mTextView2.getText().toString());
                     intent.putExtra("date", mTextView3.getText().toString());
                     intent.putExtra("location", mTextView4.getText().toString());
                     intent.putExtra("time", mTextView5.getText().toString());
                     intent.putExtra("position", getAdapterPosition());
                     context.startActivity(intent);
                }
            });

        }

        public void setUid(String newUID) {
            this.uid = newUID;
        }

        public void setCreatorName(String creatorName) {
            this.creatorName = creatorName;
        }
    } // static class EventsViewHolder ends here

    //Constructor for EventsAdapter class. This ArrayList contains the
    //complete list of items that we want to add to the View.
    public EventsAdapter(FragmentActivity context, ArrayList<EventsItem> EventsList) {
        mEventsList = EventsList;
        mContext = context;
        mEventsListFull = new ArrayList<>(EventsList);
        mProfileStorageRef = FirebaseStorage.getInstance()
                .getReference("profile picture uploads");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserRef = mFirebaseDatabase.getReference("Users");
    }

    //inflate the items in a EventsViewHolder
    @NonNull
    @Override
    public EventsAdapter.EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        EventsAdapter.EventsViewHolder evh = new EventsAdapter.EventsViewHolder(mContext, v);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull EventsAdapter.EventsViewHolder holder, final int position) {
        final EventsItem currentItem = mEventsList.get(position);
//        holder.mImageView.setImageResource(currentItem.getProfilePicture());
        final EventsViewHolder holder1 = holder;
        final String creatorUID = currentItem.getCreatorID();
        holder1.setUid(creatorUID);
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
        holder1.mTextView4.setText(currentItem.getLocationInfo());

        String date = df.format(currentItem.getDateInfo());
        holder1.mTextView3.setText(date);

        String time = currentItem.getTimeInfo();
        holder1.mTextView5.setText(time);

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

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder1.mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventsItem ei = mEventsList.get(position);

//                int index = EventsFragment.getEventsItemListCopy().indexOf(ei);
//                MylistFragment.setNumberEvents(index);

                // add to ActivityEvent firebase
                UserItem user = MainActivity.currUser;
                String eventID = ei.getEventID();
                String userID = user.getId();
                DatabaseReference mActivityEventRef = mFirebaseDatabase.getReference("ActivityEvent");
                ActivityOccasionItem activityOccasionItem = new ActivityOccasionItem(eventID, userID);
                mActivityEventRef.push().setValue(activityOccasionItem);


                Toast.makeText(mContext, "Event added to your list!", Toast.LENGTH_SHORT).show();
            }
        });

        holder1.mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView = v.findViewById(R.id.image_like);
                imageView.setImageResource(R.drawable.ic_favorite_red_24dp);

            }
        });

    }

    @Override
    public int getItemCount() {
        return mEventsList.size();
    }

    @Override
    public Filter getFilter() { // for the 'implements Filterable'
        return eventsFilter;
    }

    private Filter eventsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<EventsItem> filteredList = new ArrayList<>(); // initially empty list

            if (constraint == null || constraint.length() == 0) { // search input field empty
                filteredList.addAll(mEventsListFull); // to show everything
            } else {
                String userSearchInput = constraint.toString().toLowerCase().trim();

                for (EventsItem item : mEventsListFull) {
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

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mEventsList.clear();
            mEventsList.addAll((List) results.values); // data list contains filtered items
            notifyDataSetChanged(); // tell adapter list has changed
        }
    };


}
