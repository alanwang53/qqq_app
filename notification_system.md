# QQQ3X Strategy App Notification System

This document outlines the implementation of the notification system for the QQQ3X Strategy Android app.

## 1. Notification Requirements

The notification system must:

1. Alert users when a position change is recommended (9:10 AM PT / UTC-8)
2. Provide clear information about the recommended action
3. Be reliable and timely
4. Allow users to view details or dismiss
5. Use appropriate priority levels
6. Be configurable by users

## 2. Notification Channels

Android 8.0+ requires notification channels. We'll implement the following channels:

### 2.1 Position Change Channel

```java
private void createPositionChangeChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
            POSITION_CHANGE_CHANNEL_ID,
            "Position Change Alerts",
            NotificationManager.IMPORTANCE_HIGH
        );
        
        channel.setDescription("Alerts when the strategy recommends changing position");
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 500, 250, 500});
        channel.setShowBadge(true);
        
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
```

### 2.2 Market Update Channel

```java
private void createMarketUpdateChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
            MARKET_UPDATE_CHANNEL_ID,
            "Market Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        );
        
        channel.setDescription("Regular updates about market conditions");
        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);
        channel.enableVibration(false);
        channel.setShowBadge(false);
        
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
```

## 3. Notification Types

### 3.1 Position Change Notification

This high-priority notification is sent when the strategy recommends changing position.

```java
public void sendPositionChangeNotification(int newSignal, boolean positionChanged) {
    if (!positionChanged) return;
    
    String title = "QQQ3X Strategy: Position Change Recommended";
    String content = newSignal == 1 
        ? "SWITCH TO: LEVERAGED QQQ (3X)" 
        : "SWITCH TO: SAFE ASSET";
    
    Intent detailsIntent = new Intent(this, PositionChangeDetailsActivity.class);
    detailsIntent.putExtra("signal", newSignal);
    detailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(
        this, 0, detailsIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );
    
    Intent dismissIntent = new Intent(this, NotificationActionReceiver.class);
    dismissIntent.setAction(ACTION_DISMISS);
    PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
        this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );
    
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, POSITION_CHANGE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .addAction(R.drawable.ic_view, "View Details", pendingIntent)
        .addAction(R.drawable.ic_dismiss, "Dismiss", dismissPendingIntent)
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
    
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
    
    // Check notification permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(POSITION_CHANGE_NOTIFICATION_ID, builder.build());
        } else {
            // Handle permission not granted
            requestNotificationPermission();
        }
    } else {
        notificationManager.notify(POSITION_CHANGE_NOTIFICATION_ID, builder.build());
    }
}
```

### 3.2 Market Update Notification

This standard-priority notification provides regular market updates.

```java
public void sendMarketUpdateNotification(double qqqPrice, double vixValue, int currentSignal) {
    String title = "QQQ3X Strategy: Market Update";
    String content = String.format(
        "QQQ: $%.2f | VIX: %.2f | Current Position: %s",
        qqqPrice,
        vixValue,
        currentSignal == 1 ? "LEVERAGED QQQ (3X)" : "SAFE ASSET"
    );
    
    Intent mainIntent = new Intent(this, MainActivity.class);
    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(
        this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );
    
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MARKET_UPDATE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setCategory(NotificationCompat.CATEGORY_STATUS)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setColor(ContextCompat.getColor(this, R.color.colorPrimary));
    
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
    
    // Check notification permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(MARKET_UPDATE_NOTIFICATION_ID, builder.build());
        }
    } else {
        notificationManager.notify(MARKET_UPDATE_NOTIFICATION_ID, builder.build());
    }
}
```

## 4. Notification Actions

### 4.1 View Details Action

When a user taps on the notification or the "View Details" action, they are taken to the Position Change Details screen.

```java
public class PositionChangeDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_change_details);
        
        int signal = getIntent().getIntExtra("signal", 0);
        
        // Populate UI with signal details
        populateDetails(signal);
        
        // Mark notification as read
        NotificationManagerCompat.from(this).cancel(POSITION_CHANGE_NOTIFICATION_ID);
    }
    
    private void populateDetails(int signal) {
        // Retrieve latest data and populate UI
        // ...
    }
}
```

### 4.2 Dismiss Action

When a user taps the "Dismiss" action, the notification is dismissed without opening the app.

```java
public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (ACTION_DISMISS.equals(action)) {
            // Dismiss notification
            NotificationManagerCompat.from(context).cancel(POSITION_CHANGE_NOTIFICATION_ID);
            
            // Optionally log that user has seen the notification
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong("last_notification_dismissed", System.currentTimeMillis())
                .apply();
        }
    }
}
```

