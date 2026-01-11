package com.example.nesvie_copyzalo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "friend_requests")
public class FriendRequest {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String fromId;
    private String toId;
    private String status;   // pending / accepted / rejected
    private long timestamp;

    public FriendRequest() {}

    // Constructor tạo mới (auto timestamp)
    public FriendRequest(String fromId, String toId, String status) {
        this.fromId = fromId;
        this.toId = toId;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    // Constructor đầy đủ (DBHelper đang gọi cái này)
    public FriendRequest(int id, String fromId, String toId, String status, long timestamp) {
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
        this.status = status;
        this.timestamp = timestamp;
    }

    // ===== Getter & Setter =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFromId() { return fromId; }
    public void setFromId(String fromId) { this.fromId = fromId; }

    public String getToId() { return toId; }
    public void setToId(String toId) { this.toId = toId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
