package com.murach.tipcalculator;

import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class Settings extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content , new SettingsFragment()).commit();
    }
}