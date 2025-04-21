package com.example.qqq3xstrategy.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.qqq3xstrategy.data.database.AppDatabase;
import com.example.qqq3xstrategy.data.database.MarketDataDao;
import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.network.YahooFinanceClient;
import com.example.qqq3xstrategy.data.network.YahooFinanceService;
import com.example.qqq3xstrategy.data.network.models.ChartResponse;
import com.example.qqq3xstrategy.data.network.models.QuoteResponse;
import com.example.qqq3xstrategy.util.AppExecutors;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for Yahoo Finance data
 */
public class YahooFinanceRepository {
    private static final String TAG = "YahooFinanceRepository";
    
    private final YahooFinanceService service;
    private final MarketDataDao marketDataDao;
    private final AppExecutors executors;
    
    /**
     * Constructor
     */
    public YahooFinanceRepository(Context context) {
        YahooFinanceClient client = YahooFinanceClient.getInstance();
        service = client.getService();
        
        AppDatabase database = AppDatabase.getInstance(context);
        marketDataDao = database.marketDataDao();
        
        executors = AppExecutors.getInstance();
        
        Log.d(TAG, "YahooFinanceRepository initialized");
    }
    
    /**
     * Fetches current market data for QQQ, VIX, GLD, and SHY
     */
    public CompletableFuture<MarketData> fetchCurrentMarketData() {
        CompletableFuture<MarketData> future = new CompletableFuture<>();
        
        service.getQuotes("QQQ,^VIX,GLD,SHY").enqueue(new Callback<QuoteResponse>() {
            @Override
            public void onResponse(Call<QuoteResponse> call, Response<QuoteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    QuoteResponse quoteResponse = response.body();
                    List<QuoteResponse.Quote> quotes = quoteResponse.getData().getResults();
                    
                    if (quotes != null && !quotes.isEmpty()) {
                        MarketData marketData = new MarketData();
                        marketData.setDate(LocalDate.now());
                        
                        for (QuoteResponse.Quote quote : quotes) {
                            switch (quote.getSymbol()) {
                                case "QQQ":
                                    marketData.setQqqOpen(quote.getRegularMarketOpen());
                                    marketData.setQqqClose(quote.getRegularMarketPrice());
                                    marketData.setQqqHigh(quote.getRegularMarketDayHigh());
                                    marketData.setQqqLow(quote.getRegularMarketDayLow());
                                    marketData.setQqqVolume(quote.getRegularMarketVolume());
                                    break;
                                case "^VIX":
                                    marketData.setVixOpen(quote.getRegularMarketOpen());
                                    marketData.setVixClose(quote.getRegularMarketPrice());
                                    marketData.setVixHigh(quote.getRegularMarketDayHigh());
                                    marketData.setVixLow(quote.getRegularMarketDayLow());
                                    break;
                                case "GLD":
                                    marketData.setGldOpen(quote.getRegularMarketOpen());
                                    marketData.setGldClose(quote.getRegularMarketPrice());
                                    break;
                                case "SHY":
                                    marketData.setShyOpen(quote.getRegularMarketOpen());
                                    marketData.setShyClose(quote.getRegularMarketPrice());
                                    break;
                            }
                        }
                        
                        // Save to database
                        executors.diskIO().execute(() -> {
                            marketDataDao.insert(marketData);
                            Log.d(TAG, "Saved market data to database: " + marketData);
                        });
                        
                        future.complete(marketData);
                    } else {
                        future.completeExceptionally(new Exception("No quote data found"));
                    }
                } else {
                    String errorMsg = "Failed to fetch market data: " + 
                            (response.errorBody() != null ? response.errorBody().toString() : "Unknown error");
                    Log.e(TAG, errorMsg);
                    future.completeExceptionally(new Exception(errorMsg));
                }
            }
            
            @Override
            public void onFailure(Call<QuoteResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching market data", t);
                future.completeExceptionally(t);
            }
        });
        
