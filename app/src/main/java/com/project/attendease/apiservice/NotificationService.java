package com.project.attendease.apiservice;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.project.attendease.NotificationActivity;
import com.project.attendease.R;
import com.project.attendease.models.Notification;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class NotificationService extends Service {

    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "leave_status_channel";
    private Socket socket;

    @Override
    public void onCreate() {
        super.onCreate();

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
    }

    private final Emitter.Listener onLeaveStatusUpdated = args -> {
        Log.d(TAG, "Received leave-status-updated event");
        JSONObject data = (JSONObject) args[0];

        try {
            String status = data.getString("status");
            JSONObject leaveTypeObject = data.getJSONObject("leaveType");
            String leaveTypename = leaveTypeObject.getString("leaveTypename");
            String time = status.equals("Approved") ? data.getString("approvedAt") : data.getString("rejectedAt");

            String title = "Leave Request " + status;
            String text = "Your " + leaveTypename + " leave request has been " + status.toLowerCase() + ".";

            // Add the notification to the manager
            Notification notification = new Notification(title, text, time);
            AppNotificationManager.getInstance(getApplicationContext()).addNotification(notification);

            // Increment the unread notification count in SharedPreferences and broadcast
            incrementAndBroadcastNotificationCount();
            showSystemNotification(title, text, time);

            // Trigger vibration and play sound
            triggerVibration();
            playNotificationSound();
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
        }
    };

    private void incrementAndBroadcastNotificationCount() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int unreadCount = prefs.getInt("unread_notifications", 0);
        unreadCount++;
        prefs.edit().putInt("unread_notifications", unreadCount).apply();

        // Update the badge icon
        updateBadge(unreadCount);

        // Broadcast the updated count to other components
        broadcastNewNotification(unreadCount);
    }

    private void updateBadge(int count) {
        Intent intent = new Intent("UpdateBadgeIcon");
        intent.putExtra("badge_count", count);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastNewNotification(int notificationCount) {
        Intent intent = new Intent("LeaveStatusUpdated");
        intent.putExtra("notification_count", notificationCount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent); // Use LocalBroadcastManager
    }

    private void showSystemNotification(String title, String message, String time) {
        // Create an explicit intent for an Activity in your app
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.off("leave-status-updated", onLeaveStatusUpdated);
            socket.disconnect();
        }
    }
}
