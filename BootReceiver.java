package com.example.qqq3xstrategy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.qqq3xstrategy.util.NotificationHelper;

/**
 * Broadcast receiver for device boot to reschedule tasks
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device booted, rescheduling tasks");
            
            // Reschedule notification work
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.scheduleNotificationWork();
            
            Log.d(TAG, "Tasks rescheduled after boot");
        }
    }
}