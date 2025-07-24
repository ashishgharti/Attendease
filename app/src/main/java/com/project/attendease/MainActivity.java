package com.project.attendease;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.attendease.apiservice.RetrofitClient;
import com.project.attendease.apiservice.NotificationService;
import com.project.attendease.databinding.ActivityMainBinding;
import com.project.attendease.fragment.HomeFragment;
import com.project.attendease.fragment.LeavesFragment;
import com.project.attendease.fragment.ProfileFragment;
import com.project.attendease.fragment.StatisticsFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private BadgeDrawable badge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RetrofitClient.initialize(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNavigationView = binding.bottomNavigation;
        badge = bottomNavigationView.getOrCreateBadge(R.id.home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.statistics) {
                selectedFragment = new StatisticsFragment();
            } else if (itemId == R.id.leaves) {
                selectedFragment = new LeavesFragment();
            } else if (itemId == R.id.profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_layout, selectedFragment);
                transaction.commit();
            }
            return true;
        });

        // Start the NotificationService
        Intent serviceIntent = new Intent(this, NotificationService.class);
        startService(serviceIntent);

        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.home); // Change to your default fragment id
        }

        // Register the receiver to update badge
        LocalBroadcastManager.getInstance(this).registerReceiver(badgeUpdateReceiver, new IntentFilter("UpdateBadgeIcon"));
    }

    private final BroadcastReceiver badgeUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int badgeCount = intent.getIntExtra("badge_count", 0);
            if (badgeCount > 0) {
                badge.setVisible(true);
                badge.setNumber(badgeCount);
            } else {
                badge.setVisible(false);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(badgeUpdateReceiver);
    }
}
