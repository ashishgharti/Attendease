package com.project.attendease;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.project.attendease.apiservice.ApiService;
import com.project.attendease.apiservice.RetrofitClient;
import com.project.attendease.request.PhoneAndPasswordRequest;
import com.project.attendease.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePhoneActivity extends AppCompatActivity {

    private EditText oldPhoneEditText;
    private EditText newPhoneEditText;
    private TextView oldPhoneValidationTextView;
    private ApiService apiService;
    private String token;
    private String storedOldPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_phone);

        oldPhoneEditText = findViewById(R.id.oldPhone);
        newPhoneEditText = findViewById(R.id.newPhone);
        oldPhoneValidationTextView = findViewById(R.id.oldPhoneValidation);

        // Initialize API Service
        apiService = RetrofitClient.getApiService();

        // Fetch token and stored phone from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");

        String loginResponseJson = sharedPreferences.getString("loginResponse", null);
        if (loginResponseJson != null) {
            Gson gson = new Gson();
            LoginResponse loginResponse = gson.fromJson(loginResponseJson, LoginResponse.class);
            if (loginResponse != null) {
                storedOldPhone = loginResponse.getUser().getPhone();
            }
        }

        // Add TextWatcher to oldPhoneEditText for real-time validation
        oldPhoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateOldPhone(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });

        // Handle back button click
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        // Update button click
        findViewById(R.id.update_Phone_Button).setOnClickListener(v -> updatePhoneNumber());
    }

    private void validateOldPhone(String oldPhone) {
        if (oldPhone.equals(storedOldPhone)) {
            oldPhoneValidationTextView.setText("Old phone number matched");
            oldPhoneValidationTextView.setTextColor(getResources().getColor(R.color.green));
            oldPhoneValidationTextView.setVisibility(View.VISIBLE);
        } else {
            oldPhoneValidationTextView.setText("Old phone number does not match");
            oldPhoneValidationTextView.setTextColor(getResources().getColor(R.color.red));
            oldPhoneValidationTextView.setVisibility(View.VISIBLE);
        }
    }

    private void updatePhoneNumber() {
        String oldPhone = oldPhoneEditText.getText().toString();
        String newPhone = newPhoneEditText.getText().toString();

        if (!oldPhone.equals(storedOldPhone)) {
            Toast.makeText(this, "Old phone number does not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAndPasswordRequest updateRequest = new PhoneAndPasswordRequest(newPhone, null);

        apiService.updateUserAccount(token, updateRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Update SharedPreferences with the new phone number
                    updateStoredPhoneNumber(newPhone);
                    Toast.makeText(ChangePhoneActivity.this, "Phone number successfully changed!", Toast.LENGTH_SHORT).show();
                    finish();  // Close activity after success
                } else {
                    Toast.makeText(ChangePhoneActivity.this, "Failed to change phone number!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ChangePhoneActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStoredPhoneNumber(String newPhone) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String loginResponseJson = sharedPreferences.getString("loginResponse", null);

        if (loginResponseJson != null) {
            Gson gson = new Gson();
            LoginResponse loginResponse = gson.fromJson(loginResponseJson, LoginResponse.class);
            if (loginResponse != null) {
                loginResponse.getUser().setPhone(newPhone);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("loginResponse", gson.toJson(loginResponse));
                editor.apply();
            }
        }
    }
}
