package com.example.qqq3xstrategy.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.qqq3xstrategy.data.database.AppDatabase;
import com.example.qqq3xstrategy.data.models.SignalHistory;
import com.example.qqq3xstrategy.data.models.UserSettings;
import com.example.qqq3xstrategy.util.NotificationHelper;

/**
 * Worker class for sending notifications
 */
public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";
    
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "NotificationWorker started");
            
            // Get user preferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
            
            if (!notificationsEnabled) {
                Log.d(TAG, "Notifications are disabled");
                return Result.success();
            }
            
            // Get database
            AppDatabase database = AppDatabase.getInstance(getApplicationContext());
            
            // Get latest signal
            SignalHistory latestSignal = database.signalHistoryDao().getLatestSignal();
            
            // Get user settings
            UserSettings settings = database.userSettingsDao().getSettings();
            
            if (latestSignal != null && latestSignal.getPositionChanged()) {
                // Send notification if position change is needed
                NotificationHelper helper = new NotificationHelper(getApplicationContext());
                helper.sendPositionChangeNotification(latestSignal.getSignal(), true);
                Log.d(TAG, "Position change notification sent");
            }
            
            // Schedule next notification
            NotificationHelper helper = new NotificationHelper(getApplicationContext());
            helper.scheduleNotificationWork();
            
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error sending notification", e);
            return Result.retry();
        }
    }
}