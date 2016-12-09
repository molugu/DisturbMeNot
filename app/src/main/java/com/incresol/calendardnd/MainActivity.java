package com.incresol.calendardnd;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.multidex.MultiDex;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, NavigationView.OnNavigationItemSelectedListener {
    GoogleAccountCredential mCredential;
    public TextView mOutputText,textView_navHeader;
    ProgressDialog mProgress;
    boolean doubleBackToExitPressedOnce = false;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Choose an account to start with";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    private RecyclerView mRecyclerView;
    public RecyclerView.Adapter mRecyclerViewAdapter;
    private RecyclerView.LayoutManager mRecyclerViewLayoutManager;
    ArrayList<EventDetails> mRecyclerViewDataSet;

    private DatabaseHelper mydb;
    private DateBaseAdapter dateBaseAdapter;

    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dateBaseAdapter=new DateBaseAdapter(this);
        mOutputText=(TextView) findViewById(R.id.mOutputText);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText(BUTTON_TEXT);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");
        mRecyclerView=(RecyclerView)findViewById(R.id.m_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerViewLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mRecyclerViewLayoutManager);
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        mRecyclerViewDataSet=new ArrayList<>();
        mRecyclerViewAdapter=new RecyclerViewAdapter(mRecyclerViewDataSet,getApplicationContext());

        // Navigation Drawer Code
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);
        textView_navHeader=(TextView)header.findViewById(R.id.textView_navHeader);
        getResultsFromApi();
        textView_navHeader.setText(mCredential.getSelectedAccountName());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.mainmenu_refresh:
                mOutputText.setText("");
                getResultsFromApi();
                mRecyclerView.setAdapter(mRecyclerViewAdapter);
                textView_navHeader.setText(mCredential.getSelectedAccountName());
                break;
        }
        return true;
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {

            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                            dateBaseAdapter.openDB();
                            dateBaseAdapter.delete_calendarEvents_table();
                            dateBaseAdapter.close();
                        textView_navHeader.setText(mCredential.getSelectedAccountName());
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.nav_chooseAccount){
            SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(PREF_ACCOUNT_NAME, null);
            editor.apply();

            mCredential.setSelectedAccountName(null);
            getResultsFromApi();

        }


        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        boolean event_exist=false;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Disturb Me Not")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {

            //OPEN
            dateBaseAdapter.openDB();
            List<String> eventStrings = new ArrayList<String>();
            int num_of_rows = dateBaseAdapter.numberOfRows();
            Boolean firstTime = false;
            if(num_of_rows==0) {
                    firstTime=true;
            }else{
                firstTime=false;
            }

                DateTime now = new DateTime(System.currentTimeMillis());

                Events events = mService.events().list("primary")
                        .setMaxResults(25)
                        .setTimeMin(now)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                List<Event> items = events.getItems();
            System.out.println("************* Event live items SIZE : ==============> : "+items.size());
                if (items.size() != 0) {
                    event_exist = true;

                    //checking the database for already existing events.

                    for (int i = 0; i < items.size(); i++) {
                        Event event = items.get(i);
                        String event_id = event.getId();
                        DateTime start = event.getStart().getDateTime();
                        DateTime end = event.getEnd().getDateTime();
                        if (start == null) {
                            // All-day events don't have start times, so just use the start date.
                            start = event.getStart().getDate();
                        }

                        System.out.println("************************ Start Time : " +start.getValue());
                        if(firstTime){
                            Integer switch_state = 1;
                            //INSERT
                            dateBaseAdapter.insertIntoDB(event_id, event.getSummary(), event.getDescription(), start.getValue(), end.getValue(), switch_state, event.getLocation());
                            Intent intent =new Intent(getApplicationContext(), BackgroundService.class);
                            getApplicationContext().startService(intent);
                        }else {
                            //comparing with already existing data in database.
                            Cursor cursor_eventIds = dateBaseAdapter.getEventIDs();
                            cursor_eventIds.moveToFirst();
                            ArrayList<String> DatabaseIdsArrayList_Id = new ArrayList<>();

                            List<Event> allEventsArrayList_Id = new ArrayList<>();
                            allEventsArrayList_Id=events.getItems();
                            ArrayList<String> compare_ids_list = new ArrayList<>();
                            for (int k = 0; k < allEventsArrayList_Id.size(); k++) {
                                Event event12 = items.get(k);
                                String eventids= event12.getId();
                                compare_ids_list.add(eventids);
                            }

                            do {
                                DatabaseIdsArrayList_Id.add(cursor_eventIds.getString(0).toString());
                                System.out.println("************************ stringArrayList_Id size : " + DatabaseIdsArrayList_Id.size());
                            } while (cursor_eventIds.moveToNext());


                           for(int j=0;j<DatabaseIdsArrayList_Id.size();j++){
                                if(!compare_ids_list.contains(DatabaseIdsArrayList_Id.get(j))){
                                    System.out.println("In delete block ===========>>> ");
                                    dateBaseAdapter.delete_event(DatabaseIdsArrayList_Id.get(j));
                                }
                            }


                            if (DatabaseIdsArrayList_Id.size() != 0) {
                                if(DatabaseIdsArrayList_Id.contains(event_id)){
                                    dateBaseAdapter.update_event(event_id, event.getSummary(), event.getDescription(), start.getValue(), end.getValue(), event.getLocation());
                                }else{
                                    Integer switch_state = 1;
                                    //INSERT
                                    dateBaseAdapter.insertIntoDB(event_id, event.getSummary(), event.getDescription(), start.getValue(), end.getValue(), switch_state, event.getLocation());
                                }
                            }

                        }

                    }

                    //checking ends here

                    //CLOSING Database
                    dateBaseAdapter.close();
                }

            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mRecyclerViewDataSet.clear();
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.dismiss();
            //mProgress.hide();
            if(event_exist) {
                dateBaseAdapter.openDB();
                Cursor cursor = dateBaseAdapter.getData();
                cursor.moveToFirst();
                do {
                    EventDetails eventDetails = new EventDetails();
                    eventDetails.setEVENT_ID(cursor.getString(0));
                    eventDetails.setEVENT_SUMMARY(cursor.getString(1));
                    eventDetails.setEVENT_DESCRIPTION(cursor.getString(2));
                    eventDetails.setSTART_TIME(cursor.getLong(3));
                    eventDetails.setEND_TIME(cursor.getLong(4));
                    eventDetails.setSWITCH_STATE(cursor.getInt(5));
                    eventDetails.setLOCATION(cursor.getString(6));
                    mRecyclerViewDataSet.add(eventDetails);
                    mRecyclerViewAdapter.notifyDataSetChanged();
                } while (cursor.moveToNext());
                dateBaseAdapter.close();
                mRecyclerView.setAdapter(mRecyclerViewAdapter);
                mOutputText.setText("The Retrieved Events");
            }else{
                mRecyclerViewDataSet.clear();
                mRecyclerViewAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(),"No Events Available",Toast.LENGTH_SHORT).show();
                mOutputText.setText("No Events Available");
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n" + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }




    @Override
    public void onBackPressed() {
        if (drawer.isDrawerVisible(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } /*else {
            super.onBackPressed();
        }*/

        if (doubleBackToExitPressedOnce) {
            System.out.println("if double back ==============>>>"+doubleBackToExitPressedOnce);
            super.onBackPressed();
            return;
        } else {
            System.out.println("else double back ==============>>>"+doubleBackToExitPressedOnce);
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);

        }


    }

}