package com.example.nesvie_copyzalo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "chat_app.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_PK = "id"; // autoinc
    public static final String COLUMN_MSG_ID = "message_id"; // uuid từ server/ client
    public static final String COLUMN_SENDER = "sender_id";
    public static final String COLUMN_RECEIVER = "receiver_id";
    public static final String COLUMN_MESSAGE = "content";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_STATUS = "status";

    public ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_PK + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MSG_ID + " TEXT UNIQUE,"
                + COLUMN_SENDER + " TEXT,"
                + COLUMN_RECEIVER + " TEXT,"
                + COLUMN_MESSAGE + " TEXT,"
                + COLUMN_TIMESTAMP + " INTEGER,"
                + COLUMN_STATUS + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }
}
