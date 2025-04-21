package com.example.qqq3xstrategy.data.models.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.models.SignalHistory;

/**
 * Represents a relationship between MarketData and SignalHistory entities
 */
public class MarketDataWithSignal {
    @Embedded
    public MarketData marketData;
    
    @Relation(
        parentColumn = "date",
        entityColumn = "date"
    )
    public SignalHistory signal;
}