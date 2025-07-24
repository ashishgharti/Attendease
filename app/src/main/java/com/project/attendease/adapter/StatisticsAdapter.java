package com.project.attendease.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.attendease.R;
import com.project.attendease.models.Statistic;

import java.util.List;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.StatisticsViewHolder> {

    private final List<Statistic> statisticsList;

    public static class StatisticsViewHolder extends RecyclerView.ViewHolder {
        public final ImageView icon;
        public final TextView date;
        public final TextView time;
        public final TextView status;
        public final LinearLayout statusLayout;

        public StatisticsViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            date = itemView.findViewById(R.id.activity_date);
            time = itemView.findViewById(R.id.activity_time);
            status = itemView.findViewById(R.id.status);
            statusLayout = itemView.findViewById(R.id.status_layout);
        }
    }

    public StatisticsAdapter(List<Statistic> statisticsList) {
        this.statisticsList = statisticsList;
    }

    @NonNull
    @Override
    public StatisticsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.statistics_item, parent, false);
        return new StatisticsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StatisticsViewHolder holder, int position) {
        Statistic statistic = statisticsList.get(position);

        // Set the icon based on whether it's a check-in or check-out
        if ("Check In".equalsIgnoreCase(statistic.getType())) {
            holder.icon.setImageResource(R.drawable.ic_check_in); // Use your check-in icon resource
        } else if ("Check Out".equalsIgnoreCase(statistic.getType())) {
            holder.icon.setImageResource(R.drawable.ic_check_out); // Use your check-out icon resource
        }

        // Set the date and time
        holder.date.setText(statistic.getDate());
        holder.time.setText(statistic.getTime());

        // Set the status text and color
        String status = statistic.getStatus();
        if (status != null) {
            holder.status.setText(status);
            setStatusColor(holder, status);
        } else {
            // Handle null status if necessary
            holder.status.setText("Unknown");
            holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black)); // Default color
        }
    }

    private void setStatusColor(StatisticsViewHolder holder, String status) {
        int colorId;

        switch (status.toLowerCase()) {
            case "on time":
            case "ontime":
                colorId = R.color.on_time;
                break;
            case "late":
                colorId = R.color.late;
                break;
            case "early":
                colorId = R.color.early;
                break;
            case "onleave":
                colorId = R.color.leave;
                break;
            default:
                colorId = R.color.black; // Default or fallback color
                Log.e("StatisticsAdapter", "Unknown status: " + status);
                break;
        }

        holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), colorId));
    }

    @Override
    public int getItemCount() {
        return statisticsList.size();
    }
}
