package com.example.assignment2_work;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashView extends AppCompatActivity {

    private static int SPASH_TIME_OUT = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_view);
    /**Plash View at first when user open the application */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent(SplashView.this, MainActivity.class);
                startActivity(homeIntent);
                finish();
            }
        },SPASH_TIME_OUT);
    }
}