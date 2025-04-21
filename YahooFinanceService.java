package com.example.qqq3xstrategy.data.network;

import com.example.qqq3xstrategy.data.network.models.ChartResponse;
import com.example.qqq3xstrategy.data.network.models.QuoteResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit service interface for Yahoo Finance API
 */
public interface YahooFinanceService {
    /**
     * Get real-time quotes for multiple symbols
     * @param symbols Comma-separated list of symbols (e.g., "QQQ,^VIX,GLD,SHY")
     * @return Quote response containing data for all requested symbols
     */
    @GET("v7/finance/quote")
    Call<QuoteResponse> getQuotes(@Query("symbols") String symbols);
    
    /**
     * Get historical data for a symbol
     * @param symbol The ticker symbol (e.g., "QQQ", "^VIX")
     * @param interval Data interval (e.g., "1d" for daily)
     * @param range Date range (e.g., "1mo", "3mo", "6mo", "1y", "2y")
     * @return Chart response containing historical data
     */
    @GET("v8/finance/chart/{symbol}")
    Call<ChartResponse> getHistoricalData(
        @Path("symbol") String symbol,
        @Query("interval") String interval,
        @Query("range") String range
    );
}