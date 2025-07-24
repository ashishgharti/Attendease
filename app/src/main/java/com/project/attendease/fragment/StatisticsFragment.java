package com.project.attendease.fragment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.project.attendease.R;
import com.project.attendease.adapter.StatisticsAdapter;
import com.project.attendease.apiservice.ApiService;
import com.project.attendease.apiservice.RetrofitClient;
import com.project.attendease.models.Statistic;
import com.project.attendease.response.AttendanceRecordResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class StatisticsFragment extends Fragment {

    private List<Statistic> statisticList;
    private StatisticsAdapter adapter;
    private ApiService apiService;
    private String token;
    private String statusFilter = "";
    private String daysFilter = "7";  // Default to 7 days
    private String startDateFilter = "";
    private String endDateFilter = "";
    private TextView tvStartDate, tvEndDate;
    private Button btn7Days, btn14Days, btn30Days;
    private ProgressBar progressBar;
    private ImageView networkErrorImage;
    private TextView networkErrorText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;


    // ActivityResultLauncher for requesting permission
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Initialize UI elements
        networkErrorImage = view.findViewById(R.id.network_error_image);
        networkErrorText = view.findViewById(R.id.network_error_text);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);

        btn7Days = view.findViewById(R.id.btn7Days);
        btn14Days = view.findViewById(R.id.btn14Days);
        btn30Days = view.findViewById(R.id.btn30Days);

        // Initialize the ActivityResultLauncher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission granted, proceed with the download
                        exportFilteredDataAsPDF();
                    } else {
                        // Permission denied, show a message
                        Toast.makeText(requireContext(), "Storage permission is required to download PDF.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Set click listener for export button
        LinearLayout exportDataButton = view.findViewById(R.id.ic_export_data);
        exportDataButton.setOnClickListener(v -> checkPermissionAndExportPDF());

        // Set up default selection for 7 days
        selectButton(btn7Days);
        daysFilter = "7";

        btn7Days.setOnClickListener(v -> {
            resetFilterButtons();
            selectButton(btn7Days);
            daysFilter = "7";
            fetchAttendanceRecords();
        });

        btn14Days.setOnClickListener(v -> {
            resetFilterButtons();
            selectButton(btn14Days);
            daysFilter = "14";
            fetchAttendanceRecords();
        });

        btn30Days.setOnClickListener(v -> {
            resetFilterButtons();
            selectButton(btn30Days);
            daysFilter = "30";
            fetchAttendanceRecords();
        });

        swipeRefreshLayout.setOnRefreshListener(this::fetchAttendanceRecords);

        // Initialize ApiService and token
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        statisticList = new ArrayList<>();
        adapter = new StatisticsAdapter(statisticList);
        recyclerView.setAdapter(adapter);

        apiService = RetrofitClient.getApiService();

        fetchAttendanceRecords();

        view.findViewById(R.id.ic_filter).setOnClickListener(v -> showFilterDialog());

        return view;
    }

    private void checkPermissionAndExportPDF() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            // For Android 9 (Pie) and below
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                // Permission already granted, proceed with download
                exportFilteredDataAsPDF();
            }
        } else {
            // For Android 10 (Q) and above
            exportFilteredDataAsPDF();
        }
    }


    private void exportFilteredDataAsPDF() {
        // Show loading spinner or any other indication of processing
        showLoading();

        // Build the URL with the necessary filters
        String exportUrl = "http://139.59.37.128:3000/attendance/myattendance?exportType=pdf" +
                "&status=" + statusFilter +
                "&days=" + daysFilter +
                "&startDate=" + startDateFilter +
                "&endDate=" + endDateFilter;

        // Initialize the request using Retrofit or OkHttp
        apiService.exportAttendanceRecordsAsPDF(token, exportUrl).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    // Get the filename
                    String fileName = "AttendanceRecords.pdf";

                    // Save the PDF file to the device
                    File savedFile = savePDFToFile(response.body(), fileName);

                    if (savedFile != null) {
                        Toast.makeText(getContext(), "PDF downloaded successfully.", Toast.LENGTH_LONG).show();
                        openDownloadedPDF(savedFile);
                    } else {
                        Toast.makeText(getContext(), "Failed to save PDF.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to download PDF.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                hideLoading();
                Toast.makeText(getContext(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File savePDFToFile(ResponseBody body, String fileName) {
        File pdfFile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            // Use app's private external storage directory
            File downloadsDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            pdfFile = new File(downloadsDir, fileName);

            byte[] fileReader = new byte[4096];
            long fileSize = body.contentLength();
            long fileSizeDownloaded = 0;

            inputStream = body.byteStream();
            outputStream = new FileOutputStream(pdfFile);

            while (true) {
                int read = inputStream.read(fileReader);
                if (read == -1) {
                    break;
                }

                outputStream.write(fileReader, 0, read);
                fileSizeDownloaded += read;

                // Log the download progress
                Log.d("StatisticsFragment", "File download: " + fileSizeDownloaded + " of " + fileSize);
            }

            outputStream.flush();
            Log.d("StatisticsFragment", "PDF saved successfully to " + pdfFile.getAbsolutePath());
            return pdfFile;

        } catch (IOException e) {
            Log.e("StatisticsFragment", "Failed to save PDF: " + e.getMessage(), e);
            return null;

        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                Log.e("StatisticsFragment", "Failed to close streams: " + e.getMessage(), e);
            }
        }
    }

    private void openDownloadedPDF(File file) {
        try {
            Uri pdfUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(intent);
        } catch (Exception e) {
            Log.e("StatisticsFragment", "Failed to open PDF: " + e.getMessage(), e);
            Toast.makeText(getContext(), "No application available to view PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAttendanceRecords() {
        showLoading();
        apiService.getAttendanceRecords(token, statusFilter, daysFilter, startDateFilter, endDateFilter).enqueue(new Callback<AttendanceRecordResponse>() {
            @Override
            public void onResponse(@NonNull Call<AttendanceRecordResponse> call, @NonNull Response<AttendanceRecordResponse> response) {
                hideLoading();
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    hideNetworkError();
                    AttendanceRecordResponse attendanceRecordResponse = response.body();
                    statisticList.clear();

                    for (AttendanceRecordResponse.Record record : attendanceRecordResponse.getAttendanceRecords()) {
                        // Check if the check-in status matches the selected status
                        if (statusFilter.isEmpty() || (record.getCheckInStatus() != null && record.getCheckInStatus().equalsIgnoreCase(statusFilter))) {
                            statisticList.add(new Statistic(
                                    record.getDate(),
                                    record.getCheckInTime(),
                                    record.getCheckInStatus(),
                                    R.drawable.ic_check_in,
                                    "Check In"
                            ));
                        }

                        // Check if the check-out status matches the selected status
                        if (statusFilter.isEmpty() || (record.getCheckOutStatus() != null && record.getCheckOutStatus().equalsIgnoreCase(statusFilter))) {
                            statisticList.add(new Statistic(
                                    record.getCheckOutDate(),
                                    record.getCheckOutTime(),
                                    record.getCheckOutStatus(),
                                    R.drawable.ic_check_out,
                                    "Check Out"
                            ));
                        }
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    showNetworkError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AttendanceRecordResponse> call, @NonNull Throwable t) {
                hideLoading();
                swipeRefreshLayout.setRefreshing(false);
                showNetworkError();
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

    private void resetFilterButtons() {
        resetButtonStyle(btn7Days);
        resetButtonStyle(btn14Days);
        resetButtonStyle(btn30Days);
    }

    private void resetButtonStyle(Button button) {
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
        button.setTextColor(ContextCompat.getColor(getContext(), R.color.gray2));
    }

    private void selectButton(Button button) {
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.green)));
        button.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.layout_statistics_filter, null);

        Button btnLate = dialogView.findViewById(R.id.btnLate);
        Button btnEarly = dialogView.findViewById(R.id.btnEarly);
        Button btnOnTime = dialogView.findViewById(R.id.btnOnTime);
        Button btnOnLeave = dialogView.findViewById(R.id.btnOnLeave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnApply = dialogView.findViewById(R.id.btnApply);

        tvStartDate = dialogView.findViewById(R.id.tv_startdate);
        tvEndDate = dialogView.findViewById(R.id.tv_enddate);

        // Reset all buttons first
        resetButtonStyle(btnLate);
        resetButtonStyle(btnEarly);
        resetButtonStyle(btnOnTime);
        resetButtonStyle(btnOnLeave);

        // Set the current filter and date selections in the dialog
        if (statusFilter.equalsIgnoreCase("late")) {
            selectButton(btnLate);
        } else if (statusFilter.equalsIgnoreCase("early")) {
            selectButton(btnEarly);
        } else if (statusFilter.equalsIgnoreCase("ontime")) {
            selectButton(btnOnTime);
        } else if (statusFilter.equalsIgnoreCase("onleave")) {
            selectButton(btnOnLeave);
        }

        tvStartDate.setText(startDateFilter.isEmpty() ? "Select Start Date" : startDateFilter);
        tvEndDate.setText(endDateFilter.isEmpty() ? "Select End Date" : endDateFilter);

        tvStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        tvEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        btnLate.setOnClickListener(v -> {
            resetButtonStyle(btnEarly);
            resetButtonStyle(btnOnTime);
            resetButtonStyle(btnOnLeave);
            selectButton(btnLate);
            statusFilter = "late";
        });

        btnEarly.setOnClickListener(v -> {
            resetButtonStyle(btnLate);
            resetButtonStyle(btnOnTime);
            resetButtonStyle(btnOnLeave);
            selectButton(btnEarly);
            statusFilter = "early";
        });

        btnOnTime.setOnClickListener(v -> {
            resetButtonStyle(btnLate);
            resetButtonStyle(btnEarly);
            resetButtonStyle(btnOnLeave);
            selectButton(btnOnTime);
            statusFilter = "onTime";
        });

        btnOnLeave.setOnClickListener(v -> {
            resetButtonStyle(btnLate);
            resetButtonStyle(btnEarly);
            resetButtonStyle(btnOnTime);
            selectButton(btnOnLeave);
            statusFilter = "onLeave";
        });

        btnCancel.setOnClickListener(v -> {
            statusFilter = "";
            startDateFilter = "";
            endDateFilter = "";
            resetButtonStyle(btnLate);
            resetButtonStyle(btnEarly);
            resetButtonStyle(btnOnTime);
            resetButtonStyle(btnOnLeave);
            dialog.dismiss();
        });

        btnApply.setOnClickListener(v -> {
            fetchAttendanceRecords();
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
}
