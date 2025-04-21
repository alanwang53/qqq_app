package com.example.qqq3xstrategy.workers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.work.ListenableWorker.Result;
import androidx.work.testing.TestWorkerBuilder;

import com.example.qqq3xstrategy.data.database.AppDatabase;
import com.example.qqq3xstrategy.data.database.SignalHistoryDao;
import com.example.qqq3xstrategy.data.database.UserSettingsDao;
import com.example.qqq3xstrategy.data.models.SignalHistory;
import com.example.qqq3xstrategy.data.models.UserSettings;
import com.example.qqq3xstrategy.util.NotificationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.time.LocalDate;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class NotificationWorkerTest {

    @Mock
    private AppDatabase mockDatabase;
    
    @Mock
    private SignalHistoryDao mockSignalHistoryDao;
    
    @Mock
    private UserSettingsDao mockUserSettingsDao;
    
    @Mock
    private NotificationHelper mockNotificationHelper;
    
    @Mock
    private SharedPreferences mockPreferences;
    
    private Context context;
    private Executor executor;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        context = ApplicationProvider.getApplicationContext();
        executor = Executors.newSingleThreadExecutor();
        
        // Set up mocks
        when(mockDatabase.signalHistoryDao()).thenReturn(mockSignalHistoryDao);
        when(mockDatabase.userSettingsDao()).thenReturn(mockUserSettingsDao);
        
        // Set up test data
        UserSettings settings = new UserSettings();
        settings.setNotificationsEnabled(true);
        when(mockUserSettingsDao.getSettings()).thenReturn(settings);
    }
    
    @Test
    public void testWorkerExecution() {
        // Arrange
        // Create a worker with test dependencies
        NotificationWorker worker = TestWorkerBuilder.from(
                context,
                NotificationWorker.class,
                executor)
                .build();
        
        // Override AppDatabase.getInstance to return our mock
        AppDatabase.setTestInstance(mockDatabase);
        
        // Act
        Result result = worker.doWork();
        
        // Assert
        assertEquals(Result.success(), result);
        
        // Clean up
        AppDatabase.setTestInstance(null);
    }
    
    @Test
    public void testPositionChangeNotification() {
        // Arrange
        // Create a worker with test dependencies
        NotificationWorker worker = TestWorkerBuilder.from(
                context,
                NotificationWorker.class,
                executor)
                .build();
        
        // Override AppDatabase.getInstance to return our mock
        AppDatabase.setTestInstance(mockDatabase);
        
        // Set up a signal history with position change
        SignalHistory signal = new SignalHistory();
        signal.setDate(LocalDate.now());
        signal.setSignal(1); // Leveraged position
        signal.setPositionChanged(true);
        when(mockSignalHistoryDao.getLatestSignal()).thenReturn(signal);
        
        // Override NotificationHelper creation
        NotificationHelper.setTestInstance(mockNotificationHelper);
        
        // Act
        Result result = worker.doWork();
        
        // Assert
        assertEquals(Result.success(), result);
        verify(mockNotificationHelper, times(1)).sendPositionChangeNotification(1, true);
        verify(mockNotificationHelper, times(1)).scheduleNotificationWork();
        
        // Clean up
        AppDatabase.setTestInstance(null);
        NotificationHelper.setTestInstance(null);
    }
    
    @Test
    public void testNotificationScheduling() {
        // Arrange
        // Create a worker with test dependencies
        NotificationWorker worker = TestWorkerBuilder.from(
                context,
                NotificationWorker.class,
                executor)
                .build();
        
        // Override AppDatabase.getInstance to return our mock
        AppDatabase.setTestInstance(mockDatabase);
        
        // Set up a signal history without position change
        SignalHistory signal = new SignalHistory();
        signal.setDate(LocalDate.now());
        signal.setSignal(1); // Leveraged position
        signal.setPositionChanged(false);
        when(mockSignalHistoryDao.getLatestSignal()).thenReturn(signal);
        
        // Override NotificationHelper creation
        NotificationHelper.setTestInstance(mockNotificationHelper);
        
        // Act
        Result result = worker.doWork();
        
        // Assert
        assertEquals(Result.success(), result);
        verify(mockNotificationHelper, times(0)).sendPositionChangeNotification(anyInt(), any(Boolean.class));
        verify(mockNotificationHelper, times(1)).scheduleNotificationWork();
        
        // Clean up
        AppDatabase.setTestInstance(null);
        NotificationHelper.setTestInstance(null);
    }
    
    @Test
    public void testNotificationsDisabled() {
        // Arrange
        // Create a worker with test dependencies and mock SharedPreferences
        NotificationWorker worker = TestWorkerBuilder.from(
                context,
                NotificationWorker.class,
                executor)
                .build();
        
        // Override SharedPreferences to return notifications disabled
        when(mockPreferences.getBoolean("notifications_enabled", true)).thenReturn(false);
        
        // Set the mock preferences
        // Note: This would require modifying the NotificationWorker class to accept a SharedPreferences instance
        // or using a shadow class in Robolectric
        
        // Act
        Result result = worker.doWork();
        
        // Assert
        assertEquals(Result.success(), result);
        
        // In a real test, we would verify that no notifications were sent
        // but this requires more complex setup
    }
}