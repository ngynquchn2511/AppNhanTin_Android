package com.example.nesvie_copyzalo;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String email;
    private String tenHienThi;
    private String role;
    private String gioiTinh;
    private long createdAt;

    private String password;
    private long lastLoginAt;

    // Thêm trường avatar
    private String avatarUrl; // Có thể là URL online hoặc đường dẫn file local

    // Trường UI (không lưu DB)
    private boolean requested;
    private String friendStatus;

    // ===== Constructors =====
    public User() {}

    public User(String id, String email, String tenHienThi, String role, String gioiTinh,
                String password, long createdAt, long lastLoginAt) {
        this.id = id;
        this.email = email;
        this.tenHienThi = tenHienThi;
        this.role = role;
        this.gioiTinh = gioiTinh;
        this.password = password;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }


    public User(String id, String email, String tenHienThi, String role, String gioiTinh,
                String password, long createdAt, long lastLoginAt, String avatarUrl) {
        this.id = id;
        this.email = email;
        this.tenHienThi = tenHienThi;
        this.role = role;
        this.gioiTinh = gioiTinh;
        this.password = password;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.avatarUrl = avatarUrl;
        this.requested = false;
        this.friendStatus = null;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", tenHienThi='" + tenHienThi + '\'' +
                ", role='" + role + '\'' +
                ", gioiTinh='" + gioiTinh + '\'' +
                ", createdAt=" + createdAt +
                ", lastLoginAt=" + lastLoginAt +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }

    // ===== Getters & Setters =====
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTenHienThi() { return tenHienThi; }
    public void setTenHienThi(String tenHienThi) { this.tenHienThi = tenHienThi; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(long lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    // Avatar
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    // UI only
    public boolean isRequested() { return requested; }
    public void setRequested(boolean requested) { this.requested = requested; }

    public String getFriendStatus() { return friendStatus; }
    public void setFriendStatus(String friendStatus) { this.friendStatus = friendStatus; }
}
