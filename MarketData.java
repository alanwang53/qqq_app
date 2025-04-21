package com.example.qqq3xstrategy.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

/**
 * Entity class representing market data for a specific date
 */
@Entity(tableName = "market_data")
public class MarketData {
    @PrimaryKey
    @NonNull
    private LocalDate date;
    
    // QQQ data
    private double qqqOpen;
    private double qqqClose;
    private double qqqHigh;
    private double qqqLow;
    private double qqqVolume;
    
    // VIX data
    private double vixOpen;
    private double vixClose;
    private double vixHigh;
    private double vixLow;
    
    // GLD data
    private double gldOpen;
    private double gldClose;
    
    // SHY data
    private double shyOpen;
    private double shyClose;
    
    // Default constructor required by Room
    public MarketData() {
    }
    
    // Constructor with all fields
    public MarketData(@NonNull LocalDate date, 
                     double qqqOpen, double qqqClose, double qqqHigh, double qqqLow, double qqqVolume,
                     double vixOpen, double vixClose, double vixHigh, double vixLow,
                     double gldOpen, double gldClose,
                     double shyOpen, double shyClose) {
        this.date = date;
        this.qqqOpen = qqqOpen;
        this.qqqClose = qqqClose;
        this.qqqHigh = qqqHigh;
        this.qqqLow = qqqLow;
        this.qqqVolume = qqqVolume;
        this.vixOpen = vixOpen;
        this.vixClose = vixClose;
        this.vixHigh = vixHigh;
        this.vixLow = vixLow;
        this.gldOpen = gldOpen;
        this.gldClose = gldClose;
        this.shyOpen = shyOpen;
        this.shyClose = shyClose;
    }
    
    // Getters and setters
    @NonNull
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(@NonNull LocalDate date) {
        this.date = date;
    }
    
    public double getQqqOpen() {
        return qqqOpen;
    }
    
    public void setQqqOpen(double qqqOpen) {
        this.qqqOpen = qqqOpen;
    }
    
    public double getQqqClose() {
        return qqqClose;
    }
    
    public void setQqqClose(double qqqClose) {
        this.qqqClose = qqqClose;
    }
    
    public double getQqqHigh() {
        return qqqHigh;
    }
    
    public void setQqqHigh(double qqqHigh) {
        this.qqqHigh = qqqHigh;
    }
    
    public double getQqqLow() {
        return qqqLow;
    }
    
    public void setQqqLow(double qqqLow) {
        this.qqqLow = qqqLow;
    }
    
    public double getQqqVolume() {
        return qqqVolume;
    }
    
    public void setQqqVolume(double qqqVolume) {
        this.qqqVolume = qqqVolume;
    }
    
    public double getVixOpen() {
        return vixOpen;
    }
    
    public void setVixOpen(double vixOpen) {
        this.vixOpen = vixOpen;
    }
    
    public double getVixClose() {
        return vixClose;
    }
    
    public void setVixClose(double vixClose) {
        this.vixClose = vixClose;
    }
    
    public double getVixHigh() {
        return vixHigh;
    }
    
    public void setVixHigh(double vixHigh) {
        this.vixHigh = vixHigh;
    }
    
    public double getVixLow() {
        return vixLow;
    }
    
    public void setVixLow(double vixLow) {
        this.vixLow = vixLow;
    }
    
    public double getGldOpen() {
        return gldOpen;
    }
    
    public void setGldOpen(double gldOpen) {
        this.gldOpen = gldOpen;
    }
    
    public double getGldClose() {
        return gldClose;
    }
    
    public void setGldClose(double gldClose) {
        this.gldClose = gldClose;
    }
    
    public double getShyOpen() {
        return shyOpen;
    }
    
    public void setShyOpen(double shyOpen) {
        this.shyOpen = shyOpen;
    }
    
    public double getShyClose() {
        return shyClose;
    }
    
    public void setShyClose(double shyClose) {
        this.shyClose = shyClose;
    }
    
    @Override
    public String toString() {
        return "MarketData{" +
                "date=" + date +
                ", qqqOpen=" + qqqOpen +
                ", qqqClose=" + qqqClose +
                ", vixOpen=" + vixOpen +
                ", vixClose=" + vixClose +
                '}';
    }
}