package com.project.attendease;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class FullImageActivity extends AppCompatActivity {

    private ImageView fullImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        fullImageView = findViewById(R.id.full_image_view);

        // Get the image URL from the intent
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");

        // Load the image using Glide
        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                .skipMemoryCache(true) // Skip memory cache
                .into(fullImageView);

    }
}
