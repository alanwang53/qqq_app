package com.example.qqq3xstrategy.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.qqq3xstrategy.data.models.SignalHistory;
import com.example.qqq3xstrategy.data.models.relations.MarketDataWithSignal;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object for SignalHistory entity
 */
@Dao
public interface SignalHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SignalHistory signalHistory);
    
    @Query("SELECT * FROM signal_history WHERE date = :date")
    SignalHistory getSignalForDate(LocalDate date);
    
    @Query("SELECT * FROM signal_history WHERE date = :date")
    LiveData<SignalHistory> getSignalForDateLive(LocalDate date);
    
    @Query("SELECT * FROM signal_history ORDER BY date DESC LIMIT 1")
    SignalHistory getLatestSignal();
    
    @Query("SELECT * FROM signal_history ORDER BY date DESC LIMIT 1")
    LiveData<SignalHistory> getLatestSignalLive();
    
    @Query("SELECT * FROM signal_history WHERE position_changed = 1 ORDER BY date DESC LIMIT :limit")
    List<SignalHistory> getRecentPositionChanges(int limit);
    
    @Query("SELECT * FROM signal_history WHERE position_changed = 1 ORDER BY date DESC LIMIT :limit")
    LiveData<List<SignalHistory>> getRecentPositionChangesLive(int limit);
    
    @Query("SELECT * FROM signal_history WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    List<SignalHistory> getSignalsBetweenDates(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT * FROM signal_history WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    LiveData<List<SignalHistory>> getSignalsBetweenDatesLive(LocalDate startDate, LocalDate endDate);
    
    @Transaction
    @Query("SELECT * FROM market_data md LEFT JOIN signal_history sh ON md.date = sh.date WHERE md.date BETWEEN :startDate AND :endDate ORDER BY md.date")
    List<MarketDataWithSignal> getMarketDataWithSignals(LocalDate startDate, LocalDate endDate);
    
    @Transaction
    @Query("SELECT * FROM market_data md LEFT JOIN signal_history sh ON md.date = sh.date WHERE md.date BETWEEN :startDate AND :endDate ORDER BY md.date")
    LiveData<List<MarketDataWithSignal>> getMarketDataWithSignalsLive(LocalDate startDate, LocalDate endDate);
}