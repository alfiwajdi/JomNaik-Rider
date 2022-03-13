package com.app.jomnaikrider.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.app.jomnaikrider.R;


//Startup screen that shown at very first for few seconds
public class SplashActivity extends AppCompatActivity {
    public static final int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        //Run Background thread for 2 seconds to hold on first screen for 2 seconds.
        Thread obj = new Thread() {
            public void run() {
                try {
                    sleep(SPLASH_TIME_OUT);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        //After 28 seconds start loin screen using Intent
                        Intent intent = new Intent(getApplicationContext(), RiderLoginActivity.class);
                        startActivity(intent);
                        finish();

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        };obj.start();

    }
}