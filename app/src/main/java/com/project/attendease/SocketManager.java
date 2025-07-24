package com.project.attendease;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {
    private static final String TAG = "SocketManager";
    private Socket mSocket;
    private Context context;

    public SocketManager(Context context) {
        this.context = context;
        try {
            mSocket = IO.socket("http://139.59.37.128:3000"); // Replace with your server URL
        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket URI syntax error: " + e.getMessage());
        }
    }

    public void connect() {
        if (mSocket != null) {
            mSocket.connect();

            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "Connected to server");
                }
            });

            // Listener for the "leave-status-updated" event
            mSocket.on("leave-status-updated", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (args.length > 0) {
                        String message = args[0].toString();
                        Log.d(TAG, "Leave status updated: " + message);

                        // Parse the data received from the backend
                        // Assuming the backend sends data in JSON format
                        try {
                            JSONObject data = new JSONObject(message);
                            String title = data.getString("title"); // Example: "Leave Approved"
                            String text = data.getString("text");   // Example: "Your leave has been approved"
                            String time = data.getString("time");   // Example: "2 mins ago"

                            // Show notification
                            NotificationHelper.showNotification(context, title, text, time);

                            // Broadcast the update to other parts of the app
                            Intent intent = new Intent("LeaveStatusUpdated");
                            intent.putExtra("title", title);
                            intent.putExtra("text", text);
                            intent.putExtra("time", time);
                            context.sendBroadcast(intent);

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                        }
                    } else {
                        Log.e(TAG, "Received empty update for leave status");
                    }
                }
            });
        } else {
            Log.e(TAG, "Socket is null, connection cannot be established.");
        }
    }

    public void disconnect() {
        if (mSocket != null) {
            mSocket.disconnect();
            Log.d(TAG, "Disconnected from server");
        } else {
            Log.e(TAG, "Socket is null, cannot disconnect.");
        }
    }
}
