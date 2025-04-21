package com.example.qqq3xstrategy.util;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowNotificationManager;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class NotificationActionReceiverTest {

    private NotificationActionReceiver receiver;
    
    @Mock
    private Context mockContext;
    
    @Mock
    private NotificationManagerCompat mockNotificationManager;
    
    @Mock
    private SharedPreferences mockPreferences;
    
    @Mock
    private SharedPreferences.Editor mockEditor;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        receiver = new NotificationActionReceiver();
        
        // Set up mocks
        when(mockPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor);
        
        // Set up NotificationManagerCompat mock
        NotificationManagerCompat.setTestInstance(mockNotificationManager);
        
        // Set up PreferenceManager mock
        PreferenceManager.setTestSharedPreferences(mockPreferences);
    }
    
    @Test
    public void testDismissAction() {
        // Arrange
        Intent intent = new Intent("ACTION_DISMISS");
        
        // Act
        receiver.onReceive(mockContext, intent);
        
        // Assert
        verify(mockNotificationManager, times(1)).cancel(1001); // Position change notification ID
        verify(mockEditor, times(1)).putLong(anyString(), anyLong());
        verify(mockEditor, times(1)).apply();
        
        // Clean up
        NotificationManagerCompat.setTestInstance(null);
        PreferenceManager.setTestSharedPreferences(null);
    }
    
    @Test
    public void testOtherAction() {
        // Arrange
        Intent intent = new Intent("SOME_OTHER_ACTION");
        
        // Act
        receiver.onReceive(mockContext, intent);
        
        // Assert
        verify(mockNotificationManager, times(0)).cancel(anyInt());
        
        // Clean up
        NotificationManagerCompat.setTestInstance(null);
        PreferenceManager.setTestSharedPreferences(null);
    }
}