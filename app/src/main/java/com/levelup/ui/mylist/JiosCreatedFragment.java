package com.Mylist.LevelUp.ui.mylist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.levelup.ui.jios.JiosItem;
import com.Occasion;
import com.example.tryone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class JiosCreatedFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MylistCreatedAdapter mAdapter;
    private View rootView;
    private ArrayList<Occasion> mOccasionJios = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.occ_mylist_fragment_plain, container, false);

        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        final String fbUIDFinal = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDatabaseReferenceJios = mFirebaseDatabase.getReference().child("Jios");
        mDatabaseReferenceJios.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mOccasionJios.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    JiosItem selected = snapshot.getValue(JiosItem.class);
                    String creatorID = selected.getCreatorID();
                    if (creatorID.equals(fbUIDFinal)) {
                        if (selected.getTimeInfo().length() > 4) {
                            continue;
                        }

                        int hour = Integer.parseInt(selected.getTimeInfo().substring(0,2));
                        int min = Integer.parseInt(selected.getTimeInfo().substring(2));

                        Date eventDateZero = selected.getDateInfo();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(eventDateZero);
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, min);
                        Date eventDate = cal.getTime();

                        Date currentDate = new Date();

                        if (eventDate.compareTo(currentDate) >= 0) {
                            mOccasionJios.add(selected);
                        }
                    }
                }
                MylistCreatedAdapter mylistCreatedAdapter = new MylistCreatedAdapter(getActivity(), mOccasionJios);
                mAdapter = mylistCreatedAdapter;
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        buildRecyclerView();
        return rootView;
    }

    public void buildRecyclerView() {
        mRecyclerView = rootView.findViewById(R.id.occMylistFragmentRecyclerView);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new MylistCreatedAdapter(getActivity(), mOccasionJios);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}
