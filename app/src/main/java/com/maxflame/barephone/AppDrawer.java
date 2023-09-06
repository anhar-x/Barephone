package com.maxflame.barephone;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static android.graphics.Color.pack;

public class AppDrawer extends AppCompatActivity {

    /*
     *installedAppsName contains the name of the app
     * installedAppsPackage contains the package name of the app
     * only installedAppsName is shown to the user
     */
    ArrayAdapter arrayAdapter;
    ArrayList<App> installedApps = new ArrayList<>();

    ListView listView;

    //searchMenu is only used to change it's color
    MenuItem searchMenu;
    SearchView searchView;

    public void findAndAddAppsToList(){
        PackageManager pm = getPackageManager();

        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(pm)); //sorts the apps List

        for(ApplicationInfo app : apps) {
            String label = (String) pm.getApplicationLabel(app);

            if((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && getApplicationContext().getPackageManager().getLaunchIntentForPackage(app.packageName) != null ) {
                installedApps.add(new App(label, app.packageName));
            }else if((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 ||  getApplicationContext().getPackageManager().getLaunchIntentForPackage(app.packageName) != null){
                installedApps.add(new App(label, app.packageName));
            }
        }

        arrayAdapter.notifyDataSetChanged();
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
        setContentView(R.layout.activity_app_drawer);

        listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, installedApps);
        listView.setAdapter(arrayAdapter);

        findAndAddAppsToList();

        try{
            SecondActivity.secondActivityDialog.hide();
            MainActivity.mainActivityDialog.hide();
        }catch(Exception e){
            e.printStackTrace();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Hide the keyboard when an app is selected
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                App selectedApp = (App) arrayAdapter.getItem(position);

                Toast.makeText(getApplicationContext(), selectedApp.name + " selected!", Toast.LENGTH_SHORT).show();

                if(MainActivity.isMain){
                    MainActivity.sharedPreferences.edit().putString("packageName" + MainActivity.tagValue, selectedApp.packageName).apply();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }else{
                    //for checking if there is something saved in SharedPreferences, for retrieving, only used in SecondActivity
                    SecondActivity.sharedPreferences2.edit().putString("initialized", "initialized").apply();

                    Gson gson = new Gson();
                    String appJson = gson.toJson(selectedApp);

                    SecondActivity.sharedPreferences2.edit().putString("listData" + Integer.toString(SecondActivity.thePosition), appJson).apply();

                    Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
                    startActivity(intent);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_drawer_menu, menu);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    public void onBackPressed() {
        //TO TURN DOWN THE KEYBOARD
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        }

        Intent intent;
        if(MainActivity.isMain){
            intent = new Intent(getApplicationContext(), MainActivity.class);
        }else{
            intent = new Intent(getApplicationContext(), SecondActivity.class);
        }
        startActivity(intent);
    }

}


