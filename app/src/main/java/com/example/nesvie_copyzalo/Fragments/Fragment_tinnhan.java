package com.example.nesvie_copyzalo.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nesvie_copyzalo.ChatActivity;
import com.example.nesvie_copyzalo.DBHelper;
import com.example.nesvie_copyzalo.R;
import com.example.nesvie_copyzalo.User;
import com.example.nesvie_copyzalo.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Fragment_tinnhan extends Fragment {

    private RecyclerView rvChats;
    private ChatListAdapter adapter;
    private List<ChatItem> chatList = new ArrayList<>();
    private String currentUserId;
    private DBHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_tin_nhan, container, false);

        rvChats = v.findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DBHelper(getContext());

        SharedPreferences prefs = getContext().getSharedPreferences("user_session", Activity.MODE_PRIVATE);
        currentUserId = prefs.getString("current_user_id", null);

        adapter = new ChatListAdapter(chatList, this::openChat);
        rvChats.setAdapter(adapter);

        loadChatListFromSQLite();

        return v;
    }

    private void openChat(ChatItem chatItem) {
        // Trước khi mở activity, đánh dấu tất cả tin nhắn từ friend là đã đọc
        dbHelper.markMessagesRead(chatItem.getChatRoomId(), currentUserId);

        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("currentUserId", currentUserId);
        intent.putExtra("friendId", chatItem.getUser().getId());
        startActivity(intent);
    }


    private void loadChatListFromSQLite() {
        chatList.clear();
        List<User> friends = dbHelper.getFriends(currentUserId);

        for (User friend : friends) {
            String chatRoomId = getChatRoomId(currentUserId, friend.getId());
            ChatMessage lastMsg = dbHelper.getLastMessage(chatRoomId);

            ChatItem chatItem = new ChatItem();
            chatItem.setUser(friend);
            chatItem.setChatRoomId(chatRoomId);

            if (lastMsg != null) {
                String prefix = lastMsg.getFromUser().equals(currentUserId) ? "Bạn: " : "";

                // ✅ Xử lý preview tin nhắn ảnh
                String preview;
                if (lastMsg.getImageUri() != null && !lastMsg.getImageUri().isEmpty()) {
                    preview = "Đã gửi 1 ảnh";
                } else if (lastMsg.getText() != null && !lastMsg.getText().isEmpty()) {
                    preview = lastMsg.getText();
                } else {
                    preview = "Đã gửi tin nhắn";
                }

                chatItem.setLastMessage(prefix + preview);
                chatItem.setLastMessageTime(lastMsg.getTimestamp());
                chatItem.setUnread(!lastMsg.isRead() && !lastMsg.getFromUser().equals(currentUserId));
            } else {
                chatItem.setLastMessage("Bắt đầu trò chuyện");
                chatItem.setLastMessageTime(0);
                chatItem.setUnread(false);
            }


            chatList.add(chatItem);
        }

        Collections.sort(chatList, (a, b) -> Long.compare(b.getLastMessageTime(), a.getLastMessageTime()));
        adapter.notifyDataSetChanged();
    }



    @Override
    public void onResume() {
        super.onResume();
        loadChatListFromSQLite(); // reload để cập nhật trạng thái đọc
    }

    private String getChatRoomId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    // ==================== ChatItem ====================
    public static class ChatItem {
        private User user;
        private String lastMessage;
        private long lastMessageTime;
        private String chatRoomId;
        private boolean unread;

        public User getUser() { return user; }
        public void setUser(User user) { this.user = user; }

        public String getLastMessage() { return lastMessage; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

        public long getLastMessageTime() { return lastMessageTime; }
        public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

        public String getChatRoomId() { return chatRoomId; }
        public void setChatRoomId(String chatRoomId) { this.chatRoomId = chatRoomId; }

        public boolean isUnread() { return unread; }
        public void setUnread(boolean unread) { this.unread = unread; }
    }

    // ==================== Adapter ====================
    private static class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

        public interface OnChatClickListener {
            void onChatClick(ChatItem chatItem);
        }

        private List<ChatItem> chatList;
        private OnChatClickListener clickListener;

        public ChatListAdapter(List<ChatItem> chatList, OnChatClickListener clickListener) {
            this.chatList = chatList;
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_preview, parent, false);
            return new ChatViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatItem item = chatList.get(position);
            User friend = item.getUser();

            holder.tvFriendName.setText(
                    friend.getTenHienThi() != null && !friend.getTenHienThi().isEmpty()
                            ? friend.getTenHienThi() : friend.getEmail()
            );

            holder.tvLastMessage.setText(item.getLastMessage());

            // Tin nhắn chưa đọc → in đậm + màu đen
            if (item.isUnread()) {
                holder.tvLastMessage.setTypeface(null, android.graphics.Typeface.BOLD);
                holder.tvLastMessage.setTextColor(0xFF000000); // màu đen
            } else {
                holder.tvLastMessage.setTypeface(null, android.graphics.Typeface.NORMAL);
                holder.tvLastMessage.setTextColor(0xFF555555); // màu xám
            }

            if (item.getLastMessageTime() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                holder.tvTime.setText(sdf.format(item.getLastMessageTime()));
            } else {
                holder.tvTime.setText("");
            }

            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onChatClick(item);
                }
            });
        }

        @Override
        public int getItemCount() { return chatList.size(); }

        static class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView tvFriendName, tvLastMessage, tvTime;

            ChatViewHolder(View itemView) {
                super(itemView);
                tvFriendName = itemView.findViewById(R.id.tvFriendName);
                tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
                tvTime = itemView.findViewById(R.id.tvTime);
            }
        }
    }
}
