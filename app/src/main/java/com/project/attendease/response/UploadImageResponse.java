package com.project.attendease.response;

import com.google.gson.annotations.SerializedName;

public class UploadImageResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("photoPath")
    private String photoPath;

    public UploadImageResponse(String message, String photoPath) {
        this.message = message;
        this.photoPath = photoPath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
