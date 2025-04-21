package com.example.qqq3xstrategy.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity class representing user settings and strategy parameters
 */
@Entity(tableName = "user_settings")
public class UserSettings {
    @PrimaryKey
    private int id = 1; // Single row for settings
    
    // Notification settings
    private boolean notificationsEnabled = true;
    
    // Strategy parameters
    private String preferredSafeAsset = "GLD";
    private double targetLeverage = 3.0;
    private double safeRatio = 0.2;
    
    // SMA parameters
    private int smaShort = 5;
    private int smaLong = 15;
    private int smaShort2 = 1;
    private int smaLong2 = 3;
    private int smaShort3 = 3;
    private int smaLong3 = 9;
    private int smaYear = 155;
    
    // Default constructor
    public UserSettings() {
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
    
    public String getPreferredSafeAsset() {
        return preferredSafeAsset;
    }
    
    public void setPreferredSafeAsset(String preferredSafeAsset) {
        this.preferredSafeAsset = preferredSafeAsset;
    }
    
    public double getTargetLeverage() {
        return targetLeverage;
    }
    
    public void setTargetLeverage(double targetLeverage) {
        this.targetLeverage = targetLeverage;
    }
    
    public double getSafeRatio() {
        return safeRatio;
    }
    
    public void setSafeRatio(double safeRatio) {
        this.safeRatio = safeRatio;
    }
    
    public int getSmaShort() {
        return smaShort;
    }
    
    public void setSmaShort(int smaShort) {
        this.smaShort = smaShort;
    }
    
    public int getSmaLong() {
        return smaLong;
    }
    
    public void setSmaLong(int smaLong) {
        this.smaLong = smaLong;
    }
    
    public int getSmaShort2() {
        return smaShort2;
    }
    
    public void setSmaShort2(int smaShort2) {
        this.smaShort2 = smaShort2;
    }
    
    public int getSmaLong2() {
        return smaLong2;
    }
    
    public void setSmaLong2(int smaLong2) {
        this.smaLong2 = smaLong2;
    }
    
    public int getSmaShort3() {
        return smaShort3;
    }
    
    public void setSmaShort3(int smaShort3) {
        this.smaShort3 = smaShort3;
    }
    
    public int getSmaLong3() {
        return smaLong3;
    }
    
    public void setSmaLong3(int smaLong3) {
        this.smaLong3 = smaLong3;
    }
    
    public int getSmaYear() {
        return smaYear;
    }
    
    public void setSmaYear(int smaYear) {
        this.smaYear = smaYear;
    }
}