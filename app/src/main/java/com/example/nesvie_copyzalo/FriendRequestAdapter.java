// FriendRequestAdapter.java
package com.example.nesvie_copyzalo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder> {

    public interface OnRequestActionListener {
        void onAccept(FriendRequest request);
        void onDecline(FriendRequest request);
    }

    private List<FriendRequest> requestList;
    private OnRequestActionListener listener;
    private DBHelper dbHelper;
    private String currentUserId;

    public FriendRequestAdapter(List<FriendRequest> requestList, String currentUserId, DBHelper dbHelper,
                                OnRequestActionListener listener) {
        this.requestList = requestList;
        this.currentUserId = currentUserId;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        FriendRequest fr = requestList.get(position);

        // Lấy user từ SQLite
        User user = dbHelper.getUserById(fr.getFromId());
        if (user != null) {
            holder.tvName.setText(user.getTenHienThi() != null ? user.getTenHienThi() : user.getEmail());

            // Load avatar (nếu có), nếu không thì load avatar mặc định
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(user.getAvatarUrl())
                        .placeholder(R.drawable.ic_default_avatar)
                        .circleCrop()
                        .into(holder.imgAvatar);
            } else {
                holder.imgAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        } else {
            holder.tvName.setText(fr.getFromId());
            holder.imgAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(fr));
        holder.btnDecline.setOnClickListener(v -> listener.onDecline(fr));
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imgAvatar;
        Button btnAccept, btnDecline;

        RequestViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }
    }
}
