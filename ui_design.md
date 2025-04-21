# QQQ3X Strategy App UI Design

## 1. Main Dashboard Screen

The main dashboard is the primary interface for users to monitor their strategy status.

```
+-----------------------------------------------+
|                                               |
|  QQQ3X Strategy                      ⚙️ Menu  |
|                                               |
+-----------------------------------------------+
|                                               |
|  Current Position                             |
|  +-------------------------------------------+|
|  |                                           ||
|  |  [LEVERAGED QQQ (3X)]  or  [SAFE ASSET]   ||
|  |                                           ||
|  |  Since: April 15, 2025                    ||
|  |  Days in position: 5                      ||
|  |                                           ||
|  +-------------------------------------------+|
|                                               |
|  Market Data (Last Updated: 9:05 AM)          |
|  +-------------------------------------------+|
|  |                                           ||
|  |  QQQ: $448.50 (+0.5%)                     ||
|  |  VIX: 31.40 (-2.1%)                       ||
|  |                                           ||
|  |  QQQ SMA (5): 445.20                      ||
|  |  QQQ SMA (15): 440.10                     ||
|  |  QQQ SMA (155): 420.30                    ||
|  |                                           ||
|  +-------------------------------------------+|
|                                               |
|  Key Indicators                               |
|  +-------------------------------------------+|
|  |                                           ||
|  |  QQQ Trend: ↗️ UPTREND                    ||
|  |  VIX Trend: ↘️ DOWNTREND                  ||
|  |  Signal Strength: STRONG                  ||
|  |                                           ||
|  +-------------------------------------------+|
|                                               |
|  [REFRESH DATA]                               |
|                                               |
+-----------------------------------------------+
```

## 2. Strategy History Screen

Displays the history of position changes and performance.

```
+-----------------------------------------------+
|                                               |
|  Strategy History                    ← Back   |
|                                               |
+-----------------------------------------------+
|                                               |
|  Performance Chart                            |
|  +-------------------------------------------+|
|  |                                           ||
|  |  [Line chart showing strategy performance] ||
|  |  [with position changes highlighted]       ||
|  |                                           ||
|  +-------------------------------------------+|
|                                               |
|  Position Changes                             |
|  +-------------------------------------------+|
|  | Date       | Position  | Duration | Gain  ||
|  |------------|-----------|----------|-------||
|  | 2025-04-15 | Leveraged | 5 days   | +2.1% ||
|  | 2025-04-10 | Safe      | 12 days  | +0.3% ||
|  | 2025-03-29 | Leveraged | 25 days  | +5.2% ||
|  | ...        | ...       | ...      | ...   ||
|  +-------------------------------------------+|
|                                               |
|  [EXPORT DATA]                                |
|                                               |
+-----------------------------------------------+
```

## 3. Settings Screen

Allows users to configure app preferences and strategy parameters.

```
+-----------------------------------------------+
|                                               |
|  Settings                            ← Back   |
|                                               |
+-----------------------------------------------+
|                                               |
|  Notifications                                |
|  +-------------------------------------------+|
|  |                                           ||
|  |  Enable Notifications     [ON/OFF TOGGLE] ||
|  |  Alert Sound              [ON/OFF TOGGLE] ||
|  |  Vibration                [ON/OFF TOGGLE] ||
|  |                                           ||
|  +-------------------------------------------+|
|                                               |
|  Data Settings                                |
|  +-------------------------------------------+|
|  |                                           ||
|  |  Auto-refresh Data        [ON/OFF TOGGLE] ||
|  |  Refresh Time             [9:00 AM      ] ||
|  |  Data Source              [Yahoo Finance] ||
|  |                                           ||
|  +-------------------------------------------+|
|                                               |
|  Strategy Parameters                          |
|  +-------------------------------------------+|
|  |                                           ||
|  |  Preferred Safe Asset     [GLD ▼]         ||
|  |  Target Leverage          [3.0          ] ||
|  |  Safe Asset Ratio         [0.2          ] ||
|  |                                           ||
|  |  Advanced Parameters      [EXPAND >]      ||
|  |                                           ||
|  +-------------------------------------------+|
|                                               |
|  [RESTORE DEFAULTS]                           |
|                                               |
+-----------------------------------------------+
```

## 4. Notification Design

Alerts that appear when a position change is recommended.

```
+-----------------------------------------------+
|                                               |
|  QQQ3X Strategy                               |
|  Position Change Recommended                  |
|                                               |
|  SWITCH TO: LEVERAGED QQQ (3X)                |
|  Time: 9:10 AM, April 20, 2025                |
|                                               |
|  [VIEW DETAILS]        [DISMISS]              |
|                                               |
+-----------------------------------------------+
```

## 5. Widget Design

Home screen widget for quick status checks.

```
+-----------------------------------------------+
|                                               |
|  QQQ3X Strategy                               |
|                                               |
|  Current: LEVERAGED QQQ (3X)                  |
|  Since: April 15, 2025                        |
|                                               |
|  QQQ: $448.50 (+0.5%)                         |
|  VIX: 31.40 (-2.1%)                           |
|                                               |
|  Last Updated: 9:05 AM                        |
|                                               |
+-----------------------------------------------+
```

## 6. Position Change Details Screen

Detailed view when a position change is recommended.

```
+-----------------------------------------------+
|                                               |
|  Position Change                     ← Back   |
|                                               |
+-----------------------------------------------+
|                                               |
|  RECOMMENDATION                               |
|  +-------------------------------------------+|
|  |                                           ||
|  |  SWITCH TO: LEVERAGED QQQ (3X)            ||
|  |                                           ||
|  |  Previous Position: SAFE ASSET (GLD)      ||
|  |  Held for: 12 days                        ||
|  |                                           ||
|  +-------------------------------------------+|
|                                               |
|  SIGNAL DETAILS                               |
|  +-------------------------------------------+|
|  |                                           ||
|  |  VIX Open: 31.40 (Down 2.1% from close)   ||
|  |  QQQ Open: $448.50 (Up 0.5% from close)   ||
|  |                                           ||
|  |  QQQ > 1.03 × SMA(155): YES               ||
|  |  QQQ SMA(5) > 0.99 × SMA(15): YES         ||
|  |  VIX < 21 & QQQ Open > 1.005 × Close: NO  ||
|  |  VIX > 66: NO                             ||
|  |                                           ||
|  |  Signal Confidence: HIGH                  ||
|  |                                           ||
|  +-------------------------------------------+|
|                                               |
|  [MARK AS ACTIONED]                           |
|                                               |
+-----------------------------------------------+
```

## 7. Navigation Structure

```
Main Dashboard
├── Refresh Data
├── Strategy History
│   └── Export Data
├── Settings
│   ├── Notifications
│   ├── Data Settings
│   └── Strategy Parameters
└── Position Change Details (when applicable)
    └── Mark as Actioned