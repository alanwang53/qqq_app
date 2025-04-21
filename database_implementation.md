# QQQ3X Strategy App Database Implementation

This document outlines the database implementation for the QQQ3X Strategy Android app.

## 1. Database Overview

The app uses Room persistence library to manage an SQLite database for storing:

1. Historical market data
2. Strategy signals
3. User preferences

## 2. Database Schema

### 2.1 Market Data Table

```sql
CREATE TABLE market_data (
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
    shy_close REAL
);
```

### 2.2 Technical Indicators Table

```sql
CREATE TABLE technical_indicators (
    date TEXT PRIMARY KEY,
    qqq_sma_year REAL,
    qqq_sma_long REAL,
    qqq_sma_short REAL,
    gld_sma REAL,
    vix_sma_short REAL,
    vix_sma_long REAL,
    vix_sma_short3 REAL,
    vix_sma_long3 REAL,
    vix_c REAL,
    vix_open_close REAL,
    FOREIGN KEY (date) REFERENCES market_data(date) ON DELETE CASCADE
);
```

### 2.3 Signal History Table

```sql
CREATE TABLE signal_history (
    date TEXT PRIMARY KEY,
    raw_signal INTEGER,
    signal INTEGER NOT NULL,
    position_changed INTEGER NOT NULL,
    safe_asset TEXT NOT NULL,
    FOREIGN KEY (date) REFERENCES market_data(date) ON DELETE CASCADE
);
```

### 2.4 User Settings Table

```sql
CREATE TABLE user_settings (
    id INTEGER PRIMARY KEY,
    notifications_enabled INTEGER NOT NULL DEFAULT 1,
    preferred_safe_asset TEXT NOT NULL DEFAULT 'GLD',
    target_leverage REAL NOT NULL DEFAULT 3.0,
    safe_ratio REAL NOT NULL DEFAULT 0.2,
    sma_short INTEGER NOT NULL DEFAULT 5,
    sma_long INTEGER NOT NULL DEFAULT 15,
    sma_short2 INTEGER NOT NULL DEFAULT 1,
    sma_long2 INTEGER NOT NULL DEFAULT 3,
    sma_short3 INTEGER NOT NULL DEFAULT 3,
    sma_long3 INTEGER NOT NULL DEFAULT 9,
    sma_year INTEGER NOT NULL DEFAULT 155
);
```

## 3. Room Database Implementation

### 3.1 Database Class

```java
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
    private static final String DATABASE_NAME = "qqq3x_strategy.db";
    private static volatile AppDatabase INSTANCE;
    
    public abstract MarketDataDao marketDataDao();
    public abstract TechnicalIndicatorDao technicalIndicatorDao();
    public abstract SignalHistoryDao signalHistoryDao();
    public abstract UserSettingsDao userSettingsDao();
    
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
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
    
    private static void initializeDefaultSettings(Context context) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            UserSettingsDao dao = getInstance(context).userSettingsDao();
            if (dao.getSettings() == null) {
                dao.insert(new UserSettings());
            }
        });
    }
}
```

### 3.2 Type Converters

```java
public class DateConverter {
    @TypeConverter
    public static LocalDate fromString(String value) {
        return value == null ? null : LocalDate.parse(value);
    }
    
    @TypeConverter
    public static String dateToString(LocalDate date) {
        return date == null ? null : date.toString();
    }
}
```

## 4. Entity Classes

### 4.1 Market Data Entity

```java
@Entity(tableName = "market_data")
public class MarketData {
    @PrimaryKey
    @NonNull
    private LocalDate date;
    
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
    
    // Getters and setters
}
```

### 4.2 Technical Indicator Entity

```java
@Entity(
    tableName = "technical_indicators",
    foreignKeys = @ForeignKey(
        entity = MarketData.class,
        parentColumns = "date",
        childColumns = "date",
        onDelete = ForeignKey.CASCADE
    )
)
public class TechnicalIndicator {
    @PrimaryKey
    @NonNull
    private LocalDate date;
    
    private double qqqSmaYear;
    private double qqqSmaLong;
    private double qqqSmaShort;
    private double gldSma;
    private double vixSmaShort;
    private double vixSmaLong;
    private double vixSmaShort3;
    private double vixSmaLong3;
    private double vixC;
    private double vixOpenClose;
    
    // Getters and setters
}
```

### 4.3 Signal History Entity

```java
@Entity(
    tableName = "signal_history",
    foreignKeys = @ForeignKey(
        entity = MarketData.class,
        parentColumns = "date",
        childColumns = "date",
        onDelete = ForeignKey.CASCADE
    )
)
public class SignalHistory {
    @PrimaryKey
    @NonNull
    private LocalDate date;
    
    private Integer rawSignal; // Can be null
    
    @NonNull
    private Integer signal; // -1 for safe asset, 1 for leveraged QQQ
    
    @NonNull
    private Boolean positionChanged;
    
    @NonNull
    private String safeAsset; // "GLD" or "SHY"
    
    // Getters and setters
}
```

