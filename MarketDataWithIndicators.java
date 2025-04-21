package com.example.qqq3xstrategy.data.models.relations;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.qqq3xstrategy.data.models.MarketData;
import com.example.qqq3xstrategy.data.models.TechnicalIndicator;

/**
 * Represents a relationship between MarketData and TechnicalIndicator entities
 */
public class MarketDataWithIndicators {
    @Embedded
    public MarketData marketData;
    
    @Relation(
        parentColumn = "date",
        entityColumn = "date"
    )
    public TechnicalIndicator indicators;
}