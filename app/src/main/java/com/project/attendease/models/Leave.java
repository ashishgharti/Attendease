package com.project.attendease.models;


public class Leave {
    private String id;
    private String employee;
    private String startDate;
    private String endDate;
    private String leaveType;
    private String substitute;
    private String leaveDetails;
    private String status;
    private String createdAt;

    public Leave(String id, String employee, String startDate, String endDate, String leaveType, String substitute, String leaveDetails, String status, String createdAt) {

        this.id = id;
        this.employee = employee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = leaveType;
        this.substitute = substitute;
        this.leaveDetails = leaveDetails;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
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

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
