# QQQ3X Strategy App Components

## 1. Application Class

```java
public class QQQ3XStrategyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        scheduleDataFetchWork();
    }
    
    private void createNotificationChannel() {
        // Create notification channel for strategy alerts
    }
    
    private void scheduleDataFetchWork() {
        // Schedule daily data fetch at 9:00 AM PT
    }
}
```

## 2. Data Service

```java
public class YahooFinanceService {
    // Fetch QQQ and VIX data
    public CompletableFuture<MarketData> fetchMarketData() {
        // Use Yahoo Finance API to get current data
    }
    
    // Download historical data for strategy calculation
    public CompletableFuture<List<HistoricalData>> fetchHistoricalData(String startDate, String endDate) {
        // Fetch 2 years of historical data
    }
}
```

## 3. Database Manager

```java
public class DatabaseManager {
    private SQLiteDatabase database;
    
    // Store historical data
    public void saveHistoricalData(List<HistoricalData> data) {
        // Insert or update data in SQLite
    }
    
    // Retrieve historical data for strategy calculation
    public List<HistoricalData> getHistoricalData() {
        // Query database for historical data
    }
    
    // Save today's signal
    public void saveSignal(int signal, String date) {
        // Store signal in database
    }
    
    // Get previous signal
    public int getPreviousSignal() {
        // Get most recent signal from database
    }
}
```

## 4. Strategy Engine

```java
public class QQQ3XStrategy {
    // Calculate technical indicators
    private void calculateIndicators(DataFrame df) {
        // Calculate SMAs, VIX indicators, etc.
    }
    
    // Determine trading signal
    public int calculateSignal(DataFrame historicalData, double vixOpenToday, double qqqOpenToday) {
        // Implement strategy_today() logic
    }
    
    // Check if position change is needed
    public boolean isPositionChangeNeeded(int newSignal, int previousSignal) {
        return newSignal != previousSignal;
    }
}
```

## 5. Notification Manager

```java
public class NotificationManager {
    // Send alert for position change
    public void sendPositionChangeAlert(int signal, Context context) {
        // Create and show notification based on signal
    }
    
    // Create detailed notification content
    private String createNotificationContent(int signal) {
        // Generate appropriate message based on signal
    }
}
```

## 6. Worker for Scheduled Tasks

```java
public class DataFetchWorker extends Worker {
    @Override
    public Result doWork() {
        // 1. Fetch market data
        // 2. Calculate strategy signal
        // 3. Compare with previous signal
        // 4. Send notification if needed
        // 5. Save new signal to database
    }
}
```

## 7. Main Activity

```java
public class MainActivity extends AppCompatActivity {
    // Display current position
    private void updatePositionDisplay(int signal) {
        // Update UI based on current signal
    }
    
    // Manual refresh option
    private void refreshData() {
        // Manually trigger data fetch and strategy calculation
    }
    
    // Display historical performance
    private void showPerformanceChart() {
        // Visualize strategy performance
    }
}
```

## 8. Settings Activity

```java
public class SettingsActivity extends AppCompatActivity {
    // Notification preferences
    // Data refresh frequency options
    // Safe asset selection (GLD or SHY)
}