package com.project.attendease.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.project.attendease.ChangePasswordActivity;
import com.project.attendease.ChangePhoneActivity;
import com.project.attendease.FullImageActivity;
import com.project.attendease.LoginActivity;
import com.project.attendease.R;
import com.project.attendease.UploadImgActivity;
import com.project.attendease.apiservice.ApiService;
import com.project.attendease.apiservice.RetrofitClient;
import com.project.attendease.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    // UI Elements
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText joinDateEditText;
    private EditText phoneNumberEditText;
    private ImageView profileImage;
    private ActivityResultLauncher<Intent> launcher;
    private String baseUrl = "http://139.59.37.128:3000";
    private LinearLayout logoutLinearLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profileImage);
        ImageView cameraIcon = view.findViewById(R.id.cameraIcon);

        // Initialize UI elements
        emailEditText = view.findViewById(R.id.email);
        passwordEditText = view.findViewById(R.id.password);
        joinDateEditText = view.findViewById(R.id.join_date_input);
        phoneNumberEditText = view.findViewById(R.id.phone_number_input);
        logoutLinearLayout = view.findViewById(R.id.logoutLinearLayout);
        // Find the pencil icons
        ImageView passwordPencil = view.findViewById(R.id.password_pencil);
        ImageView phonePencil = view.findViewById(R.id.phone_pencil);
        logoutLinearLayout.setOnClickListener(v -> handleLogout());
        profileImage.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String profilePhotoPath = sharedPreferences.getString("profilePhotoPath", null);

            if (profilePhotoPath != null) {
                String fullPath = baseUrl + profilePhotoPath;
                Intent intent = new Intent(getActivity(), FullImageActivity.class);
                intent.putExtra("imageUrl", fullPath);
                startActivity(intent);
            }
        });

        // Set click listeners
        passwordPencil.setOnClickListener(v -> {
            // Start ChangePasswordActivity
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        phonePencil.setOnClickListener(v -> {
            // Start ChangePhoneActivity
            Intent intent = new Intent(getActivity(), ChangePhoneActivity.class);
            startActivity(intent);
        });

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String photoPath = result.getData().getStringExtra("photoPath");
                        if (photoPath != null) {
                            // Save the photoPath in SharedPreferences
                            saveProfilePhotoPath(photoPath);
                            // Load the image directly from SharedPreferences
                            loadUserData();
                        }
                    }
                });

        cameraIcon.setOnClickListener(v -> {
            // Start UploadImgActivity
            Intent intent = new Intent(getActivity(), UploadImgActivity.class);
            launcher.launch(intent);
        });



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload user data to reflect changes
        loadUserData();
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String loginResponseJson = sharedPreferences.getString("loginResponse", null);
        String profilePhotoPath = sharedPreferences.getString("profilePhotoPath", null);

        if (loginResponseJson != null) {
            Gson gson = new Gson();
            LoginResponse loginResponse = gson.fromJson(loginResponseJson, LoginResponse.class);

            if (loginResponse != null) {
                emailEditText.setText(loginResponse.getUser().getEmail());
                passwordEditText.setText(loginResponse.getUser().getPassword());
                joinDateEditText.setText(loginResponse.getUser().getCreatedAt());
                phoneNumberEditText.setText(loginResponse.getUser().getPhone());

                // Load profile image if exists
                if (profilePhotoPath != null && !profilePhotoPath.isEmpty()) {
                    String fullPath = baseUrl + profilePhotoPath + "?timestamp=" + System.currentTimeMillis();
                    Log.d("ProfileFragment", "Loading image from URL: " + fullPath);
                    Glide.with(this)
                            .load(fullPath)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(R.drawable.profile_pic)
                            .error(R.drawable.ic_network_error) // Handle image load error
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("GlideError", "Load failed", e);
                                    return false; // Allow Glide to handle the error image
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false; // Allow Glide to handle displaying the image
                                }
                            })
                            .into(profileImage);

                } else {
                    Glide.with(this)
                            .load(R.drawable.profile_pic) // Fallback to a default image if no path exists
                            .into(profileImage);
                }
            }
        }
    }

    private void saveProfilePhotoPath(String photoPath) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profilePhotoPath", photoPath);
        editor.apply();
    }

    private void handleLogout() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        ApiService apiService = RetrofitClient.getApiService();
        apiService.logout("Bearer " + token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getActivity(), "Logout successful!", Toast.LENGTH_SHORT).show();

                    // Clear SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    // Redirect to login activity
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), "Failed to logout", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

