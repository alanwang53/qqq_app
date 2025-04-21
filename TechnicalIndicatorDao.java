package com.example.qqq3xstrategy.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.qqq3xstrategy.data.models.TechnicalIndicator;
import com.example.qqq3xstrategy.data.models.relations.MarketDataWithIndicators;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object for TechnicalIndicator entity
 */
@Dao
public interface TechnicalIndicatorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TechnicalIndicator indicator);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TechnicalIndicator> indicators);
    
    @Query("SELECT * FROM technical_indicators WHERE date = :date")
    TechnicalIndicator getIndicatorForDate(LocalDate date);
    
    @Query("SELECT * FROM technical_indicators WHERE date = :date")
    LiveData<TechnicalIndicator> getIndicatorForDateLive(LocalDate date);
    
    @Query("SELECT * FROM technical_indicators ORDER BY date DESC LIMIT 1")
    TechnicalIndicator getLatestIndicator();
    
    @Query("SELECT * FROM technical_indicators ORDER BY date DESC LIMIT 1")
    LiveData<TechnicalIndicator> getLatestIndicatorLive();
    
    @Query("SELECT * FROM technical_indicators WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    List<TechnicalIndicator> getIndicatorsBetweenDates(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT * FROM technical_indicators WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    LiveData<List<TechnicalIndicator>> getIndicatorsBetweenDatesLive(LocalDate startDate, LocalDate endDate);
    
    @Transaction
    @Query("SELECT * FROM market_data md LEFT JOIN technical_indicators ti ON md.date = ti.date WHERE md.date BETWEEN :startDate AND :endDate ORDER BY md.date")
    List<MarketDataWithIndicators> getMarketDataWithIndicators(LocalDate startDate, LocalDate endDate);
    
    @Transaction
    @Query("SELECT * FROM market_data md LEFT JOIN technical_indicators ti ON md.date = ti.date WHERE md.date BETWEEN :startDate AND :endDate ORDER BY md.date")
    LiveData<List<MarketDataWithIndicators>> getMarketDataWithIndicatorsLive(LocalDate startDate, LocalDate endDate);
}