package com.example.nesvie_copyzalo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chat_app.db";
    private static final int DATABASE_VERSION = 15;

    // Bảng users
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";

    // Bảng messages
    private static final String TABLE_MESSAGES = "messages";
    private static final String COL_MSG_ID = "id";
    private static final String COL_FROM = "fromUser";
    private static final String COL_TO = "toUser";
    private static final String COL_TEXT = "text";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_ROOM = "chatRoomId";
    private static final String COL_IMAGE = "imageUri";

    private static final String TABLE_LOGIN_HISTORY = "login_history";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_IP_ADDRESS = "ip_address";
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bảng users
        String CREATE_USERS_TABLE = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE, " +
                "tenHienThi TEXT, " +
                "role TEXT, " +
                "gioiTinh TEXT, " +
                "password TEXT, " +
                "createdAt INTEGER, " +
                "lastLoginAt INTEGER)";
        db.execSQL(CREATE_USERS_TABLE);

        // Bảng messages
        String CREATE_MESSAGES_TABLE = "CREATE TABLE messages (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "messageId TEXT UNIQUE, " +       // UUID
                "fromUser TEXT, " +
                "toUser TEXT, " +
                "text TEXT, " +
                "imageUri TEXT," +
                "timestamp INTEGER, " +
                "chatRoomId TEXT, " +
                "isRead INTEGER DEFAULT 0, " +   // 0 = chưa đọc, 1 = đã đọc
                "status TEXT DEFAULT 'sending'" +
                ")";
        db.execSQL(CREATE_MESSAGES_TABLE);

        // Bảng friend_requests
        String CREATE_FRIEND_REQUESTS_TABLE = "CREATE TABLE friend_requests (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fromId TEXT, " +
                "toId TEXT, " +
                "status TEXT, " +
                "timestamp INTEGER)";
        db.execSQL(CREATE_FRIEND_REQUESTS_TABLE);

        // Bảng friends
        String CREATE_FRIENDS_TABLE = "CREATE TABLE friends (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user1_id TEXT, " +
                "user2_id TEXT, " +
                "createdAt INTEGER DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_FRIENDS_TABLE);

        // Bảng user_activity
        String createActivityTable = "CREATE TABLE IF NOT EXISTS user_activity (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "userId TEXT," +
                "timestamp LONG," +
                "durationMinutes INTEGER)";
        db.execSQL(createActivityTable);

        // Bảng login_history
        String createLoginHistoryTable = "CREATE TABLE " + TABLE_LOGIN_HISTORY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +   // thêm id
                "userId TEXT, " +
                "device TEXT, " +                            // thêm device
                "loginTime INTEGER)";                        // thay cho timestamp

        db.execSQL(createLoginHistoryTable);

        // Tạo admin
        ContentValues admin = new ContentValues();
        admin.put("email", "admin@gmail.com");
        admin.put("tenHienThi", "Admin");
        admin.put("role", "admin");
        admin.put("gioiTinh", "Nam");
        admin.put("password", "123456");
        admin.put("createdAt", System.currentTimeMillis());
        admin.put("lastLoginAt", System.currentTimeMillis());

        long adminId = db.insert("users", null, admin);
        if (adminId > 0) {
            fakeUserActivityData(String.valueOf(adminId), db);
        }
    }

    public List<Integer> getFakeActivityChart(String type) {
        List<Integer> data = new ArrayList<>();

        switch (type) {
            case "ngay": // 24 giờ trong ngày
                for (int i = 0; i < 24; i++) {
                    if (i >= 0 && i < 6) {
                        data.add((int)(Math.random() * 10)); // Đêm: 0-10 phút
                    } else if (i >= 6 && i < 9) {
                        data.add(10 + (int)(Math.random() * 20)); // Sáng sớm: 10-30 phút
                    } else if (i >= 9 && i < 18) {
                        data.add(20 + (int)(Math.random() * 40)); // Giờ làm việc: 20-60 phút
                    } else if (i >= 18 && i < 23) {
                        data.add(30 + (int)(Math.random() * 50)); // Tối: 30-80 phút
                    } else {
                        data.add(5 + (int)(Math.random() * 15)); // Đêm khuya: 5-20 phút
                    }
                }
                break;

            case "tuan": // 7 ngày trong tuần
                for (int i = 0; i < 7; i++) {
                    if (i == 0 || i == 6) {
                        data.add(100 + (int)(Math.random() * 100)); // Cuối tuần: 100-200 phút
                    } else {
                        data.add(50 + (int)(Math.random() * 80)); // Ngày thường: 50-130 phút
                    }
                }
                break;

            case "thang": // 30 ngày trong tháng
                for (int i = 0; i < 30; i++) {
                    data.add(30 + (int)(Math.random() * 90)); // 30-120 phút/ngày
                }
                break;

            case "nam": // 12 tháng trong năm
                for (int i = 0; i < 12; i++) {
                    data.add(1800 + (int)(Math.random() * 900)); // 1800-2700 phút/tháng
                }
                break;
        }

        return data;
    }
    // =========================
