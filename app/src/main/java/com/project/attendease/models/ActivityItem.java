package com.project.attendease.models;

public class ActivityItem {

    private String status;
    private String date;
    private String time;
    private int iconResId;

    public ActivityItem(String status, String date, String time, int iconResId) {
        this.status = status;
        this.date = date;
        this.time = time;
        this.iconResId = iconResId;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getIconResId() {
        return iconResId;
    }
}
