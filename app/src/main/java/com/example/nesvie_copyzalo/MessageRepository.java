package com.example.nesvie_copyzalo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private ChatDatabaseHelper dbHelper;

    public MessageRepository(Context ctx) {
        dbHelper = new ChatDatabaseHelper(ctx);
    }

    public void insertMessage(Message m) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ChatDatabaseHelper.COLUMN_MSG_ID, m.getMessageId());
        cv.put(ChatDatabaseHelper.COLUMN_SENDER, m.getSenderId());
        cv.put(ChatDatabaseHelper.COLUMN_RECEIVER, m.getReceiverId());
        cv.put(ChatDatabaseHelper.COLUMN_MESSAGE, m.getContent());
        cv.put(ChatDatabaseHelper.COLUMN_TIMESTAMP, m.getTimestamp());
        cv.put(ChatDatabaseHelper.COLUMN_STATUS, m.getStatus());
        db.insertWithOnConflict(ChatDatabaseHelper.TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public List<Message> getMessagesBetween(String a, String b) {
        List<Message> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT * FROM " + ChatDatabaseHelper.TABLE_MESSAGES +
                " WHERE (" + ChatDatabaseHelper.COLUMN_SENDER + "=? AND " + ChatDatabaseHelper.COLUMN_RECEIVER + "=?)" +
                " OR (" + ChatDatabaseHelper.COLUMN_SENDER + "=? AND " + ChatDatabaseHelper.COLUMN_RECEIVER + "=?)" +
                " ORDER BY " + ChatDatabaseHelper.COLUMN_TIMESTAMP + " ASC";
        Cursor c = db.rawQuery(sql, new String[]{a, b, b, a});
        while (c.moveToNext()) {
            Message m = new Message();
            m.setMessageId(c.getString(c.getColumnIndexOrThrow(ChatDatabaseHelper.COLUMN_MSG_ID)));
            m.setSenderId(c.getString(c.getColumnIndexOrThrow(ChatDatabaseHelper.COLUMN_SENDER)));
            m.setReceiverId(c.getString(c.getColumnIndexOrThrow(ChatDatabaseHelper.COLUMN_RECEIVER)));
            m.setContent(c.getString(c.getColumnIndexOrThrow(ChatDatabaseHelper.COLUMN_MESSAGE)));
            m.setTimestamp(c.getLong(c.getColumnIndexOrThrow(ChatDatabaseHelper.COLUMN_TIMESTAMP)));
            m.setStatus(c.getString(c.getColumnIndexOrThrow(ChatDatabaseHelper.COLUMN_STATUS)));
            list.add(m);
        }
        c.close();
        db.close();
        return list;
    }

    public void updateStatus(String messageId, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ChatDatabaseHelper.COLUMN_STATUS, status);
        db.update(ChatDatabaseHelper.TABLE_MESSAGES, cv, ChatDatabaseHelper.COLUMN_MSG_ID + "=?", new String[]{messageId});
        db.close();
    }
}
