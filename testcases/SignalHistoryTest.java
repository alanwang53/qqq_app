package com.example.qqq3xstrategy.data.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

public class SignalHistoryTest {

    private SignalHistory signal1;
    private SignalHistory signal2;
    
    @Before
    public void setUp() {
        // Create first signal history object
        signal1 = new SignalHistory();
        signal1.setId(1);
        signal1.setDate(LocalDate.of(2025, 4, 20));
        signal1.setSignal(1); // Leveraged position
        signal1.setRawSignal(1);
        signal1.setPositionChanged(true);
        signal1.setSafeAsset("SHY");
        signal1.setActioned(false);
        
        // Create second signal history object with same values
        signal2 = new SignalHistory();
        signal2.setId(1);
        signal2.setDate(LocalDate.of(2025, 4, 20));
        signal2.setSignal(1); // Leveraged position
        signal2.setRawSignal(1);
        signal2.setPositionChanged(true);
        signal2.setSafeAsset("SHY");
        signal2.setActioned(false);
    }
    
    @Test
    public void testSignalHistoryCreation() {
        // Assert all fields are set correctly
        assertEquals(1, signal1.getId());
        assertEquals(LocalDate.of(2025, 4, 20), signal1.getDate());
        assertEquals(1, signal1.getSignal());
        assertEquals(1, signal1.getRawSignal());
        assertTrue(signal1.getPositionChanged());
        assertEquals("SHY", signal1.getSafeAsset());
        assertFalse(signal1.getActioned());
    }
    
    @Test
    public void testSignalHistoryEquality() {
        // Test equals method
        assertEquals(signal1, signal2);
        
        // Test hashCode
        assertEquals(signal1.hashCode(), signal2.hashCode());
        
        // Modify one field and test inequality
        signal2.setSignal(-1);
        assertNotEquals(signal1, signal2);
        assertNotEquals(signal1.hashCode(), signal2.hashCode());
    }
    
    @Test
    public void testPositionChangedFlag() {
        // Test position changed flag
        SignalHistory noChange = new SignalHistory();
        noChange.setPositionChanged(false);
        assertFalse(noChange.getPositionChanged());
        
        // Test setting and getting position changed flag
        noChange.setPositionChanged(true);
        assertTrue(noChange.getPositionChanged());
    }
    
    @Test
    public void testActionedFlag() {
        // Test actioned flag
        assertFalse(signal1.getActioned());
        
        // Test setting and getting actioned flag
        signal1.setActioned(true);
        assertTrue(signal1.getActioned());
    }
    
    @Test
    public void testSignalValues() {
        // Test leveraged position
        SignalHistory leveraged = new SignalHistory();
        leveraged.setSignal(1);
        assertEquals(1, leveraged.getSignal());
        
        // Test safe asset position
        SignalHistory safeAsset = new SignalHistory();
        safeAsset.setSignal(-1);
        assertEquals(-1, safeAsset.getSignal());
    }
    
    @Test
    public void testToString() {
        // Test toString method returns non-empty string
        String toString = signal1.toString();
        assertFalse(toString.isEmpty());
        
        // Verify toString contains key information
        assertTrue(toString.contains("2025-04-20"));
        assertTrue(toString.contains("signal=1"));
        assertTrue(toString.contains("positionChanged=true"));
    }
}