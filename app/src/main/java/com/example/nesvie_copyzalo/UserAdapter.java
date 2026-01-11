package com.example.nesvie_copyzalo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;
    private OnAddFriendClickListener listener;

    // Interface lắng nghe sự kiện click "Kết bạn"
    public interface OnAddFriendClickListener {
        void onAddFriendClick(User user);
    }

    public UserAdapter(List<User> userList, OnAddFriendClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        context = parent.getContext();
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvName.setText(user.getTenHienThi() != null ? user.getTenHienThi() : "No Name");
        holder.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "No Email");

        String friendStatus = user.getFriendStatus();

        // Xử lý hiển thị trạng thái nút
        if ("Bạn bè".equals(friendStatus)) {
            holder.btnAdd.setText("Bạn bè");
            holder.btnAdd.setBackgroundColor(Color.parseColor("#4CAF50")); // xanh lá

        } else if ("Đang chờ".equals(friendStatus)) {
            holder.btnAdd.setText("Đã gửi");
            holder.btnAdd.setBackgroundColor(Color.parseColor("#9E9E9E")); // xám

        } else {
            holder.btnAdd.setText("Kết bạn");
            holder.btnAdd.setBackgroundColor(Color.parseColor("#2196F3")); // xanh dương
        }

        // ✅ Cho phép click trong mọi trạng thái
        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddFriendClick(user);
            }
        });
    }




    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    // ✅ Hàm updateList để search/filter user
    public void updateList(List<User> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        Button btnAdd;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}
