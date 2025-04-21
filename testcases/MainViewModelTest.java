package com.example.qqq3xstrategy.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.work.WorkManager;

import com.example.qqq3xstrategy.data.database.AppDatabase;
import com.example.qqq3xstrategy.data.database.MarketDataDao;
import com.example.qqq3xstrategy.data.database.SignalHistoryDao;
import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.models.MarketDataWithSignal;
import com.example.qqq3xstrategy.data.models.SignalHistory;
import com.example.qqq3xstrategy.data.repository.YahooFinanceRepository;

import org.junit.Before;
import org.junit.Rule;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MainViewModelTest {

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
    
    @Mock
    private YahooFinanceRepository mockRepository;
    
    @Mock
    private WorkManager mockWorkManager;
    
    private MainViewModel viewModel;
    
    // LiveData for testing
    private MutableLiveData<MarketData> latestMarketData = new MutableLiveData<>();
    private MutableLiveData<SignalHistory> latestSignal = new MutableLiveData<>();
    private MutableLiveData<List<MarketDataWithSignal>> recentData = new MutableLiveData<>();
    
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
        when(mockMarketDataDao.getRecentMarketDataWithSignalLive(any(Integer.class))).thenReturn(recentData);
        
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
        testSignal.setPositionChanged(false);
        
        // Set initial values
        latestMarketData.setValue(testMarketData);
        latestSignal.setValue(testSignal);
        recentData.setValue(new ArrayList<>());
        
        // Override AppDatabase.getInstance
        AppDatabase.setTestInstance(mockDatabase);
        
        // Override YahooFinanceRepository.getInstance
        YahooFinanceRepository.setTestInstance(mockRepository);
        
        // Create view model
        viewModel = new MainViewModel(mockApplication);
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
        assertEquals(false, result[0].getPositionChanged());
    }
    
    @Test
    public void testRefreshData() {
        // Arrange
        when(mockRepository.fetchCurrentMarketData()).thenReturn(new MarketData());
        
        // Act
        viewModel.refreshData();
        
        // Assert
        verify(mockRepository, times(1)).fetchCurrentMarketData();
        verify(mockMarketDataDao, times(1)).insert(any(MarketData.class));
    }
    
    @Test
    public void testGetPositionText() throws InterruptedException {
        // Arrange
        SignalHistory leveragedSignal = new SignalHistory();
        leveragedSignal.setSignal(1);
        latestSignal.setValue(leveragedSignal);
        
        // Act
        String leveragedText = viewModel.getPositionText();
        
        // Assert
        assertEquals("LEVERAGED QQQ (3X)", leveragedText);
        
        // Test safe asset position
        SignalHistory safeSignal = new SignalHistory();
        safeSignal.setSignal(-1);
        safeSignal.setSafeAsset("SHY");
        latestSignal.setValue(safeSignal);
        
        // Act
        String safeText = viewModel.getPositionText();
        
        // Assert
        assertEquals("SAFE ASSET (SHY)", safeText);
    }
    
    @Test
    public void testGetRecentMarketData() throws InterruptedException {
        // Arrange
        List<MarketDataWithSignal> testData = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MarketDataWithSignal item = new MarketDataWithSignal();
            MarketData data = new MarketData();
            data.setDate(LocalDate.now().minusDays(i));
            data.setQqqClose(450.0 - i);
            data.setVixClose(20.0 + i);
            
            SignalHistory signal = new SignalHistory();
            signal.setSignal(i % 2 == 0 ? 1 : -1);
            
            item.marketData = data;
            item.signalHistory = signal;
            testData.add(item);
        }
        recentData.setValue(testData);
        
        // Act
        final CountDownLatch latch = new CountDownLatch(1);
        final List<MarketDataWithSignal>[] result = new List[1];
        
        viewModel.getRecentMarketData().observeForever(new Observer<List<MarketDataWithSignal>>() {
            @Override
            public void onChanged(List<MarketDataWithSignal> data) {
                result[0] = data;
                latch.countDown();
            }
        });
        
        // Wait for LiveData to emit
        latch.await(2, TimeUnit.SECONDS);
        
        // Assert
        assertNotNull("Recent data should not be null", result[0]);
        assertEquals(5, result[0].size());
        assertEquals(LocalDate.now(), result[0].get(0).marketData.getDate());
    }
}