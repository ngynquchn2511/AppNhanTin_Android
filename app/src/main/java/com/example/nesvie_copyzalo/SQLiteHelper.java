package com.example.nesvie_copyzalo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "zalo_app.db";
    private static final int DB_VERSION = 1;

    public SQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id TEXT PRIMARY KEY," +
                "displayName TEXT," +
                "email TEXT," +
                "role TEXT)");

        db.execSQL("CREATE TABLE friend_requests (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "fromUserId TEXT," +
                "toUserId TEXT," +
                "status TEXT," +      // pending | accepted | declined
                "timestamp INTEGER)");

        db.execSQL("CREATE TABLE friends (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId TEXT," +          // current user
                "friendId TEXT," +        // bạn bè
                "timestamp INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS friend_requests");
        db.execSQL("DROP TABLE IF EXISTS friends");
        onCreate(db);
    }

    // Thêm user
    public void insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", user.getId());
        values.put("displayName", user.getTenHienThi());
        values.put("email", user.getEmail());
        values.put("role", user.getRole());
        db.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // Gửi lời mời kết bạn
    public void sendFriendRequest(String fromUser, String toUser) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fromUserId", fromUser);
        values.put("toUserId", toUser);
        values.put("status", "pending");
        values.put("timestamp", System.currentTimeMillis());
        db.insert("friend_requests", null, values);
    }

    // Lấy danh sách lời mời đến current user
    public List<User> getIncomingRequests(String currentUserId) {
        List<User> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u.id, u.displayName, u.email, u.role " +
                "FROM friend_requests fr " +
                "JOIN users u ON fr.fromUserId = u.id " +
                "WHERE fr.toUserId=? AND fr.status='pending'";
        Cursor cursor = db.rawQuery(query, new String[]{currentUserId});
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getString(0));
                user.setTenHienThi(cursor.getString(1));
                user.setEmail(cursor.getString(2));
                user.setRole(cursor.getString(3));
                requests.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return requests;
    }

    // Accept lời mời
    public void acceptRequest(String fromUser, String toUser) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Update request thành accepted
        db.execSQL("UPDATE friend_requests SET status='accepted' WHERE fromUserId=? AND toUserId=?",
                new Object[]{fromUser, toUser});
        // Thêm bạn bè 2 chiều
        ContentValues v1 = new ContentValues();
        v1.put("userId", fromUser);
        v1.put("friendId", toUser);
        v1.put("timestamp", System.currentTimeMillis());
        db.insert("friends", null, v1);

        ContentValues v2 = new ContentValues();
        v2.put("userId", toUser);
        v2.put("friendId", fromUser);
        v2.put("timestamp", System.currentTimeMillis());
        db.insert("friends", null, v2);
    }

    // Decline lời mời
    public void declineRequest(String fromUser, String toUser) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE friend_requests SET status='declined' WHERE fromUserId=? AND toUserId=?",
                new Object[]{fromUser, toUser});
    }

    // Lấy danh sách bạn bè
    public List<User> getFriends(String userId) {
        List<User> friends = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT u.id, u.displayName, u.email, u.role " +
                "FROM friends f JOIN users u ON f.friendId = u.id " +
                "WHERE f.userId=?";
        Cursor cursor = db.rawQuery(query, new String[]{userId});
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getString(0));
                user.setTenHienThi(cursor.getString(1));
                user.setEmail(cursor.getString(2));
                user.setRole(cursor.getString(3));
                friends.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return friends;
    }
}
