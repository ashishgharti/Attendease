package com.project.attendease;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.project.attendease.apiservice.ApiService;
import com.project.attendease.apiservice.RetrofitClient;
import com.project.attendease.request.LoginRequest;
import com.project.attendease.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private static final String TAG = "LoginActivity";
    private ImageView passwordVisibilityToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        passwordEditText = findViewById(R.id.password);
        passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle);

        passwordVisibilityToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility_off);
                passwordVisibilityToggle.setColorFilter(ContextCompat.getColor(LoginActivity.this, R.color.gray1));
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility);
                passwordVisibilityToggle.setColorFilter(ContextCompat.getColor(LoginActivity.this, R.color.green));
            }
            isPasswordVisible = !isPasswordVisible;
            passwordEditText.setSelection(passwordEditText.length());
        });

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        signInButton = findViewById(R.id.signInButton);

        apiService = RetrofitClient.getApiService();

        signInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            login(email, password);
        });
    }

    private void login(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    saveUserInfo(loginResponse);
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // In LoginActivity, when saving user info:
    private void saveUserInfo(LoginResponse loginResponse) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String profilePhotoPath = loginResponse.getUser().getPhoto();
        editor.putString("profilePhotoPath", profilePhotoPath);

        Gson gson = new Gson();
        String loginResponseJson = gson.toJson(loginResponse);
        editor.putString("loginResponse", loginResponseJson);
        editor.putString("token", loginResponse.getToken());  // Explicitly save the token
        editor.putString("userId", loginResponse.getUser().getId());
        editor.putString("name", loginResponse.getUser().getName());
        editor.apply();
    }

}
