package com.example.nesvie_copyzalo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 101;

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private EditText etMessage;
    private ImageButton btnSend, btnAttach;
    private ImageView btnBack;
    private TextView tvChatTitle;

    private String currentUserId;
    private String friendId;
    private String chatRoomId;

    private DBHelper dbHelper;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dbHelper = new DBHelper(this);

        recyclerView = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnAttach = findViewById(R.id.btnAttach);
        btnBack = findViewById(R.id.btnBack);
        tvChatTitle = findViewById(R.id.tvChatTitle);

        btnBack.setOnClickListener(v -> finish());

        currentUserId = getIntent().getStringExtra("currentUserId");
        friendId = getIntent().getStringExtra("friendId");

        // Hiển thị tên bạn bè lên tiêu đề
        User friend = dbHelper.getUserById(friendId);
        if (friend != null && friend.getTenHienThi() != null && !friend.getTenHienThi().isEmpty()) {
            tvChatTitle.setText(friend.getTenHienThi());
        } else {
            tvChatTitle.setText("Chat");
        }

        if (currentUserId == null) {
            SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
            currentUserId = prefs.getString("current_user_id", null);
        }

        if (currentUserId == null || friendId == null) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin người dùng!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        chatRoomId = getChatRoomId(currentUserId, friendId);

        // Load tin nhắn
        List<ChatMessage> messages = dbHelper.getMessages(chatRoomId);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ChatAdapter(messages, currentUserId);
        recyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                etMessage.setText("");
            }
        });

        btnAttach.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh hoặc file"), PICK_FILE_REQUEST);
        });
    }

    private void sendMessage(String text) {
        ChatMessage chatMsg = new ChatMessage(currentUserId, friendId, text, chatRoomId);
        chatMsg.setTimestamp(System.currentTimeMillis());
        chatMsg.setStatus("sending");

        dbHelper.addMessage(chatMsg);
        adapter.addMessage(chatMsg);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void sendFileMessage(Uri fileUri) {
        if (fileUri == null) return;

        ChatMessage chatMsg = new ChatMessage(currentUserId, friendId, null, chatRoomId);
        chatMsg.setImageUri(fileUri.toString());
        chatMsg.setTimestamp(System.currentTimeMillis());
        chatMsg.setStatus("sending");

        dbHelper.addMessage(chatMsg);
        adapter.addMessage(chatMsg);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            sendFileMessage(fileUri);
        }
    }

    private String getChatRoomId(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}
