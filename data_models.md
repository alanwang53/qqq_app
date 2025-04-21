# QQQ3X Strategy App Data Models

## 1. Market Data Model

```java
public class MarketData {
    private double qqqOpen;
    private double qqqClose;
    private double qqqHigh;
    private double qqqLow;
    private double qqqVolume;
    
    private double vixOpen;
    private double vixClose;
    private double vixHigh;
    private double vixLow;
    
    private double gldOpen;
    private double gldClose;
    
    private double shyOpen;
    private double shyClose;
    
    private String date;
    
    // Getters and setters
}
```

## 2. Historical Data Model

```java
public class HistoricalData {
    private String date;
    
    // QQQ data
    private double qqqOpen;
    private double qqqClose;
    private double qqqHigh;
    private double qqqLow;
    private double qqqVolume;
    
    // VIX data
    private double vixOpen;
    private double vixClose;
    private double vixHigh;
    private double vixLow;
    
    // GLD data
    private double gldOpen;
    private double gldClose;
    
    // SHY data
    private double shyOpen;
    private double shyClose;
    
    // Calculated indicators
    private double qqqSmaYear;
    private double qqqSmaLong;
    private double qqqSmaShort;
    private double gldSma;
    private double vixSmaShort;
    private double vixSmaLong;
    private double vixSmaShort3;
    private double vixSmaLong3;
    
    // Getters and setters
}
```

## 3. Signal Model

```java
public class SignalData {
    private String date;
    private int signal;  // 1 for leveraged QQQ, -1 for safe asset
    private boolean positionChanged;
    private String safeAsset;  // "GLD" or "SHY"
    
    // Getters and setters
}
```

## 4. Strategy Parameters Model

```java
public class StrategyParameters {
    // Constants from the original strategy
    private int smaShort = 5;
    private int smaLong = 15;
    private int smaShort2 = 1;
    private int smaLong2 = 3;
    private int smaShort3 = 3;
    private int smaLong3 = 9;
    private int smaYear = 155;
    private double targetLeverage = 3.0;
    private double safeRatio = 0.2;
    private double managementFee = 0.0095;
    
    // Getters and setters
}
```

## 5. Database Schema

### Historical Data Table

```sql
CREATE TABLE historical_data (
    date TEXT PRIMARY KEY,
    qqq_open REAL,
    qqq_close REAL,
    qqq_high REAL,
    qqq_low REAL,
    qqq_volume REAL,
    vix_open REAL,
    vix_close REAL,
    vix_high REAL,
    vix_low REAL,
    gld_open REAL,
    gld_close REAL,
    shy_open REAL,
    shy_close REAL,
    qqq_sma_year REAL,
    qqq_sma_long REAL,
    qqq_sma_short REAL,
    gld_sma REAL,
    vix_sma_short REAL,
    vix_sma_long REAL,
    vix_sma_short3 REAL,
    vix_sma_long3 REAL
);
```

### Signal History Table

```sql
CREATE TABLE signal_history (
    date TEXT PRIMARY KEY,
    signal INTEGER,
    position_changed INTEGER,
    safe_asset TEXT,
    vix_open REAL,
    qqq_open REAL
);
```

### User Settings Table

```sql
CREATE TABLE user_settings (
    id INTEGER PRIMARY KEY,
    notifications_enabled INTEGER DEFAULT 1,
    preferred_safe_asset TEXT DEFAULT 'GLD',
    custom_leverage REAL DEFAULT 3.0,
    custom_safe_ratio REAL DEFAULT 0.2
);
```

## 6. Data Transfer Objects (DTOs)

```java
public class StrategyResult {
    private int signal;
    private boolean positionChanged;
    private String recommendedAction;
    private Map<String, Double> keyIndicators;
    
    // Getters and setters
}
```

## 7. Data Repository Interface

```java
public interface DataRepository {
    CompletableFuture<List<HistoricalData>> getHistoricalData();
    CompletableFuture<Void> saveHistoricalData(List<HistoricalData> data);
    CompletableFuture<SignalData> getLatestSignal();
    CompletableFuture<Void> saveSignal(SignalData signal);
    CompletableFuture<StrategyParameters> getStrategyParameters();
    CompletableFuture<Void> updateStrategyParameters(StrategyParameters parameters);
}