package com.project.attendease;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.project.attendease.apiservice.ApiService;
import com.project.attendease.apiservice.RetrofitClient;
import com.project.attendease.request.LeaveRequest;
import com.project.attendease.response.LeaveTypeResponse;
import com.project.attendease.response.LeaveRequestResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddLeavesRequestActivity extends AppCompatActivity {

    private Spinner leaveTypeSpinner;
    private TextView etFromDate;
    private TextView etToDate;
    private EditText etSubstitute;
    private EditText etDetails;
    private Button submitRequestButton;

    private List<LeaveTypeResponse.LeaveType> leaveTypes;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_leaves_request);

        etFromDate = findViewById(R.id.et_from);
        etToDate = findViewById(R.id.et_to);
        etSubstitute = findViewById(R.id.et_substitute);
        etDetails = findViewById(R.id.et_details);
        submitRequestButton = findViewById(R.id.submit_request_button);
        leaveTypeSpinner = findViewById(R.id.custom_spinner);

        ImageView fromCalendar = findViewById(R.id.from_calendar);
        ImageView toCalendar = findViewById(R.id.to_calendar);

        fromCalendar.setOnClickListener(v -> showDatePickerDialog(etFromDate));
        toCalendar.setOnClickListener(v -> showDatePickerDialog(etToDate));

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        // Fetch the token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Token not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchLeaveTypes(); // Fetch leave types from backend

        submitRequestButton.setOnClickListener(v -> submitLeaveRequest()); // Handle form submission
    }

    private void fetchLeaveTypes() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getLeaveTypes().enqueue(new Callback<LeaveTypeResponse>() {
            @Override
            public void onResponse(Call<LeaveTypeResponse> call, Response<LeaveTypeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    leaveTypes = response.body().getLeaveTypes();
                    setupSpinner(leaveTypes);
                } else {
                    Toast.makeText(AddLeavesRequestActivity.this, "Failed to load leave types", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LeaveTypeResponse> call, Throwable t) {
                Toast.makeText(AddLeavesRequestActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinner(List<LeaveTypeResponse.LeaveType> leaveTypes) {
        List<String> items = new ArrayList<>();
        items.add("Leave Type");
        for (LeaveTypeResponse.LeaveType type : leaveTypes) {
            items.add(type.getLeaveTypename());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.custom_spinner_item,
                items
        ) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0; // Disable the first item (hint)
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        leaveTypeSpinner.setAdapter(adapter);
        leaveTypeSpinner.setSelection(0);
    }

    private void showDatePickerDialog(final TextView textView) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
            textView.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void submitLeaveRequest() {
        String startDate = etFromDate.getText().toString();
        String endDate = etToDate.getText().toString();
        int selectedPosition = leaveTypeSpinner.getSelectedItemPosition();

        if (selectedPosition == 0) {
            Toast.makeText(this, "Please select a leave type", Toast.LENGTH_SHORT).show();
            return;
        }

        String leaveTitle = leaveTypes.get(selectedPosition - 1).get_id();
        String substitute = etSubstitute.getText().toString();
        String leaveDetails = etDetails.getText().toString();

        LeaveRequest leaveRequest = new LeaveRequest(startDate, endDate, leaveTitle, substitute, leaveDetails);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.submitLeaveRequest("Bearer " + token, leaveRequest).enqueue(new Callback<LeaveRequestResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeaveRequestResponse> call, @NonNull Response<LeaveRequestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AddLeavesRequestActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    finish(); // Close the current activity and return to the previous one (LeavesFragment)
                } else {
                    Toast.makeText(AddLeavesRequestActivity.this, "Failed to submit leave request", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LeaveRequestResponse> call, @NonNull Throwable t) {
                Toast.makeText(AddLeavesRequestActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
