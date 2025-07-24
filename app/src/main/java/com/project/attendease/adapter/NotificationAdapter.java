package com.project.attendease.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.project.attendease.R;
import com.project.attendease.apiservice.AppNotificationManager;
import com.project.attendease.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final List<Notification> notifications;
    private final Context context;

    public NotificationAdapter(List<Notification> notifications, Context context) {
        this.notifications = notifications;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.titleView.setText(notification.getTitle());
        holder.timeView.setText(notification.getTime());
        holder.messageView.setText(notification.getMessage());
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void removeNotification(int position) {
        notifications.remove(position);
        notifyItemRemoved(position);
        // Update the storage
        AppNotificationManager.getInstance(context).saveNotifications(notifications);
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleView, timeView, messageView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.notification_title);
            timeView = itemView.findViewById(R.id.notification_time);
            messageView = itemView.findViewById(R.id.notification_message);
        }
    }
}
