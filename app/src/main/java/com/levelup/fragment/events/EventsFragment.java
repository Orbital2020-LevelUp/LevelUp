package com.levelup.fragment.events;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.levelup.R;
import com.levelup.activity.MainActivity;
import com.levelup.ui.events.EventsAdapter;
import com.levelup.ui.events.EventsAdder;
import com.levelup.ui.events.EventsItem;
import com.levelup.ui.events.EventsLikedFragment;
import com.levelup.ui.events.EventsMyListFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EventsFragment extends Fragment {
    private static ArrayList<EventsItem> EventsItemList;
    private static ArrayList<EventsItem> copy;
    FirebaseDatabase mDatabase;
    DatabaseReference mDatabaseReference;
    ValueEventListener mValueEventListener;
    private static final String[] categories = {"All",
            "Arts", "Sports", "Talks", "Volunteering", "Food", "Others"};
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private EventsAdapter mAdapter;
    private View rootView;

    public FloatingActionButton floatingActionButton;

    private SwipeRefreshLayout swipeRefreshLayout;

    public static boolean refresh;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_events, container, false);


        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("Events");
        createEventsList();

        buildRecyclerView();
        floatingActionButton = rootView.findViewById(R.id.fab);
        if (MainActivity.getCurrentUser() != null && MainActivity.getCurrentUser().getIsStaff()) {
            floatingActionButton.setAlpha(0.50f); // setting transparency
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), EventsAdder.class);
                    startActivity(intent);
                }
            });
        } else {
            floatingActionButton.setVisibility(View.INVISIBLE);
        }
        loadDataEvents();

        // setting up toolbar
        setHasOptionsMenu(true);
        Toolbar toolbar = rootView.findViewById(R.id.events_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        swipeRefreshLayout = rootView.findViewById(R.id.swiperefreshlayoutevents);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                EventsItemList.clear();
                loadDataEvents();
                // mAdapter.notifyDataSetChanged(); - added this line into loadDataEvents itself
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return rootView;
    }

    public void createEventsList() {
        EventsItemList = new ArrayList<>();
    }

    public void buildRecyclerView() {
        mRecyclerView = rootView.findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new EventsAdapter(getActivity(), EventsItemList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_search:
                MenuItem searchItem = item;
                SearchView searchView = (SearchView) searchItem.getActionView();
                // searchView.setQueryHint("Search");
                // searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                searchItem.setActionView(searchView);

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        mAdapter.getFilter().filter(newText);
                        return false;
                    }
                });
                break;

            case R.id.action_filter:
                break;
            case R.id.subitem1:
                loadDataEvents();
            case R.id.subitem2:
                getSelectedCategoryData(0);
                break;
            case R.id.subitem3:
                getSelectedCategoryData(1);
                break;
            case R.id.subitem4:
                getSelectedCategoryData(2);
                break;
            case R.id.subitem5:
                getSelectedCategoryData(3);
                break;
            case R.id.subitem6:
                getSelectedCategoryData(4);
                break;
            case R.id.subitem7:
                getSelectedCategoryData(5);
                break;
            case R.id.action_cfmed_events: // the tick
                EventsMyListFragment nextFrag = new EventsMyListFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, nextFrag)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.action_fav: // the heart
                EventsLikedFragment nextFrag2 = new EventsLikedFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, nextFrag2)
                        .addToBackStack(null)
                        .commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.occasions_top_menu, menu);

        // setting the search function UI
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search");

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
//                EventsItemList.clear();
//                loadDataEvents();
//                mAdapter.notifyDataSetChanged();
                mAdapter.resetAdapter();
                mRecyclerView.setAdapter(mAdapter);
                closeKeyboard();
                return true;
            }
        });

//        MenuItem filterItem = menu.findItem(R.id.action_filter);
//        Spinner spinnerMenu = (Spinner) filterItem.getActionView();
//        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(),
//                android.R.layout.simple_spinner_item, categories);
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerMenu.setAdapter(spinnerAdapter);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void closeKeyboard() {
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }
    }

    public static void setEventsItemList(ArrayList<EventsItem> eventsList) {
        EventsItemList = eventsList;
    }

    public static ArrayList<EventsItem> getEventsItemListCopy() {
        return copy;
    }

    public static ArrayList<EventsItem> getEventsItemList() { return EventsItemList; }

    public void loadDataEvents() {
        mValueEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                EventsItemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventsItem selected = snapshot.getValue(EventsItem.class);
                    // EventsItemList.add(selected);

                    // To show ALL Events created comment out lines 231 to 261 and uncomment out line 227

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

                    // Toast.makeText(getActivity(), eventDate.toString(), Toast.LENGTH_SHORT).show();

                    Date currentDate = new Date();
                    // eventDate.compareTo(currentDate) >= 0
                    //eventDate.after(currentDate)
                    if (eventDate.compareTo(currentDate) >= 0) {
                        EventsItemList.add(selected);
                    }
                }
                copy = new ArrayList<>(EventsItemList);
                EventsAdapter eventsAdapter = new EventsAdapter(getActivity(), EventsItemList);
                mRecyclerView.setAdapter(eventsAdapter);
                mAdapter = eventsAdapter; // YI EN ADDED THIS LINE
                MainActivity.sort(EventsItemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addListenerForSingleValueEvent(mValueEventListener);
        mAdapter.notifyDataSetChanged();
    }

    private void getSelectedCategoryData(int categoryID) {
        //This arraylist will contain only those in the certain categories
        ArrayList<EventsItem> list = new ArrayList<>();
        EventsAdapter eventsAdapter;
//        if (categoryID == 0) {
//            eventsAdapter = new EventsAdapter(getActivity(), EventsItemList);
//        } else {
            //filter by id
            for (EventsItem eventsItem : EventsItemList) {
                if (eventsItem.getCategory() == categoryID) {
                    list.add(eventsItem);
                }
            }
            eventsAdapter = new EventsAdapter(getActivity(), list);
        // }
        mRecyclerView.setAdapter(eventsAdapter);
        mAdapter = eventsAdapter;
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    public static void setRefresh(boolean toSet) {
        refresh = toSet;
    }

    @Override
    public void onResume() {
        if (refresh) {
            loadDataEvents();
            refresh = false;
        }
        super.onResume();
    }
}

