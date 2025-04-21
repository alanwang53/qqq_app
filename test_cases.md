# QQQ3X Strategy Android App Test Cases

## 1. Unit Tests

### 1.1 Strategy Logic Tests

#### QQQ3XStrategyTest
- **testCalculateSignalWithLeveragedConditions**: Verify signal is 1 (leveraged) when VIX < 21 and QQQ is in uptrend
- **testCalculateSignalWithSafeAssetConditions**: Verify signal is -1 (safe asset) when VIX > 32 and QQQ is in downtrend
- **testCalculateSignalWithVixUpMuch**: Verify signal is -1 when VIX increases by more than 20%
- **testCalculateSignalWithQqqYearUp**: Verify signal is 1 when QQQ is above 1.03 × SMA(155)
- **testCalculateSignalWithQqqYearDown**: Verify signal is -1 when QQQ is below 0.99 × SMA(155)
- **testCalculateSignalWithVixAbove66**: Verify signal is 1 when VIX > 66
- **testCalculateSignalWithInsufficientData**: Verify default to safe asset (-1) when insufficient historical data
- **testCalculateSMA**: Verify SMA calculation is correct for various periods

### 1.2 Data Model Tests

#### MarketDataTest
- **testMarketDataCreation**: Verify MarketData object can be created with all fields
- **testMarketDataEquality**: Verify equals() and hashCode() work correctly

#### SignalHistoryTest
- **testSignalHistoryCreation**: Verify SignalHistory object can be created with all fields
- **testPositionChangedFlag**: Verify positionChanged flag is set correctly

### 1.3 Repository Tests

#### YahooFinanceRepositoryTest
- **testFetchCurrentMarketData**: Verify repository can fetch current market data
- **testFetchHistoricalData**: Verify repository can fetch historical data
- **testMergeHistoricalData**: Verify data from different symbols is merged correctly
- **testCacheFallback**: Verify repository falls back to cached data when network fails

### 1.4 Database Tests

#### MarketDataDaoTest
- **testInsertAndRetrieveMarketData**: Verify data can be inserted and retrieved
- **testGetLatestMarketData**: Verify latest market data is returned correctly
- **testGetMarketDataBetweenDates**: Verify date range queries work correctly

#### SignalHistoryDaoTest
- **testInsertAndRetrieveSignalHistory**: Verify data can be inserted and retrieved
- **testGetLatestSignal**: Verify latest signal is returned correctly
- **testGetRecentPositionChanges**: Verify position changes are returned correctly

#### TechnicalIndicatorDaoTest
- **testInsertAndRetrieveTechnicalIndicator**: Verify data can be inserted and retrieved
- **testGetLatestIndicator**: Verify latest indicator is returned correctly

## 2. Integration Tests

### 2.1 Data Flow Tests

#### StrategyCalculationTest
- **testEndToEndCalculation**: Verify market data flows through repository, database, and strategy calculation
- **testSignalPersistence**: Verify calculated signals are saved to database
- **testPositionChangeDetection**: Verify position changes are correctly identified

### 2.2 Background Task Tests

#### DataFetchWorkerTest
- **testWorkerExecution**: Verify worker executes and fetches data
- **testHistoricalDataUpdate**: Verify historical data is updated once per day
- **testServiceTrigger**: Verify StrategyCalculationService is triggered after data fetch

#### NotificationWorkerTest
- **testWorkerExecution**: Verify worker executes and checks for position changes
- **testNotificationScheduling**: Verify next notification is scheduled

### 2.3 Notification Tests

#### NotificationHelperTest
- **testPositionChangeNotification**: Verify position change notifications are created correctly
- **testMarketUpdateNotification**: Verify market update notifications are created correctly
- **testNotificationScheduling**: Verify notifications are scheduled for 9:10 AM PT

## 3. UI Tests

### 3.1 MainActivity Tests

#### MainActivityTest
- **testInitialDisplay**: Verify main activity displays correctly on launch
- **testDataRefresh**: Verify refresh button triggers data update
- **testNavigationToHistory**: Verify navigation to history screen works
- **testNavigationToSettings**: Verify navigation to settings screen works
- **testPositionDisplay**: Verify current position is displayed correctly
- **testMarketDataDisplay**: Verify market data is displayed correctly

### 3.2 PositionChangeDetailsActivity Tests

#### PositionChangeDetailsActivityTest
- **testInitialDisplay**: Verify details activity displays correctly
- **testMarkAsActioned**: Verify "Mark as Actioned" button works
- **testNavigationBack**: Verify back navigation works

## 4. System Tests

### 4.1 Scheduled Execution Tests

