package com.example.qqq3xstrategy.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.qqq3xstrategy.data.models.MarketData;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object for MarketData entity
 */
@Dao
public interface MarketDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MarketData marketData);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MarketData> marketDataList);
    
    @Query("SELECT * FROM market_data WHERE date = :date")
    MarketData getMarketDataForDate(LocalDate date);
    
    @Query("SELECT * FROM market_data WHERE date = :date")
    LiveData<MarketData> getMarketDataForDateLive(LocalDate date);
    
    @Query("SELECT * FROM market_data ORDER BY date DESC LIMIT 1")
    MarketData getLatestMarketData();
    
    @Query("SELECT * FROM market_data ORDER BY date DESC LIMIT 1")
    LiveData<MarketData> getLatestMarketDataLive();
    
    @Query("SELECT * FROM market_data WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    List<MarketData> getMarketDataBetweenDates(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT * FROM market_data WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    LiveData<List<MarketData>> getMarketDataBetweenDatesLive(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(*) FROM market_data")
    int getCount();
    
    @Query("DELETE FROM market_data WHERE date < :date")
    void deleteOlderThan(LocalDate date);
}