// Fake dữ liệu user_activity
// =========================
    public void fakeUserActivityData(String userId, SQLiteDatabase db) {
        db.beginTransaction();
        try {
            Calendar cal = Calendar.getInstance();

            // 7 ngày gần nhất
            for (int i = 6; i >= 0; i--) {
                cal.setTimeInMillis(System.currentTimeMillis());
                cal.add(Calendar.DAY_OF_YEAR, -i);
                long timestamp = cal.getTimeInMillis();
                int duration = (int)(Math.random() * 120); // phút
                ContentValues values = new ContentValues();
                values.put("userId", userId);
                values.put("timestamp", timestamp);
                values.put("durationMinutes", duration);
                db.insert("user_activity", null, values);
            }

            // 4 tuần gần nhất
            for (int i = 3; i >= 0; i--) {
                cal.setTimeInMillis(System.currentTimeMillis());
                cal.add(Calendar.WEEK_OF_YEAR, -i);
                long timestamp = cal.getTimeInMillis();
                int duration = (int)(Math.random() * 600); // tổng phút tuần
                ContentValues values = new ContentValues();
                values.put("userId", userId);
                values.put("timestamp", timestamp);
                values.put("durationMinutes", duration);
                db.insert("user_activity", null, values);
            }

            // 30 ngày gần nhất
            for (int i = 29; i >= 0; i--) {
                cal.setTimeInMillis(System.currentTimeMillis());
                cal.add(Calendar.DAY_OF_YEAR, -i);
                long timestamp = cal.getTimeInMillis();
                int duration = (int)(Math.random() * 180); // phút
                ContentValues values = new ContentValues();
                values.put("userId", userId);
                values.put("timestamp", timestamp);
                values.put("durationMinutes", duration);
                db.insert("user_activity", null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }




    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS friend_requests");
        db.execSQL("DROP TABLE IF EXISTS friends");
        db.execSQL("DROP TABLE IF EXISTS user_activity");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN_HISTORY);

        onCreate(db);


    }


    public void addLoginHistory(String userId, String device, long loginTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userId", userId);
        values.put("device", device);
        values.put("loginTime", loginTime);
        db.insert("login_history", null, values);
        db.close();
    }

    // Lấy toàn bộ lịch sử đăng nhập của 1 user
    public List<LoginHistory> getLoginHistory(String userId) {
        List<LoginHistory> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, userId, device, loginTime FROM login_history WHERE userId = ? ORDER BY loginTime DESC",
                new String[]{userId});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                LoginHistory history = new LoginHistory();
                history.setId(cursor.getInt(0));
                history.setUserId(cursor.getString(1));
                history.setDevice(cursor.getString(2));
                history.setLoginTime(cursor.getLong(3));
                list.add(history);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return list;
    }
    // Thêm helper load messages theo 2 người
    public List<ChatMessage> getChatMessages(String currentUserId, String friendId) {
        List<ChatMessage> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM messages WHERE " +
                "((fromUser=? AND toUser=?) OR (fromUser=? AND toUser=?)) " +
                "ORDER BY timestamp ASC";

        Cursor cursor = db.rawQuery(query, new String[]{currentUserId, friendId, friendId, currentUserId});
        if (cursor.moveToFirst()) {
            do {
                ChatMessage msg = new ChatMessage(
                        cursor.getString(cursor.getColumnIndexOrThrow("fromUser")),
                        cursor.getString(cursor.getColumnIndexOrThrow("toUser")),
                        cursor.getString(cursor.getColumnIndexOrThrow("text")),
                        cursor.getString(cursor.getColumnIndexOrThrow("chatRoomId"))
                );
                msg.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));
                list.add(msg);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public long addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);
        return db.insert(TABLE_USERS, null, values);
    }

    public void addUserActivity(String userId, long timestamp, int durationMinutes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userId", userId);
        values.put("timestamp", timestamp);
        values.put("durationMinutes", durationMinutes);
        db.insert("user_activity", null, values);
        db.close();
    }

    public List<ChatMessage> getUnreadMessages(String chatRoomId, String friendId, String currentUserId) {
        List<ChatMessage> unread = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM messages WHERE chatRoomId=? AND fromUser=? AND toUser=? AND isRead=0 ORDER BY timestamp ASC";
        Cursor cursor = db.rawQuery(query, new String[]{chatRoomId, friendId, currentUserId});

        if (cursor.moveToFirst()) {
            do {
                ChatMessage msg = new ChatMessage(
                        cursor.getString(cursor.getColumnIndexOrThrow("fromUser")),
                        cursor.getString(cursor.getColumnIndexOrThrow("toUser")),
                        cursor.getString(cursor.getColumnIndexOrThrow("text")),
                        cursor.getString(cursor.getColumnIndexOrThrow("chatRoomId"))
                );
                msg.setMessageId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                msg.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")));
                msg.setRead(cursor.getInt(cursor.getColumnIndexOrThrow("isRead")) != 0);
                unread.add(msg);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return unread;
    }

    public void markMessagesRead(String chatRoomId, String currentUserId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("isRead", 1);
        // Chỉ đánh dấu tin nhắn từ friend (không phải do mình gửi)
        db.update("messages", cv, "chatRoomId=? AND fromUser<>?", new String[]{chatRoomId, currentUserId});
    }

    // Thêm bạn bè (khi đồng ý kết bạn)
    public long addFriend(String user1Id, String user2Id) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (areFriends(user1Id, user2Id)) {
            db.close();
            return -1; // Đã tồn tại
        }
        // Sort để tránh duplicate (user1 < user2)
        String minId = user1Id.compareTo(user2Id) < 0 ? user1Id : user2Id;
        String maxId = user1Id.compareTo(user2Id) < 0 ? user2Id : user1Id;
        ContentValues values = new ContentValues();
        values.put("user1_id", minId);
        values.put("user2_id", maxId);
        values.put("createdAt", System.currentTimeMillis());
        long result = db.insert("friends", null, values);

        return result;
    }

    // Kiểm tra 2 người đã là bạn chưa
    public boolean areFriends(String user1Id, String user2Id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM friends WHERE (user1_id=? AND user2_id=?) OR (user1_id=? AND user2_id=?)",
                new String[]{user1Id, user2Id, user2Id, user1Id});
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();

        return exists;
    }

    // Lấy danh sách bạn bè của 1 người
    // DBHelper.java
    // DBHelper.java
    // Thay thế method getFriends trong DBHelper.java
    public List<User> getFriends(String currentUserId) {
        List<User> friends = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT u.id, u.email, u.tenHienThi, u.role, u.gioiTinh, u.password, " +
                        "u.createdAt, u.lastLoginAt " +
                        "FROM users u " +
                        "JOIN friends f ON (f.user1_id = u.id OR f.user2_id = u.id) " +
                        "WHERE (f.user1_id = ? OR f.user2_id = ?) AND u.id != ?";

        Cursor cursor = db.rawQuery(query, new String[]{currentUserId, currentUserId, currentUserId});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // ✅ Lấy id và convert thành String an toàn
                String id;
                try {
                    // Thử lấy dưới dạng String trước
                    id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                    if (id == null) {
                        // Nếu null, thử lấy dưới dạng int rồi convert
                        int intId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                        id = String.valueOf(intId);
                    }
                } catch (Exception e) {
                    Log.e("DBHelper", " Lỗi khi lấy id: " + e.getMessage());
                    continue; // Bỏ qua record này nếu không lấy được id
                }


                if (id == null || id.trim().isEmpty()) {
                    Log.w("DBHelper", "⚠ ID null/empty, bỏ qua user này");
                    continue;
                }

                User user = new User(
                        id.trim(),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("tenHienThi")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role")),
                        cursor.getString(cursor.getColumnIndexOrThrow("gioiTinh")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("lastLoginAt"))
                );

                friends.add(user);


                Log.d("DBHelper", " Friend loaded: id=" + id + ", email=" + user.getEmail());
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        Log.d("DBHelper", "📊 Total friends loaded: " + friends.size());
        return friends;
    }
    public List<ActivityRecord> getActivityData(String userId, String period) {
        List<ActivityRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        Calendar cal = Calendar.getInstance();

        try {
            switch (period) {
                case "Ngày": // 7 ngày gần nhất
                    for (int i = 6; i >= 0; i--) {
                        cal.setTimeInMillis(System.currentTimeMillis());
                        cal.add(Calendar.DAY_OF_YEAR, -i);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);

                        long start = cal.getTimeInMillis();
                        cal.add(Calendar.DAY_OF_YEAR, 1);
                        long end = cal.getTimeInMillis();

                        cursor = db.rawQuery(
                                "SELECT SUM(durationMinutes) FROM user_activity WHERE userId=? AND timestamp>=? AND timestamp<?",
                                new String[]{userId, String.valueOf(start), String.valueOf(end)}
                        );

                        int duration = 0;
                        if (cursor.moveToFirst()) {
                            duration = cursor.isNull(0) ? 0 : cursor.getInt(0);
                        }
                        cursor.close();

                        cal.add(Calendar.DAY_OF_YEAR, -1); // Reset lại
                        list.add(new ActivityRecord(sdf.format(new Date(start)), duration));
                    }
                    break;

                case "Tuần": // 4 tuần gần nhất
                    for (int i = 3; i >= 0; i--) {
                        cal.setTimeInMillis(System.currentTimeMillis());
                        cal.add(Calendar.WEEK_OF_YEAR, -i);
                        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);

                        long start = cal.getTimeInMillis();
                        cal.add(Calendar.WEEK_OF_YEAR, 1);
                        long end = cal.getTimeInMillis();

                        cursor = db.rawQuery(
                                "SELECT SUM(durationMinutes) FROM user_activity WHERE userId=? AND timestamp>=? AND timestamp<?",
                                new String[]{userId, String.valueOf(start), String.valueOf(end)}
                        );

                        int duration = 0;
                        if (cursor.moveToFirst()) {
                            duration = cursor.isNull(0) ? 0 : cursor.getInt(0);
                        }
                        cursor.close();

                        String label = sdf.format(new Date(start)) + "-" + sdf.format(new Date(end - 86400000));
                        list.add(new ActivityRecord(label, duration));
                    }
                    break;

                case "Tháng": // 30 ngày gần nhất
                    for (int i = 29; i >= 0; i--) {
                        cal.setTimeInMillis(System.currentTimeMillis());
                        cal.add(Calendar.DAY_OF_YEAR, -i);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);

                        long start = cal.getTimeInMillis();
                        cal.add(Calendar.DAY_OF_YEAR, 1);
                        long end = cal.getTimeInMillis();

                        cursor = db.rawQuery(
                                "SELECT SUM(durationMinutes) FROM user_activity WHERE userId=? AND timestamp>=? AND timestamp<?",
                                new String[]{userId, String.valueOf(start), String.valueOf(end)}
                        );

                        int duration = 0;
                        if (cursor.moveToFirst()) {
                            duration = cursor.isNull(0) ? 0 : cursor.getInt(0);
                        }
                        cursor.close();

                        cal.add(Calendar.DAY_OF_YEAR, -1); // Reset lại
                        list.add(new ActivityRecord(sdf.format(new Date(start)), duration));
                    }
                    break;

                case "Năm": // 12 tháng gần nhất
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy");
                    for (int i = 11; i >= 0; i--) {
                        cal.setTimeInMillis(System.currentTimeMillis());
                        cal.add(Calendar.MONTH, -i);
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);

                        long start = cal.getTimeInMillis();
                        cal.add(Calendar.MONTH, 1);
                        long end = cal.getTimeInMillis();

                        cursor = db.rawQuery(
                                "SELECT SUM(durationMinutes) FROM user_activity WHERE userId=? AND timestamp>=? AND timestamp<?",
                                new String[]{userId, String.valueOf(start), String.valueOf(end)}
                        );

                        int duration = 0;
                        if (cursor.moveToFirst()) {
                            duration = cursor.isNull(0) ? 0 : cursor.getInt(0);
                        }
                        cursor.close();

                        cal.add(Calendar.MONTH, -1); // Reset lại
                        list.add(new ActivityRecord(monthFormat.format(new Date(start)), duration));
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return list;
    }


    // dem tin nhan chua doc
    public int getTotalUnreadCount(String currentUserId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM messages WHERE toUser = ? AND isRead = 0",
                new String[]{currentUserId}
        );

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // Đếm số lời mời kết bạn đang chờ
    public int getPendingFriendRequestCount(String currentUserId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM friend_requests WHERE toId=? AND status='pending'",
                new String[]{currentUserId});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }
    // Trong DBHelper.java
    public void markAllMessagesRead(String currentUserId, String friendId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String chatRoomId = getChatRoomId(currentUserId, friendId);

        ContentValues values = new ContentValues();
        values.put("isRead", 1); // 1 = đã đọc

        db.update(
                "messages",
                values,
                "chatRoomId = ? AND toUser = ? AND isRead = 0",
                new String[]{chatRoomId, currentUserId}
        );

        db.close();
    }

    // Nếu chưa có, thêm phương thức tạo chatRoomId:
    private String getChatRoomId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }



    // Cập nhật đã đọc
    public void updateMessageReadStatus(String messageId, boolean isRead) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("isRead", isRead ? 1 : 0);
        db.update("messages", cv, "messageId=?", new String[]{messageId});
        db.close();
    }



    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);

        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }

    public void addMessage(ChatMessage msg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FROM, msg.getFromUser());
        values.put(COL_TO, msg.getToUser());
        values.put(COL_TEXT, msg.getText());
        values.put(COL_IMAGE, msg.getImageUri());
        values.put(COL_TIMESTAMP, msg.getTimestamp());
        values.put(COL_ROOM, msg.getChatRoomId());
        db.insert(TABLE_MESSAGES, null, values);
    }

    public List<ChatMessage> getMessages(String chatRoomId) {
        List<ChatMessage> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MESSAGES, null,
                COL_ROOM + "=?",
                new String[]{chatRoomId},
                null, null, COL_TIMESTAMP + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String from = cursor.getString(cursor.getColumnIndexOrThrow(COL_FROM));
                String to = cursor.getString(cursor.getColumnIndexOrThrow(COL_TO));
                String text = cursor.getString(cursor.getColumnIndexOrThrow(COL_TEXT));
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE)); // ✅
                long time = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP));
                String room = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROOM));

                ChatMessage msg = new ChatMessage(from, to, text, room);
                msg.setImageUri(imageUri);
                msg.setTimestamp(time);
                list.add(msg);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public long addFriendRequest(String fromId, String toId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fromId", fromId);
        values.put("toId", toId);
        values.put("status", "pending");
        values.put("timestamp", System.currentTimeMillis());
        return db.insert("friend_requests", null, values);
    }

    public int updateFriendRequestStatus(int requestId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", newStatus);
        return db.update("friend_requests", values, "id=?", new String[]{String.valueOf(requestId)});
    }

    public ChatMessage getLastMessage(String chatRoomId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ChatMessage message = null;

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_MESSAGES + " WHERE chatRoomId = ? ORDER BY timestamp DESC LIMIT 1",
                new String[]{chatRoomId}
        );

        if (cursor != null && cursor.moveToFirst()) {
            String fromUser = cursor.getString(cursor.getColumnIndexOrThrow("fromUser"));
            String toUser = cursor.getString(cursor.getColumnIndexOrThrow("toUser"));
            String text = cursor.getString(cursor.getColumnIndexOrThrow("text"));
            String imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri")); // ✅ Lấy imageUri
            long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));
            String roomId = cursor.getString(cursor.getColumnIndexOrThrow("chatRoomId"));
            int isReadInt = cursor.getInt(cursor.getColumnIndexOrThrow("isRead"));

            message = new ChatMessage(fromUser, toUser, text, roomId);
            message.setImageUri(imageUri);
            message.setTimestamp(timestamp);
            message.setRead(isReadInt != 0);
        }

        if (cursor != null) cursor.close();
        db.close();
        return message;
    }
    public void acceptFriendRequest(int requestId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "accepted");
        db.update("friend_requests", values, "id=?", new String[]{String.valueOf(requestId)});

        Cursor cursor = db.rawQuery("SELECT fromId, toId FROM friend_requests WHERE id=?", new String[]{String.valueOf(requestId)});
        if (cursor.moveToFirst()) {
            String fromId = cursor.getString(0);
            String toId = cursor.getString(1);
            addFriend(fromId, toId);
        }
        cursor.close();
        db.close();
    }

    public boolean checkFriendRequestExists(String fromId, String toId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM friend_requests WHERE (fromId=? AND toId=?) OR (fromId=? AND toId=?)",
                new String[]{fromId, toId, toId, fromId});
        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    public long getPreviousPeriodTotal(String userId, String period) {
        SQLiteDatabase db = this.getReadableDatabase();
        long total = 0;
        Calendar cal = Calendar.getInstance();
        long start, end;
        Cursor cursor = null;

        try {
            switch (period) {
                case "Ngày":
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    start = cal.getTimeInMillis();
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    end = cal.getTimeInMillis();
                    break;
                case "Tuần":
                    cal.add(Calendar.WEEK_OF_YEAR, -1);
                    cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                    start = cal.getTimeInMillis();
                    cal.add(Calendar.WEEK_OF_YEAR, 1);
                    end = cal.getTimeInMillis();
                    break;
                case "Tháng":
                    cal.add(Calendar.MONTH, -1);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    start = cal.getTimeInMillis();
                    cal.add(Calendar.MONTH, 1);
                    end = cal.getTimeInMillis();
                    break;
                default:
                    return 0;
            }

            cursor = db.rawQuery(
                    "SELECT SUM(durationMinutes) FROM user_activity WHERE userId = ? AND timestamp >= ? AND timestamp < ?",
                    new String[]{userId, String.valueOf(start), String.valueOf(end)}
            );

            if (cursor.moveToFirst()) {
                total = cursor.isNull(0) ? 0 : cursor.getLong(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return total;
    }
    public List<Integer> getActivitySummary(String userId, String type) {
        List<Integer> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "";
        switch (type) {
            case "ngay":
                query = "SELECT strftime('%H', datetime(timestamp/1000, 'unixepoch', 'localtime')) AS hour, " +
                        "SUM(durationMinutes) AS total " +
                        "FROM user_activity " +
                        "WHERE userId = ? AND date(datetime(timestamp/1000, 'unixepoch', 'localtime')) = date(?) " +
                        "GROUP BY hour ORDER BY hour ASC";
                break;

            case "tuan":
                query = "SELECT strftime('%w', datetime(timestamp/1000, 'unixepoch', 'localtime')) AS weekday, " +
                        "SUM(durationMinutes) AS total " +
                        "FROM user_activity " +
                        "WHERE userId = ? " +
                        "AND date(datetime(timestamp/1000, 'unixepoch', 'localtime')) BETWEEN " +
                        "date('now', 'weekday 1') AND date('now', 'weekday 0', '+0 days') " +
                        "GROUP BY weekday ORDER BY weekday ASC";
                break;

            case "thang":
                query = "SELECT strftime('%d', datetime(timestamp/1000, 'unixepoch', 'localtime')) AS day, " +
                        "SUM(durationMinutes) AS total " +
                        "FROM user_activity " +
                        "WHERE userId = ? AND strftime('%Y-%m', datetime(timestamp/1000, 'unixepoch', 'localtime')) = strftime('%Y-%m', ?) " +
                        "GROUP BY day ORDER BY day ASC";
                break;

            case "nam":
                query = "SELECT strftime('%m', datetime(timestamp/1000, 'unixepoch', 'localtime')) AS month, " +
                        "SUM(durationMinutes) AS total " +
                        "FROM user_activity " +
                        "WHERE userId = ? AND strftime('%Y', datetime(timestamp/1000, 'unixepoch', 'localtime')) = strftime('%Y', ?) " +
                        "GROUP BY month ORDER BY month ASC";
                break;
        }

        long currentTime = System.currentTimeMillis();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime);
        Log.d("DBHelper", "Executing query: " + query + " with userId: " + userId + ", date: " + currentDate);

        Cursor cursor = null;
        try {
            // ✅ Khởi tạo sẵn mảng kết quả toàn 0
            int size = 0;
            if (type.equals("ngay")) size = 24; // 24 giờ
            else if (type.equals("tuan")) size = 7; // Thứ 2 → CN
            else if (type.equals("thang"))
                size = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
            else if (type.equals("nam")) size = 12; // 12 tháng

            for (int i = 0; i < size; i++) result.add(0);

            // ✅ Chạy truy vấn
            cursor = db.rawQuery(query, new String[]{userId, currentDate});
            Log.d("DBHelper", "Cursor count: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    int total = cursor.getInt(cursor.getColumnIndexOrThrow("total"));

                    switch (type) {
                        case "ngay": {
                            int hour = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("hour")));
                            result.set(hour, total);
                            break;
                        }
                        case "tuan": {
                            int weekday = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("weekday")));
                            // SQLite: 0=CN → 6=Thứ 7 | chuyển về 0=Thứ 2
                            int index = (weekday == 0) ? 6 : weekday - 1;
                            result.set(index, total);
                            break;
                        }
                        case "thang": {
                            int day = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("day")));
                            result.set(day - 1, total);
                            break;
                        }
                        case "nam": {
                            int month = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("month")));
                            result.set(month - 1, total);
                            break;
                        }
                    }
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("DBHelper", " Lỗi getActivitySummary: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        Log.d("DBHelper", " Result size: " + result.size() + ", Data: " + result);
        return result;
    }


    public List<FriendRequest> getFriendRequestsForUser(String userId) {
        List<FriendRequest> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM friend_requests WHERE fromId = ? OR toId = ? ORDER BY timestamp DESC",
                new String[]{userId, userId}
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String fromId = cursor.getString(cursor.getColumnIndexOrThrow("fromId"));
                String toId = cursor.getString(cursor.getColumnIndexOrThrow("toId"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"));

                FriendRequest request = new FriendRequest(id, fromId, toId, status, timestamp);
                requests.add(request);
            } while (cursor.moveToNext());
            cursor.close();
        }


        return requests;
    }

    public int deleteFriendRequest(int requestId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("friend_requests", "id=?", new String[]{String.valueOf(requestId)});
        db.close();
        return rows;
    }
    public boolean deleteFriend(String user1, String user2) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("friends",
                "(user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)",
                new String[]{user1, user2, user2, user1});
        db.close();
        return rows > 0;
    }


    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM users", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {

                String id;
                try {
                    int intId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    id = String.valueOf(intId);
                } catch (Exception e) {
                    Log.e("DBHelper", " Lỗi khi lấy user id: " + e.getMessage());
                    continue;
                }

                User user = new User();
                user.setId(id);
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                user.setTenHienThi(cursor.getString(cursor.getColumnIndexOrThrow("tenHienThi")));
                user.setGioiTinh(cursor.getString(cursor.getColumnIndexOrThrow("gioiTinh")));
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
                user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")));
                user.setLastLoginAt(cursor.getLong(cursor.getColumnIndexOrThrow("lastLoginAt")));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow("password")));

                userList.add(user);

                Log.d("DBHelper", " User loaded: id=" + id + ", email=" + user.getEmail());
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return userList;
    }

    public int getActivityCount(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM user_activity WHERE userId = ?", new String[]{userId});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public void clearUserActivity(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("user_activity", "userId = ?", new String[]{userId});
        db.close();
    }

    public User getUserById(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE id = ? LIMIT 1",
                new String[]{userId}
        );

        if (cursor != null && cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String tenHienThi = cursor.getString(cursor.getColumnIndexOrThrow("tenHienThi"));
            String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
            String gioiTinh = cursor.getString(cursor.getColumnIndexOrThrow("gioiTinh"));
            String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt"));
            long lastLoginAt = cursor.getLong(cursor.getColumnIndexOrThrow("lastLoginAt"));

            user = new User(id, email, tenHienThi, role, gioiTinh, password, createdAt, lastLoginAt);
        }

        if (cursor != null) cursor.close();
        db.close();
        return user;
    }

    public int deleteFriendRequestBetween(String user1, String user2) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("friend_requests",
                "(fromId = ? AND toId = ?) OR (fromId = ? AND toId = ?)",
                new String[]{user1, user2, user2, user1});
        db.close();
        return rows;
    }



    public int updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("email", user.getEmail());
        values.put("tenHienThi", user.getTenHienThi());
        values.put("role", user.getRole());
        values.put("gioiTinh", user.getGioiTinh());
        values.put("password", user.getPassword());
        values.put("createdAt", user.getCreatedAt());
        values.put("lastLoginAt", user.getLastLoginAt());

        int rows = db.update(
                TABLE_USERS,
                values,
                "id = ?",
                new String[]{user.getId()}
        );

        db.close();
        return rows;
    }

    public User getUserByEmailAndPassword(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                "email = ? AND password = ?",
                new String[]{email, password},
                null, null, null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            user = new User(
                    String.valueOf(id),   // ép int sang String để dùng nhất quán
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("tenHienThi")),
                    cursor.getString(cursor.getColumnIndexOrThrow("role")),
                    cursor.getString(cursor.getColumnIndexOrThrow("gioiTinh")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("lastLoginAt"))
            );

            cursor.close();
        }
        db.close();
        return user;
    }

    public int updateLastLogin(String userId, long lastLoginAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("lastLoginAt", lastLoginAt);

        int rows = db.update(TABLE_USERS, values, "id = ?", new String[]{userId});
        db.close();
        return rows;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                "email = ?",
                new String[]{email},
                null,
                null,
                null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("tenHienThi")),
                    cursor.getString(cursor.getColumnIndexOrThrow("role")),
                    cursor.getString(cursor.getColumnIndexOrThrow("gioiTinh")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("createdAt")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("lastLoginAt"))
            );
            cursor.close();
        }
        db.close();
        return user;
    }


    public long insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", user.getEmail());
        values.put("tenHienThi", user.getTenHienThi());
        values.put("role", user.getRole());
        values.put("gioiTinh", user.getGioiTinh());
        values.put("password", user.getPassword());
        values.put("createdAt", user.getCreatedAt());
        values.put("lastLoginAt", user.getLastLoginAt());

        long result = db.insert("users", null, values);

        if (result > 0) {
            user.setId(String.valueOf(result)); // gán id mới cho User
        }

        db.close();
        return result;
    }

}