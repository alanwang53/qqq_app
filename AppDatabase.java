package com.example.qqq3xstrategy.data.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.models.SignalHistory;
import com.example.qqq3xstrategy.data.models.TechnicalIndicator;
import com.example.qqq3xstrategy.data.models.UserSettings;
import com.example.qqq3xstrategy.util.AppExecutors;

/**
 * Room database for the QQQ3X Strategy app
 */
@Database(
    entities = {
        MarketData.class,
        TechnicalIndicator.class,
        SignalHistory.class,
        UserSettings.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String TAG = "AppDatabase";
    private static final String DATABASE_NAME = "qqq3x_strategy.db";
    private static volatile AppDatabase INSTANCE;
    
    // DAOs
    public abstract MarketDataDao marketDataDao();
    public abstract TechnicalIndicatorDao technicalIndicatorDao();
    public abstract SignalHistoryDao signalHistoryDao();
    public abstract UserSettingsDao userSettingsDao();
    
    /**
     * Get the singleton instance of the database
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Log.d(TAG, "Creating new database instance");
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                    
                    // Initialize default settings if needed
                    initializeDefaultSettings(context);
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Initialize default settings if they don't exist
     */
    private static void initializeDefaultSettings(Context context) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            UserSettingsDao dao = getInstance(context).userSettingsDao();
            if (dao.getSettings() == null) {
                Log.d(TAG, "Initializing default settings");
                dao.insert(new UserSettings());
            }
        });
    }
}