package com.project.attendease.models;

public class Statistic {
    private String date;
    private String time;
    private String status;
    private int iconResId;
    private String type; // Either "Check In" or "Check Out"

    public Statistic(String date, String time, String status, int iconResId, String type) {
        this.date = date;
        this.time = time;
        this.status = status;
        this.iconResId = iconResId;
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
