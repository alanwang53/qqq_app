package com.example.qqq3xstrategy.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

/**
 * Broadcast receiver for notification actions
 */
public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";
    private static final String ACTION_DISMISS = "ACTION_DISMISS";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (ACTION_DISMISS.equals(action)) {
            // Dismiss notification
            NotificationManagerCompat.from(context).cancel(1001); // Position change notification ID
            
            // Optionally log that user has seen the notification
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong("last_notification_dismissed", System.currentTimeMillis())
                .apply();
            
            Log.d(TAG, "Notification dismissed by user");
        }
    }
}