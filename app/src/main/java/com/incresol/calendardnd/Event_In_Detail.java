package com.incresol.calendardnd;

import android.app.ActionBar;
import android.media.AudioManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class Event_In_Detail extends AppCompatActivity{

    TextView textView_eventSummary,textView_eventDescription,textView_eventStartTime,textView_eventEndTime,textView_eventLocation;
    String event_summary, event_description,  event_location;
    long event_start_time, event_end_time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event__in__detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textView_eventSummary=(TextView)findViewById(R.id.textView_eventSummary);
        textView_eventDescription=(TextView)findViewById(R.id.textView_eventDescription);
        textView_eventStartTime=(TextView)findViewById(R.id.textView_eventStartTime);
        textView_eventEndTime=(TextView)findViewById(R.id.textView_eventEndTime);
        textView_eventLocation=(TextView)findViewById(R.id.textView_eventLocation);

        event_summary = getIntent().getStringExtra("Event_Summary");
        event_description = getIntent().getStringExtra("Event_Description");
        event_start_time = getIntent().getLongExtra("Event_Start_Time",0);
        event_end_time = getIntent().getLongExtra("Event_End_Time",0);
        event_location = getIntent().getStringExtra("Event_Location");

        System.out.println(" ******** event description ************* : " +event_description );

        if(event_description==null){
            textView_eventDescription.setText("No Description");
        }else{
            textView_eventDescription.setText(event_description);
        }

        textView_eventSummary.setText(event_summary);
        textView_eventStartTime.setText(new SimpleDateFormat("HH:mm").format(event_start_time).toString());
        textView_eventEndTime.setText(" - "+new SimpleDateFormat("HH:mm").format(event_end_time).toString());

        if(event_location==null){
            textView_eventLocation.setText("No Location");
        }else{
            textView_eventLocation.setText(event_location);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
