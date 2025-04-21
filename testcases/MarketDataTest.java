package com.example.qqq3xstrategy.data.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

public class MarketDataTest {

    private MarketData marketData1;
    private MarketData marketData2;
    
    @Before
    public void setUp() {
        // Create first market data object
        marketData1 = new MarketData();
        marketData1.setId(1);
        marketData1.setDate(LocalDate.of(2025, 4, 20));
        marketData1.setQqqOpen(450.0);
        marketData1.setQqqClose(455.0);
        marketData1.setQqqHigh(460.0);
        marketData1.setQqqLow(448.0);
        marketData1.setQqqVolume(10000000L);
        marketData1.setVixOpen(20.0);
        marketData1.setVixClose(19.5);
        marketData1.setVixHigh(21.0);
        marketData1.setVixLow(19.0);
        marketData1.setGldOpen(180.0);
        marketData1.setGldClose(182.0);
        marketData1.setShyOpen(80.0);
        marketData1.setShyClose(80.5);
        
        // Create second market data object with same values
        marketData2 = new MarketData();
        marketData2.setId(1);
        marketData2.setDate(LocalDate.of(2025, 4, 20));
        marketData2.setQqqOpen(450.0);
        marketData2.setQqqClose(455.0);
        marketData2.setQqqHigh(460.0);
        marketData2.setQqqLow(448.0);
        marketData2.setQqqVolume(10000000L);
        marketData2.setVixOpen(20.0);
        marketData2.setVixClose(19.5);
        marketData2.setVixHigh(21.0);
        marketData2.setVixLow(19.0);
        marketData2.setGldOpen(180.0);
        marketData2.setGldClose(182.0);
        marketData2.setShyOpen(80.0);
        marketData2.setShyClose(80.5);
    }
    
    @Test
    public void testMarketDataCreation() {
        // Assert all fields are set correctly
        assertEquals(1, marketData1.getId());
        assertEquals(LocalDate.of(2025, 4, 20), marketData1.getDate());
        assertEquals(450.0, marketData1.getQqqOpen(), 0.001);
        assertEquals(455.0, marketData1.getQqqClose(), 0.001);
        assertEquals(460.0, marketData1.getQqqHigh(), 0.001);
        assertEquals(448.0, marketData1.getQqqLow(), 0.001);
        assertEquals(10000000L, marketData1.getQqqVolume());
        assertEquals(20.0, marketData1.getVixOpen(), 0.001);
        assertEquals(19.5, marketData1.getVixClose(), 0.001);
        assertEquals(21.0, marketData1.getVixHigh(), 0.001);
        assertEquals(19.0, marketData1.getVixLow(), 0.001);
        assertEquals(180.0, marketData1.getGldOpen(), 0.001);
        assertEquals(182.0, marketData1.getGldClose(), 0.001);
        assertEquals(80.0, marketData1.getShyOpen(), 0.001);
        assertEquals(80.5, marketData1.getShyClose(), 0.001);
    }
    
    @Test
    public void testMarketDataEquality() {
        // Test equals method
        assertEquals(marketData1, marketData2);
        
        // Test hashCode
        assertEquals(marketData1.hashCode(), marketData2.hashCode());
        
        // Modify one field and test inequality
        marketData2.setQqqClose(460.0);
        assertNotEquals(marketData1, marketData2);
        assertNotEquals(marketData1.hashCode(), marketData2.hashCode());
    }
    
    @Test
    public void testMarketDataComparison() {
        // Create data for different dates
        MarketData earlier = new MarketData();
        earlier.setDate(LocalDate.of(2025, 4, 19));
        
        MarketData later = new MarketData();
        later.setDate(LocalDate.of(2025, 4, 21));
        
        // Test comparisons if MarketData implements Comparable
        // This assumes MarketData implements Comparable based on date
        // If it doesn't, these tests should be removed
        assertTrue(earlier.compareTo(marketData1) < 0);
        assertTrue(marketData1.compareTo(later) < 0);
        assertEquals(0, marketData1.compareTo(marketData2));
    }
    
    @Test
    public void testToString() {
        // Test toString method returns non-empty string
        String toString = marketData1.toString();
        assertFalse(toString.isEmpty());
        
        // Verify toString contains key information
        assertTrue(toString.contains("2025-04-20"));
        assertTrue(toString.contains("455.0"));
    }
}