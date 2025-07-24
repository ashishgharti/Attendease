package com.project.attendease.response;

import com.google.gson.annotations.SerializedName;

public class CheckOutResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("attendance")
    private Attendance attendance;

    @SerializedName("payroll")
    private Payroll payroll;

    public CheckOutResponse(String message, Attendance attendance, Payroll payroll) {
        this.message = message;
        this.attendance = attendance;
        this.payroll = payroll;
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

    public Payroll getPayroll() {
        return payroll;
    }

    public void setPayroll(Payroll payroll) {
        this.payroll = payroll;
    }

    public static class Attendance {

        @SerializedName("employeeId")
        private String employeeId;

        @SerializedName("checkInTime")
        private String checkInTime;

        @SerializedName("checkOutTime")
        private String checkOutTime;

        @SerializedName("checkOutDate")
        private String checkOutDate;

        @SerializedName("checkOutStatus")
        private String checkOutStatus;

        @SerializedName("totalWorkedTime")
        private String totalWorkedTime;

        @SerializedName("earlyMinutes")
        private String earlyMinutes;

        @SerializedName("lateMinutes")
        private String lateMinutes;

        @SerializedName("location")
        private Location location;

        @SerializedName("_id")
        private String id;

        @SerializedName("__v")
        private int version;

        public Attendance(String employeeId, String checkInTime, String checkOutTime, String checkOutDate, String checkOutStatus, String totalWorkedTime, String earlyMinutes, String lateMinutes, Location location, String id, int version) {
            this.employeeId = employeeId;
            this.checkInTime = checkInTime;
            this.checkOutTime = checkOutTime;
            this.checkOutDate = checkOutDate;
            this.checkOutStatus = checkOutStatus;
            this.totalWorkedTime = totalWorkedTime;
            this.earlyMinutes = earlyMinutes;
            this.lateMinutes = lateMinutes;
            this.location = location;
            this.id = id;
            this.version = version;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getCheckInTime() {
            return checkInTime;
        }

        public void setCheckInTime(String checkInTime) {
            this.checkInTime = checkInTime;
        }

        public String getCheckOutTime() {
            return checkOutTime;
        }

        public void setCheckOutTime(String checkOutTime) {
            this.checkOutTime = checkOutTime;
        }

        public String getCheckOutDate() {
            return checkOutDate;
        }

        public void setCheckOutDate(String checkOutDate) {
            this.checkOutDate = checkOutDate;
        }

        public String getCheckOutStatus() {
            return checkOutStatus;
        }

        public void setCheckOutStatus(String checkOutStatus) {
            this.checkOutStatus = checkOutStatus;
        }

        public String getTotalWorkedTime() {
            return totalWorkedTime;
        }

        public void setTotalWorkedTime(String totalWorkedTime) {
            this.totalWorkedTime = totalWorkedTime;
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

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
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

    public static class Payroll {

        @SerializedName("employeeId")
        private String employeeId;

        @SerializedName("basicSalary")
        private double basicSalary;

        @SerializedName("overtimePay")
        private double overtimePay;

        @SerializedName("holidayPay")
        private double holidayPay;

        @SerializedName("lateDeduction")
        private double lateDeduction;

        @SerializedName("earlyCheckoutDeduction")
        private double earlyCheckoutDeduction;

        @SerializedName("totalSalary")
        private double totalSalary;

        @SerializedName("month")
        private String month;

        @SerializedName("year")
        private String year;

        public Payroll(String employeeId, double basicSalary, double overtimePay, double holidayPay, double lateDeduction, double earlyCheckoutDeduction, double totalSalary, String month, String year) {
            this.employeeId = employeeId;
            this.basicSalary = basicSalary;
            this.overtimePay = overtimePay;
            this.holidayPay = holidayPay;
            this.lateDeduction = lateDeduction;
            this.earlyCheckoutDeduction = earlyCheckoutDeduction;
            this.totalSalary = totalSalary;
            this.month = month;
            this.year = year;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public double getBasicSalary() {
            return basicSalary;
        }

        public void setBasicSalary(double basicSalary) {
            this.basicSalary = basicSalary;
        }

        public double getOvertimePay() {
            return overtimePay;
        }

        public void setOvertimePay(double overtimePay) {
            this.overtimePay = overtimePay;
        }

        public double getHolidayPay() {
            return holidayPay;
        }

        public void setHolidayPay(double holidayPay) {
            this.holidayPay = holidayPay;
        }

        public double getLateDeduction() {
            return lateDeduction;
        }

        public void setLateDeduction(double lateDeduction) {
            this.lateDeduction = lateDeduction;
        }

        public double getEarlyCheckoutDeduction() {
            return earlyCheckoutDeduction;
        }

        public void setEarlyCheckoutDeduction(double earlyCheckoutDeduction) {
            this.earlyCheckoutDeduction = earlyCheckoutDeduction;
        }

        public double getTotalSalary() {
            return totalSalary;
        }

        public void setTotalSalary(double totalSalary) {
            this.totalSalary = totalSalary;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }
    }
}
