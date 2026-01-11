package com.example.nesvie_copyzalo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddUser extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvUsers;
    private UserAdapter adapter;

    private List<User> userList = new ArrayList<>();
    private List<User> filteredList = new ArrayList<>();
    private Set<String> pendingRequestIds = new HashSet<>();
    private Set<String> acceptedFriendIds = new HashSet<>();

    private DBHelper dbHelper;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        etSearch = findViewById(R.id.etSearch);
        rvUsers = findViewById(R.id.rvUsers);
        dbHelper = new DBHelper(this);

        // Lấy currentUserId từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        currentUserId = prefs.getString("current_user_id", null);
        if (currentUserId == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new UserAdapter(filteredList, this::sendFriendRequest);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Load dữ liệu
        loadFriendshipData();
        loadUsers();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterUsers(s.toString().trim()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Socket lắng nghe khi người khác chấp nhận lời mời
        if (SocketManager.socket() != null) {
            SocketManager.socket().on("friendRequestAccepted", args -> runOnUiThread(() -> {
                try {
                    JSONObject obj = (JSONObject) args[0];
                    String fromId = obj.getString("fromId");
                    String toId = obj.getString("toId");

                    if (currentUserId.equals(fromId)) {
                        // Người nhận đã đồng ý, cập nhật trạng thái
                        pendingRequestIds.remove(toId);
                        acceptedFriendIds.add(toId);
                        filterUsers(etSearch.getText().toString().trim());
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }));
        }
    }

    private void loadFriendshipData() {
        pendingRequestIds.clear();
        acceptedFriendIds.clear();

        // Lấy tất cả friend requests
        List<FriendRequest> requests = dbHelper.getFriendRequestsForUser(currentUserId);
        for (FriendRequest req : requests) {
            if (req.getFromId().equals(currentUserId)) {
                if ("pending".equals(req.getStatus())) pendingRequestIds.add(req.getToId());
                else if ("accepted".equals(req.getStatus())) acceptedFriendIds.add(req.getToId());
            } else if (req.getToId().equals(currentUserId) && "accepted".equals(req.getStatus())) {
                acceptedFriendIds.add(req.getFromId());
            }
        }

        // Lấy danh sách bạn bè thật từ bảng friends
        List<User> friends = dbHelper.getFriends(currentUserId);
        for (User u : friends) {
            acceptedFriendIds.add(u.getId());
        }
    }

    private void loadUsers() {
        userList.clear();
        List<User> allUsers = dbHelper.getAllUsers();
        for (User u : allUsers) {
            if (!u.getId().equals(currentUserId)) {
                if (acceptedFriendIds.contains(u.getId())) u.setRequested(true);
                else if (pendingRequestIds.contains(u.getId())) u.setRequested(true);
                else u.setRequested(false);

                userList.add(u);
            }
        }
        filterUsers("");
    }

    private void filterUsers(String keyword) {
        filteredList.clear();
        keyword = keyword.toLowerCase();

        for (User u : userList) {
            String displayName = u.getTenHienThi() != null ? u.getTenHienThi() : u.getEmail();
            if (displayName != null && displayName.toLowerCase().contains(keyword)) {

                if (acceptedFriendIds.contains(u.getId())) {
                    u.setRequested(true);
                    u.setFriendStatus("Bạn bè");

                    // Ẩn bạn bè khi không gõ gì
                    if (!keyword.isEmpty()) {
                        filteredList.add(u);
                    }
                } else if (pendingRequestIds.contains(u.getId())) {
                    u.setRequested(true);
                    u.setFriendStatus("Đang chờ");
                    filteredList.add(u);
                } else {
                    u.setRequested(false);
                    u.setFriendStatus(null);
                    filteredList.add(u);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }


    private void sendFriendRequest(User user) {
        String targetId = user.getId();

        // 🔹 Nếu đang là bạn bè → hỏi xác nhận hủy kết bạn
        if (acceptedFriendIds.contains(targetId)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(" Hủy kết bạn")
                    .setMessage("Bạn có chắc muốn hủy kết bạn với \"" + user.getTenHienThi() + "\" không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        boolean deleted = dbHelper.deleteFriend(currentUserId, targetId);
                        if (deleted) {
                            acceptedFriendIds.remove(targetId);
                            user.setRequested(false);
                            user.setFriendStatus(null);

                            Toast.makeText(this,
                                    "Đã hủy kết bạn với " + user.getTenHienThi(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    " Lỗi khi hủy kết bạn!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        // 🔹 Nếu đang chờ (đã gửi lời mời) → hỏi xác nhận hủy lời mời
        if (pendingRequestIds.contains(targetId)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Hủy lời mời kết bạn")
                    .setMessage("Bạn có muốn hủy lời mời kết bạn đã gửi đến \"" + user.getTenHienThi() + "\" không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        int deleted = dbHelper.deleteFriendRequestBetween(currentUserId, targetId);
                        if (deleted > 0) {
                            pendingRequestIds.remove(targetId);
                            user.setRequested(false);
                            user.setFriendStatus(null);

                            Toast.makeText(this,
                                    "Đã hủy lời mời kết bạn",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    " Không thể hủy lời mời!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        // 🔹 Nếu chưa là bạn và chưa gửi → gửi lời mời mới
        long requestId = dbHelper.addFriendRequest(currentUserId, targetId);
        if (requestId != -1) {
            pendingRequestIds.add(targetId);
            user.setRequested(true);
            user.setFriendStatus("Đang chờ");
            adapter.notifyDataSetChanged();

            Toast.makeText(this,
                    " Đã gửi lời mời kết bạn đến " + user.getTenHienThi(),
                    Toast.LENGTH_SHORT).show();

            // 🔹 Gửi socket thông báo (nếu có)
            if (SocketManager.socket() != null) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("fromId", currentUserId);
                    obj.put("toId", targetId);
                    SocketManager.socket().emit("friendRequestSent", obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this,
                    " Gửi lời mời thất bại!",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
