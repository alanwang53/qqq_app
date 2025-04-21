package com.example.qqq3xstrategy.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.qqq3xstrategy.data.models.UserSettings;

/**
 * Data Access Object for UserSettings entity
 */
@Dao
public interface UserSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserSettings settings);
    
    @Update
    void update(UserSettings settings);
    
    @Query("SELECT * FROM user_settings LIMIT 1")
    UserSettings getSettings();
    
    @Query("SELECT * FROM user_settings LIMIT 1")
    LiveData<UserSettings> getSettingsLive();
}