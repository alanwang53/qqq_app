package com.example.qqq3xstrategy.strategy;

import android.util.Log;

import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.models.UserSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the QQQ3X strategy
 */
public class QQQ3XStrategy {
    private static final String TAG = "QQQ3XStrategy";
    
    // Strategy parameters
    private final int smaShort;
    private final int smaLong;
    private final int smaShort2;
    private final int smaLong2;
    private final int smaShort3;
    private final int smaLong3;
    private final int smaYear;
    
    /**
     * Constructor with user settings
     */
    public QQQ3XStrategy(UserSettings settings) {
        this.smaShort = settings.getSmaShort();
        this.smaLong = settings.getSmaLong();
        this.smaShort2 = settings.getSmaShort2();
        this.smaLong2 = settings.getSmaLong2();
        this.smaShort3 = settings.getSmaShort3();
        this.smaLong3 = settings.getSmaLong3();
        this.smaYear = settings.getSmaYear();
    }
    
    /**
     * Calculate strategy signal based on historical data and today's open prices
     * 
     * @param historicalData Historical market data
     * @param vixOpenToday VIX opening price today
     * @param qqqOpenToday QQQ opening price today
     * @return Signal (-1 for safe asset, 1 for leveraged QQQ)
     */
    public int calculateSignal(List<MarketData> historicalData, double vixOpenToday, double qqqOpenToday) {
        if (historicalData == null || historicalData.isEmpty()) {
            Log.e(TAG, "Historical data is empty");
            return -1; // Default to safe asset
        }
        
        // Ensure we have enough data for calculations
        if (historicalData.size() < smaYear + 1) {
            Log.e(TAG, "Not enough historical data to calculate signal");
            return -1; // Default to safe asset
        }
        
        // Create a copy of the data to work with
        List<MarketData> data = new ArrayList<>(historicalData);
        
        // Get the last data point (yesterday)
        MarketData yesterday = data.get(data.size() - 1);
        
        // Calculate technical indicators
        double[] qqqPrices = data.stream().mapToDouble(MarketData::getQqqClose).toArray();
        double[] vixPrices = data.stream().mapToDouble(MarketData::getVixClose).toArray();
        double[] gldPrices = data.stream().mapToDouble(MarketData::getGldClose).toArray();
        
        // Calculate SMAs
        double qqqSmaYear = calculateSMA(qqqPrices, smaYear);
        double qqqSmaLong = calculateSMA(qqqPrices, smaLong);
        double qqqSmaShort = calculateSMA(qqqPrices, smaShort);
        double gldSma = calculateSMA(gldPrices, 100);
        
        double vixSmaShort = calculateSMA(vixPrices, smaShort2);
        double vixSmaLong = calculateSMA(vixPrices, smaLong2);
        
        double vixSmaShort3 = calculateSMA(vixPrices, smaShort3);
        double vixSmaLong3 = calculateSMA(vixPrices, smaLong3);
        
        // Calculate VIX indicators
        double vixC = (vixOpenToday / yesterday.getVixOpen()) - 1;
        double vixOpenClose = (vixOpenToday / yesterday.getVixClose()) - 1;
        
        // Calculate conditions
        boolean qqq_open_close_b = qqqOpenToday > yesterday.getQqqClose() * 1.005;
        boolean qqq_open_close_l = qqqOpenToday < yesterday.getQqqClose() * 0.96;
        
        boolean vix_up_much = vixC > 0.2 || vixOpenClose > 0.2;
        boolean vix_sell = vixSmaShort > 1.2 * vixSmaLong || vixOpenToday > 1.2 * vixSmaLong;
        
        boolean vix_down_today = vixOpenClose < -0.03;
        boolean vix_down_today2 = vixOpenClose < 0.05;
        boolean vix_down_smooth = vixSmaShort3 < 0.97 * vixSmaLong3;
        boolean vix_down_smooth2 = vixSmaShort3 < 0.95 * vixSmaLong3 && 
                (yesterday.getVixClose() > 50 || vixOpenToday > 50);
        
        boolean vix_no_need_safe = vix_down_today2 && vix_down_smooth2;
        boolean qqq_up_trend = qqqSmaShort > 0.99 * qqqSmaLong;
        boolean qqq_down_trend = qqqSmaShort < 0.95 * qqqSmaLong;
        
        boolean qqq_year_up = yesterday.getQqqClose() > 1.03 * qqqSmaYear && 
                qqqOpenToday > 1.03 * qqqSmaYear;
        boolean qqq_year_down = yesterday.getQqqClose() < 0.99 * qqqSmaYear || 
                qqqOpenToday < 0.99 * qqqSmaYear;
        
        boolean VIX_b_66 = yesterday.getVixClose() > 66 && vixOpenToday > 66;
        boolean VIX_l_60 = yesterday.getVixClose() < 60 && vixOpenToday < 60;
        
        boolean VIX_l_21 = vixOpenToday < 21 && qqq_open_close_b;
        boolean VIX_b_23 = vixOpenToday > 23 || qqq_open_close_l;
        boolean VIX_b_32 = vixOpenToday > 32 || yesterday.getVixClose() > 32;
        
        boolean cond_up = VIX_b_32 && vix_down_today && vix_down_smooth;
        boolean cond_down = VIX_b_32 && (vix_up_much || vix_sell);
        
        boolean lev_cond = qqq_year_up || (qqq_up_trend && VIX_l_21) || VIX_b_66;
        boolean safe_cond = qqq_year_down && (qqq_down_trend || VIX_b_23) && VIX_l_60;
        
        // Determine signal
        int signal;
        if ((safe_cond && !cond_up && !vix_no_need_safe) || cond_down) {
            signal = -1; // Safe asset
        } else if ((lev_cond && !cond_down) || cond_up) {
            signal = 1; // Leveraged QQQ
        } else {
            // No change, use previous signal or default to safe asset
            signal = -1;
        }
        
        Log.d(TAG, "Signal calculation: " + signal);
        Log.d(TAG, "Conditions: safe_cond=" + safe_cond + ", cond_up=" + cond_up + 
                ", vix_no_need_safe=" + vix_no_need_safe + ", cond_down=" + cond_down + 
                ", lev_cond=" + lev_cond);
        
        return signal;
    }
    
    /**
     * Calculate Simple Moving Average
     */
    private double calculateSMA(double[] prices, int period) {
        if (prices.length < period) {
            return 0;
        }
        
        double sum = 0;
        for (int i = prices.length - period; i < prices.length; i++) {
            sum += prices[i];
        }
        return sum / period;
    }
}