package com.example.qqq3xstrategy.data.network;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Client for Yahoo Finance API
 */
public class YahooFinanceClient {
    private static final String TAG = "YahooFinanceClient";
    private static final String BASE_URL = "https://query1.finance.yahoo.com/";
    private static final int TIMEOUT_SECONDS = 30;
    private static final int RATE_LIMIT_MS = 2000; // 2 seconds between requests
    
    private static YahooFinanceClient instance;
    private final YahooFinanceService service;
    
    /**
     * Get singleton instance of the client
     */
    public static synchronized YahooFinanceClient getInstance() {
        if (instance == null) {
            instance = new YahooFinanceClient();
        }
        return instance;
    }
    
    /**
     * Private constructor
     */
    private YahooFinanceClient() {
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(new HeaderInterceptor())
            .addInterceptor(new RateLimitInterceptor())
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build();
        
        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        
        service = retrofit.create(YahooFinanceService.class);
        Log.d(TAG, "YahooFinanceClient initialized");
    }
    
    /**
     * Get the service interface
     */
    public YahooFinanceService getService() {
        return service;
    }
    
    /**
     * Interceptor to add required headers to requests
     */
    private static class HeaderInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            
            // Add required headers
            Request request = original.newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .header("Accept", "application/json")
                .method(original.method(), original.body())
                .build();
            
            return chain.proceed(request);
        }
    }
    
    /**
     * Interceptor to implement rate limiting
     */
    private static class RateLimitInterceptor implements Interceptor {
        private static long lastRequestTime = 0;
        
        @Override
        public Response intercept(Chain chain) throws IOException {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastRequest = currentTime - lastRequestTime;
            
            if (timeSinceLastRequest < RATE_LIMIT_MS) {
                try {
                    long sleepTime = RATE_LIMIT_MS - timeSinceLastRequest;
                    Log.d(TAG, "Rate limiting: sleeping for " + sleepTime + "ms");
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.e(TAG, "Rate limiting interrupted", e);
                }
            }
            
            lastRequestTime = System.currentTimeMillis();
            return chain.proceed(chain.request());
        }
    }
}