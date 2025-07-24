package com.project.attendease.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.project.attendease.R;
import com.project.attendease.models.Leave;

import java.util.List;

public class LeavesAdapter extends RecyclerView.Adapter<LeavesAdapter.LeaveViewHolder> {

    private final List<Leave> leaveList;

    public LeavesAdapter(List<Leave> leaveList) {
        this.leaveList = leaveList;
    }

    @NonNull
    @Override
    public LeaveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leave, parent, false);
        return new LeaveViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaveViewHolder holder, int position) {
        Leave leave = leaveList.get(position);

        // Display leave type name
        holder.tvLeaveType.setText(leave.getLeaveType());

        // Concatenate startDate and endDate with a hyphen
        String leaveDateRange = leave.getStartDate() + " - " + leave.getEndDate();
        holder.tvLeaveDate.setText(leaveDateRange);

        holder.tvLeaveStatus.setText(leave.getStatus());

        // Set color based on leave status
        switch (leave.getStatus()) {
            case "Approved":
                holder.tvLeaveStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.approved));
                break;
            case "Pending":
                holder.tvLeaveStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.pending));
                break;
            case "Rejected":
                holder.tvLeaveStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.rejected));
                break;
            default:
                holder.tvLeaveStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gray));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return leaveList.size();
    }

    public static class LeaveViewHolder extends RecyclerView.ViewHolder {

        public final TextView tvLeaveType;
        public final TextView tvLeaveDate;
        public final TextView tvLeaveStatus;

        public LeaveViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLeaveType = itemView.findViewById(R.id.tv_leave_type);
            tvLeaveDate = itemView.findViewById(R.id.tv_leave_date);
            tvLeaveStatus = itemView.findViewById(R.id.tv_leave_status);
        }
    }
}
