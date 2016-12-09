package com.incresol.calendardnd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by incresol-026 on 21/11/16.
 */

public class DateBaseAdapter {

    Context c;
    SQLiteDatabase db;
    DatabaseHelper helper;
    public static final String TABLE_NAME = "calendarEvents";
    public static final String EVENT_ID="EVENT_ID";
    public static final String EVENT_SUMMARY="EVENT_SUMMARY";
    public static final String EVENT_DESCRIPTION="EVENT_DESCRIPTION";
    public static final String START_TIME="START_TIME";
    public static final String END_TIME="END_TIME";
    public static final String SWITCH_STATE="SWITCH_STATE";
    public static final String LOCATION="LOCATION";

    public DateBaseAdapter(Context ctx){
        this.c=ctx;
        helper=new DatabaseHelper(c);
    }

    //OPEN DB
    public DateBaseAdapter openDB(){

        try{
                db=helper.getWritableDatabase();
        }catch (SQLException e){
                e.printStackTrace();
        }
        return this;
    }

    //CLOSE
    public void close(){
        try {
            helper.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    //INSERT DATE INTO DB

        public void insertIntoDB(String event_id, String event_summary, String event_description, long start_time, long end_time, Integer switch_state, String event_location){

        System.out.println("inside ==========================> insertIntoDB()");

        try{

        ContentValues contentValues = new ContentValues();
        contentValues.put("EVENT_ID",event_id);
        contentValues.put("EVENT_SUMMARY",event_summary);
        contentValues.put("EVENT_DESCRIPTION",event_description);
        contentValues.put("START_TIME",start_time);
        contentValues.put("END_TIME",end_time);
        contentValues.put("SWITCH_STATE",switch_state);
        contentValues.put("LOCATION",event_location);
        db.insert(TABLE_NAME,null,contentValues);

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    //DELETE FROM DATABASE

    public void delete_calendarEvents_table(){
        System.out.println("inside ==========================> delete_calendarEvents_table()");
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

            helper.onCreate(db);
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    //RETRIEVE DATE FROM DATABASE

    public Cursor getData(){

        System.out.println("inside ==========================> getData()");

        String[] projection = {"EVENT_ID","EVENT_SUMMARY","EVENT_DESCRIPTION","START_TIME","END_TIME","SWITCH_STATE","LOCATION"};
        return db.query(TABLE_NAME,projection,null,null,null,null,START_TIME+" ASC");

    }

    //NUMBER OF ROWS

    public int numberOfRows(){

        System.out.println("inside /db==========================> numberOfRows()" + (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME) );
       return  (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
    }


    public void update_switch_state(String Event_Id, int Event_Switch_State){

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("SWITCH_STATE", Event_Switch_State);

            db.update(TABLE_NAME, contentValues, "EVENT_ID =?", new String[]{String.valueOf(Event_Id)});

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void update_event(String Event_Id, String Event_summary, String Event_description, long Start_time, long End_time, String Event_location){

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(EVENT_ID, Event_Id);
            contentValues.put(EVENT_SUMMARY, Event_summary);
            contentValues.put(EVENT_DESCRIPTION, Event_description);
            contentValues.put(START_TIME, Start_time);
            contentValues.put(END_TIME, End_time);
            contentValues.put(LOCATION, Event_location);

            db.update(TABLE_NAME, contentValues, "EVENT_ID =?", new String[]{String.valueOf(Event_Id)});

        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    public void delete_event(String Event_Id){

        try {
            db.execSQL("delete from "+TABLE_NAME+" where EVENT_ID='"+Event_Id+"'");
            System.out.println("DELETED in event deletion case==========>>");
           // db.delete(TABLE_NAME, EVENT_ID + "=" + Event_Id, null);
        }catch(SQLException e){
            System.out.println("SQLException in event deletion case==========>>");
            e.printStackTrace();
        }

    }


    public Cursor getEventIDs(){

        String[] projection = {"EVENT_ID"};

        return db.query(TABLE_NAME,projection,null,null,null,null,null);

    }


}
