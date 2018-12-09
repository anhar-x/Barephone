package com.example.anhar.barephone;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    TextView timeTextView;
    TextView dateTextView;
    TextView nextAlarmTextView;

    ImageView nextAlarmImageView;
    ImageView app1;
    ImageView app2;
    ImageView app3;
    ImageView app4;
    ImageView app5;

    public static ArrayList<ImageView> apps = new ArrayList<>();
    public static SharedPreferences sharedPreferences;
    //the tag of the view
    public static Object tagValue;
    // to check whether the intent came from MainActivity or Second Activity. If MainActivity then true else false. Check AppDrawer for more
    public static boolean isMain;

    //Loading Dialog
    public static ProgressDialog mainActivityDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("shared1", MODE_PRIVATE);
        try{
            if(sharedPreferences.getBoolean("light", false)){
                setTheme(R.style.AppTheme);
                getSupportActionBar().hide();
            }else if(!sharedPreferences.getBoolean("light", false)){
                setTheme(R.style.DarkTheme);
                getSupportActionBar().hide();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isMain = false;

        DateTimeUI();

        ImageView downArrow = findViewById(R.id.secondActivityButton);

        try{
            if(sharedPreferences.getBoolean("light", false)){
                timeTextView.setTextColor(Color.BLACK);
                dateTextView.setTextColor(Color.BLACK);
                nextAlarmTextView.setTextColor(Color.BLACK);
                nextAlarmImageView.setImageResource(R.drawable.alarm_black);
                downArrow.setImageResource(R.drawable.arrow_down_black);
            }else if(!sharedPreferences.getBoolean("light", false)){
                timeTextView.setTextColor(Color.WHITE);
                dateTextView.setTextColor(Color.WHITE);
                nextAlarmTextView.setTextColor(Color.WHITE);
                nextAlarmImageView.setImageResource(R.drawable.alarm_white);
                downArrow.setImageResource(R.drawable.arrow_down_white);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        app1 = findViewById(R.id.app1);
        app2 = findViewById(R.id.app2);
        app3 = findViewById(R.id.app3);
        app4 = findViewById(R.id.app4);
        app5 = findViewById(R.id.app5);

        apps.add(app1);
        apps.add(app2);
        apps.add(app3);
        apps.add(app4);
        apps.add(app5);

        for(int i = 0; i < apps.size(); i++){
            if(sharedPreferences.getString("packageName" + apps.get(i).getTag(), null) != null){
                try{
                    String thePreference = sharedPreferences.getString("packageName" + apps.get(i).getTag(), null);

                    //fetching icon of the app using packageName
                    Drawable icon = getPackageManager().getApplicationIcon(thePreference);
                    apps.get(i).setImageDrawable(icon);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }

            apps.get(i).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    /*
                    *packageName is stored in sharedPreferences, with the tag of the imageView
                    * if a packageName is null, onClick the AppDrawer open
                    * else, the app is opened by retrieving the data from sharedPreferences
                     */
                    if(sharedPreferences.getString("packageName" + v.getTag(), null) == null){
                        tagValue = v.getTag();
                        isMain = true;

                        mainActivityDialog =new ProgressDialog(MainActivity.this);
                        mainActivityDialog.setMessage("Loading...");
                        mainActivityDialog.setCancelable(false);
                        mainActivityDialog.setInverseBackgroundForced(false);
                        mainActivityDialog.show();
                        Intent intent = new Intent(getApplicationContext(), AppDrawer.class);
                        startActivity(intent);
                    }

                    if(sharedPreferences.getString("packageName" + v.getTag(), null) != null){
                        try{
                            String packageName = sharedPreferences.getString("packageName" + v.getTag(), null);

                            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                            startActivity(launchIntent);

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            });

            //FOR DELETING
            apps.get(i).setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(final View v) {
                    /* to make sure that the Dialog doesn't come when tried to delete the add_new_item */
                    if(sharedPreferences.getString("packageName" + v.getTag(), null) == null){
                        return false;
                    }else{
                        new AlertDialog.Builder(MainActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Are you sure?")
                                .setMessage("Do you want to remove this?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        sharedPreferences.edit().remove("packageName" + v.getTag()).apply();
                                        ImageView img = v.findViewWithTag(v.getTag());
                                        img.setImageResource(R.drawable.add_new_item);

                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                        return true;
                    }

                }
            });
        }
    }

    //if clicked on down arrow
    public void openSecondActivity(View view){
        Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
        startActivity(intent);
    }

    public void DateTimeUI(){
        timeTextView = findViewById(R.id.timeTextView);
        dateTextView = findViewById(R.id.dateTextView);
        nextAlarmTextView = findViewById(R.id.nextAlarmTextView);
        nextAlarmImageView = findViewById(R.id.nextAlarmImageView);

        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        //TO STOP THE DELAY
                        if(timeTextView.getText().equals("")){
                            Thread.sleep(1);
                        }else{
                            Thread.sleep(2000);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //gets time in this format
                                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");
                                //gets date in this format
                                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyy");

                                //gets the next alarm that is set
                                String nextAlarm = Settings.System.getString(getContentResolver(),Settings.System.NEXT_ALARM_FORMATTED);

                                nextAlarmTextView.setText(nextAlarm);

                                if(!nextAlarmTextView.getText().equals("")){
                                    nextAlarmImageView.setVisibility(View.VISIBLE);
                                }else{
                                    nextAlarmImageView.setVisibility(View.INVISIBLE);
                                }

                                Date date = Calendar.getInstance().getTime();
                                String currentTimeString = timeFormat.format(new Date());
                                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy"); // Set your date format
                                String currentData = sdf.format(date); // Get Date String according to date format

                                timeTextView.setText(currentTimeString);
                                dateTextView.setText(currentData);
                            }
                        });
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }; thread.start();

        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                startActivity(launchIntent);
            }
        });

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long startMillis = System.currentTimeMillis();
                Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                ContentUris.appendId(builder, startMillis);
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
                startActivity(intent);
            }
        });


    }

    @Override
    public void onBackPressed() {
    }


}
