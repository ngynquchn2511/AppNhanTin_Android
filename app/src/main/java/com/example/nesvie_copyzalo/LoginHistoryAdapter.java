package com.example.nesvie_copyzalo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LoginHistoryAdapter extends RecyclerView.Adapter<LoginHistoryAdapter.HistoryViewHolder> {

    private List<LoginHistory> historyList;

    public LoginHistoryAdapter(List<LoginHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_login_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        LoginHistory history = historyList.get(position);

        holder.tvDevice.setText("Thiết bị: " + history.getDevice());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
        holder.tvTime.setText("Thời gian: " + sdf.format(new java.util.Date(history.getLoginTime())));

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvDevice, tvTime;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDevice = itemView.findViewById(R.id.tvDevice);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
