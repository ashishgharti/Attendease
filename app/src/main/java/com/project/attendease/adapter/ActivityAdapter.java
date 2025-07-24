package com.project.attendease.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.project.attendease.R;
import com.project.attendease.models.ActivityItem;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private final List<ActivityItem> activityItems;

    public ActivityAdapter(List<ActivityItem> activityItems) {
        this.activityItems = activityItems;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityItem activityItem = activityItems.get(position);

        // Set the date, time, and status
        holder.activityDate.setText(activityItem.getDate());
        holder.activityTime.setText(activityItem.getTime());
        holder.activityStatus.setText(activityItem.getStatus());

        // Set the icon based on the status
        holder.activityIcon.setImageResource(activityItem.getIconResId());

        // Set text color based on status using a simple conditional
        int color;
        if ("Check In".equalsIgnoreCase(activityItem.getStatus())) {
            holder.activityIcon.setImageResource(R.drawable.ic_check_in);
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.green);
        } else if ("Check Out".equalsIgnoreCase(activityItem.getStatus())) {
            holder.activityIcon.setImageResource(R.drawable.ic_check_out);
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.red);
        } else if ("onLeave".equalsIgnoreCase(activityItem.getStatus())) {
            holder.activityIcon.setImageResource(R.drawable.leaves);  // Set the icon for leave status
            color = ContextCompat.getColor(holder.itemView.getContext(), R.color.leave); // Set a color for leave status
        } else {
            Log.e("onBindViewHolder", "Unknown status: " + activityItem.getStatus());
            return; // Skip unknown statuses
        }

        holder.activityStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return activityItems.size();
    }

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {

        ImageView activityIcon;
        TextView activityDate;
        TextView activityTime;
        TextView activityStatus;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            activityIcon = itemView.findViewById(R.id.activity_icon);
            activityDate = itemView.findViewById(R.id.activity_date);
            activityTime = itemView.findViewById(R.id.activity_time);
            activityStatus = itemView.findViewById(R.id.activity_status);
        }
    }
}
