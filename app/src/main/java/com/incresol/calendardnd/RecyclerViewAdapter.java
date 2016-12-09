package com.incresol.calendardnd;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Incresol-078 on 16-09-2016.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static ArrayList<EventDetails> mDatasetEvents;
    Context mContext;

    public RecyclerViewAdapter(){
        super();
    }

    public RecyclerViewAdapter(ArrayList<EventDetails> mDataSetEvents, Context ctx){
        this.mDatasetEvents=mDataSetEvents;
        this.mContext=ctx;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView textView_eventSummary,textView_eventStartTime,textView_eventEndTime,textView_switch;
        public LinearLayout rowLayout_main;
        ArrayList<EventDetails> eventDetails = new ArrayList<EventDetails>();
        Context mCtx;
        android.support.v7.widget.SwitchCompat dnd_switch;


        public ViewHolder(View itemView, final Context ctx, final ArrayList<EventDetails> eventDetails) {
            super(itemView);
            this.eventDetails = eventDetails;
            this.mCtx = ctx;
            itemView.setOnClickListener(this);
            textView_eventSummary=(TextView)itemView.findViewById(R.id.textView_eventSummary);
            textView_eventStartTime=(TextView)itemView.findViewById(R.id.textView_eventStartTime);
            textView_eventEndTime=(TextView)itemView.findViewById(R.id.textView_eventEndTime);
            dnd_switch=(android.support.v7.widget.SwitchCompat)itemView.findViewById(R.id.dnd_switch);
            textView_switch=(TextView)itemView.findViewById(R.id.textView_switch);
            rowLayout_main=(LinearLayout) itemView.findViewById(R.id.rowlayout_main);

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            EventDetails eventDetails = this.eventDetails.get(position);
            Intent intent_eventDetails = new Intent(mCtx,Event_In_Detail.class);
            intent_eventDetails.putExtra("Event_Id",eventDetails.getEVENT_ID());
            intent_eventDetails.putExtra("Event_Summary",eventDetails.getEVENT_SUMMARY());
            intent_eventDetails.putExtra("Event_Description",eventDetails.getEVENT_DESCRIPTION());
            intent_eventDetails.putExtra("Event_Start_Time",eventDetails.getSTART_TIME());
            intent_eventDetails.putExtra("Event_End_Time",eventDetails.getEND_TIME());
            intent_eventDetails.putExtra("Event_Location",eventDetails.getLOCATION());
            intent_eventDetails.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mCtx.startActivity(intent_eventDetails);
        }
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.rowlayout,parent,false);
        ViewHolder viewHolder=new ViewHolder(v,mContext,mDatasetEvents);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final EventDetails events = mDatasetEvents.get(position);
        String Event_Summary = events.getEVENT_SUMMARY();
        long Event_Start_Time = events.getSTART_TIME();
        long Event_End_Time = events.getEND_TIME();



        holder.textView_eventSummary.setText(Event_Summary);
        holder.textView_eventStartTime.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(Event_Start_Time).toString());
        holder.textView_eventEndTime.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(Event_End_Time).toString());

        //experiment
        int switch_state = events.getSWITCH_STATE();
        if(switch_state==1){
            holder.dnd_switch.setChecked(true);
            holder.textView_switch.setText("ON");
        }else{
            holder.dnd_switch.setChecked(false);
            holder.textView_switch.setText("OFF");
        }

        holder.dnd_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean on = ((android.support.v7.widget.SwitchCompat)view).isChecked();
                int switch_value;
                if(on) {
                    System.out.println("inside on method in switch onclick method");
                    switch_value = 1;
                    holder.textView_switch.setText("ON");
                } else {
                    System.out.println("inside off method in switch onclick method");
                    switch_value = 0;
                    holder.textView_switch.setText("OFF");
                }

                DateBaseAdapter dateBaseAdapter = new DateBaseAdapter(mContext);
                dateBaseAdapter.openDB();
                dateBaseAdapter.update_switch_state(events.getEVENT_ID(),switch_value);
                dateBaseAdapter.close();

            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatasetEvents.size();
    }

}
