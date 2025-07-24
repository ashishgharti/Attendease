package com.project.attendease.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ncorti.slidetoact.SlideToActView;
import com.project.attendease.GeoFencingActivity;
import com.project.attendease.NotificationActivity;
import com.project.attendease.R;
import com.project.attendease.adapter.ActivityAdapter;
import com.project.attendease.apiservice.ApiService;
import com.project.attendease.apiservice.RetrofitClient;
import com.project.attendease.models.ActivityItem;
import com.project.attendease.response.AttendanceRecordResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String PREFS_NOTIFICATION_COUNT = "notification_count";
    private Handler handler;
    private Runnable timeUpdater;
    private SlideToActView slideToCheckInView;
    private SlideToActView slideToCheckOutView;
    private TextView userNameTextView;
    private TextView checkInTimeTextView;
    private TextView checkOutTimeTextView;
    private TextView currentTimeTextView;
    private TextView currentDateTextView;
    private RecyclerView recyclerView;
    private ActivityAdapter activityAdapter;
    private List<ActivityItem> activityItems;
    private String token;
    private String userId;
    private ImageView profileImage;
    private String baseUrl = "http://139.59.37.128:3000";
    private ProgressBar progressBar;
    private ImageView networkErrorImage;
    private TextView networkErrorText;
    private ApiService apiService;
    private BadgeDrawable badge;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ActivityResultLauncher<Intent> checkInActivityResultLauncher;
    private int notificationCount = 0;
    private TextView notificationBadge;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ImageView notification = view.findViewById(R.id.notification_icon);
        notificationBadge = view.findViewById(R.id.notification_badge);
        profileImage = view.findViewById(R.id.profile_image);
        userNameTextView = view.findViewById(R.id.user_name);
        checkInTimeTextView = view.findViewById(R.id.check_in_time);
        checkOutTimeTextView = view.findViewById(R.id.check_out_time);
        currentTimeTextView = view.findViewById(R.id.time);
        currentDateTextView = view.findViewById(R.id.date);
        slideToCheckInView = view.findViewById(R.id.slide_to_check_in);
        slideToCheckOutView = view.findViewById(R.id.slide_to_check_out);
        recyclerView = view.findViewById(R.id.activity_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        networkErrorImage = view.findViewById(R.id.network_error_image);
        networkErrorText = view.findViewById(R.id.network_error_text);

        // Get the BottomNavigationView from your Activity
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        badge = bottomNavigationView.getOrCreateBadge(R.id.home);

        // Initialize notification count from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        notificationCount = prefs.getInt(PREFS_NOTIFICATION_COUNT, 0);
        updateBadge();

        // Register the broadcast receiver for notifications
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(notificationReceiver, new IntentFilter("LeaveStatusUpdated"));
        IntentFilter filter = new IntentFilter("com.project.attendease.NEW_NOTIFICATION");
        getActivity().registerReceiver(notificationReceiver, filter);

        notification.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationActivity.class);
            startActivity(intent);

            // Reset notification count when the icon is clicked
            notificationCount = 0;
            updateBadge();

            // Store the updated count in SharedPreferences
            prefs.edit().putInt(PREFS_NOTIFICATION_COUNT, notificationCount).apply();
        });

        // Initialize the Handler and Runnable
        handler = new Handler();
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                updateTimeAndDate();
                handler.postDelayed(this, 1000); // Update every second
            }
        };

        // Start the time and date updater
        handler.post(timeUpdater);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::fetchAttendanceRecords);

        checkInActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        String status = result.getData().getStringExtra("status");
                        boolean isCheckOut = result.getData().getBooleanExtra("isCheckOut", false);

                        if ("leave".equals(status)) {
                            // If the user is on leave, do not record any check-in or check-out data
                            Toast.makeText(getActivity(), "You are on leave, no check-in recorded.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if ("success".equals(status) && !isCheckOut) {
                            slideToCheckInView.setVisibility(View.GONE);
                            slideToCheckOutView.setVisibility(View.VISIBLE);
                            checkOutTimeTextView.setText("--:-- --");
                            fetchAttendanceRecords();
                        } else if ("success".equals(status) && isCheckOut) {
                            slideToCheckInView.setVisibility(View.VISIBLE);
                            slideToCheckOutView.setVisibility(View.GONE);
                            fetchAttendanceRecords();
                        }
                    }
                }
        );

        slideToCheckInView.setOnSlideCompleteListener(slideToActView -> {
            Intent intent = new Intent(requireActivity(), GeoFencingActivity.class);
            intent.putExtra("isCheckOut", false);
            checkInActivityResultLauncher.launch(intent);
        });

        slideToCheckOutView.setOnSlideCompleteListener(slideToActView -> {
            Intent intent = new Intent(requireActivity(), GeoFencingActivity.class);
            intent.putExtra("isCheckOut", true);
            checkInActivityResultLauncher.launch(intent);
        });

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("name", null);
        token = sharedPreferences.getString("token", null);
        userId = sharedPreferences.getString("userId", null);

        if (userName != null) {
            userNameTextView.setText("Hello, " + userName);
        }

        activityItems = new ArrayList<>();
        activityAdapter = new ActivityAdapter(activityItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(activityAdapter);

        apiService = RetrofitClient.getApiService();
        fetchAttendanceRecords();
        restoreBadgeCount();
        loadProfileImage();

        return view;
    }
    private void restoreBadgeCount() {
        // Retrieve the unread notification count from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int unreadCount = prefs.getInt(PREFS_NOTIFICATION_COUNT, 0);

        // Set the badge count
        if (unreadCount > 0) {
            badge.setVisible(true);
            badge.setNumber(unreadCount);
        } else {
            badge.setVisible(false);
        }
    }
    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int receivedCount = intent.getIntExtra("notification_count", 0);

            if (notificationCount != receivedCount) {
                notificationCount = receivedCount;
                updateBadge();
            } else {
                Log.d(TAG, "Notification already processed, skipping.");
            }
        }
    };

    private void updateBadge() {
        if (notificationCount > 0) {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText(String.valueOf(notificationCount));
            badge.setVisible(true);
            badge.setNumber(notificationCount);
        } else {
            notificationBadge.setVisibility(View.GONE);
            badge.setVisible(false);
        }
    }

    private void loadProfileImage() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String profilePhotoPath = sharedPreferences.getString("profilePhotoPath", null);

        if (profilePhotoPath != null && !profilePhotoPath.isEmpty()) {
            String fullPath = baseUrl + profilePhotoPath + "?timestamp=" + System.currentTimeMillis();
            Log.d(TAG, "Loading image from URL: " + fullPath);
            Glide.with(this)
                    .load(fullPath)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.profile_pic)
                    .error(R.drawable.ic_network_error)
                    .into(profileImage);
        } else {
            Glide.with(this)
                    .load(R.drawable.profile_pic)
                    .into(profileImage);
        }
    }

    private void addLeaveActivity() {
        String leaveDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        activityItems.add(new ActivityItem("Leave", leaveDate, "", R.drawable.leaves));
        activityAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isCheckedIn = sharedPreferences.getBoolean("isCheckedIn", false);
        if (isCheckedIn) {
            slideToCheckInView.setVisibility(View.GONE);
            slideToCheckOutView.setVisibility(View.VISIBLE);
        } else {
            slideToCheckInView.setVisibility(View.VISIBLE);
            slideToCheckOutView.setVisibility(View.GONE);
        }

        slideToCheckInView.resetSlider();
        slideToCheckOutView.resetSlider();
        fetchAttendanceRecords();
        loadProfileImage();
    }

    private void fetchAttendanceRecords() {
        showLoading();

        apiService.getAttendanceRecords(token,"","","","").enqueue(new Callback<AttendanceRecordResponse>() {
            @Override
            public void onResponse(@NonNull Call<AttendanceRecordResponse> call, @NonNull Response<AttendanceRecordResponse> response) {
                hideLoading();
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    hideNetworkError();
                    AttendanceRecordResponse attendanceRecordResponse = response.body();
                    activityItems.clear();

                    for (AttendanceRecordResponse.Record record : attendanceRecordResponse.getAttendanceRecords()) {
                        if (record.getCheckInTime() != null) {
                            activityItems.add(new ActivityItem(
                                    "Check In",
                                    record.getDate(),
                                    record.getCheckInTime(),
                                    R.drawable.ic_check_in
                            ));
                        }

                        if (record.getCheckOutTime() != null) {
                            activityItems.add(new ActivityItem(
                                    "Check Out",
                                    record.getCheckOutDate(),
                                    record.getCheckOutTime(),
                                    R.drawable.ic_check_out
                            ));
                        }
                    }

                    activityAdapter.notifyDataSetChanged();
                    updateLatestCheckInOutTimes();
                } else {
                    showNetworkError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AttendanceRecordResponse> call, @NonNull Throwable t) {
                hideLoading();
                swipeRefreshLayout.setRefreshing(false);
                showNetworkError();
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        networkErrorImage.setVisibility(View.GONE);
        networkErrorText.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showNetworkError() {
        networkErrorImage.setVisibility(View.VISIBLE);
        networkErrorText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideNetworkError() {
        networkErrorImage.setVisibility(View.GONE);
        networkErrorText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void updateLatestCheckInOutTimes() {
        String latestCheckInTime = null;
        String latestCheckOutTime = null;

        for (ActivityItem item : activityItems) {
            if ("Check In".equals(item.getStatus()) && latestCheckInTime == null) {
                latestCheckInTime = item.getTime();
            } else if ("Check Out".equals(item.getStatus()) && latestCheckOutTime == null) {
                latestCheckOutTime = item.getTime();
            }

            if (latestCheckInTime != null && latestCheckOutTime != null) {
                break;
            }
        }

        checkInTimeTextView.setText(latestCheckInTime != null ? latestCheckInTime : "--:-- --");
        checkOutTimeTextView.setText(latestCheckOutTime != null ? latestCheckOutTime : "--:-- --");
    }

    private void updateTimeAndDate() {
        String currentTime = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date());
        String currentDate = new java.text.SimpleDateFormat("EEE, MMM d, yyyy", java.util.Locale.getDefault()).format(new java.util.Date());

        currentTimeTextView.setText(currentTime);
        currentDateTextView.setText(currentDate);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(notificationReceiver);
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(notificationReceiver);
        if (handler != null) {
            handler.removeCallbacks(timeUpdater);
        }
    }
}
