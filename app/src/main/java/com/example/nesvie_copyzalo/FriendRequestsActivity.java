// FriendRequestsActivity.java
package com.example.nesvie_copyzalo;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestsActivity extends AppCompatActivity {

    private RecyclerView rvFriendRequests;
    private FriendRequestAdapter adapter;
    private List<FriendRequest> requestList = new ArrayList<>();
    private DBHelper dbHelper;
    private String currentUserId;
    private ImageView backArrow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        dbHelper = new DBHelper(this);
        currentUserId = getCurrentUserId(); // ví dụ lấy từ SharedPreferences hoặc login session

        rvFriendRequests = findViewById(R.id.rvFriendRequests);
        backArrow = findViewById(R.id.backArrow);

        adapter = new FriendRequestAdapter(requestList, currentUserId, dbHelper, new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request) {
                acceptRequest(request);
            }

            @Override
            public void onDecline(FriendRequest request) {
                declineRequest(request);
            }
        });

        rvFriendRequests.setLayoutManager(new LinearLayoutManager(this));
        rvFriendRequests.setAdapter(adapter);

        backArrow.setOnClickListener(v -> finish());

        loadRequests();
    }

    private void loadRequests() {
        // Lấy danh sách friend requests cho user hiện tại
        requestList.clear();
        requestList.addAll(dbHelper.getFriendRequestsForUser(currentUserId));
        adapter.notifyDataSetChanged();
    }

    private void acceptRequest(FriendRequest request) {
        if (request == null) return;

        // 1. Thêm bạn vào bảng friends
        long result = dbHelper.addFriend(request.getFromId(), request.getToId());
        if (result != -1) {
            // 2. Cập nhật trạng thái friend_request thành accepted
            dbHelper.updateFriendRequestStatus(request.getId(), "accepted");

            // 3. Gửi socket event
            if (SocketManager.socket() != null) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("fromId", request.getFromId());
                    obj.put("toId", request.getToId());
                    SocketManager.socket().emit("friendRequestAccepted", obj);
                } catch (Exception e) { e.printStackTrace(); }
            }

            // 4. Xóa khỏi adapter
            requestList.remove(request);
            adapter.notifyDataSetChanged();
        }
    }

    private void declineRequest(FriendRequest request) {
        if (request == null) return;

        // Xóa request khỏi SQLite
        dbHelper.deleteFriendRequest(request.getId());

        // Xóa khỏi adapter
        requestList.remove(request);
        adapter.notifyDataSetChanged();
    }

    private String getCurrentUserId() {
        // Thay bằng logic lấy userId hiện tại
        return "1"; // ví dụ tạm
    }
}
