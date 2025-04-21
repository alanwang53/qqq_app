package com.example.qqq3xstrategy.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import com.example.qqq3xstrategy.data.database.AppDatabase;
import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.models.SignalHistory;
import com.example.qqq3xstrategy.data.models.TechnicalIndicator;

/**
 * ViewModel for the position change details screen
 */
public class PositionChangeViewModel extends AndroidViewModel {
    private static final String TAG = "PositionChangeViewModel";
    
    private final AppDatabase database;
    private final SharedPreferences preferences;
    
    private final LiveData<MarketData> latestMarketData;
    private final LiveData<TechnicalIndicator> latestIndicators;
    private final LiveData<SignalHistory> previousSignal;
    
    /**
     * Constructor
     */
    public PositionChangeViewModel(@NonNull Application application) {
        super(application);
        
        database = AppDatabase.getInstance(application);
        preferences = PreferenceManager.getDefaultSharedPreferences(application);
        
        latestMarketData = database.marketDataDao().getLatestMarketDataLive();
        latestIndicators = database.technicalIndicatorDao().getLatestIndicatorLive();
        
        // Get the second-to-last signal (previous position)
        previousSignal = database.signalHistoryDao().getPreviousSignalLive();
        
        Log.d(TAG, "PositionChangeViewModel initialized");
    }
    
    /**
     * Get latest market data
     */
    public LiveData<MarketData> getLatestMarketData() {
        return latestMarketData;
    }
    
    /**
     * Get latest technical indicators
     */
    public LiveData<TechnicalIndicator> getLatestIndicators() {
        return latestIndicators;
    }
    
    /**
     * Get previous signal
     */
    public LiveData<SignalHistory> getPreviousSignal() {
        return previousSignal;
    }
    
    /**
     * Mark position change as actioned
     */
    public void markAsActioned() {
        preferences.edit()
            .putLong("last_position_change_actioned", System.currentTimeMillis())
            .apply();
        
        Log.d(TAG, "Position change marked as actioned");
    }
}