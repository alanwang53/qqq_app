package com.example.qqq3xstrategy.data.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model class for Yahoo Finance chart API response
 */
public class ChartResponse {
    @SerializedName("chart")
    private ChartData chart;
    
    public ChartData getChart() {
        return chart;
    }
    
    public static class ChartData {
        @SerializedName("result")
        private List<Result> results;
        
        @SerializedName("error")
        private String error;
        
        public List<Result> getResults() {
            return results;
        }
        
        public String getError() {
            return error;
        }
    }
    
    public static class Result {
        @SerializedName("meta")
        private Meta meta;
        
        @SerializedName("timestamp")
        private List<Long> timestamp;
        
        @SerializedName("indicators")
        private Indicators indicators;
        
        public Meta getMeta() {
            return meta;
        }
        
        public List<Long> getTimestamp() {
            return timestamp;
        }
        
        public Indicators getIndicators() {
            return indicators;
        }
    }
    
    public static class Meta {
        @SerializedName("symbol")
        private String symbol;
        
        @SerializedName("currency")
        private String currency;
        
        @SerializedName("exchangeName")
        private String exchangeName;
        
        @SerializedName("instrumentType")
        private String instrumentType;
        
        @SerializedName("firstTradeDate")
        private long firstTradeDate;
        
        @SerializedName("regularMarketTime")
        private long regularMarketTime;
        
        @SerializedName("gmtoffset")
        private int gmtOffset;
        
        @SerializedName("timezone")
        private String timezone;
        
        // Getters
        public String getSymbol() {
            return symbol;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public String getExchangeName() {
            return exchangeName;
        }
        
        public String getInstrumentType() {
            return instrumentType;
        }
        
        public long getFirstTradeDate() {
            return firstTradeDate;
        }
        
        public long getRegularMarketTime() {
            return regularMarketTime;
        }
        
        public int getGmtOffset() {
            return gmtOffset;
        }
        
        public String getTimezone() {
            return timezone;
        }
    }
    
    public static class Indicators {
        @SerializedName("quote")
        private List<Quote> quotes;
        
        @SerializedName("adjclose")
        private List<AdjClose> adjclose;
        
        public List<Quote> getQuotes() {
            return quotes;
        }
        
        public List<AdjClose> getAdjclose() {
            return adjclose;
        }
    }
    
    public static class Quote {
        @SerializedName("open")
        private List<Double> open;
        
        @SerializedName("high")
        private List<Double> high;
        
        @SerializedName("low")
        private List<Double> low;
        
        @SerializedName("close")
        private List<Double> close;
        
        @SerializedName("volume")
        private List<Long> volume;
        
        // Getters
        public List<Double> getOpen() {
            return open;
        }
        
        public List<Double> getHigh() {
            return high;
        }
        
        public List<Double> getLow() {
            return low;
        }
        
        public List<Double> getClose() {
            return close;
        }
        
        public List<Long> getVolume() {
            return volume;
        }
    }
    
    public static class AdjClose {
        @SerializedName("adjclose")
        private List<Double> adjclose;
        
        public List<Double> getAdjclose() {
            return adjclose;
        }
    }
}