package com.example.qqq3xstrategy.data.database;

import androidx.room.TypeConverter;

import java.time.LocalDate;

/**
 * Type converter for Room to handle LocalDate objects
 */
public class DateConverter {
    @TypeConverter
    public static LocalDate fromString(String value) {
        return value == null ? null : LocalDate.parse(value);
    }
    
    @TypeConverter
    public static String dateToString(LocalDate date) {
        return date == null ? null : date.toString();
    }
}