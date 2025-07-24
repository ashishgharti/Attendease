package com.project.attendease.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AttendanceRecordResponse {

    @SerializedName("attendanceRecords")
    private List<Record> attendanceRecords;

    public AttendanceRecordResponse(List<Record> attendanceRecords) {
        this.attendanceRecords = attendanceRecords;
    }

    public List<Record> getAttendanceRecords() {
        return attendanceRecords;
    }

    public void setAttendanceRecords(List<Record> attendanceRecords) {
        this.attendanceRecords = attendanceRecords;
    }

    public static class Record {
        @SerializedName("employeeName")
        private String employeeName;

        @SerializedName("date")
        private String date;

        @SerializedName("checkInTime")
        private String checkInTime;

        @SerializedName("checkInStatus")
        private String checkInStatus;

        @SerializedName("checkOutDate")
        private String checkOutDate;

        @SerializedName("checkOutTime")
        private String checkOutTime;

        @SerializedName("checkOutStatus")
        private String checkOutStatus;

        @SerializedName("earlyMinutes")
        private String earlyMinutes;

        @SerializedName("lateMinutes")
        private String lateMinutes;

        @SerializedName("totalWorkedTime")
        private String totalWorkedTime;

        @SerializedName("location")
        private Location location;

        public Record(String employeeName, String date, String checkInTime, String checkInStatus, String checkOutDate, String checkOutTime, String checkOutStatus, String earlyMinutes, String lateMinutes, String totalWorkedTime, Location location) {
            this.employeeName = employeeName;
            this.date = date;
            this.checkInTime = checkInTime;
            this.checkInStatus = checkInStatus;
            this.checkOutDate = checkOutDate;
            this.checkOutTime = checkOutTime;
            this.checkOutStatus = checkOutStatus;
            this.earlyMinutes = earlyMinutes;
            this.lateMinutes = lateMinutes;
            this.totalWorkedTime = totalWorkedTime;
            this.location = location;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getCheckInTime() {
            return checkInTime;
        }

        public void setCheckInTime(String checkInTime) {
            this.checkInTime = checkInTime;
        }

        public String getCheckInStatus() {
            return checkInStatus;
        }

        public void setCheckInStatus(String checkInStatus) {
            this.checkInStatus = checkInStatus;
        }

        public String getCheckOutDate() {
            return checkOutDate;
        }

        public void setCheckOutDate(String checkOutDate) {
            this.checkOutDate = checkOutDate;
        }

        public String getCheckOutTime() {
            return checkOutTime;
        }

        public void setCheckOutTime(String checkOutTime) {
            this.checkOutTime = checkOutTime;
        }

        public String getCheckOutStatus() {
            return checkOutStatus;
        }

        public void setCheckOutStatus(String checkOutStatus) {
            this.checkOutStatus = checkOutStatus;
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

        public String getTotalWorkedTime() {
            return totalWorkedTime;
        }

        public void setTotalWorkedTime(String totalWorkedTime) {
            this.totalWorkedTime = totalWorkedTime;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
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
