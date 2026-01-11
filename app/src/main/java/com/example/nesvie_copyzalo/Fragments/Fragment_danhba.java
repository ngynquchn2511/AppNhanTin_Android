package com.example.nesvie_copyzalo.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nesvie_copyzalo.ChatActivity;
import com.example.nesvie_copyzalo.FriendRequestActivity;
import com.example.nesvie_copyzalo.R;
import com.example.nesvie_copyzalo.User;
import com.example.nesvie_copyzalo.DBHelper;
import com.example.nesvie_copyzalo.SocketManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Fragment_danhba extends Fragment {

    private RecyclerView rvFriends;
    private FriendAdapter friendAdapter;
    private List<User> friendList = new ArrayList<>();
    private String currentUserId;
    private DBHelper dbHelper;
    private TextView tvFriendRequests;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_fragment_danhba, container, false);

        rvFriends = v.findViewById(R.id.rvFriends);
        tvFriendRequests = v.findViewById(R.id.tvFriendRequests);

        dbHelper = new DBHelper(getContext());

        // Lấy current user id từ SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences("user_session", getContext().MODE_PRIVATE);
        currentUserId = prefs.getString("current_user_id", null);
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return v;
        }

        friendAdapter = new FriendAdapter(friendList, this::startChat);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriends.setAdapter(friendAdapter);

        // Xem lời mời kết bạn
        tvFriendRequests.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), FriendRequestActivity.class);
            startActivity(intent);
        });

        loadFriends();

        // Socket lắng nghe khi có ai đó đồng ý kết bạn
        if (SocketManager.socket() != null) {
            SocketManager.socket().on("friendRequestAccepted", args -> getActivity().runOnUiThread(() -> {
                try {
                    JSONObject obj = (JSONObject) args[0];
                    String fromId = obj.getString("fromId");
                    String toId = obj.getString("toId");

                    // Nếu currentUser là người gửi hoặc nhận, reload danh sách bạn bè
                    if (currentUserId.equals(fromId) || currentUserId.equals(toId)) {
                        loadFriends();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        return v;
    }

    private void loadFriends() {
        friendList.clear();
        List<User> friends = dbHelper.getFriends(currentUserId);
        if (friends != null) friendList.addAll(friends);
        friendAdapter.notifyDataSetChanged();
    }

    private void startChat(User friend) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("friendUser", friend);
        startActivity(intent);
    }

    // Adapter cho danh sách bạn bè
    private static class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

        public interface OnChatClickListener {
            void onChatClick(User friend);
        }

        private List<User> friendList;
        private OnChatClickListener chatListener;

        public FriendAdapter(List<User> friendList, OnChatClickListener chatListener) {
            this.friendList = friendList;
            this.chatListener = chatListener;
        }

        @NonNull
        @Override
        public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_friend, parent, false);
            return new FriendViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
            User friend = friendList.get(position);

            String displayName = friend.getTenHienThi() != null ? friend.getTenHienThi() : friend.getEmail();
            holder.tvFriendName.setText(displayName);

            // Hiển thị email
            holder.tvFriendEmail.setText(friend.getEmail());

            holder.btnChat.setOnClickListener(v -> {
                if (chatListener != null) {
                    chatListener.onChatClick(friend);
                }
            });

            holder.itemView.setOnClickListener(v -> {
                if (chatListener != null) {
                    chatListener.onChatClick(friend);
                }
            });
        }


        @Override
        public int getItemCount() {
            return friendList.size();
        }


            static class FriendViewHolder extends RecyclerView.ViewHolder {
                TextView tvFriendName, tvFriendEmail;
                TextView btnChat;

                FriendViewHolder(View itemView) {
                    super(itemView);
                    tvFriendName = itemView.findViewById(R.id.tvFriendName);
                    tvFriendEmail = itemView.findViewById(R.id.tvFriendEmail);
                    btnChat = itemView.findViewById(R.id.btnChat);
                }
            }

        }
    }

