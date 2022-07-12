package com.arpit_kyada.chat_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.arpit_kyada.chat_app.utility.Constants;
import com.arpit_kyada.chat_app.utility.PreferenceManager;

public class SplashScreenActivity extends AppCompatActivity {

    PreferenceManager pm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        pm = new PreferenceManager(getApplicationContext());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(pm.getBoolean(Constants.KEY_IS_SIGNED_IN))
                {
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }
                else {
                    startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                    finish();
                }

            }
        },0);


    }
}