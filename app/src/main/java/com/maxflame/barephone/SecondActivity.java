package com.maxflame.barephone;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class SecondActivity extends AppCompatActivity {

    public static ArrayList<App> listOfApps = new ArrayList<>();

    ArrayAdapter arrayAdapter;
    ListView listView;

    App clickedApp;

    //this is the position of the app that was clicked. This is used in sharedPreference!
    public static int thePosition;
    int sizeOfSavedList = 0;

    //this is only used for the secondActivity.
    public static SharedPreferences sharedPreferences2;

    //loading dialog
    public static ProgressDialog secondActivityDialog;

    //searchMenu is only used to change it's color
    MenuItem searchMenu;
    SearchView searchView;

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



        getMenuInflater().inflate(R.menu.second_activity_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();

        searchMenu = menu.findItem(R.id.app_bar_search);
        if(MainActivity.sharedPreferences.getBoolean("light", false)){
            searchMenu.setIcon(R.drawable.search_black);
        }else{
            searchMenu.setIcon(R.drawable.search_white);
        }

        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                arrayAdapter.getFilter().filter(newText);
                return true;
            }
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
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
        if(item.getItemId() == R.id.addNewApp){
            thePosition = listOfApps.size();

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

        //TO SHOW KEYBOARD IF THE USER CLICKS ON THE SEARCH ICON
        if(item.getItemId() == R.id.app_bar_search){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(searchView, 0);
                searchView.setIconified(false);
                searchView.requestFocus();

            }
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

        sharedPreferences2 = getSharedPreferences("shared2", MODE_PRIVATE);

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


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                clickedApp = (App) listView.getItemAtPosition(i);
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(clickedApp.packageName);
                startActivity(launchIntent);

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

        if(sharedPreferences2.contains("initialized")){
            try{
                retrieveListData();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void saveListToDatabase() {
        for (int i = 0; i < listOfApps.size(); i++) {
            Gson gson = new Gson();
            String appJson = gson.toJson(listOfApps.get(i));
            sharedPreferences2.edit().putString("listData" + i, appJson).apply();
        }
    }

    public void retrieveListData(){
        listOfApps.clear();

        for(int i = 0; sharedPreferences2.contains("listData" + i) ; i++){
            Gson gson = new Gson();
            String appJson = sharedPreferences2.getString("listData" + i, "");

            App app = gson.fromJson(appJson, App.class);
            listOfApps.add(i, app);
        }

        arrayAdapter.notifyDataSetChanged();
    }

    public void openMainActivity(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //TO TURN DOWN THE KEYBOARD
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        }

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }



}
