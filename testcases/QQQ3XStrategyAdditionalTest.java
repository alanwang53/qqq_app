package com.example.qqq3xstrategy.strategy;

import static org.junit.Assert.assertEquals;

import com.example.qqq3xstrategy.data.models.UserSettings;

import org.junit.Before;
import org.junit.Test;

/**
 * Additional unit tests for the QQQ3X strategy implementation
 * focusing on the SMA calculation functionality
 */
public class QQQ3XStrategyAdditionalTest {
    
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
    public void testCalculateSMA() {
        // Test SMA calculation with a simple array
        // We need to use reflection to access the private method
        
        // Create test data
        double[] prices = {100, 110, 120, 130, 140};
        
        // Calculate SMA for different periods
        double sma3 = calculateSMA(prices, 3);
        double sma5 = calculateSMA(prices, 5);
        
        // Assert results
        assertEquals("SMA(3) should be average of last 3 values", 130.0, sma3, 0.001);
        assertEquals("SMA(5) should be average of all 5 values", 120.0, sma5, 0.001);
    }
    
    @Test
    public void testCalculateSMAWithInsufficientData() {
        // Test SMA calculation when there's not enough data
        
        // Create test data with fewer elements than the period
        double[] prices = {100, 110};
        
        // Calculate SMA for a period larger than the array
        double sma3 = calculateSMA(prices, 3);
        
        // Assert result
        assertEquals("SMA should be 0 when there's insufficient data", 0.0, sma3, 0.001);
    }
    
    @Test
    public void testCalculateSMAWithLargeDataset() {
        // Test SMA calculation with a larger dataset
        
        // Create test data
        double[] prices = new double[200];
        for (int i = 0; i < prices.length; i++) {
            prices[i] = 100 + i;
        }
        
        // Calculate SMA for different periods
        double sma10 = calculateSMA(prices, 10);
        double sma50 = calculateSMA(prices, 50);
        double sma100 = calculateSMA(prices, 100);
        
        // Assert results
        assertEquals("SMA(10) should be correct", 195.5, sma10, 0.001);
        assertEquals("SMA(50) should be correct", 175.5, sma50, 0.001);
        assertEquals("SMA(100) should be correct", 150.5, sma100, 0.001);
    }
    
    @Test
    public void testCalculateSMAWithConstantValues() {
        // Test SMA calculation with constant values
        
        // Create test data with all the same value
        double[] prices = {100, 100, 100, 100, 100};
        
        // Calculate SMA
        double sma3 = calculateSMA(prices, 3);
        double sma5 = calculateSMA(prices, 5);
        
        // Assert results
        assertEquals("SMA of constant values should equal the constant", 100.0, sma3, 0.001);
        assertEquals("SMA of constant values should equal the constant", 100.0, sma5, 0.001);
    }
    
    /**
     * Helper method to call the private calculateSMA method using reflection
     */
    private double calculateSMA(double[] prices, int period) {
        try {
            java.lang.reflect.Method method = QQQ3XStrategy.class.getDeclaredMethod("calculateSMA", double[].class, int.class);
            method.setAccessible(true);
            return (double) method.invoke(strategy, prices, period);
        } catch (Exception e) {
            throw new RuntimeException("Error calling calculateSMA method", e);
        }
    }
}