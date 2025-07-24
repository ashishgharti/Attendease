package com.project.attendease.response;

import com.project.attendease.models.Leave;

import java.util.List;


public class LeaveRequestListResponse {
    private List<Leave> leaveRequests;

    public LeaveRequestListResponse(List<Leave> leaveRequests) {
        this.leaveRequests = leaveRequests;
    }

    public List<Leave> getLeaveRequests() {
        return leaveRequests;
    }

    public void setLeaveRequests(List<Leave> leaveRequests) {
        this.leaveRequests = leaveRequests;
    }
}
