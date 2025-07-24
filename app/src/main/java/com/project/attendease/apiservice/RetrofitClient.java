package com.project.attendease.apiservice;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static Context appContext; // Application context

    private RetrofitClient() {
    }

    public static void initialize(Context context) {
        appContext = context.getApplicationContext(); // Initialize with application context
    }

    public static ApiService getApiService() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request originalRequest = chain.request();
                        String token = getToken(); // Retrieve token from SharedPreferences
                        Request newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(newRequest);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("http://139.59.37.128:3000/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    private static String getToken() {
        if (appContext != null) {
            SharedPreferences sharedPreferences = appContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            return sharedPreferences.getString("token", "");
        }
        return "";
    }
}
