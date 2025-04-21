package com.example.qqq3xstrategy.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

/**
 * Entity class representing technical indicators calculated for a specific date
 */
@Entity(
    tableName = "technical_indicators",
    foreignKeys = @ForeignKey(
        entity = MarketData.class,
        parentColumns = "date",
        childColumns = "date",
        onDelete = ForeignKey.CASCADE
    )
)
public class TechnicalIndicator {
    @PrimaryKey
    @NonNull
    private LocalDate date;
    
    // QQQ SMAs
    private double qqqSmaYear;
    private double qqqSmaLong;
    private double qqqSmaShort;
    
    // GLD SMA
    private double gldSma;
    
    // VIX SMAs
    private double vixSmaShort;
    private double vixSmaLong;
    private double vixSmaShort3;
    private double vixSmaLong3;
    
    // VIX indicators
    private double vixC;
    private double vixOpenClose;
    
    // Default constructor required by Room
    public TechnicalIndicator() {
    }
    
    // Constructor with all fields
    public TechnicalIndicator(@NonNull LocalDate date, 
                             double qqqSmaYear, double qqqSmaLong, double qqqSmaShort,
                             double gldSma,
                             double vixSmaShort, double vixSmaLong, 
                             double vixSmaShort3, double vixSmaLong3,
                             double vixC, double vixOpenClose) {
        this.date = date;
        this.qqqSmaYear = qqqSmaYear;
        this.qqqSmaLong = qqqSmaLong;
        this.qqqSmaShort = qqqSmaShort;
        this.gldSma = gldSma;
        this.vixSmaShort = vixSmaShort;
        this.vixSmaLong = vixSmaLong;
        this.vixSmaShort3 = vixSmaShort3;
        this.vixSmaLong3 = vixSmaLong3;
        this.vixC = vixC;
        this.vixOpenClose = vixOpenClose;
    }
    
    // Getters and setters
    @NonNull
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(@NonNull LocalDate date) {
        this.date = date;
    }
    
    public double getQqqSmaYear() {
        return qqqSmaYear;
    }
    
    public void setQqqSmaYear(double qqqSmaYear) {
        this.qqqSmaYear = qqqSmaYear;
    }
    
    public double getQqqSmaLong() {
        return qqqSmaLong;
    }
    
    public void setQqqSmaLong(double qqqSmaLong) {
        this.qqqSmaLong = qqqSmaLong;
    }
    
    public double getQqqSmaShort() {
        return qqqSmaShort;
    }
    
    public void setQqqSmaShort(double qqqSmaShort) {
        this.qqqSmaShort = qqqSmaShort;
    }
    
    public double getGldSma() {
        return gldSma;
    }
    
    public void setGldSma(double gldSma) {
        this.gldSma = gldSma;
    }
    
    public double getVixSmaShort() {
        return vixSmaShort;
    }
    
    public void setVixSmaShort(double vixSmaShort) {
        this.vixSmaShort = vixSmaShort;
    }
    
    public double getVixSmaLong() {
        return vixSmaLong;
    }
    
    public void setVixSmaLong(double vixSmaLong) {
        this.vixSmaLong = vixSmaLong;
    }
    
    public double getVixSmaShort3() {
        return vixSmaShort3;
    }
    
    public void setVixSmaShort3(double vixSmaShort3) {
        this.vixSmaShort3 = vixSmaShort3;
    }
    
    public double getVixSmaLong3() {
        return vixSmaLong3;
    }
    
    public void setVixSmaLong3(double vixSmaLong3) {
        this.vixSmaLong3 = vixSmaLong3;
    }
    
    public double getVixC() {
        return vixC;
    }
    
    public void setVixC(double vixC) {
        this.vixC = vixC;
    }
    
    public double getVixOpenClose() {
        return vixOpenClose;
    }
    
    public void setVixOpenClose(double vixOpenClose) {
        this.vixOpenClose = vixOpenClose;
    }
    
    @Override
    public String toString() {
        return "TechnicalIndicator{" +
                "date=" + date +
                ", qqqSmaYear=" + qqqSmaYear +
                ", qqqSmaLong=" + qqqSmaLong +
                ", qqqSmaShort=" + qqqSmaShort +
                ", vixSmaShort=" + vixSmaShort +
                ", vixSmaLong=" + vixSmaLong +
                '}';
    }
}