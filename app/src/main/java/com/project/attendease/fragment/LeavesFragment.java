package com.project.attendease.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.project.attendease.AddLeavesRequestActivity;
import com.project.attendease.R;
import com.project.attendease.SocketManager;
import com.project.attendease.adapter.LeavesAdapter;
import com.project.attendease.apiservice.ApiService;
import com.project.attendease.apiservice.RetrofitClient;
import com.project.attendease.models.Leave;
import com.project.attendease.response.LeaveRequestListResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class LeavesFragment extends Fragment {

    private LeavesAdapter adapter;
    private SocketManager socketManager;
    private List<Leave> leaveList;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ImageView networkErrorImage;
    private TextView networkErrorText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String statusFilter = "";
    private String daysFilter = "7";  // Default to 7 days
    private String startDateFilter = "";
    private String endDateFilter = "";
    private ApiService apiService;
    private TextView tvStartDate, tvEndDate;
    private Button btn7Days, btn14Days, btn30Days;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaves, container, false);

        // Initialize ApiService
        apiService = RetrofitClient.getApiService();

        // Initialize UI elements
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        networkErrorImage = view.findViewById(R.id.network_error_image);
        networkErrorText = view.findViewById(R.id.network_error_text);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        btn7Days = view.findViewById(R.id.btn7Days);
        btn14Days = view.findViewById(R.id.btn14Days);
        btn30Days = view.findViewById(R.id.btn30Days);

        // Set default selection to 7 days
        selectButton(btn7Days);
        daysFilter = "7";

        btn7Days.setOnClickListener(v -> {
            resetFilterButtons();
            selectButton(btn7Days);
            daysFilter = "7";
            fetchLeaveRequests();
        });

        btn14Days.setOnClickListener(v -> {
            resetFilterButtons();
            selectButton(btn14Days);
            daysFilter = "14";
            fetchLeaveRequests();
        });

        btn30Days.setOnClickListener(v -> {
            resetFilterButtons();
            selectButton(btn30Days);
            daysFilter = "30";
            fetchLeaveRequests();
        });

        // Initialize SocketManager
        socketManager = new SocketManager(getContext());
        socketManager.connect();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        leaveList = new ArrayList<>();
        adapter = new LeavesAdapter(leaveList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::fetchLeaveRequests);

        // Set click listener for Add Leaves button
        ImageView addLeaves = view.findViewById(R.id.add_leaves);
        addLeaves.setOnClickListener(v -> {
            // Start AddLeavesRequestActivity
            Intent intent = new Intent(getActivity(), AddLeavesRequestActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.ic_filter).setOnClickListener(v -> showFilterDialog());

        // Fetch leave requests initially
        fetchLeaveRequests();

        return view;
    }

    private void resetFilterButtons() {
        resetButtonStyle(btn7Days);
        resetButtonStyle(btn14Days);
        resetButtonStyle(btn30Days);
    }

    private void resetButtonStyle(Button button) {
        if (button != null) {
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
            button.setTextColor(ContextCompat.getColor(getContext(), R.color.gray2));
        }
    }

    private void selectButton(Button button) {
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.green)));
        button.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    private void fetchLeaveRequests() {
        showLoading();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        apiService.getMyLeaveRequests("Bearer " + token, statusFilter, daysFilter, startDateFilter, endDateFilter)
                .enqueue(new Callback<LeaveRequestListResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LeaveRequestListResponse> call, @NonNull Response<LeaveRequestListResponse> response) {
                        hideLoading();
                        swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
                        if (response.isSuccessful() && response.body() != null) {
                            hideNetworkError();
                            leaveList.clear();
                            leaveList.addAll(response.body().getLeaveRequests());
                            adapter.notifyDataSetChanged();
                        } else {
                            showNetworkError();
                            Toast.makeText(getContext(), "Failed to fetch leave requests", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LeaveRequestListResponse> call, @NonNull Throwable t) {
                        hideLoading();
                        swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
                        showNetworkError();
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        networkErrorImage.setVisibility(View.GONE);
        networkErrorText.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showNetworkError() {
        networkErrorImage.setVisibility(View.VISIBLE);
        networkErrorText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideNetworkError() {
        networkErrorImage.setVisibility(View.GONE);
        networkErrorText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.layout_leaves_filter, null);

        Button btnAccepted = dialogView.findViewById(R.id.btnAccepted);
        Button btnRejected = dialogView.findViewById(R.id.btnRejected);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnApply = dialogView.findViewById(R.id.btnApply);

        tvStartDate = dialogView.findViewById(R.id.tv_start_date);
        tvEndDate = dialogView.findViewById(R.id.tv_end_date);

        // Reset all buttons first
        resetButtonStyle(btnAccepted);
        resetButtonStyle(btnRejected);

        // Set the current filter and date selections in the dialog
        if (statusFilter.equalsIgnoreCase("Approved")) {
            selectButton(btnAccepted);
        } else if (statusFilter.equalsIgnoreCase("Rejected")) {
            selectButton(btnRejected);
        }

        tvStartDate.setText(startDateFilter.isEmpty() ? "Select Start Date" : startDateFilter);
        tvEndDate.setText(endDateFilter.isEmpty() ? "Select End Date" : endDateFilter);

        tvStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        tvEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        btnAccepted.setOnClickListener(v -> {
            resetButtonStyle(btnRejected);
            selectButton(btnAccepted);
            statusFilter = "Approved";
        });

        btnRejected.setOnClickListener(v -> {
            resetButtonStyle(btnAccepted);
            selectButton(btnRejected);
            statusFilter = "Rejected";
        });

        btnCancel.setOnClickListener(v -> {
            statusFilter = "";
            startDateFilter = "";
            endDateFilter = "";
            resetButtonStyle(btnAccepted);
            resetButtonStyle(btnRejected);
            dialog.dismiss();
        });

        btnApply.setOnClickListener(v -> {
            fetchLeaveRequests();
            dialog.dismiss();
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }


    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            if (isStartDate) {
                startDateFilter = selectedDate;
                tvStartDate.setText(selectedDate);
            } else {
                endDateFilter = selectedDate;
                tvEndDate.setText(selectedDate);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        socketManager.disconnect();
    }
}
