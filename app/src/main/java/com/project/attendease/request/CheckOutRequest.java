package com.project.attendease.request;

public class CheckOutRequest {
    private double latitude;
    private double longitude;
    private String token;

    public CheckOutRequest(double latitude, double longitude, String token) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.token = token;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
