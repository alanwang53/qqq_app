package com.example.qqq3xstrategy.receivers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import android.content.Context;
import android.content.Intent;

import com.example.qqq3xstrategy.util.NotificationHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class BootReceiverTest {

    private BootReceiver bootReceiver;
    
    @Mock
    private Context mockContext;
    
    @Mock
    private NotificationHelper mockNotificationHelper;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        bootReceiver = new BootReceiver();
        
        // Set up test instance for NotificationHelper
        NotificationHelper.setTestInstance(mockNotificationHelper);
    }
    
    @Test
    public void testBootCompleted() {
        // Arrange
        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        
        // Act
        bootReceiver.onReceive(mockContext, intent);
        
        // Assert
        verify(mockNotificationHelper, times(1)).scheduleNotificationWork();
        
        // Clean up
        NotificationHelper.setTestInstance(null);
    }
    
    @Test
    public void testOtherAction() {
        // Arrange
        Intent intent = new Intent("some.other.action");
        
        // Act
        bootReceiver.onReceive(mockContext, intent);
        
        // Assert
        verifyNoInteractions(mockNotificationHelper);
        
        // Clean up
        NotificationHelper.setTestInstance(null);
    }
}