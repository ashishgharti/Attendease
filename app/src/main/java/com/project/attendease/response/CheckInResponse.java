package com.project.attendease.response;

import com.google.gson.annotations.SerializedName;

public class CheckInResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("attendance")
    private Attendance attendance;

    public CheckInResponse(String message, Attendance attendance) {
        this.message = message;
        this.attendance = attendance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public static class Attendance {

        @SerializedName("employeeId")
        private String employeeId;

        @SerializedName("checkInDate")
        private String checkInDate;

        @SerializedName("checkInTime")
        private String checkInTime;

        @SerializedName("location")
        private Location location;

        @SerializedName("isCheckedOut")
        private boolean isCheckedOut;

        @SerializedName("earlyMinutes")
        private String earlyMinutes;

        @SerializedName("lateMinutes")
        private String lateMinutes;

        @SerializedName("checkInStatus")
        private String checkInStatus;

        @SerializedName("_id")
        private String id;

        @SerializedName("__v")
        private int version;

        public Attendance(String employeeId, String checkInDate, String checkInTime, Location location, boolean isCheckedOut, String earlyMinutes, String lateMinutes, String checkInStatus, String id, int version) {
            this.employeeId = employeeId;
            this.checkInDate = checkInDate;
            this.checkInTime = checkInTime;
            this.location = location;
            this.isCheckedOut = isCheckedOut;
            this.earlyMinutes = earlyMinutes;
            this.lateMinutes = lateMinutes;
            this.checkInStatus = checkInStatus;
            this.id = id;
            this.version = version;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getCheckInDate() {
            return checkInDate;
        }

        public void setCheckInDate(String checkInDate) {
            this.checkInDate = checkInDate;
        }

        public String getCheckInTime() {
            return checkInTime;
        }

        public void setCheckInTime(String checkInTime) {
            this.checkInTime = checkInTime;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public boolean isCheckedOut() {
            return isCheckedOut;
        }

        public void setCheckedOut(boolean checkedOut) {
            isCheckedOut = checkedOut;
        }

        public String getEarlyMinutes() {
            return earlyMinutes;
        }

        public void setEarlyMinutes(String earlyMinutes) {
            this.earlyMinutes = earlyMinutes;
        }

        public String getLateMinutes() {
            return lateMinutes;
        }

        public void setLateMinutes(String lateMinutes) {
            this.lateMinutes = lateMinutes;
        }

        public String getCheckInStatus() {
            return checkInStatus;
        }

        public void setCheckInStatus(String checkInStatus) {
            this.checkInStatus = checkInStatus;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public static class Location {

            @SerializedName("type")
            private String type;

            @SerializedName("coordinates")
            private double[] coordinates;

            public Location(String type, double[] coordinates) {
                this.type = type;
                this.coordinates = coordinates;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public double[] getCoordinates() {
                return coordinates;
            }

            public void setCoordinates(double[] coordinates) {
                this.coordinates = coordinates;
            }
        }

    }
}
