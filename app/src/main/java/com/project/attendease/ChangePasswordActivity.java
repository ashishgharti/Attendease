package com.project.attendease;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.project.attendease.apiservice.ApiService;
import com.project.attendease.apiservice.RetrofitClient;
import com.project.attendease.request.PhoneAndPasswordRequest;
import com.project.attendease.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private ImageView oldPasswordVisibilityToggle;
    private ImageView newPasswordVisibilityToggle;
    private TextView oldPasswordValidationTextView;
    private boolean isOldPasswordVisible = false;
    private boolean isNewPasswordVisible = false;

    private ApiService apiService;
    private String token;
    private String storedOldPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        oldPasswordEditText = findViewById(R.id.oldPassword);
        newPasswordEditText = findViewById(R.id.newPassword);
        oldPasswordVisibilityToggle = findViewById(R.id.old_password_Visibility_Toggle);
        newPasswordVisibilityToggle = findViewById(R.id.new_password_Visibility_Toggle);
        oldPasswordValidationTextView = findViewById(R.id.oldPasswordValidation);

        // Initialize API Service
        apiService = RetrofitClient.getApiService();

        // Fetch token and stored password from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");

        String loginResponseJson = sharedPreferences.getString("loginResponse", null);
        if (loginResponseJson != null) {
            Gson gson = new Gson();
            LoginResponse loginResponse = gson.fromJson(loginResponseJson, LoginResponse.class);
            if (loginResponse != null) {
                storedOldPassword = loginResponse.getUser().getPassword();
            }
        }

        // Add TextWatcher to oldPasswordEditText for real-time validation
        oldPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateOldPassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });

        // Handle back button click
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        oldPasswordVisibilityToggle.setOnClickListener(v -> {
            togglePasswordVisibility(oldPasswordEditText, oldPasswordVisibilityToggle, isOldPasswordVisible);
            isOldPasswordVisible = !isOldPasswordVisible;
        });

        newPasswordVisibilityToggle.setOnClickListener(v -> {
            togglePasswordVisibility(newPasswordEditText, newPasswordVisibilityToggle, isNewPasswordVisible);
            isNewPasswordVisible = !isNewPasswordVisible;
        });

        // Update button click
        findViewById(R.id.update_Password_Button).setOnClickListener(v -> updatePassword());
    }

    private void validateOldPassword(String oldPassword) {
        if (oldPassword.equals(storedOldPassword)) {
            oldPasswordValidationTextView.setText("Old password matched");
            oldPasswordValidationTextView.setTextColor(getResources().getColor(R.color.green));
            oldPasswordValidationTextView.setVisibility(View.VISIBLE);
        } else {
            oldPasswordValidationTextView.setText("Old password does not match");
            oldPasswordValidationTextView.setTextColor(getResources().getColor(R.color.red));
            oldPasswordValidationTextView.setVisibility(View.VISIBLE);
        }
    }

    private void togglePasswordVisibility(EditText passwordEditText, ImageView passwordVisibilityToggle, boolean isPasswordVisible) {
        if (isPasswordVisible) {
            // Hide password
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility_off);
            passwordVisibilityToggle.setColorFilter(ContextCompat.getColor(ChangePasswordActivity.this, R.color.gray1));
        } else {
            // Show password
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility);
            passwordVisibilityToggle.setColorFilter(ContextCompat.getColor(ChangePasswordActivity.this, R.color.green));
        }

        // Move cursor to the end of the text
        passwordEditText.setSelection(passwordEditText.length());
    }

    private void updatePassword() {
        String oldPassword = oldPasswordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();

        if (!oldPassword.equals(storedOldPassword)) {
            Toast.makeText(this, "Old password does not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAndPasswordRequest updateRequest = new PhoneAndPasswordRequest(null, newPassword);

        apiService.updateUserAccount(token, updateRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Update SharedPreferences with the new password
                    updateStoredPassword(newPassword);
                    Toast.makeText(ChangePasswordActivity.this, "Password successfully changed!", Toast.LENGTH_SHORT).show();
                    finish();  // Close activity after success
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Failed to change password!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStoredPassword(String newPassword) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String loginResponseJson = sharedPreferences.getString("loginResponse", null);

        if (loginResponseJson != null) {
            Gson gson = new Gson();
            LoginResponse loginResponse = gson.fromJson(loginResponseJson, LoginResponse.class);
            if (loginResponse != null) {
                loginResponse.getUser().setPassword(newPassword);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("loginResponse", gson.toJson(loginResponse));
                editor.apply();
            }
        }
    }
}
