package com.project.attendease.response;

import com.project.attendease.models.Leave;


public class LeaveRequestResponse {
    private String message;
    private Leave leaveRequest; // Use the Leave model class

    public LeaveRequestResponse(String message, Leave leaveRequest) {
        this.message = message;
        this.leaveRequest = leaveRequest;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Leave getLeaveRequest() {
        return leaveRequest;
    }

    public void setLeaveRequest(Leave leaveRequest) {
        this.leaveRequest = leaveRequest;
    }
}
