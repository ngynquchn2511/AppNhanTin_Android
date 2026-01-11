package com.example.nesvie_copyzalo;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ChatMessage.class}, version = 1, exportSchema = false)
public abstract class MessageDatabase extends RoomDatabase {
    private static MessageDatabase instance;

    public abstract MessageDao messageDao();

    public static synchronized MessageDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            MessageDatabase.class, "chat_database")
                    .allowMainThreadQueries() // ⚠️ chỉ dùng cho demo, thực tế nên chạy Async
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
