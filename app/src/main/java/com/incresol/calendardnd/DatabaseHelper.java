package com.incresol.calendardnd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Settings;

/**
 * Created by incresol-026 on 1/11/16.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "CalendarEventsDB";
    public static final String TABLE_NAME = "calendarEvents";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS calendarEvents(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, EVENT_ID VARCHAR, EVENT_SUMMARY VARCHAR, EVENT_DESCRIPTION VARCHAR, START_TIME INTEGER, END_TIME INTEGER, SWITCH_STATE INTEGER, LOCATION VARCHAR)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
