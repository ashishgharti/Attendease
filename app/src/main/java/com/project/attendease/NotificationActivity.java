package com.project.attendease;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.attendease.adapter.NotificationAdapter;
import com.project.attendease.apiservice.AppNotificationManager;
import com.project.attendease.models.Notification;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";
    private static final String CHANNEL_ID = "leave_status_channel";
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private Socket socket;

    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.notifications_recycler_view);
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.notifications_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch the list of notifications from the manager
        List<Notification> notifications = AppNotificationManager.getInstance(this).getNotifications();

        adapter = new NotificationAdapter(notifications, this);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                adapter.removeNotification(position);
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Load notifications
        loadNotifications();

        // Reset unread notification count
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("unread_notifications", 0).apply();

        // Broadcast to update the badge count
        Intent intent = new Intent("com.project.attendease.NEW_NOTIFICATION");
        intent.putExtra("notification_count", 0);
        sendBroadcast(intent);

        // Initialize Socket.IO
        try {
            socket = IO.socket("http://139.59.37.128:3000"); // Replace with your server URL
            socket.connect();
            socket.on(Socket.EVENT_CONNECT, args -> Log.d(TAG, "Connected to server"));
            socket.on(Socket.EVENT_DISCONNECT, args -> Log.d(TAG, "Disconnected from server"));

            // Listen for real-time notifications
            socket.on("leave-status-updated", onLeaveStatusUpdated);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket connection error: " + e.getMessage(), e);
        }

        // Create the notification channel (required for Android 8.0 and above)
        createNotificationChannel();

        // Set up swipe-to-delete functionality
        setupSwipeToDelete();
    }

    private void loadNotifications() {
        notificationList = AppNotificationManager.getInstance(this).getNotifications();
        adapter = new NotificationAdapter(notificationList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false; // We are not moving items, just swiping them
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition(); // Get the swiped item position

                // Remove the notification from the list
                Notification removedNotification = notificationList.remove(position);

                // Notify the adapter about the removed item
                adapter.notifyItemRemoved(position);

                // Optionally remove the notification from cache or storage
                AppNotificationManager.getInstance(NotificationActivity.this).removeNotification(removedNotification);

                // Show a message
                Toast.makeText(NotificationActivity.this, "Notification dismissed", Toast.LENGTH_SHORT).show();
            }
        };

        // Attach the ItemTouchHelper to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private final Emitter.Listener onLeaveStatusUpdated = args -> runOnUiThread(() -> {
        Log.d(TAG, "Received leave-status-updated event");
        JSONObject data = (JSONObject) args[0];

        try {
            String status = data.getString("status");
            JSONObject leaveTypeObject = data.getJSONObject("leaveType");
            String leaveTypename = leaveTypeObject.getString("leaveTypename");
            String time;

            if (status.equals("Approved")) {
                time = data.getString("approvedAt");
            } else {
                time = data.getString("rejectedAt");
            }

            String title = "Leave Request " + status;
            String text = "Your " + leaveTypename + " leave request has been " + status.toLowerCase() + ".";

            Log.d(TAG, "Notification details - Title: " + title + ", Text: " + text + ", Time: " + time);

            // Add the notification to UI
            addNotificationToUI(title, text, time);

            // Show system notification
            showSystemNotification(title, text, time);

        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
        }
    });

    private void addNotificationToUI(String title, String text, String time) {
        Notification notification = new Notification(title, text, time);
        notificationList.add(notification);
        adapter.notifyItemInserted(notificationList.size() - 1);
        AppNotificationManager.getInstance(this).addNotification(notification);

        // Play sound
        playNotificationSound();

        // Trigger vibration
        triggerVibration();
    }

    private void showSystemNotification(String title, String message, String time) {
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_color)
                .setContentTitle(title)
                .setContentText(message)
                .setSubText(time)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Leave Status Notifications";
            String description = "Channel for leave status notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void playNotificationSound() {
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.notification_sound);
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "Error playing notification sound: " + e.getMessage(), e);
        }
    }

    private void triggerVibration() {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error triggering vibration: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.off("leave-status-updated", onLeaveStatusUpdated);
            socket.disconnect();
        }
    }
}
