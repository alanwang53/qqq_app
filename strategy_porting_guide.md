# Porting QQQ3X Strategy from Python to Java/Kotlin

This guide outlines how to port the QQQ3X strategy from the Python implementation to Java/Kotlin for the Android app.

## 1. Strategy Overview

The QQQ3X strategy is a trading strategy that switches between leveraged QQQ ETF (3X) and safe assets (GLD or SHY) based on market conditions. The key components of the strategy are:

1. Technical indicators calculation (SMAs of different periods)
2. VIX-based conditions
3. QQQ trend analysis
4. Signal generation (-1 for safe asset, 1 for leveraged QQQ)

## 2. Required Libraries

### Python Dependencies
```python
import pandas as pd
import numpy as np
import talib
import yfinance as yf
from datetime import datetime
from dateutil.relativedelta import relativedelta
```

### Java/Kotlin Alternatives
```kotlin
// For data manipulation (instead of pandas)
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// For numerical operations (instead of numpy)
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

// For technical indicators (instead of talib)
// We'll implement our own SMA calculation functions
```

## 3. Data Structures

### Python DataFrame to Java/Kotlin
In Python, the strategy uses pandas DataFrame. In Java/Kotlin, we'll create custom data classes:

```kotlin
data class MarketData(
    val date: LocalDate,
    val qqqOpen: Double,
    val qqqClose: Double,
    val qqqHigh: Double,
    val qqqLow: Double,
    val qqqVolume: Double,
    val vixOpen: Double,
    val vixClose: Double,
    val gldClose: Double,
    val shyClose: Double
)

data class CalculatedData(
    val marketData: MarketData,
    // Technical indicators
    var qqqSmaYear: Double = 0.0,
    var qqqSmaLong: Double = 0.0,
    var qqqSmaShort: Double = 0.0,
    var gldSma: Double = 0.0,
    var vixSmaShort: Double = 0.0,
    var vixSmaLong: Double = 0.0,
    var vixSmaShort3: Double = 0.0,
    var vixSmaLong3: Double = 0.0,
    var vixC: Double = 0.0,
    var vixOpenClose: Double = 0.0,
    // Signal
    var rawSignal: Int? = null,
    var signal: Int = -1
)
```

## 4. Technical Indicator Calculations

### Simple Moving Average (SMA)
Python using talib:
```python
df["QQQ_SMA_YEAR"] = talib.MA(df["QQQ"], timeperiod=SMA_YEAR)
```

Java/Kotlin implementation:
```kotlin
fun calculateSMA(prices: List<Double>, period: Int): Double {
    if (prices.size < period) return 0.0
    
    val recentPrices = prices.takeLast(period)
    return recentPrices.sum() / period
}

// Usage
val qqqPrices = historicalData.map { it.qqqClose }
val qqqSmaYear = calculateSMA(qqqPrices, SMA_YEAR)
```

### Percentage Change
Python:
```python
df["VIX_C"] = df['VIX_OPEN'].pct_change().fillna(0)
```

Java/Kotlin:
```kotlin
fun calculatePercentageChange(values: List<Double>): List<Double> {
    val result = mutableListOf<Double>()
    result.add(0.0) // First element has no previous value
    
    for (i in 1 until values.size) {
        val change = (values[i] / values[i-1]) - 1
        result.add(change)
    }
    
    return result
}

// Usage
val vixOpenValues = historicalData.map { it.vixOpen }
val vixC = calculatePercentageChange(vixOpenValues)
```

### VIX Open/Close Ratio
Python:
```python
df["VIX_OPEN_CLOSE"] = ((df['VIX_OPEN']/df["^VIX"].shift(1)) - 1)
```

Java/Kotlin:
```kotlin
fun calculateVixOpenCloseRatio(vixOpen: List<Double>, vixClose: List<Double>): List<Double> {
    val result = mutableListOf<Double>()
    result.add(0.0) // First element has no previous close
    
    for (i in 1 until vixOpen.size) {
        val ratio = (vixOpen[i] / vixClose[i-1]) - 1
        result.add(ratio)
    }
    
    return result
}

// Usage
val vixOpenValues = historicalData.map { it.vixOpen }
val vixCloseValues = historicalData.map { it.vixClose }
val vixOpenClose = calculateVixOpenCloseRatio(vixOpenValues, vixCloseValues)
```

## 5. Condition Calculations

### QQQ Open/Close Conditions
Python:
```python
qqq_open_close_b = (df['QQQ_OPEN'].shift(-1) > df['QQQ']*1.005)
qqq_open_close_l = (df['QQQ_OPEN'].shift(-1) < df['QQQ']*0.96)
```

Java/Kotlin:
```kotlin
fun calculateQqqOpenCloseBullish(qqqOpen: Double, previousQqqClose: Double): Boolean {
    return qqqOpen > previousQqqClose * 1.005
}

fun calculateQqqOpenCloseBearish(qqqOpen: Double, previousQqqClose: Double): Boolean {
    return qqqOpen < previousQqqClose * 0.96
}

// Usage for today's condition
val qqqOpenCloseBullish = calculateQqqOpenCloseBullish(todayQqqOpen, yesterdayQqqClose)
val qqqOpenCloseBearish = calculateQqqOpenCloseBearish(todayQqqOpen, yesterdayQqqClose)
```

### VIX Conditions
Python:
```python
vix_up_much = (df["VIX_C"].shift(-1) > 0.2) | (df["VIX_OPEN_CLOSE"].shift(-1) > 0.2)
vix_sell = (df["VIX_SMA_SHORT"] > 1.2*df["VIX_SMA_LONG"]) | (df["VIX_OPEN"].shift(-1) > 1.2*df["VIX_SMA_LONG"])
```

