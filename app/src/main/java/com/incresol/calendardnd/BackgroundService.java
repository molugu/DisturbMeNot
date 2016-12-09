package com.incresol.calendardnd;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.google.api.client.util.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by incresol-026 on 24/11/16.
 */

public class BackgroundService extends Service {

    private Timer timer = new Timer();
    Handler mHandler = new Handler();
    private DateBaseAdapter dateBaseAdapter;
    Boolean inTime;
    public static AudioManager audioManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        inTime=false;
        dateBaseAdapter = new DateBaseAdapter(this);
        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);

        final Runnable mRunBackgroundService = new Runnable() {
            public void run() {


                checkDnd();

            }
        };

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(mRunBackgroundService);
            }
        },0,15*1000);
    }

    public void checkDnd(){

        Integer event_switch_state;
        long event_start_time, event_end_time;
        SimpleDateFormat dateFormat;
        String nowString,startString,endString,event_id;
        Date nowone,startone,endone;
        int num_of_rows;

       // Toast.makeText(getApplicationContext(),"Background Service is Running",Toast.LENGTH_LONG).show();

        DateTime now = new DateTime(System.currentTimeMillis());
        String pattern = "HH:mm:ss";
        dateBaseAdapter.openDB();
        Cursor cursor = dateBaseAdapter.getData();
        cursor.moveToFirst();
        num_of_rows = dateBaseAdapter.numberOfRows();
        System.out.println(" ***************** num_of_rows &&&&&&&&&&& : "+num_of_rows);
        if(num_of_rows!=0) {
            if (cursor != null) {
                do {
                    event_id = cursor.getString(0).toString();
                    event_switch_state = cursor.getInt(5);
                    event_start_time = cursor.getLong(3);
                    event_end_time = cursor.getLong(4);
                    dateFormat = new SimpleDateFormat(pattern);
                    nowString = dateFormat.format(now.getValue());
                    startString = dateFormat.format(event_start_time);
                    endString = dateFormat.format(event_end_time);
                    nowone = null;
                    startone = null;
                    endone = null;
                    try {
                        nowone = dateFormat.parse(nowString);
                        startone = dateFormat.parse(startString);
                        endone = dateFormat.parse(endString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    System.out.println(" ***************** event switch state &&&&&&&&&&& ");

                    if (nowone.after(startone) && nowone.before(endone)) {

                        inTime = true;
                        if (event_switch_state == 1) {

                            System.out.println(" ***************** In DND Mode On ****************** ");

                            dnd_mode_on();
                        } else {
                            System.out.println(" ***************** In DND Mode Off ****************** ");
                            dnd_mode_off();
                        }
                    }


                } while (cursor.moveToNext());
                dateBaseAdapter.close();
                if (!inTime) {
                    System.out.println(" ***************** In inTime if block ****************** ");
                    dnd_mode_off();
                }
                inTime = false;

            }
        }

    }


    public static void dnd_mode_on(){

        //turn ringer silent
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            System.out.println(" ********** inside the marshmallow ***********");

            //turn off sound, disable notifications
            audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);

            //notifications
            audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);

            //alarm
            audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);

            //ringer
            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);

            //media
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
        }else{
            System.out.println(" ********** inside the lollipop on ***********");
            //turn off sound, disable notifications
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
            //notifications
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            //alarm
            audioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
            //ringer
            audioManager.setStreamMute(AudioManager.STREAM_RING, true);
            //media
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }

    }

    public static void dnd_mode_off(){
        //turn ringer to normal
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            System.out.println(" ********** inside the marshmallow off***********");
            // turn on sound, enable notifications
            audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);

            //notifications on
            audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);

            //alarm on
            audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);

            //ringer on
            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);

            //media on
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);

        }else{
            System.out.println(" ********** inside the lollipop off***********");
            // turn on sound, enable notifications
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
            //notifications on
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            //alarm on
            audioManager.setStreamMute(AudioManager.STREAM_ALARM, false);
            //ringer on
            audioManager.setStreamMute(AudioManager.STREAM_RING, false);
            //media on
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
    }

}





