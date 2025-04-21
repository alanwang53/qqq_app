package com.example.qqq3xstrategy.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.qqq3xstrategy.data.database.AppDatabase;
import com.example.qqq3xstrategy.data.database.SignalHistoryDao;
import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.models.SignalHistory;
import com.example.qqq3xstrategy.data.models.UserSettings;
import com.example.qqq3xstrategy.strategy.QQQ3XStrategy;
import com.example.qqq3xstrategy.util.AppExecutors;
import com.example.qqq3xstrategy.util.NotificationHelper;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for calculating strategy signals
 */
public class StrategyCalculationService extends Service {
    private static final String TAG = "StrategyCalcService";
    
    private AppDatabase database;
    private AppExecutors executors;
    private NotificationHelper notificationHelper;
    
    @Override
    public void onCreate() {
        super.onCreate();
        database = AppDatabase.getInstance(this);
        executors = AppExecutors.getInstance();
        notificationHelper = new NotificationHelper(this);
        Log.d(TAG, "StrategyCalculationService created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            double vixOpen = intent.getDoubleExtra("vix_open", 0);
            double qqqOpen = intent.getDoubleExtra("qqq_open", 0);
            
            if (vixOpen > 0 && qqqOpen > 0) {
                calculateStrategy(vixOpen, qqqOpen);
            } else {
                Log.e(TAG, "Invalid VIX or QQQ open values");
            }
        }
        
        return START_NOT_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    /**
     * Calculate strategy signal and send notification if needed
     */
    private void calculateStrategy(double vixOpen, double qqqOpen) {
        executors.diskIO().execute(() -> {
            try {
                Log.d(TAG, "Calculating strategy for VIX=" + vixOpen + ", QQQ=" + qqqOpen);
                
                // Get user settings
                UserSettings settings = database.userSettingsDao().getSettings();
                
                // Get historical data (2 years)
                LocalDate today = LocalDate.now();
                LocalDate twoYearsAgo = today.minusYears(2);
                List<MarketData> historicalData = database.marketDataDao().getMarketDataBetweenDates(twoYearsAgo, today);
                
                if (historicalData.isEmpty()) {
                    Log.e(TAG, "No historical data available");
                    return;
                }
                
                // Calculate strategy signal
                QQQ3XStrategy strategy = new QQQ3XStrategy(settings);
                int signal = strategy.calculateSignal(historicalData, vixOpen, qqqOpen);
                
                // Get previous signal
                SignalHistoryDao signalDao = database.signalHistoryDao();
                SignalHistory previousSignal = signalDao.getLatestSignal();
                
                // Determine if position changed
                boolean positionChanged = previousSignal == null || previousSignal.getSignal() != signal;
                
                // Create new signal history entry
                SignalHistory newSignal = new SignalHistory();
                newSignal.setDate(today);
                newSignal.setSignal(signal);
                newSignal.setRawSignal(signal); // In this case, raw signal is the same as final signal
                newSignal.setPositionChanged(positionChanged);
                newSignal.setSafeAsset(settings.getPreferredSafeAsset());
                
                // Save to database
                signalDao.insert(newSignal);
                
                Log.d(TAG, "Strategy calculation complete. Signal: " + signal + 
                        ", Position changed: " + positionChanged);
                
                // Send notification if position changed
                if (positionChanged && settings.isNotificationsEnabled()) {
                    executors.mainThread().execute(() -> {
                        notificationHelper.sendPositionChangeNotification(signal, positionChanged);
                    });
                }
                
                // Schedule notification for 9:10 AM
                notificationHelper.scheduleNotificationWork();
                
            } catch (Exception e) {
                Log.e(TAG, "Error calculating strategy", e);
            }
        });
    }
}