package com.example.nesvie_copyzalo;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestActivity extends AppCompatActivity {

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

        rvFriendRequests = findViewById(R.id.rvFriendRequests);
        backArrow = findViewById(R.id.backArrow);

        dbHelper = new DBHelper(this);

        // Lấy currentUserId từ SharedPreferences hoặc session
        currentUserId = getSharedPreferences("user_session", MODE_PRIVATE)
                .getString("current_user_id", null);

        if (currentUserId == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new FriendRequestAdapter(
                requestList,
                currentUserId,  // String
                dbHelper,       // DBHelper
                new FriendRequestAdapter.OnRequestActionListener() {
                    @Override
                    public void onAccept(FriendRequest request) {
                        acceptRequest(request);
                    }

                    @Override
                    public void onDecline(FriendRequest request) {
                        declineRequest(request);
                    }
                }
        );


        rvFriendRequests.setLayoutManager(new LinearLayoutManager(this));
        rvFriendRequests.setAdapter(adapter);

        backArrow.setOnClickListener(v -> finish());

        loadRequests();
    }

    private void loadRequests() {
        requestList.clear();
        List<FriendRequest> allRequests = dbHelper.getFriendRequestsForUser(currentUserId);

        for (FriendRequest fr : allRequests) {
            if ("pending".equals(fr.getStatus()) && currentUserId.equals(fr.getToId())) {
                requestList.add(fr);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void acceptRequest(FriendRequest request) {
        // 1. Cập nhật bảng friend_requests
        dbHelper.updateFriendRequestStatus(request.getId(), "accepted");

        // 2. Thêm vào bảng friends
        dbHelper.addFriend(currentUserId, request.getFromId());

        // 3. Gửi Socket.io thông báo người gửi
        if (SocketManager.socket() != null) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("fromId", request.getFromId());
                obj.put("toId", currentUserId);
                SocketManager.socket().emit("friendRequestAccepted", obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(this, "Đã chấp nhận kết bạn", Toast.LENGTH_SHORT).show();
        loadRequests();
    }

    private void declineRequest(FriendRequest request) {
        // Xóa lời mời
        dbHelper.deleteFriendRequest(request.getId());

        // Thông báo Socket.io nếu cần
        if (SocketManager.socket() != null) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("fromId", request.getFromId());
                obj.put("toId", currentUserId);
                SocketManager.socket().emit("friendRequestDeclined", obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Toast.makeText(this, "Đã từ chối kết bạn", Toast.LENGTH_SHORT).show();
        loadRequests();
    }
}
