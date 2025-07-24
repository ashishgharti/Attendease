package com.project.attendease.apiservice;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.project.attendease.models.Notification;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AppNotificationManager {

    private static final String PREFS_NAME = "notification_prefs";
    private static final String NOTIFICATION_KEY = "notifications";
    private static AppNotificationManager instance;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    private AppNotificationManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized AppNotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppNotificationManager(context);
        }
        return instance;
    }

    public void addNotification(Notification notification) {
        List<Notification> notifications = getNotifications();
        if (notifications.size() >= 15) {
            notifications.remove(0);
        }
        notifications.add(notification);
        saveNotifications(notifications);
    }

    public List<Notification> getNotifications() {
        String json = sharedPreferences.getString(NOTIFICATION_KEY, null);
        Type type = new TypeToken<List<Notification>>() {}.getType();
        return json == null ? new ArrayList<>() : gson.fromJson(json, type);
    }

    public void saveNotifications(List<Notification> notifications) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(notifications);
        editor.putString(NOTIFICATION_KEY, json);
        editor.apply();
    }


    public void clearNotifications() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(NOTIFICATION_KEY);
        editor.apply();
    }

    public void removeNotification(Notification notification) {
        List<Notification> notifications = getNotifications();
        notifications.remove(notification);
        saveNotifications(notifications);
    }
}
