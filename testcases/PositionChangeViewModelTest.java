package com.example.qqq3xstrategy.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.qqq3xstrategy.data.database.AppDatabase;
import com.example.qqq3xstrategy.data.database.MarketDataDao;
import com.example.qqq3xstrategy.data.database.SignalHistoryDao;
import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.models.SignalHistory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class PositionChangeViewModelTest {

    // Rule for testing LiveData
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    
    @Mock
    private Application mockApplication;
    
    @Mock
    private AppDatabase mockDatabase;
    
    @Mock
    private MarketDataDao mockMarketDataDao;
    
    @Mock
    private SignalHistoryDao mockSignalHistoryDao;
    
    private PositionChangeViewModel viewModel;
    
    // LiveData for testing
    private MutableLiveData<MarketData> latestMarketData = new MutableLiveData<>();
    private MutableLiveData<SignalHistory> latestSignal = new MutableLiveData<>();
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Set up mocks
        when(mockApplication.getApplicationContext()).thenReturn(mockApplication);
        when(mockDatabase.marketDataDao()).thenReturn(mockMarketDataDao);
        when(mockDatabase.signalHistoryDao()).thenReturn(mockSignalHistoryDao);
        
        // Set up LiveData
        when(mockMarketDataDao.getLatestMarketDataLive()).thenReturn(latestMarketData);
        when(mockSignalHistoryDao.getLatestSignalLive()).thenReturn(latestSignal);
        
        // Set up test data
        MarketData testMarketData = new MarketData();
        testMarketData.setDate(LocalDate.now());
        testMarketData.setQqqOpen(450.0);
        testMarketData.setQqqClose(455.0);
        testMarketData.setVixOpen(20.0);
        testMarketData.setVixClose(19.5);
        
        SignalHistory testSignal = new SignalHistory();
        testSignal.setDate(LocalDate.now());
        testSignal.setSignal(1); // Leveraged position
        testSignal.setPositionChanged(true);
        
        // Set initial values
        latestMarketData.setValue(testMarketData);
        latestSignal.setValue(testSignal);
        
        // Override AppDatabase.getInstance
        AppDatabase.setTestInstance(mockDatabase);
        
        // Create view model with signal parameter
        viewModel = new PositionChangeViewModel(mockApplication, 1);
    }
    
    @Test
    public void testGetLatestMarketData() throws InterruptedException {
        // Arrange
        final CountDownLatch latch = new CountDownLatch(1);
        final MarketData[] result = new MarketData[1];
        
        // Act
        viewModel.getLatestMarketData().observeForever(new Observer<MarketData>() {
            @Override
            public void onChanged(MarketData marketData) {
                result[0] = marketData;
                latch.countDown();
            }
        });
        
        // Wait for LiveData to emit
        latch.await(2, TimeUnit.SECONDS);
        
        // Assert
        assertNotNull("Market data should not be null", result[0]);
        assertEquals(LocalDate.now(), result[0].getDate());
        assertEquals(450.0, result[0].getQqqOpen(), 0.001);
    }
    
    @Test
    public void testGetLatestSignal() throws InterruptedException {
        // Arrange
        final CountDownLatch latch = new CountDownLatch(1);
        final SignalHistory[] result = new SignalHistory[1];
        
        // Act
        viewModel.getLatestSignal().observeForever(new Observer<SignalHistory>() {
            @Override
            public void onChanged(SignalHistory signalHistory) {
                result[0] = signalHistory;
                latch.countDown();
            }
        });
        
        // Wait for LiveData to emit
        latch.await(2, TimeUnit.SECONDS);
        
        // Assert
        assertNotNull("Signal should not be null", result[0]);
        assertEquals(1, result[0].getSignal());
        assertEquals(true, result[0].getPositionChanged());
    }
    
    @Test
    public void testGetPositionText() {
        // Arrange - leveraged position is set in setUp
        
        // Act
        String leveragedText = viewModel.getPositionText();
        
        // Assert
        assertEquals("LEVERAGED QQQ (3X)", leveragedText);
        
        // Test safe asset position
        viewModel = new PositionChangeViewModel(mockApplication, -1);
        
        // Act
        String safeText = viewModel.getPositionText();
        
        // Assert
        assertEquals("SAFE ASSET", safeText);
    }
    
    @Test
    public void testMarkAsActioned() {
        // Arrange
        SignalHistory signal = new SignalHistory();
        signal.setId(1);
        signal.setSignal(1);
        signal.setPositionChanged(true);
        signal.setActioned(false);
        when(mockSignalHistoryDao.getLatestSignal()).thenReturn(signal);
        
        // Act
        viewModel.markAsActioned();
        
        // Assert
        verify(mockSignalHistoryDao, times(1)).update(any(SignalHistory.class));
    }
    
    @Test
    public void testGetRecommendationText() {
        // Test leveraged position
        viewModel = new PositionChangeViewModel(mockApplication, 1);
        assertEquals("Switch to LEVERAGED QQQ (3X)", viewModel.getRecommendationText());
        
        // Test safe asset position
        viewModel = new PositionChangeViewModel(mockApplication, -1);
        assertEquals("Switch to SAFE ASSET", viewModel.getRecommendationText());
    }
    
    @Test
    public void testGetRecommendationDetails() {
        // Test leveraged position
        viewModel = new PositionChangeViewModel(mockApplication, 1);
        String leveragedDetails = viewModel.getRecommendationDetails();
        assertNotNull(leveragedDetails);
        
        // Test safe asset position
        viewModel = new PositionChangeViewModel(mockApplication, -1);
        String safeDetails = viewModel.getRecommendationDetails();
        assertNotNull(safeDetails);
    }
}