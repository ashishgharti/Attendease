package com.project.attendease.apiservice;

import com.project.attendease.request.CheckInRequest;
import com.project.attendease.request.CheckOutRequest;
import com.project.attendease.request.LeaveRequest;
import com.project.attendease.request.PhoneAndPasswordRequest;
import com.project.attendease.response.AttendanceRecordResponse;
import com.project.attendease.response.CheckInResponse;
import com.project.attendease.request.LoginRequest;
import com.project.attendease.response.CheckOutResponse;
import com.project.attendease.response.GeofenceResponse;
import com.project.attendease.response.LeaveRequestListResponse;
import com.project.attendease.response.LeaveRequestResponse;
import com.project.attendease.response.LeaveTypeResponse;
import com.project.attendease.response.LoginResponse;
import com.project.attendease.response.UploadImageResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {
    @POST("empauth/emp-login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
    @POST("empauth/emp-logout") // Replace with your actual logout endpoint
    Call<Void> logout(@Header("Authorization") String token);

    @GET("location/geofences")
    Call<List<GeofenceResponse>> getGeofence();

    @POST("attendance/checkin")
    Call<CheckInResponse> checkIn(@Header("Authorization") String token, @Body CheckInRequest checkInRequest);

    @POST("attendance/checkout")
    Call<CheckOutResponse> checkOut(@Header("Authorization") String token, @Body CheckOutRequest checkOutRequest);

    @GET("attendance/myattendance")
    Call<AttendanceRecordResponse> getAttendanceRecords(
            @Header("Authorization") String token,
            @Query("status") String status,
            @Query("days") String days,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @PUT("employee/user-account")
    Call<Void> updateUserAccount(@Header("Authorization") String token, @Body PhoneAndPasswordRequest request);

    @Multipart
    @POST("photo/upload-photo")
    Call<UploadImageResponse> uploadProfilePhoto(
            @Header("Authorization") String token,
            @Part MultipartBody.Part profilePhoto
    );
    @GET("leave/types")
    Call<LeaveTypeResponse> getLeaveTypes();

    @POST("/leaverequest/submit")
    Call<LeaveRequestResponse> submitLeaveRequest(@Header("Authorization") String token, @Body LeaveRequest leaveRequest);

    @GET("/leaverequest/myrequests")
    Call<LeaveRequestListResponse> getMyLeaveRequests(
            @Header("Authorization") String token,
            @Query("status") String status,
            @Query("days") String days,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );
    @GET
    Call<ResponseBody> exportAttendanceRecordsAsPDF(
            @Header("Authorization") String token,
            @Url String url
    );


}
