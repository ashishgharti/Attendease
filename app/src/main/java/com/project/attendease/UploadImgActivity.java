package com.project.attendease;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.project.attendease.apiservice.ApiService;
import com.project.attendease.apiservice.RetrofitClient;
import com.project.attendease.response.UploadImageResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadImgActivity extends AppCompatActivity {

    private TextView selectedImageText;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> launcher;
    private ApiService apiService;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_img);

        selectedImageText = findViewById(R.id.selectedImageText);

        // Initialize API Service
        apiService = RetrofitClient.getApiService();

        // Fetch token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");

        // Initialize ActivityResultLauncher
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        String fileName = getFileName(selectedImageUri);
                        selectedImageText.setText(fileName); // Update the TextView with the selected file name
                    } else {
                        Log.d("UploadImgActivity", "Image selection cancelled");
                    }
                });

        Button browseFilesButton = findViewById(R.id.browseFilesButton);
        browseFilesButton.setOnClickListener(v -> openFileChooser());

        Button saveButton = findViewById(R.id.update_Phone_Button);
        saveButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadImageToServer(selectedImageUri);
            } else {
                Toast.makeText(this, "Please select a valid image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcher.launch(intent);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        result = cursor.getString(displayNameIndex);
                        Log.d("UploadImgActivity", "File name retrieved: " + result);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("UploadImgActivity", "Error retrieving file name: " + e.getMessage());
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
            Log.d("UploadImgActivity", "Using last path segment as fallback: " + result);
        }
        return result;
    }

    private void uploadImageToServer(Uri imageUri) {
        try {
            File imageFile = getFileFromUri(imageUri);
            if (imageFile != null) {
                String mimeType = getContentResolver().getType(imageUri); // Get the correct MIME type
                RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), imageFile);
                MultipartBody.Part body = MultipartBody.Part.createFormData("profilePhoto", imageFile.getName(), requestFile);

                apiService.uploadProfilePhoto("Bearer " + token, body).enqueue(new Callback<UploadImageResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<UploadImageResponse> call, @NonNull Response<UploadImageResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UploadImageResponse uploadResponse = response.body();
                            Toast.makeText(UploadImgActivity.this, uploadResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("UploadImgActivity", "Photo path: " + uploadResponse.getPhotoPath());

                            // Save photoPath in SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("profilePhotoPath", uploadResponse.getPhotoPath());
                            editor.apply();

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("photoPath", uploadResponse.getPhotoPath());
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(UploadImgActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                            Log.e("UploadImgActivity", "Upload failed with response code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UploadImageResponse> call, @NonNull Throwable t) {
                        Toast.makeText(UploadImgActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("UploadImgActivity", "Upload failed: " + t.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to prepare image for upload", Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileFromUri(Uri uri) throws Exception {
        File file = null;
        if (uri.getScheme().equals("content")) {
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                if (inputStream != null) {
                    String fileName = getFileName(uri);
                    File tempFile = new File(getCacheDir(), fileName);
                    try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        file = tempFile;
                    }
                }
            }
        } else if (uri.getScheme().equals("file")) {
            file = new File(uri.getPath());
        }
        return file;
    }
}