#### ScheduledExecutionTest
- **testMorningDataFetch**: Verify data is fetched at 9:00 AM PT
- **testNotificationDelivery**: Verify notifications are delivered at 9:10 AM PT
- **testBootRescheduling**: Verify tasks are rescheduled after device reboot

### 4.2 Network Condition Tests

#### NetworkConditionTest
- **testSlowNetwork**: Verify app handles slow network connections
- **testNetworkFailure**: Verify app handles network failures gracefully
- **testReconnection**: Verify app recovers when network becomes available

### 4.3 Device State Tests

#### DeviceStateTest
- **testLowBattery**: Verify app functions correctly under low battery conditions
- **testBackgroundExecution**: Verify background tasks execute when app is not in foreground
- **testDeviceSleep**: Verify scheduled tasks wake device if needed

## 5. Instrumented Tests

### 5.1 End-to-End Tests

#### AppFlowTest
- **testCompleteAppFlow**: Simulate a full day cycle including data fetch, calculation, and notification
- **testPositionChangeFlow**: Simulate a position change scenario and verify user flow

### 5.2 Performance Tests

#### PerformanceTest
- **testDatabasePerformance**: Measure database operation performance with large datasets
- **testNetworkPerformance**: Measure network request performance
- **testUIResponsiveness**: Verify UI remains responsive during background operations

## 6. Test Implementation Examples

### 6.1 Unit Test Example (JUnit)

```java
@Test
public void testCalculateSignalWithLeveragedConditions() {
    // Arrange
    List<MarketData> historicalData = createMockHistoricalData(true); // uptrend data
    double vixOpenToday = 18.5; // VIX < 21
    double qqqOpenToday = 450.0; // QQQ in uptrend
    
    UserSettings settings = new UserSettings();
    QQQ3XStrategy strategy = new QQQ3XStrategy(settings);
    
    // Act
    int signal = strategy.calculateSignal(historicalData, vixOpenToday, qqqOpenToday);
    
    // Assert
    assertEquals(1, signal); // Should recommend leveraged position
}

@Test
public void testCalculateSignalWithSafeAssetConditions() {
    // Arrange
    List<MarketData> historicalData = createMockHistoricalData(false); // downtrend data
    double vixOpenToday = 35.0; // VIX > 32
    double qqqOpenToday = 430.0; // QQQ in downtrend
    
    UserSettings settings = new UserSettings();
    QQQ3XStrategy strategy = new QQQ3XStrategy(settings);
    
    // Act
    int signal = strategy.calculateSignal(historicalData, vixOpenToday, qqqOpenToday);
    
    // Assert
    assertEquals(-1, signal); // Should recommend safe asset
}
```

### 6.2 UI Test Example (Espresso)

```java
@Test
public void testPositionDisplay() {
    // Set up test data in the database
    SignalHistory signal = new SignalHistory();
    signal.setDate(LocalDate.now());
    signal.setSignal(1); // Leveraged position
    signal.setPositionChanged(false);
    
    // Launch activity
    ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
    
    // Verify position text is displayed correctly
    onView(withId(R.id.tv_current_position))
        .check(matches(withText("LEVERAGED QQQ (3X)")));
}

@Test
public void testDataRefresh() {
    // Launch activity
    ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
    
    // Click refresh button
    onView(withId(R.id.btn_refresh_data)).perform(click());
    
    // Verify toast message is shown
    onView(withText("Refreshing data..."))
        .inRoot(withDecorView(not(is(getActivity(scenario).getWindow().getDecorView()))))
        .check(matches(isDisplayed()));
}
```

### 6.3 Integration Test Example

```java
@Test
public void testEndToEndCalculation() {
    // Set up test context
    Context context = ApplicationProvider.getApplicationContext();
    
    // Insert test market data
    AppDatabase database = AppDatabase.getInstance(context);
    List<MarketData> testData = createTestMarketData();
    database.marketDataDao().insertAll(testData);
    
    // Create service and run calculation
    StrategyCalculationService service = new StrategyCalculationService();
    service.calculateStrategy(25.0, 440.0); // VIX and QQQ values
    
    // Verify signal was calculated and saved
    SignalHistory signal = database.signalHistoryDao().getLatestSignal();
    assertNotNull(signal);
    assertEquals(LocalDate.now(), signal.getDate());
}
```

## 7. Test Automation Strategy

### 7.1 CI/CD Integration

- Configure GitHub Actions or similar CI/CD service to run unit tests on every commit
- Run instrumented tests nightly on a device farm
- Generate test coverage reports and enforce minimum coverage thresholds

### 7.2 Test Data Management

- Create a set of realistic test data fixtures
- Implement database seeding for instrumented tests
- Use mock web server for network tests

### 7.3 Test Environment

- Test on multiple Android versions (API 26+)
- Test on different screen sizes (phone and tablet)
- Test with different system settings (language, time zone, etc.)