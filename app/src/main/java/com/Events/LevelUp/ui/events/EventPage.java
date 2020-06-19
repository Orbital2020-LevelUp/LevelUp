package com.Events.LevelUp.ui.events;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.MainActivity;
import com.example.LevelUp.ui.events.EventsFragment;
import com.example.LevelUp.ui.mylist.MylistFragment;
import com.example.tryone.R;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class EventPage extends AppCompatActivity {

    private ImageView mImageView;
    private ImageView mAddButton;
    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;
    private TextView mTextView4;
    private TextView mTextView5;
    private int position;
    private ArrayList<EventsItem> eventsItemArrayList = EventsFragment.getEventsItemList();
    private Context mContext = this;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.occasion_page);
        mImageView = findViewById(R.id.event_page_image);
        mAddButton = findViewById(R.id.events_page_image_add);
        mTextView1 = findViewById(R.id.event_page_title);
        mTextView2 = findViewById(R.id.event_page_date);
        mTextView3 = findViewById(R.id.event_page_time);
        mTextView4 = findViewById(R.id.event_page_location);
        mTextView5 = findViewById(R.id.event_page_description);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        String location = intent.getStringExtra("location");
        String description = intent.getStringExtra("description");
        position = intent.getIntExtra("position", 0);
        mImageView.setImageResource(R.mipmap.ic_launcher_round);
        mAddButton.setImageResource(R.drawable.ic_add_black_24dp);
        mTextView1.setText(title);
        mTextView2.setText(date);
        mTextView3.setText(time);
        mTextView4.setText(location);
        mTextView5.setText(description);

        // add plus then add to my list
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Button clicked", Toast.LENGTH_SHORT).show();
                EventsItem ei = eventsItemArrayList.get(position);
                int index = MainActivity.getEventsListCopy().indexOf(ei);
                MylistFragment.setNumberEvents(index);
            }
        });
    }
}