package com.project.attendease.request;


public class LeaveRequest {
    private String startDate;
    private String endDate;
    private String leaveTitle;
    private String substitute;
    private String leaveDetails;

    public LeaveRequest(String startDate, String endDate, String leaveTitle, String substitute, String leaveDetails) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveTitle = leaveTitle;
        this.substitute = substitute;
        this.leaveDetails = leaveDetails;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getLeaveTitle() {
        return leaveTitle;
    }

    public void setLeaveTitle(String leaveTitle) {
        this.leaveTitle = leaveTitle;
    }

    public String getSubstitute() {
        return substitute;
    }

    public void setSubstitute(String substitute) {
        this.substitute = substitute;
    }

    public String getLeaveDetails() {
        return leaveDetails;
    }

    public void setLeaveDetails(String leaveDetails) {
        this.leaveDetails = leaveDetails;
    }
}
