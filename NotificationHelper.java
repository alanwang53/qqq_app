package com.example.qqq3xstrategy.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.qqq3xstrategy.QQQ3XStrategyApp;
import com.example.qqq3xstrategy.R;
import com.example.qqq3xstrategy.ui.MainActivity;
import com.example.qqq3xstrategy.ui.PositionChangeDetailsActivity;
import com.example.qqq3xstrategy.workers.NotificationWorker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for managing notifications
 */
public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    
    private static final int POSITION_CHANGE_NOTIFICATION_ID = 1001;
    private static final int MARKET_UPDATE_NOTIFICATION_ID = 1002;
    
    private final Context context;
    
    /**
     * Constructor
     */
    public NotificationHelper(Context context) {
        this.context = context;
    }
    
    /**
     * Send notification for position change
     */
    public void sendPositionChangeNotification(int newSignal, boolean positionChanged) {
        if (!positionChanged) return;
        
        String title = "QQQ3X Strategy: Position Change Recommended";
        String content = newSignal == 1 
            ? "SWITCH TO: LEVERAGED QQQ (3X)" 
            : "SWITCH TO: SAFE ASSET";
        
        Intent detailsIntent = new Intent(context, PositionChangeDetailsActivity.class);
        detailsIntent.putExtra("signal", newSignal);
        detailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, detailsIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Intent dismissIntent = new Intent(context, NotificationActionReceiver.class);
        dismissIntent.setAction("ACTION_DISMISS");
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
            context, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, QQQ3XStrategyApp.POSITION_CHANGE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_view, "View Details", pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .setColor(newSignal == 1 ? Color.GREEN : Color.RED);
        
        // Use big text style for more details
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
            .bigText(content + "\n\nTime: " + getCurrentTimeString() + 
                    "\nTap to view detailed information about this recommendation.")
            .setSummaryText("Strategy Alert");
        
        builder.setStyle(bigTextStyle);
        
        // Add full screen intent for high visibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setFullScreenIntent(pendingIntent, true);
        }
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        try {
            notificationManager.notify(POSITION_CHANGE_NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Position change notification sent");
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted", e);
        }
    }
    
    /**
     * Send notification for market update
     */
    public void sendMarketUpdateNotification(double qqqPrice, double vixValue, int currentSignal) {
        String title = "QQQ3X Strategy: Market Update";
        String content = String.format(
            Locale.US,
            "QQQ: $%.2f | VIX: %.2f | Current Position: %s",
            qqqPrice,
            vixValue,
            currentSignal == 1 ? "LEVERAGED QQQ (3X)" : "SAFE ASSET"
        );
        
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, QQQ3XStrategyApp.MARKET_UPDATE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(Color.BLUE);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        try {
            notificationManager.notify(MARKET_UPDATE_NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Market update notification sent");
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted", e);
        }
    }
    
    /**
     * Schedule notification work
     */
    public void scheduleNotificationWork() {
        // Create constraints
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
        
        // Create daily work request for 9:10 AM PT
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, 0);
        
        long now = System.currentTimeMillis();
        long delay = calendar.getTimeInMillis() - now;
        
        // If time has already passed today, schedule for tomorrow
        if (delay < 0) {
            delay += 24 * 60 * 60 * 1000; // Add one day
        }
        
        // Create work request
        OneTimeWorkRequest notificationWorkRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("notification_work")
            .build();
        
        // Enqueue work
        WorkManager.getInstance(context).enqueueUniqueWork(
            "daily_notification",
            ExistingWorkPolicy.REPLACE,
            notificationWorkRequest
        );
        
        Log.d(TAG, "Notification scheduled for " + new Date(calendar.getTimeInMillis()));
    }
    
    /**
     * Get current time as formatted string
     */
    private String getCurrentTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
        return sdf.format(new Date());
    }
}