### 4.4 User Settings Entity

```java
@Entity(tableName = "user_settings")
public class UserSettings {
    @PrimaryKey
    private int id = 1; // Single row for settings
    
    private boolean notificationsEnabled = true;
    private String preferredSafeAsset = "GLD";
    private double targetLeverage = 3.0;
    private double safeRatio = 0.2;
    private int smaShort = 5;
    private int smaLong = 15;
    private int smaShort2 = 1;
    private int smaLong2 = 3;
    private int smaShort3 = 3;
    private int smaLong3 = 9;
    private int smaYear = 155;
    
    // Getters and setters
}
```

## 5. Data Access Objects (DAOs)

### 5.1 Market Data DAO

```java
@Dao
public interface MarketDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MarketData marketData);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MarketData> marketDataList);
    
    @Query("SELECT * FROM market_data WHERE date = :date")
    MarketData getMarketDataForDate(LocalDate date);
    
    @Query("SELECT * FROM market_data ORDER BY date DESC LIMIT 1")
    MarketData getLatestMarketData();
    
    @Query("SELECT * FROM market_data WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    List<MarketData> getMarketDataBetweenDates(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(*) FROM market_data")
    int getCount();
    
    @Query("DELETE FROM market_data WHERE date < :date")
    void deleteOlderThan(LocalDate date);
}
```

### 5.2 Technical Indicator DAO

```java
@Dao
public interface TechnicalIndicatorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TechnicalIndicator indicator);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TechnicalIndicator> indicators);
    
    @Query("SELECT * FROM technical_indicators WHERE date = :date")
    TechnicalIndicator getIndicatorForDate(LocalDate date);
    
    @Query("SELECT * FROM technical_indicators ORDER BY date DESC LIMIT 1")
    TechnicalIndicator getLatestIndicator();
    
    @Query("SELECT * FROM technical_indicators WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    List<TechnicalIndicator> getIndicatorsBetweenDates(LocalDate startDate, LocalDate endDate);
    
    @Transaction
    @Query("SELECT * FROM market_data md LEFT JOIN technical_indicators ti ON md.date = ti.date WHERE md.date BETWEEN :startDate AND :endDate ORDER BY md.date")
    List<MarketDataWithIndicators> getMarketDataWithIndicators(LocalDate startDate, LocalDate endDate);
}
```

### 5.3 Signal History DAO

```java
@Dao
public interface SignalHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SignalHistory signalHistory);
    
    @Query("SELECT * FROM signal_history WHERE date = :date")
    SignalHistory getSignalForDate(LocalDate date);
    
    @Query("SELECT * FROM signal_history ORDER BY date DESC LIMIT 1")
    SignalHistory getLatestSignal();
    
    @Query("SELECT * FROM signal_history WHERE position_changed = 1 ORDER BY date DESC LIMIT 10")
    List<SignalHistory> getRecentPositionChanges();
    
    @Query("SELECT * FROM signal_history WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    List<SignalHistory> getSignalsBetweenDates(LocalDate startDate, LocalDate endDate);
    
    @Transaction
    @Query("SELECT * FROM market_data md LEFT JOIN signal_history sh ON md.date = sh.date WHERE md.date BETWEEN :startDate AND :endDate ORDER BY md.date")
    List<MarketDataWithSignal> getMarketDataWithSignals(LocalDate startDate, LocalDate endDate);
}
```

### 5.4 User Settings DAO

```java
@Dao
public interface UserSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserSettings settings);
    
    @Update
    void update(UserSettings settings);
    
    @Query("SELECT * FROM user_settings LIMIT 1")
    UserSettings getSettings();
}
```

## 6. Relationship Classes

### 6.1 Market Data with Indicators

```java
public class MarketDataWithIndicators {
    @Embedded
    public MarketData marketData;
    
    @Relation(
        parentColumn = "date",
        entityColumn = "date"
    )
    public TechnicalIndicator indicators;
}
```

### 6.2 Market Data with Signal

```java
public class MarketDataWithSignal {
    @Embedded
    public MarketData marketData;
    
    @Relation(
        parentColumn = "date",
        entityColumn = "date"
    )
    public SignalHistory signal;
}
```

## 7. Database Repository

