package com.example.qqq3xstrategy;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.qqq3xstrategy.data.database.AppDatabase;
import com.example.qqq3xstrategy.workers.DataFetchWorker;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class QQQ3XStrategyApp extends Application {
    private static final String TAG = "QQQ3XStrategyApp";
    public static final String POSITION_CHANGE_CHANNEL_ID = "position_change_channel";
    public static final String MARKET_UPDATE_CHANNEL_ID = "market_update_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application starting up");
        
        // Initialize database
        AppDatabase.getInstance(this);
        
        // Create notification channels
        createNotificationChannels();
        
        // Schedule data fetch work
        scheduleDataFetch();
    }
    
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Position Change Channel (high priority)
            NotificationChannel positionChangeChannel = new NotificationChannel(
                POSITION_CHANGE_CHANNEL_ID,
                "Position Change Alerts",
                NotificationManager.IMPORTANCE_HIGH
            );
            positionChangeChannel.setDescription("Alerts when the strategy recommends changing position");
            positionChangeChannel.enableLights(true);
            positionChangeChannel.setLightColor(Color.RED);
            positionChangeChannel.enableVibration(true);
            positionChangeChannel.setVibrationPattern(new long[]{0, 500, 250, 500});
            positionChangeChannel.setShowBadge(true);
            
            // Market Update Channel (default priority)
            NotificationChannel marketUpdateChannel = new NotificationChannel(
                MARKET_UPDATE_CHANNEL_ID,
                "Market Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            marketUpdateChannel.setDescription("Regular updates about market conditions");
            marketUpdateChannel.enableLights(true);
            marketUpdateChannel.setLightColor(Color.BLUE);
            marketUpdateChannel.enableVibration(false);
            marketUpdateChannel.setShowBadge(false);
            
            // Register the channels
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(positionChangeChannel);
            notificationManager.createNotificationChannel(marketUpdateChannel);
            
            Log.d(TAG, "Notification channels created");
        }
    }
    
    private void scheduleDataFetch() {
        // Create constraints - require network connectivity
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
        
        // Schedule for 9:00 AM PT (UTC-8)
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        long now = System.currentTimeMillis();
        long delay = calendar.getTimeInMillis() - now;
        
        // If time has already passed today, schedule for tomorrow
        if (delay < 0) {
            delay += 24 * 60 * 60 * 1000; // Add one day
        }
        
        // Create work request
        OneTimeWorkRequest dataFetchWorkRequest = new OneTimeWorkRequest.Builder(DataFetchWorker.class)
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("data_fetch_work")
            .build();
        
        // Enqueue work
        WorkManager.getInstance(this).enqueueUniqueWork(
            "daily_data_fetch",
            ExistingWorkPolicy.REPLACE,
            dataFetchWorkRequest
        );
        
        Log.d(TAG, "Data fetch scheduled for " + calendar.getTime());
    }
    
    // Helper method to get application context from anywhere
    public static QQQ3XStrategyApp getInstance(Context context) {
        return (QQQ3XStrategyApp) context.getApplicationContext();
    }
}