package com.example.nesvie_copyzalo;

public class LoginHistory {
    private int id;
    private String userId;
    private String device;
    private long loginTime;

    public LoginHistory() {}

    public LoginHistory(String userId, String device, long loginTime) {
        this.userId = userId;
        this.device = device;
        this.loginTime = loginTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }

    public long getLoginTime() { return loginTime; }
    public void setLoginTime(long loginTime) { this.loginTime = loginTime; }
}
