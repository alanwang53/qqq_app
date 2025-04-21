# QQQ3X Strategy Android App Architecture

## Overview
The app will implement the QQQ3X strategy from the provided Python code, fetching market data before NYSE opening (9:30 AM ET) and alerting users when they need to change their position based on the strategy's signals.

## Core Components

### 1. Data Service
- Fetches financial data from Yahoo Finance API
- Runs daily at 9:00 AM PT (UTC-8)
- Retrieves VIX and QQQ opening prices
- Stores historical data in SQLite database

### 2. Strategy Engine
- Implements the `strategy_today()` function logic
- Calculates technical indicators (SMAs, etc.)
- Determines trading signals based on market conditions
- Compares new signal with previous day's signal

### 3. Notification System
- Sends alerts at 9:10 AM PT (UTC-8) when position changes are needed
- Provides detailed information about the recommended action
- Uses Android notification channels for high visibility

### 4. User Interface
- Dashboard showing current position and strategy status
- Historical performance visualization
- Settings for notification preferences
- Manual data refresh option

## Data Flow
1. Scheduled job runs at 9:00 AM PT
2. App fetches latest VIX and QQQ data
3. Strategy engine processes data and generates signal
4. If signal differs from previous day, notification is triggered
5. User interface updates with latest recommendation

## Database Schema
- Historical price data table
- Signal history table
- User settings table

## Technical Requirements
- Android 8.0+ (API level 26+)
- Background processing capabilities
- Notification permission
- Internet permission
- SQLite database