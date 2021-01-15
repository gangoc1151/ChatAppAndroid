package com.example.assignment2_work;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import io.grpc.LoadBalancer;

public class ImageViewActivity extends AppCompatActivity {
    ImageView imageView;
    String Url;

    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /**Display the image from chat message */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        imageView = findViewById(R.id.image_view);
        Url = getIntent().getStringExtra("url");
        toolbar = findViewById(R.id.toolbar);


        Picasso.get().load(Url).into(imageView);

    }
}