```java
public class DatabaseRepository {
    private final MarketDataDao marketDataDao;
    private final TechnicalIndicatorDao technicalIndicatorDao;
    private final SignalHistoryDao signalHistoryDao;
    private final UserSettingsDao userSettingsDao;
    
    public DatabaseRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        marketDataDao = database.marketDataDao();
        technicalIndicatorDao = database.technicalIndicatorDao();
        signalHistoryDao = database.signalHistoryDao();
        userSettingsDao = database.userSettingsDao();
    }
    
    // Market Data methods
    public void saveMarketData(MarketData data) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            marketDataDao.insert(data);
        });
    }
    
    public void saveMarketDataList(List<MarketData> dataList) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            marketDataDao.insertAll(dataList);
        });
    }
    
    public LiveData<MarketData> getLatestMarketData() {
        MutableLiveData<MarketData> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            result.postValue(marketDataDao.getLatestMarketData());
        });
        return result;
    }
    
    // Technical Indicator methods
    public void saveTechnicalIndicator(TechnicalIndicator indicator) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            technicalIndicatorDao.insert(indicator);
        });
    }
    
    public void saveTechnicalIndicatorList(List<TechnicalIndicator> indicators) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            technicalIndicatorDao.insertAll(indicators);
        });
    }
    
    // Signal History methods
    public void saveSignal(SignalHistory signal) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            signalHistoryDao.insert(signal);
        });
    }
    
    public LiveData<SignalHistory> getLatestSignal() {
        MutableLiveData<SignalHistory> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            result.postValue(signalHistoryDao.getLatestSignal());
        });
        return result;
    }
    
    public LiveData<List<SignalHistory>> getRecentPositionChanges() {
        MutableLiveData<List<SignalHistory>> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            result.postValue(signalHistoryDao.getRecentPositionChanges());
        });
        return result;
    }
    
    // User Settings methods
    public LiveData<UserSettings> getUserSettings() {
        MutableLiveData<UserSettings> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            result.postValue(userSettingsDao.getSettings());
        });
        return result;
    }
    
    public void updateUserSettings(UserSettings settings) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            userSettingsDao.update(settings);
        });
    }
    
    // Combined data methods
    public LiveData<List<MarketDataWithSignal>> getHistoricalDataWithSignals(LocalDate startDate, LocalDate endDate) {
        MutableLiveData<List<MarketDataWithSignal>> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            result.postValue(signalHistoryDao.getMarketDataWithSignals(startDate, endDate));
        });
        return result;
    }
    
    // Database maintenance
    public void cleanupOldData(int retentionDays) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
            marketDataDao.deleteOlderThan(cutoffDate);
        });
    }
}
```

## 8. Database Testing

### 8.1 Unit Tests

```java
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    private AppDatabase database;
    private MarketDataDao marketDataDao;
    private SignalHistoryDao signalHistoryDao;
    
    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        marketDataDao = database.marketDataDao();
        signalHistoryDao = database.signalHistoryDao();
    }
    
    @After
    public void closeDb() {
        database.close();
    }
    
    @Test
    public void insertAndRetrieveMarketData() {
        LocalDate today = LocalDate.now();
        MarketData data = new MarketData();
        data.setDate(today);
        data.setQqqOpen(350.0);
        data.setQqqClose(355.0);
        data.setVixOpen(20.0);
        data.setVixClose(19.5);
        
        marketDataDao.insert(data);
        
        MarketData retrieved = marketDataDao.getMarketDataForDate(today);
        assertNotNull(retrieved);
        assertEquals(350.0, retrieved.getQqqOpen(), 0.001);
        assertEquals(355.0, retrieved.getQqqClose(), 0.001);
        assertEquals(20.0, retrieved.getVixOpen(), 0.001);
        assertEquals(19.5, retrieved.getVixClose(), 0.001);
    }
    
    @Test
    public void insertAndRetrieveSignalHistory() {
        LocalDate today = LocalDate.now();
        
        // First insert market data (due to foreign key constraint)
        MarketData data = new MarketData();
        data.setDate(today);
        data.setQqqOpen(350.0);
        marketDataDao.insert(data);
        
        // Then insert signal
        SignalHistory signal = new SignalHistory();
        signal.setDate(today);
        signal.setSignal(1);
        signal.setPositionChanged(true);
        signal.setSafeAsset("GLD");
        
        signalHistoryDao.insert(signal);
        
        SignalHistory retrieved = signalHistoryDao.getSignalForDate(today);
        assertNotNull(retrieved);
        assertEquals(Integer.valueOf(1), retrieved.getSignal());
        assertTrue(retrieved.getPositionChanged());
        assertEquals("GLD", retrieved.getSafeAsset());
    }
}
```

## 9. Best Practices

1. **Use Room's Strengths**: Leverage Room's compile-time verification, LiveData integration, and RxJava support.

2. **Background Processing**: Always perform database operations on background threads using AppExecutors or coroutines.

3. **Transactions**: Use transactions for operations that update multiple tables to maintain data consistency.

4. **Indexing**: Add indices to frequently queried columns for better performance.

5. **Data Pruning**: Implement a strategy to remove old data to prevent database bloat.

6. **Error Handling**: Implement robust error handling for database operations.

7. **Migration Strategy**: Plan for schema migrations as the app evolves.