Java/Kotlin:
```kotlin
fun isVixUpMuch(vixC: Double, vixOpenClose: Double): Boolean {
    return vixC > 0.2 || vixOpenClose > 0.2
}

fun isVixSellSignal(vixSmaShort: Double, vixSmaLong: Double, vixOpen: Double): Boolean {
    return vixSmaShort > 1.2 * vixSmaLong || vixOpen > 1.2 * vixSmaLong
}

// Usage
val vixUpMuch = isVixUpMuch(todayVixC, todayVixOpenClose)
val vixSell = isVixSellSignal(vixSmaShort, vixSmaLong, todayVixOpen)
```

## 6. Signal Generation

### Raw Signal Calculation
Python:
```python
df["Raw_Signal"] = np.select(
    condlist=[
        safe_cond | cond_down,
        lev_cond
    ],
    choicelist=[
        -1,   # Safe asset
        1     # Leveraged
    ],
    default=np.nan  # No change
)
```

Java/Kotlin:
```kotlin
fun calculateRawSignal(
    safeCond: Boolean,
    condDown: Boolean,
    levCond: Boolean
): Int? {
    return when {
        safeCond || condDown -> -1  // Safe asset
        levCond -> 1               // Leveraged
        else -> null               // No change (equivalent to NaN)
    }
}

// Usage
val rawSignal = calculateRawSignal(safeCond, condDown, levCond)
```

### Final Signal with Forward Fill
Python:
```python
df["Signal"] = df["Raw_Signal"].ffill().fillna(-1)
```

Java/Kotlin:
```kotlin
fun calculateFinalSignal(rawSignals: List<Int?>): List<Int> {
    val result = mutableListOf<Int>()
    var lastValidSignal = -1 // Default to safe asset
    
    for (signal in rawSignals) {
        if (signal != null) {
            lastValidSignal = signal
        }
        result.add(lastValidSignal)
    }
    
    return result
}

// Usage
val rawSignals = calculatedData.map { it.rawSignal }
val finalSignals = calculateFinalSignal(rawSignals)
```

## 7. Strategy Today Function

### Python Implementation
```python
def strategy_today(vix_open_today, qqq_open_today):
    today = datetime.today()
    three_years_ago = today - relativedelta(years=2)
    three_years_ago_str = three_years_ago.strftime("%Y-%m-%d")
    today_str = today.strftime("%Y-%m-%d")
    
    df = download_data(three_years_ago_str, today_str)
    
    # Calculate indicators
    # ...
    
    # Add today's data
    df = add_today_row(df)
    df["VIX_OPEN"].iloc[-1] = vix_open_today
    df['QQQ_OPEN'].iloc[-1] = qqq_open_today
    
    # Calculate conditions
    # ...
    
    # Generate signal
    # ...
    
    return df
```

### Java/Kotlin Implementation
```kotlin
fun strategyToday(vixOpenToday: Double, qqqOpenToday: Double): StrategyResult {
    // Get historical data from database
    val twoYearsAgo = LocalDate.now().minus(2, ChronoUnit.YEARS)
    val historicalData = dataRepository.getHistoricalData(twoYearsAgo, LocalDate.now())
    
    // Calculate indicators for historical data
    val calculatedData = calculateIndicators(historicalData)
    
    // Add today's data
    val today = MarketData(
        date = LocalDate.now(),
        qqqOpen = qqqOpenToday,
        qqqClose = 0.0, // Not available yet
        qqqHigh = 0.0,  // Not available yet
        qqqLow = 0.0,   // Not available yet
        qqqVolume = 0.0, // Not available yet
        vixOpen = vixOpenToday,
        vixClose = 0.0, // Not available yet
        gldClose = 0.0, // Not available yet
        shyClose = 0.0  // Not available yet
    )
    
    // Calculate today's conditions
    val todayConditions = calculateTodayConditions(
        calculatedData.last(), // Yesterday's data
        today
    )
    
    // Generate signal
    val rawSignal = calculateRawSignal(
        todayConditions.safeCond,
        todayConditions.condDown,
        todayConditions.levCond
    )
    
    // Get previous signal
    val previousSignal = calculatedData.last().signal
    
    // Determine final signal
    val finalSignal = rawSignal ?: previousSignal
    
    // Check if position change is needed
    val positionChanged = finalSignal != previousSignal
    
    // Create result
    return StrategyResult(
        signal = finalSignal,
        positionChanged = positionChanged,
        recommendedAction = getRecommendedAction(finalSignal, positionChanged),
        keyIndicators = todayConditions.keyIndicators
    )
}
```

## 8. Testing Strategy Port

To ensure the Java/Kotlin implementation matches the Python version:

1. Run both implementations with the same historical data
2. Compare signals generated for each day
3. Verify that position changes occur on the same days
4. Test edge cases (market gaps, extreme VIX values, etc.)

## 9. Optimization for Mobile

For efficient execution on mobile devices:

1. Pre-calculate indicators where possible
2. Use efficient data structures
3. Implement lazy loading for historical data
4. Cache calculation results
5. Use background processing for intensive calculations
6. Implement incremental updates (only recalculate what's needed)

## 10. Error Handling

Add robust error handling:

1. Handle missing or invalid data
2. Implement fallback strategies
3. Log calculation steps for debugging
4. Validate inputs and outputs
5. Handle edge cases (market holidays, data gaps, etc.)