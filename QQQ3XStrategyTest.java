package com.example.qqq3xstrategy.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.models.UserSettings;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the QQQ3X strategy implementation
 */
public class QQQ3XStrategyTest {
    
    private UserSettings settings;
    private QQQ3XStrategy strategy;
    
    @Before
    public void setUp() {
        // Create default settings for testing
        settings = new UserSettings();
        settings.setSmaShort(5);
        settings.setSmaLong(15);
        settings.setSmaShort2(5);
        settings.setSmaLong2(15);
        settings.setSmaShort3(3);
        settings.setSmaLong3(10);
        settings.setSmaYear(155);
        
        // Create strategy with test settings
        strategy = new QQQ3XStrategy(settings);
    }
    
    @Test
    public void testCalculateSignalWithLeveragedConditions() {
        // Arrange
        List<MarketData> historicalData = createMockHistoricalData(true); // uptrend data
        double vixOpenToday = 18.5; // VIX < 21
        double qqqOpenToday = 450.0; // QQQ in uptrend
        
        // Act
        int signal = strategy.calculateSignal(historicalData, vixOpenToday, qqqOpenToday);
        
        // Assert
        assertEquals("Should recommend leveraged position", 1, signal);
    }
    
    @Test
    public void testCalculateSignalWithSafeAssetConditions() {
        // Arrange
        List<MarketData> historicalData = createMockHistoricalData(false); // downtrend data
        double vixOpenToday = 35.0; // VIX > 32
        double qqqOpenToday = 430.0; // QQQ in downtrend
        
        // Act
        int signal = strategy.calculateSignal(historicalData, vixOpenToday, qqqOpenToday);
        
        // Assert
        assertEquals("Should recommend safe asset", -1, signal);
    }
    
    @Test
    public void testCalculateSignalWithVixUpMuch() {
        // Arrange
        List<MarketData> historicalData = createMockHistoricalData(true); // uptrend data
        
        // Yesterday's VIX close was 20, today's open is 25 (25% increase)
        historicalData.get(historicalData.size() - 1).setVixClose(20.0);
        double vixOpenToday = 25.0; // 25% increase
        double qqqOpenToday = 450.0;
        
        // Act
        int signal = strategy.calculateSignal(historicalData, vixOpenToday, qqqOpenToday);
        
        // Assert
        assertEquals("Should recommend safe asset when VIX jumps significantly", -1, signal);
    }
    
    @Test
    public void testCalculateSignalWithQqqYearUp() {
        // Arrange
        List<MarketData> historicalData = createMockHistoricalData(true);
        
        // Set QQQ price well above yearly SMA
        double qqqSmaYear = 400.0;
        double qqqCloseYesterday = 420.0;
        double qqqOpenToday = 425.0;
        
        // Update last data point
        historicalData.get(historicalData.size() - 1).setQqqClose(qqqCloseYesterday);
        
        // Set all historical prices to create the desired SMA
        for (int i = 0; i < historicalData.size(); i++) {
            historicalData.get(i).setQqqClose(qqqSmaYear);
        }
        
        // Act
        int signal = strategy.calculateSignal(historicalData, 25.0, qqqOpenToday);
        
        // Assert
        assertEquals("Should recommend leveraged position when QQQ is above yearly SMA", 1, signal);
    }
    
    @Test
    public void testCalculateSignalWithQqqYearDown() {
        // Arrange
        List<MarketData> historicalData = createMockHistoricalData(false);
        
        // Set QQQ price below yearly SMA
        double qqqSmaYear = 400.0;
        double qqqCloseYesterday = 390.0;
        double qqqOpenToday = 385.0;
        
        // Update last data point
        historicalData.get(historicalData.size() - 1).setQqqClose(qqqCloseYesterday);
        
        // Set all historical prices to create the desired SMA
        for (int i = 0; i < historicalData.size(); i++) {
            historicalData.get(i).setQqqClose(qqqSmaYear);
        }
        
        // Act
        int signal = strategy.calculateSignal(historicalData, 25.0, qqqOpenToday);
        
        // Assert
        assertEquals("Should recommend safe asset when QQQ is below yearly SMA", -1, signal);
    }
    
    @Test
    public void testCalculateSignalWithVixAbove66() {
        // Arrange
        List<MarketData> historicalData = createMockHistoricalData(false); // downtrend data
        
        // Set VIX above 66
        historicalData.get(historicalData.size() - 1).setVixClose(68.0);
        double vixOpenToday = 70.0;
        double qqqOpenToday = 430.0;
        
        // Act
        int signal = strategy.calculateSignal(historicalData, vixOpenToday, qqqOpenToday);
        
        // Assert
        assertEquals("Should recommend leveraged position when VIX is extremely high", 1, signal);
    }
    
    @Test
    public void testCalculateSignalWithInsufficientData() {
        // Arrange
        List<MarketData> historicalData = new ArrayList<>(); // Empty data
        double vixOpenToday = 25.0;
        double qqqOpenToday = 450.0;
        
        // Act
        int signal = strategy.calculateSignal(historicalData, vixOpenToday, qqqOpenToday);
        
        // Assert
        assertEquals("Should default to safe asset with insufficient data", -1, signal);
    }
    
    /**
     * Helper method to create mock historical data
     * @param uptrend true for uptrend data, false for downtrend
     * @return List of MarketData objects
     */
    private List<MarketData> createMockHistoricalData(boolean uptrend) {
        List<MarketData> data = new ArrayList<>();
        
        // Create enough data points for all SMAs
        int dataPoints = Math.max(settings.getSmaYear(), 200);
        LocalDate startDate = LocalDate.now().minusDays(dataPoints);
        
        // Base values
        double qqqBase = uptrend ? 400.0 : 450.0;
        double vixBase = uptrend ? 25.0 : 20.0;
        double gldBase = 180.0;
        double shyBase = 80.0;
        
        // Create data points
        for (int i = 0; i < dataPoints; i++) {
            MarketData point = new MarketData();
            point.setDate(startDate.plusDays(i));
            
            // Calculate prices with trend
            double trendFactor = uptrend ? 0.1 : -0.1;
            double dayFactor = (double) i / dataPoints;
            
            double qqqPrice = qqqBase * (1 + trendFactor * dayFactor);
            double vixPrice = vixBase * (1 - trendFactor * dayFactor); // VIX moves opposite to market
            
            // Set values
            point.setQqqOpen(qqqPrice * 0.99);
            point.setQqqClose(qqqPrice);
            point.setQqqHigh(qqqPrice * 1.02);
            point.setQqqLow(qqqPrice * 0.98);
            point.setQqqVolume(1000000L);
            
            point.setVixOpen(vixPrice * 0.99);
            point.setVixClose(vixPrice);
            point.setVixHigh(vixPrice * 1.05);
            point.setVixLow(vixPrice * 0.95);
            
            point.setGldOpen(gldBase * 0.99);
            point.setGldClose(gldBase);
            
            point.setShyOpen(shyBase * 0.99);
            point.setShyClose(shyBase);
            
            data.add(point);
        }
        
        return data;
    }
}