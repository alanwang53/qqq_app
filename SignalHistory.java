package com.example.qqq3xstrategy.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

/**
 * Entity class representing strategy signals for a specific date
 */
@Entity(
    tableName = "signal_history",
    foreignKeys = @ForeignKey(
        entity = MarketData.class,
        parentColumns = "date",
        childColumns = "date",
        onDelete = ForeignKey.CASCADE
    )
)
public class SignalHistory {
    @PrimaryKey
    @NonNull
    private LocalDate date;
    
    // Raw signal can be null (no change)
    private Integer rawSignal;
    
    // Final signal after forward fill (-1 for safe asset, 1 for leveraged QQQ)
    @NonNull
    private Integer signal;
    
    // Whether position changed from previous day
    @NonNull
    private Boolean positionChanged;
    
    // Which safe asset is recommended ("GLD" or "SHY")
    @NonNull
    private String safeAsset;
    
    // Default constructor required by Room
    public SignalHistory() {
        this.signal = -1; // Default to safe asset
        this.positionChanged = false;
        this.safeAsset = "GLD"; // Default safe asset
    }
    
    // Constructor with all fields
    public SignalHistory(@NonNull LocalDate date, 
                        Integer rawSignal, 
                        @NonNull Integer signal, 
                        @NonNull Boolean positionChanged, 
                        @NonNull String safeAsset) {
        this.date = date;
        this.rawSignal = rawSignal;
        this.signal = signal;
        this.positionChanged = positionChanged;
        this.safeAsset = safeAsset;
    }
    
    // Getters and setters
    @NonNull
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(@NonNull LocalDate date) {
        this.date = date;
    }
    
    public Integer getRawSignal() {
        return rawSignal;
    }
    
    public void setRawSignal(Integer rawSignal) {
        this.rawSignal = rawSignal;
    }
    
    @NonNull
    public Integer getSignal() {
        return signal;
    }
    
    public void setSignal(@NonNull Integer signal) {
        this.signal = signal;
    }
    
    @NonNull
    public Boolean getPositionChanged() {
        return positionChanged;
    }
    
    public void setPositionChanged(@NonNull Boolean positionChanged) {
        this.positionChanged = positionChanged;
    }
    
    @NonNull
    public String getSafeAsset() {
        return safeAsset;
    }
    
    public void setSafeAsset(@NonNull String safeAsset) {
        this.safeAsset = safeAsset;
    }
    
    @Override
    public String toString() {
        return "SignalHistory{" +
                "date=" + date +
                ", rawSignal=" + rawSignal +
                ", signal=" + signal +
                ", positionChanged=" + positionChanged +
                ", safeAsset='" + safeAsset + '\'' +
                '}';
    }
}