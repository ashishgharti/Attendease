package com.project.attendease.response;

import java.util.List;


public class LeaveTypeResponse {
    private List<LeaveType> leaveTypes;

    public LeaveTypeResponse(List<LeaveType> leaveTypes) {
        this.leaveTypes = leaveTypes;
    }

    public List<LeaveType> getLeaveTypes() {
        return leaveTypes;
    }

    public void setLeaveTypes(List<LeaveType> leaveTypes) {
        this.leaveTypes = leaveTypes;
    }

    public static class LeaveType {
        private String _id;
        private String leaveTypename;
        private int __v;

        public LeaveType(String _id, String leaveTypename, int __v) {
            this._id = _id;
            this.leaveTypename = leaveTypename;
            this.__v = __v;
        }

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getLeaveTypename() {
            return leaveTypename;
        }

        public void setLeaveTypename(String leaveTypename) {
            this.leaveTypename = leaveTypename;
        }

        public int get__v() {
            return __v;
        }

        public void set__v(int __v) {
            this.__v = __v;
        }
    }
}