## 5. Notification Scheduling

Notifications are scheduled using WorkManager to ensure they are delivered at the right time.

```java
public void scheduleNotificationWork() {
    // Create constraints
    Constraints constraints = new Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build();
    
    // Create daily work request for 9:10 AM PT
    Calendar calendar = Calendar.getInstance();
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
    WorkManager.getInstance(this).enqueueUniqueWork(
        "daily_notification",
        ExistingWorkPolicy.REPLACE,
        notificationWorkRequest
    );
}
```

## 6. Notification Worker

The NotificationWorker class handles the background processing for notifications.

```java
public class NotificationWorker extends Worker {
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            // Get user preferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
            
            if (!notificationsEnabled) {
                return Result.success();
            }
            
            // Get strategy data
            StrategyRepository repository = new StrategyRepository(getApplicationContext());
            StrategyResult result = repository.getLatestStrategyResult();
            
            // Send notification if position change is needed
            if (result.isPositionChanged()) {
                NotificationHelper helper = new NotificationHelper(getApplicationContext());
                helper.sendPositionChangeNotification(result.getSignal(), true);
            }
            
            // Schedule next notification
            NotificationHelper helper = new NotificationHelper(getApplicationContext());
            helper.scheduleNotificationWork();
            
            return Result.success();
        } catch (Exception e) {
            Log.e("NotificationWorker", "Error sending notification", e);
            return Result.retry();
        }
    }
}
```

## 7. Notification Permission Handling

For Android 13+, we need to request notification permission.

```java
private void requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
            
            // Check if we should show rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                // Show dialog explaining why notifications are important
                showNotificationRationaleDialog();
            } else {
                // Request permission directly
                ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                );
            }
        }
    }
}

private void showNotificationRationaleDialog() {
    new AlertDialog.Builder(this)
        .setTitle("Notification Permission")
        .setMessage("The QQQ3X Strategy app needs to send notifications to alert you when position changes are recommended. Without this permission, you may miss important trading signals.")
        .setPositiveButton("Grant Permission", (dialog, which) -> {
            ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        })
        .setNegativeButton("Not Now", null)
        .show();
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    
    if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, update UI
            updateNotificationPreferences(true);
        } else {
            // Permission denied, update UI
            updateNotificationPreferences(false);
            
            // Show settings option if permanently denied
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                showSettingsDialog();
            }
        }
    }
}

private void showSettingsDialog() {
    new AlertDialog.Builder(this)
        .setTitle("Notification Permission")
        .setMessage("Notifications are required for timely strategy alerts. Please enable them in app settings.")
        .setPositiveButton("Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        })
        .setNegativeButton("Not Now", null)
        .show();
}
```

## 8. User Preferences for Notifications

Allow users to customize notification settings.

```java
public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        
        // Handle notification toggle
        SwitchPreferenceCompat notificationPref = findPreference("notifications_enabled");
        if (notificationPref != null) {
            notificationPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean enabled = (Boolean) newValue;
                
                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Check if we have permission
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) 
                            != PackageManager.PERMISSION_GRANTED) {
                        // Request permission
                        requestNotificationPermission();
                        return false; // Don't update preference yet
                    }
                }
                
                // Update notification scheduling
                NotificationHelper helper = new NotificationHelper(requireContext());
                if (enabled) {
                    helper.scheduleNotificationWork();
                } else {
                    WorkManager.getInstance(requireContext()).cancelUniqueWork("daily_notification");
                }
                
                return true;
            });
        }
    }
    
    private void requestNotificationPermission() {
        // Implementation as shown above
    }
}
```

## 9. Testing Notification System

To ensure the notification system works correctly:

1. **Unit Tests**
   - Test notification content generation
   - Test scheduling logic
   - Test permission handling

2. **Integration Tests**
   - Test end-to-end notification flow
   - Test notification actions
   - Test notification scheduling

3. **Manual Tests**
   - Test on different Android versions
   - Test with different user preferences
   - Test with app in foreground/background
   - Test with device in different states (idle, doze mode, etc.)

## 10. Notification Best Practices

1. **Be Respectful of User Attention**
   - Only send notifications when truly needed
   - Use appropriate priority levels
   - Group related notifications

2. **Provide Clear Value**
   - Make the action clear
   - Provide enough context
   - Make it easy to take action

3. **Handle Edge Cases**
   - Network failures
   - Permission denials
   - Battery optimization

4. **Internationalization**
   - Support multiple languages
   - Use locale-appropriate formatting

5. **Accessibility**
   - Support screen readers
   - Use high contrast colors
   - Provide haptic feedback