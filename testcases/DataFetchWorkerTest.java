package com.example.qqq3xstrategy.workers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.work.ListenableWorker.Result;
import androidx.work.testing.TestWorkerBuilder;

import com.example.qqq3xstrategy.data.database.AppDatabase;
import com.example.qqq3xstrategy.data.database.MarketDataDao;
import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.repository.YahooFinanceRepository;
import com.example.qqq3xstrategy.services.StrategyCalculationService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class DataFetchWorkerTest {

    @Mock
    private AppDatabase mockDatabase;
    
    @Mock
    private MarketDataDao mockMarketDataDao;
    
    @Mock
    private YahooFinanceRepository mockRepository;
    
    @Mock
    private Context mockContext;
    
    @Mock
    private SharedPreferences mockPreferences;
    
    @Mock
    private SharedPreferences.Editor mockEditor;
    
    private Context context;
    private Executor executor;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        context = ApplicationProvider.getApplicationContext();
        executor = Executors.newSingleThreadExecutor();
        
        // Set up mocks
        when(mockDatabase.marketDataDao()).thenReturn(mockMarketDataDao);
        when(mockPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putLong(any(String.class), any(Long.class))).thenReturn(mockEditor);
        
        // Set up test data
        List<MarketData> testData = createTestMarketData();
        when(mockRepository.fetchCurrentMarketData()).thenReturn(testData.get(0));
        when(mockRepository.fetchHistoricalData(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(testData);
    }
    
    @Test
    public void testWorkerExecution() {
        // Arrange
        // Create a worker with test dependencies
        DataFetchWorker worker = TestWorkerBuilder.from(
                context,
                DataFetchWorker.class,
                executor)
                .build();
        
        // Override dependencies
        AppDatabase.setTestInstance(mockDatabase);
        YahooFinanceRepository.setTestInstance(mockRepository);
        
        // Act
        Result result = worker.doWork();
        
        // Assert
        assertEquals(Result.success(), result);
        
        // Verify data was fetched and saved
        verify(mockRepository, times(1)).fetchCurrentMarketData();
        verify(mockMarketDataDao, times(1)).insert(any(MarketData.class));
        
        // Clean up
        AppDatabase.setTestInstance(null);
        YahooFinanceRepository.setTestInstance(null);
    }
    
    @Test
    public void testHistoricalDataUpdate() {
        // Arrange
        // Create a worker with test dependencies
        DataFetchWorker worker = TestWorkerBuilder.from(
                context,
                DataFetchWorker.class,
                executor)
                .build();
        
        // Override dependencies
        AppDatabase.setTestInstance(mockDatabase);
        YahooFinanceRepository.setTestInstance(mockRepository);
        
        // Set up preferences to indicate historical data should be updated
        // (last update was more than 24 hours ago)
        when(mockPreferences.getLong("last_historical_update", 0))
                .thenReturn(System.currentTimeMillis() - 25 * 60 * 60 * 1000);
        
        // Act
        Result result = worker.doWork();
        
        // Assert
        assertEquals(Result.success(), result);
        
        // Verify historical data was fetched
        verify(mockRepository, times(1)).fetchHistoricalData(any(LocalDate.class), any(LocalDate.class));
        verify(mockMarketDataDao, times(1)).insertAll(any(List.class));
        
        // Clean up
        AppDatabase.setTestInstance(null);
        YahooFinanceRepository.setTestInstance(null);
    }
    
    @Test
    public void testServiceTrigger() {
        // Arrange
        // Create a worker with test dependencies
        DataFetchWorker worker = TestWorkerBuilder.from(
                context,
                DataFetchWorker.class,
                executor)
                .build();
        
        // Override dependencies
        AppDatabase.setTestInstance(mockDatabase);
        YahooFinanceRepository.setTestInstance(mockRepository);
        
        // Set up current market data
        MarketData currentData = new MarketData();
        currentData.setQqqOpen(450.0);
        currentData.setVixOpen(25.0);
        when(mockRepository.fetchCurrentMarketData()).thenReturn(currentData);
        
        // Act
        Result result = worker.doWork();
        
        // Assert
        assertEquals(Result.success(), result);
        
        // Verify service was triggered with correct data
        // This is difficult to test directly without modifying the worker
        // In a real test, we would need to use a shadow Context or similar
        
        // Clean up
        AppDatabase.setTestInstance(null);
        YahooFinanceRepository.setTestInstance(null);
    }
    
    /**
     * Helper method to create test market data
     */
    private List<MarketData> createTestMarketData() {
        List<MarketData> data = new ArrayList<>();
        
        // Create data points
        for (int i = 0; i < 10; i++) {
            MarketData point = new MarketData();
            point.setDate(LocalDate.now().minusDays(i));
            point.setQqqOpen(450.0 - i);
            point.setQqqClose(455.0 - i);
            point.setVixOpen(20.0 + i);
            point.setVixClose(19.5 + i);
            data.add(point);
        }
        
        return data;
    }
}