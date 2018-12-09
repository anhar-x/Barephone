package com.example.anhar.barephone;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class SecondActivity extends AppCompatActivity {

    public static ArrayList<String> listOfApps = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    ListView listView;

    //if the clicked app equal "Select an app", the indexOfThis is added to realClickedApp
    String clickedApp;
    //this is the index of the clickedApp
    public static int realClickedApp = -1;

    //this is the id of newly added textView, this is increased!
    int numberOfAppsAdded = -1;
    //this is the position of the app that was clicked. This is used in sharedPreference!
    public static int thePosition;
    int sizeOfSavedList = 0;

    //this is only used for the secondActivity.
    public static SharedPreferences sharedPreferences2;

    //loading dialog
    public static ProgressDialog secondActivityDialog;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        try{
            if(MainActivity.sharedPreferences.getBoolean("light", false)){
                changeMenuItemColor(0, Color.BLACK, menu);
                changeMenuItemColor(1, Color.BLACK, menu);
            }else if(!MainActivity.sharedPreferences.getBoolean("light", false)){
                changeMenuItemColor(0, WHITE, menu);
                changeMenuItemColor(1, WHITE, menu);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return true;
    }

    public void changeMenuItemColor(int i, int color, Menu menu){
        MenuItem item = menu.getItem(i);
        SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(color), 0,     spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //ADDS A NEW TEXT VIEW
        numberOfAppsAdded++;
        if(item.getItemId() == R.id.addNewApp){
            TextView textView = new TextView(SecondActivity.this);
            textView.setId(numberOfAppsAdded);
            textView.setText("Select an app");

            listOfApps.add(textView.getText().toString());
            arrayAdapter.notifyDataSetChanged();

            realClickedApp = listOfApps.size();
            thePosition = listOfApps.size() -1;

            secondActivityDialog =new ProgressDialog(SecondActivity.this);
            secondActivityDialog.setMessage("Loading...");
            secondActivityDialog.setCancelable(false);
            secondActivityDialog.setInverseBackgroundForced(false);
            secondActivityDialog.show();

            Intent intent = new Intent(getApplicationContext(), AppDrawer.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.settings){
            Intent intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            if(MainActivity.sharedPreferences.getBoolean("light", false)){
                setTheme(R.style.AppTheme);
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(WHITE));
            }else if(!MainActivity.sharedPreferences.getBoolean("light", false)){
                setTheme(R.style.DarkTheme);
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(BLACK));

            }
        }catch(Exception e){
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        ImageView upArrow = findViewById(R.id.mainActivityButton);

        sharedPreferences2 =getSharedPreferences("shared2", MODE_PRIVATE);

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfApps);

        listView = findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);

        //if listView is null, this text will be shown
        TextView noListTextView = findViewById(R.id.noList);
        listView.setEmptyView(noListTextView);


        try{
            if(MainActivity.sharedPreferences.getBoolean("light", false)){
                upArrow.setImageResource(R.drawable.arrow_up_black);
                noListTextView.setTextColor(Color.BLACK);

            }else if(!MainActivity.sharedPreferences.getBoolean("light", false)){
                upArrow.setImageResource(R.drawable.arrow_up_white);
                noListTextView.setTextColor(Color.WHITE);
            }

        }catch (Exception e){
            e.printStackTrace();
        }


        if(realClickedApp != -1){
            try{
                String appName = sharedPreferences2.getString("selectedItem" + thePosition, "Select an app");
                listOfApps.set(thePosition, appName); //sets appName to the position of realClickedApp
                arrayAdapter.notifyDataSetChanged();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                clickedApp = listView.getItemAtPosition(i).toString();

                if(clickedApp.equals("Select an app")){
                    thePosition = i;
                    realClickedApp = listOfApps.indexOf(clickedApp) ;

                    Intent appDrawerIntent = new Intent(getApplicationContext(), AppDrawer.class);
                    startActivity(appDrawerIntent);
                }else{
                    String packageName = sharedPreferences2.getString("packageName" + thePosition, "");

                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                    startActivity(launchIntent);
                }
            }

        });

        //ALERT DIALOG FOR REMOVING APP ON LONG CLICK
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(SecondActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are you sure?")
                        .setMessage("Do you want to remove this?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                sharedPreferences2.edit().remove("packageName" + position).apply();
                                sharedPreferences2.edit().remove("listData" + position).apply();
                                listOfApps.remove(position);
                                arrayAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
        });

        while(listOfApps.size() > sizeOfSavedList){
            saveListToDatabase();
            sizeOfSavedList++;
        }

        if(sharedPreferences2.contains("initialized") && listOfApps.size() == 0){
            try{
                retrieveListData();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void saveListToDatabase() {
        for (int i = 0; i < listOfApps.size(); i++) {
            sharedPreferences2.edit().putString("listData" + i, listOfApps.get(i)).apply();
        }
    }

    public void retrieveListData(){
        for(int i = 0; sharedPreferences2.contains("listData" + i) ; i++){
            listOfApps.add(i, sharedPreferences2.getString("listData" + i, "it's not working"));
            arrayAdapter.notifyDataSetChanged();
        }
    }

    public void openMainActivity(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }


}
