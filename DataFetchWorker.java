package com.example.qqq3xstrategy.workers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.repository.YahooFinanceRepository;
import com.example.qqq3xstrategy.services.StrategyCalculationService;
import com.example.qqq3xstrategy.util.QQQ3XStrategyApp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Worker class for fetching market data in the background
 */
public class DataFetchWorker extends Worker {
    private static final String TAG = "DataFetchWorker";
    
    public DataFetchWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Starting data fetch at " + LocalDateTime.now());
            
            YahooFinanceRepository repository = new YahooFinanceRepository(getApplicationContext());
            
            // Fetch current market data
            MarketData marketData = repository.fetchCurrentMarketData().get(30, TimeUnit.SECONDS);
            
            // Check if we need to update historical data
            LocalDate today = LocalDate.now();
            LocalDate twoYearsAgo = today.minusYears(2);
            
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String lastUpdateStr = prefs.getString("last_historical_update", "");
            
            boolean needsHistoricalUpdate = true;
            if (!lastUpdateStr.isEmpty()) {
                LocalDate lastUpdate = LocalDate.parse(lastUpdateStr);
                // Only update historical data once per day
                needsHistoricalUpdate = !lastUpdate.equals(today);
            }
            
            if (needsHistoricalUpdate) {
                Log.d(TAG, "Updating historical data");
                repository.fetchAllHistoricalData(twoYearsAgo, today).get(60, TimeUnit.SECONDS);
                
                // Update last update timestamp
                prefs.edit().putString("last_historical_update", today.toString()).apply();
            }
            
            // Trigger strategy calculation
            Intent intent = new Intent(getApplicationContext(), StrategyCalculationService.class);
            intent.putExtra("vix_open", marketData.getVixOpen());
            intent.putExtra("qqq_open", marketData.getQqqOpen());
            getApplicationContext().startService(intent);
            
            Log.d(TAG, "Data fetch completed successfully");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching data", e);
            return Result.retry();
        }
    }
}