        return future;
    }
    
    /**
     * Fetches historical data for a symbol
     */
    public CompletableFuture<List<MarketData>> fetchHistoricalData(String symbol, String interval, String range) {
        CompletableFuture<List<MarketData>> future = new CompletableFuture<>();
        
        service.getHistoricalData(symbol, interval, range).enqueue(new Callback<ChartResponse>() {
            @Override
            public void onResponse(Call<ChartResponse> call, Response<ChartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChartResponse chartResponse = response.body();
                    List<ChartResponse.Result> results = chartResponse.getChart().getResults();
                    
                    if (results != null && !results.isEmpty()) {
                        ChartResponse.Result result = results.get(0);
                        List<Long> timestamps = result.getTimestamp();
                        ChartResponse.Quote quote = result.getIndicators().getQuotes().get(0);
                        
                        List<MarketData> marketDataList = new ArrayList<>();
                        
                        for (int i = 0; i < timestamps.size(); i++) {
                            // Convert timestamp to LocalDate
                            LocalDate date = Instant.ofEpochSecond(timestamps.get(i))
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                            
                            // Create or get existing MarketData for this date
                            MarketData data = new MarketData();
                            data.setDate(date);
                            
                            // Set data based on symbol
                            if (quote.getOpen() != null && i < quote.getOpen().size() && quote.getOpen().get(i) != null) {
                                switch (symbol) {
                                    case "QQQ":
                                        data.setQqqOpen(quote.getOpen().get(i));
                                        data.setQqqClose(quote.getClose().get(i));
                                        data.setQqqHigh(quote.getHigh().get(i));
                                        data.setQqqLow(quote.getLow().get(i));
                                        data.setQqqVolume(quote.getVolume().get(i));
                                        break;
                                    case "^VIX":
                                        data.setVixOpen(quote.getOpen().get(i));
                                        data.setVixClose(quote.getClose().get(i));
                                        data.setVixHigh(quote.getHigh().get(i));
                                        data.setVixLow(quote.getLow().get(i));
                                        break;
                                    case "GLD":
                                        data.setGldOpen(quote.getOpen().get(i));
                                        data.setGldClose(quote.getClose().get(i));
                                        break;
                                    case "SHY":
                                        data.setShyOpen(quote.getOpen().get(i));
                                        data.setShyClose(quote.getClose().get(i));
                                        break;
                                }
                            }
                            
                            marketDataList.add(data);
                        }
                        
                        future.complete(marketDataList);
                    } else {
                        future.completeExceptionally(new Exception("No historical data found"));
                    }
                } else {
                    String errorMsg = "Failed to fetch historical data: " + 
                            (response.errorBody() != null ? response.errorBody().toString() : "Unknown error");
                    Log.e(TAG, errorMsg);
                    future.completeExceptionally(new Exception(errorMsg));
                }
            }
            
            @Override
            public void onFailure(Call<ChartResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching historical data", t);
                future.completeExceptionally(t);
            }
        });
        
        return future;
    }
    
    /**
     * Fetches and merges historical data for all required symbols
     */
    public CompletableFuture<List<MarketData>> fetchAllHistoricalData(LocalDate startDate, LocalDate endDate) {
        // Calculate range in days
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        String range = days <= 30 ? "1mo" : days <= 90 ? "3mo" : days <= 180 ? "6mo" : days <= 365 ? "1y" : "2y";
        
        CompletableFuture<List<MarketData>> qqqFuture = fetchHistoricalData("QQQ", "1d", range);
        CompletableFuture<List<MarketData>> vixFuture = fetchHistoricalData("^VIX", "1d", range);
        CompletableFuture<List<MarketData>> gldFuture = fetchHistoricalData("GLD", "1d", range);
        CompletableFuture<List<MarketData>> shyFuture = fetchHistoricalData("SHY", "1d", range);
        
        return CompletableFuture.allOf(qqqFuture, vixFuture, gldFuture, shyFuture)
            .thenApply(v -> {
                // Merge all data by date
                Map<LocalDate, MarketData> mergedData = new HashMap<>();
                
                // Process QQQ data
                for (MarketData data : qqqFuture.join()) {
                    mergedData.put(data.getDate(), data);
                }
                
                // Process VIX data
                for (MarketData data : vixFuture.join()) {
                    LocalDate date = data.getDate();
                    MarketData existing = mergedData.getOrDefault(date, new MarketData());
                    existing.setDate(date);
                    existing.setVixOpen(data.getVixOpen());
                    existing.setVixClose(data.getVixClose());
                    existing.setVixHigh(data.getVixHigh());
                    existing.setVixLow(data.getVixLow());
                    mergedData.put(date, existing);
                }
                
                // Process GLD data
                for (MarketData data : gldFuture.join()) {
                    LocalDate date = data.getDate();
                    MarketData existing = mergedData.getOrDefault(date, new MarketData());
                    existing.setDate(date);
                    existing.setGldOpen(data.getGldOpen());
                    existing.setGldClose(data.getGldClose());
                    mergedData.put(date, existing);
                }
                
                // Process SHY data
                for (MarketData data : shyFuture.join()) {
                    LocalDate date = data.getDate();
                    MarketData existing = mergedData.getOrDefault(date, new MarketData());
                    existing.setDate(date);
                    existing.setShyOpen(data.getShyOpen());
                    existing.setShyClose(data.getShyClose());
                    mergedData.put(date, existing);
                }
                
                // Convert to list and sort by date
                List<MarketData> result = new ArrayList<>(mergedData.values());
                result.sort(Comparator.comparing(MarketData::getDate));
                
                // Save to database
                executors.diskIO().execute(() -> {
                    marketDataDao.insertAll(result);
                    Log.d(TAG, "Saved " + result.size() + " historical data points to database");
                });
                
                return result;
            });
    }
    
    /**
     * Get current market data with fallback to cached data
     */
    public CompletableFuture<MarketData> getCurrentMarketData() {
        CompletableFuture<MarketData> future = new CompletableFuture<>();
        
        // Try to fetch fresh data
        fetchCurrentMarketData().whenComplete((data, error) -> {
            if (error != null) {
                Log.w(TAG, "Error fetching current data, falling back to cache", error);
                
                // Fall back to cached data
                executors.diskIO().execute(() -> {
                    MarketData cachedData = marketDataDao.getLatestMarketData();
                    
                    if (cachedData != null) {
                        future.complete(cachedData);
                    } else {
                        future.completeExceptionally(new Exception("No cached data available"));
                    }
                });
            } else {
                future.complete(data);
            }
        });
        
        return future;
    }
    
    /**
     * Fetch with retry mechanism
     */
    private <T> CompletableFuture<T> fetchWithRetry(Callable<CompletableFuture<T>> fetchFunction, int maxRetries) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        fetchWithRetryInternal(fetchFunction, maxRetries, 0, future);
        
        return future;
    }
    
    private <T> void fetchWithRetryInternal(Callable<CompletableFuture<T>> fetchFunction, 
                                         int maxRetries, int currentRetry, 
                                         CompletableFuture<T> resultFuture) {
        try {
            CompletableFuture<T> attempt = fetchFunction.call();
            
            attempt.whenComplete((result, error) -> {
                if (error != null) {
                    if (currentRetry < maxRetries) {
                        // Exponential backoff
                        long delay = (long) Math.pow(2, currentRetry) * 1000;
                        Log.w(TAG, "Retry " + (currentRetry + 1) + " after " + delay + "ms");
                        
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            fetchWithRetryInternal(fetchFunction, maxRetries, currentRetry + 1, resultFuture);
                        }, delay);
                    } else {
                        resultFuture.completeExceptionally(error);
                    }
                } else {
                    resultFuture.complete(result);
                }
            });
        } catch (Exception e) {
            resultFuture.completeExceptionally(e);
        }
    }
}