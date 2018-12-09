package com.example.anhar.barephone;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if(MainActivity.sharedPreferences.getBoolean("light", false)){
            setTheme(R.style.AppTheme);
            getSupportActionBar().hide();
        }else if(!MainActivity.sharedPreferences.getBoolean("light", false)){
            setTheme(R.style.DarkTheme);
            getSupportActionBar().hide();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch lightThemeSwitch = findViewById(R.id.lightThemeSwitch);
        TextView setDefaultTextView = findViewById(R.id.setAsDefault);
        ImageView goBackImageView = findViewById(R.id.goBackImageView);
        TextView toSettingsTextView = findViewById(R.id.toSettings);

        if(MainActivity.sharedPreferences.getBoolean("light", false)){
            lightThemeSwitch.setChecked(true);
            setDefaultTextView.setTextColor(Color.BLACK);
            lightThemeSwitch.setTextColor(Color.BLACK);
            toSettingsTextView.setTextColor(Color.BLACK);
            goBackImageView.setImageResource(R.drawable.arrow_left_black);
        }else{
            lightThemeSwitch.setChecked(false);
            setDefaultTextView.setTextColor(Color.WHITE);
            lightThemeSwitch.setTextColor(Color.WHITE);
            toSettingsTextView.setTextColor(Color.WHITE);
            goBackImageView.setImageResource(R.drawable.arrow_left_white);
        }

        lightThemeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity.sharedPreferences.edit().putBoolean("light", true).apply();
                    Intent intent = new Intent(getApplicationContext(), Settings.class);
                    startActivity(intent);
                }else{
                    MainActivity.sharedPreferences.edit().putBoolean("light", false).apply();
                    Intent intent = new Intent(getApplicationContext(), Settings.class);
                    startActivity(intent);


                }
            }
        });

    }

    public void goBack(View view){
        Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
        startActivity(intent);
    }

    public void setAsDefaultLauncher(View view){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            final Intent intent = new Intent(android.provider.Settings.ACTION_HOME_SETTINGS);
            startActivity(intent);
        }
        else {
            final Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
            startActivity(intent);
        }
    }

    public void toSettings(View view){
        startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);

    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
        startActivity(intent);
    }
}