package com.example.qqq3xstrategy.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.qqq3xstrategy.data.database.AppDatabase;
import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.models.SignalHistory;
import com.example.qqq3xstrategy.data.repository.YahooFinanceRepository;
import com.example.qqq3xstrategy.util.AppExecutors;

import java.util.concurrent.ExecutionException;

/**
 * ViewModel for the main screen
 */
public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "MainViewModel";
    
    private final AppDatabase database;
    private final YahooFinanceRepository repository;
    private final AppExecutors executors;
    
    private final LiveData<MarketData> latestMarketData;
    private final LiveData<SignalHistory> latestSignal;
    
    /**
     * Constructor
     */
    public MainViewModel(@NonNull Application application) {
        super(application);
        
        database = AppDatabase.getInstance(application);
        repository = new YahooFinanceRepository(application);
        executors = AppExecutors.getInstance();
        
        latestMarketData = database.marketDataDao().getLatestMarketDataLive();
        latestSignal = database.signalHistoryDao().getLatestSignalLive();
        
        Log.d(TAG, "MainViewModel initialized");
    }
    
    /**
     * Get latest market data
     */
    public LiveData<MarketData> getLatestMarketData() {
        return latestMarketData;
    }
    
    /**
     * Get latest signal
     */
    public LiveData<SignalHistory> getLatestSignal() {
        return latestSignal;
    }
    
    /**
     * Refresh market data
     */
    public void refreshData() {
        executors.networkIO().execute(() -> {
            try {
                MarketData data = repository.fetchCurrentMarketData().get();
                Log.d(TAG, "Data refreshed: " + data);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error refreshing data", e);
            }
        });
    }
}