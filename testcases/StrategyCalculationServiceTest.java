package com.example.qqq3xstrategy.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.example.qqq3xstrategy.data.database.AppDatabase;
import com.example.qqq3xstrategy.data.database.MarketDataDao;
import com.example.qqq3xstrategy.data.database.SignalHistoryDao;
import com.example.qqq3xstrategy.data.database.UserSettingsDao;
import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.models.SignalHistory;
import com.example.qqq3xstrategy.data.models.UserSettings;
import com.example.qqq3xstrategy.util.AppExecutors;
import com.example.qqq3xstrategy.util.NotificationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class StrategyCalculationServiceTest {

    private StrategyCalculationService service;
    
    @Mock
    private AppDatabase mockDatabase;
    
    @Mock
    private MarketDataDao mockMarketDataDao;
    
    @Mock
    private SignalHistoryDao mockSignalHistoryDao;
    
    @Mock
    private UserSettingsDao mockUserSettingsDao;
    
    @Mock
    private NotificationHelper mockNotificationHelper;
    
    @Mock
    private AppExecutors mockExecutors;
    
    @Mock
    private Executor mockDiskExecutor;
    
    @Mock
    private Executor mockMainExecutor;
    
    private UserSettings testSettings;
    private List<MarketData> testHistoricalData;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Set up test data
        testSettings = new UserSettings();
        testSettings.setSmaShort(5);
        testSettings.setSmaLong(15);
        testSettings.setSmaShort2(5);
        testSettings.setSmaLong2(15);
        testSettings.setSmaShort3(3);
        testSettings.setSmaLong3(10);
        testSettings.setSmaYear(155);
        testSettings.setPreferredSafeAsset("SHY");
        testSettings.setNotificationsEnabled(true);
        
        // Create test historical data
        testHistoricalData = createTestHistoricalData();
        
        // Set up mocks
        when(mockDatabase.marketDataDao()).thenReturn(mockMarketDataDao);
        when(mockDatabase.signalHistoryDao()).thenReturn(mockSignalHistoryDao);
        when(mockDatabase.userSettingsDao()).thenReturn(mockUserSettingsDao);
        
        when(mockUserSettingsDao.getSettings()).thenReturn(testSettings);
        when(mockMarketDataDao.getMarketDataBetweenDates(any(), any())).thenReturn(testHistoricalData);
        
        when(mockExecutors.diskIO()).thenReturn(mockDiskExecutor);
        when(mockExecutors.mainThread()).thenReturn(mockMainExecutor);
        
        // Make the disk executor run immediately for testing
        when(mockDiskExecutor.execute(any())).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        });
        
        // Make the main executor run immediately for testing
        when(mockMainExecutor.execute(any())).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        });
        
        // Create service with mocks
        service = new StrategyCalculationService();
        // Inject mocks using reflection
        try {
            java.lang.reflect.Field dbField = StrategyCalculationService.class.getDeclaredField("database");
            dbField.setAccessible(true);
            dbField.set(service, mockDatabase);
            
            java.lang.reflect.Field executorsField = StrategyCalculationService.class.getDeclaredField("executors");
            executorsField.setAccessible(true);
            executorsField.set(service, mockExecutors);
            
            java.lang.reflect.Field notificationHelperField = StrategyCalculationService.class.getDeclaredField("notificationHelper");
            notificationHelperField.setAccessible(true);
            notificationHelperField.set(service, mockNotificationHelper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testEndToEndCalculation() {
        // Arrange
        double vixOpen = 25.0;
        double qqqOpen = 450.0;
        
        // Create intent with test data
        Intent intent = new Intent();
        intent.putExtra("vix_open", vixOpen);
        intent.putExtra("qqq_open", qqqOpen);
        
        // Act
        service.onStartCommand(intent, 0, 0);
        
        // Assert
        // Verify that the signal was calculated and saved
        ArgumentCaptor<SignalHistory> signalCaptor = ArgumentCaptor.forClass(SignalHistory.class);
        verify(mockSignalHistoryDao).insert(signalCaptor.capture());
        
        SignalHistory capturedSignal = signalCaptor.getValue();
        assertNotNull("Signal should not be null", capturedSignal);
        assertEquals("Signal date should be today", LocalDate.now(), capturedSignal.getDate());
    }
    
    @Test
    public void testSignalPersistence() {
        // Arrange
        double vixOpen = 25.0;
        double qqqOpen = 450.0;
        
        // Act
        // Call the calculateStrategy method directly
        try {
            java.lang.reflect.Method method = StrategyCalculationService.class.getDeclaredMethod("calculateStrategy", double.class, double.class);
            method.setAccessible(true);
            method.invoke(service, vixOpen, qqqOpen);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // Assert
        // Verify that the signal was saved to the database
        verify(mockSignalHistoryDao, times(1)).insert(any(SignalHistory.class));
    }
    
    @Test
    public void testPositionChangeDetection_NoChange() {
        // Arrange
        double vixOpen = 25.0;
        double qqqOpen = 450.0;
        
        // Set up previous signal with same value (no change)
        SignalHistory previousSignal = new SignalHistory();
        previousSignal.setDate(LocalDate.now().minusDays(1));
        previousSignal.setSignal(1); // Leveraged position
        when(mockSignalHistoryDao.getLatestSignal()).thenReturn(previousSignal);
        
        // Act
        try {
            java.lang.reflect.Method method = StrategyCalculationService.class.getDeclaredMethod("calculateStrategy", double.class, double.class);
            method.setAccessible(true);
            method.invoke(service, vixOpen, qqqOpen);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // Assert
        // Verify that positionChanged flag is false
        ArgumentCaptor<SignalHistory> signalCaptor = ArgumentCaptor.forClass(SignalHistory.class);
        verify(mockSignalHistoryDao).insert(signalCaptor.capture());
        
        SignalHistory capturedSignal = signalCaptor.getValue();
        assertEquals("Position should not have changed", false, capturedSignal.getPositionChanged());
        
        // Verify notification was not sent
        verify(mockNotificationHelper, times(0)).sendPositionChangeNotification(anyInt(), any(Boolean.class));
    }
    
    @Test
    public void testPositionChangeDetection_WithChange() {
        // Arrange
        double vixOpen = 35.0; // High VIX to trigger safe asset
        double qqqOpen = 400.0;
        
        // Set up previous signal with different value (position change)
        SignalHistory previousSignal = new SignalHistory();
        previousSignal.setDate(LocalDate.now().minusDays(1));
        previousSignal.setSignal(1); // Leveraged position
        when(mockSignalHistoryDao.getLatestSignal()).thenReturn(previousSignal);
        
        // Act
        try {
            java.lang.reflect.Method method = StrategyCalculationService.class.getDeclaredMethod("calculateStrategy", double.class, double.class);
            method.setAccessible(true);
            method.invoke(service, vixOpen, qqqOpen);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // Assert
        // Verify that positionChanged flag is true
        ArgumentCaptor<SignalHistory> signalCaptor = ArgumentCaptor.forClass(SignalHistory.class);
        verify(mockSignalHistoryDao).insert(signalCaptor.capture());
        
        SignalHistory capturedSignal = signalCaptor.getValue();
        assertEquals("Position should have changed", true, capturedSignal.getPositionChanged());
        
        // Verify notification was sent
        verify(mockNotificationHelper, times(1)).sendPositionChangeNotification(anyInt(), any(Boolean.class));
    }
    
    /**
     * Helper method to create test historical data
     */
    private List<MarketData> createTestHistoricalData() {
        List<MarketData> data = new ArrayList<>();
        
        // Create enough data points for all SMAs
        int dataPoints = 200;
        LocalDate startDate = LocalDate.now().minusDays(dataPoints);
        
        // Base values
        double qqqBase = 400.0;
        double vixBase = 25.0;
        double gldBase = 180.0;
        double shyBase = 80.0;
        
        // Create data points
        for (int i = 0; i < dataPoints; i++) {
            MarketData point = new MarketData();
            point.setDate(startDate.plusDays(i));
            
            // Calculate prices with trend
            double trendFactor = 0.1;
            double dayFactor = (double) i / dataPoints;
            
            double qqqPrice = qqqBase * (1 + trendFactor * dayFactor);
            double vixPrice = vixBase * (1 - trendFactor * dayFactor); // VIX moves opposite to market
            
            // Set values
            point.setQqqOpen(qqqPrice * 0.99);
            point.setQqqClose(qqqPrice);
            point.setQqqHigh(qqqPrice * 1.02);
            point.setQqqLow(qqqPrice * 0.98);
            point.setQqqVolume(1000000L);
            
            point.setVixOpen(vixPrice * 0.99);
            point.setVixClose(vixPrice);
            point.setVixHigh(vixPrice * 1.05);
            point.setVixLow(vixPrice * 0.95);
            
            point.setGldOpen(gldBase * 0.99);
            point.setGldClose(gldBase);
            
            point.setShyOpen(shyBase * 0.99);
            point.setShyClose(shyBase);
            
            data.add(point);
        }
        
        return data;
    }
}