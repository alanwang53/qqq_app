package com.example.qqq3xstrategy.data.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model class for Yahoo Finance quote API response
 */
public class QuoteResponse {
    @SerializedName("quoteResponse")
    private QuoteResponseData data;
    
    public QuoteResponseData getData() {
        return data;
    }
    
    public static class QuoteResponseData {
        @SerializedName("result")
        private List<Quote> results;
        
        @SerializedName("error")
        private String error;
        
        public List<Quote> getResults() {
            return results;
        }
        
        public String getError() {
            return error;
        }
    }
    
    public static class Quote {
        @SerializedName("symbol")
        private String symbol;
        
        @SerializedName("regularMarketOpen")
        private double regularMarketOpen;
        
        @SerializedName("regularMarketPrice")
        private double regularMarketPrice;
        
        @SerializedName("regularMarketDayHigh")
        private double regularMarketDayHigh;
        
        @SerializedName("regularMarketDayLow")
        private double regularMarketDayLow;
        
        @SerializedName("regularMarketVolume")
        private long regularMarketVolume;
        
        @SerializedName("regularMarketTime")
        private long regularMarketTime;
        
        // Getters
        public String getSymbol() {
            return symbol;
        }
        
        public double getRegularMarketOpen() {
            return regularMarketOpen;
        }
        
        public double getRegularMarketPrice() {
            return regularMarketPrice;
        }
        
        public double getRegularMarketDayHigh() {
            return regularMarketDayHigh;
        }
        
        public double getRegularMarketDayLow() {
            return regularMarketDayLow;
        }
        
        public long getRegularMarketVolume() {
            return regularMarketVolume;
        }
        
        public long getRegularMarketTime() {
            return regularMarketTime;
        }
    }
}