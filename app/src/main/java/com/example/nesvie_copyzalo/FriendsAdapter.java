package com.example.nesvie_copyzalo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private final List<User> friends;
    private final Context context;

    public FriendsAdapter(List<User> friends, Context context) {
        this.friends = friends;
        this.context = context;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friends.get(position);

        // Hiển thị tên hiển thị hoặc email
        String displayName = (friend.getTenHienThi() != null && !friend.getTenHienThi().isEmpty())
                ? friend.getTenHienThi()
                : friend.getEmail();

        holder.tvName.setText(displayName != null ? displayName : "Người dùng");
        holder.tvEmail.setText(friend.getEmail() != null ? friend.getEmail() : "");

        // Xử lý khi bấm nút Chat
        holder.btnChat.setOnClickListener(v -> {
            String currentUserId = getCurrentUserId();
            String friendId = friend.getId();

            // ✅ Log debug ngay từ đầu
            Log.d("FriendsAdapter", "🔍 BEFORE CHECK - currentUserId = '" + currentUserId + "'");
            Log.d("FriendsAdapter", "🔍 BEFORE CHECK - friendId = '" + friendId + "'");
            Log.d("FriendsAdapter", "🔍 Friend object = " + friend.toString());

            if (currentUserId == null || currentUserId.trim().isEmpty()) {
                Log.e("FriendsAdapter", "❌ currentUserId null/empty");
                Toast.makeText(context, "Lỗi: Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (friendId == null || friendId.trim().isEmpty()) {
                Log.e("FriendsAdapter", "❌ friendId null/empty cho user: " + friend.getEmail());
                Toast.makeText(context, "Lỗi: Không tìm thấy ID của bạn bè!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Trim data
            currentUserId = currentUserId.trim();
            friendId = friendId.trim();

            // ✅ Kiểm tra nếu currentUserId và friendId giống nhau
            if (currentUserId.equals(friendId)) {
                Toast.makeText(context, "Không thể chat với chính mình!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Log final data trước khi tạo Intent
            Log.d("FriendsAdapter", "✅ FINAL DATA - currentUserId = '" + currentUserId + "'");
            Log.d("FriendsAdapter", "✅ FINAL DATA - friendId = '" + friendId + "'");

            try {
                Intent intent = new Intent(context, ChatActivity.class);

                // ✅ QUAN TRỌNG: Chỉ gửi String data, KHÔNG gửi object User
                intent.putExtra("currentUserId", currentUserId);
                intent.putExtra("friendId", friendId);

                // ✅ KHÔNG làm điều này: intent.putExtra("friendUser", friend);

                // ✅ Log Intent extras để verify
                Log.d("FriendsAdapter", "📤 Intent extras - currentUserId: '" + intent.getStringExtra("currentUserId") + "'");
                Log.d("FriendsAdapter", "📤 Intent extras - friendId: '" + intent.getStringExtra("friendId") + "'");

                // ✅ Thêm flag nếu context không phải Activity
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Log.d("FriendsAdapter", "🚩 Added FLAG_ACTIVITY_NEW_TASK");
                }

                Log.d("FriendsAdapter", "🚀 Starting ChatActivity...");
                context.startActivity(intent);

            } catch (Exception e) {
                Log.e("FriendsAdapter", "❌ Exception khi start ChatActivity: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(context, "Lỗi khi mở chat: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Click vào cả item cũng mở chat
        holder.itemView.setOnClickListener(v -> holder.btnChat.performClick());
    }

    @Override
    public int getItemCount() {
        return friends != null ? friends.size() : 0;
    }

    /** Lấy user đang đăng nhập từ SharedPreferences */
    private String getCurrentUserId() {
        try {
            SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
            String userId = prefs.getString("current_user_id", null);
            Log.d("FriendsAdapter", "📱 getCurrentUserId = " + userId);
            return userId;
        } catch (Exception e) {
            Log.e("FriendsAdapter", "❌ Lỗi khi lấy currentUserId: " + e.getMessage());
            return null;
        }
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        Button btnChat;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFriendName);
            tvEmail = itemView.findViewById(R.id.tvFriendEmail);
            btnChat = itemView.findViewById(R.id.btnChat);
        }
    }
}