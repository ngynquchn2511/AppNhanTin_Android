package com.example.nesvie_copyzalo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatMessage message);

    @Query("SELECT * FROM chat_messages WHERE " +
            "(fromUser = :user1 AND toUser = :user2) OR " +
            "(fromUser = :user2 AND toUser = :user1) " +
            "ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesBetween(String user1, String user2);
}
