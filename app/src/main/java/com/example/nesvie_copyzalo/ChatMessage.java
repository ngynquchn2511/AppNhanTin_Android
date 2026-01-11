package com.example.nesvie_copyzalo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    private int id; // ID tự tăng, bắt buộc phải có PrimaryKey
    private boolean isEmoji;
    private String fromUser;
    private String toUser;
    private String text;
    private String chatRoomId;
    private long timestamp;
    private String messageId;
    private boolean isRead;
    private String imageUri; // ✅ chuẩn với DB

    private String status;

    // Constructor
    public ChatMessage(String fromUser, String toUser, String text, String chatRoomId) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.text = text;
        this.chatRoomId = chatRoomId;
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFromUser() { return fromUser; }
    public void setFromUser(String fromUser) { this.fromUser = fromUser; }

    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getChatRoomId() { return chatRoomId; }
    public void setChatRoomId(String chatRoomId) { this.chatRoomId = chatRoomId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public boolean isEmoji() { return isEmoji; }
    public void setEmoji(boolean emoji) { isEmoji = emoji; }
}
