
import pandas as pd
import yfinance as yf
import numpy as np
import matplotlib.pyplot as plt
import statsmodels.api as sm
from datetime import datetime
from dateutil.relativedelta import relativedelta
import talib
from analysis import *
from download import *
# ====================
# 用戶自定義參數區
# ====================
#START_DATE = "2010-01-01"     # 回測起始日期
START_DATE = "2003-09-18"     # QQQ 上市日
END_DATE = "2025-05-12"       # 回測結束日期
INITIAL_CAPITAL = 10000       # 初始資金
COMMISSION = 0.008            # 單次交易佣金率（0.1%）+ slippage 

TARGET_LEVERAGE = 3       # 目標槓桿倍數（2~3之間）
#SAFE_ASSET = "GLD"            # 避險資產代碼（可換為 "SHY"（短債）、"GLD"（黃金）、"CASH"（現金）等
MANAGEMENT_FEE = 0.0095       # 年度管理費（TQQQ 實際年費 0.95%）
ORI_DISCOUNT =  1          # encounter max drawa down 65% at beggining 
SAFE_RATIO = 0.2       # safe asset ratio
daily_fee =  MANAGEMENT_FEE  / 252
SMA_SHORT = 5
SMA_LONG =  15
SMA_SHORT2 = 1
SMA_LONG2 =  3

SMA_SHORT3 = 3
SMA_LONG3 =  9
SMA_YEAR = 155

def get_trading_signal(df_historical, vix_open_today, qqq_open_today):
    """
    Calculates the trading signal for today based on historical data and today's open prices.

    Args:
        df_historical (pd.DataFrame): DataFrame containing historical data for QQQ, ^VIX, GLD, SHY.
                                      Must include 'Open', 'Close', 'High', 'Low', 'Volume' for each ticker.
                                      The last row should be yesterday's data.
        vix_open_today (float): The opening price of VIX for today.
        qqq_open_today (float): The opening price of QQQ for today.

    Returns:
        int: The trading signal for today (-1 for safe asset, 1 for leverage QQQ).
             Returns None if there is not enough historical data.
    """
    df = df_historical.copy()

    # Add a row for today with open prices
    today = datetime.now().strftime("%Y-%m-%d")
    # Check if the last date in df is today, if so, update it. Otherwise, append.
    if df.index[-1].strftime("%Y-%m-%d") == today:
         df.loc[df.index[-1], 'VIX_OPEN'] = vix_open_today
         df.loc[df.index[-1], 'QQQ_OPEN'] = qqq_open_today
    else:
        today_data = {
            'QQQ_OPEN': qqq_open_today, 'QQQ': np.nan, 'QQQ_HIGH': np.nan, 'QQQ_LOW': np.nan, 'QQQ_VOLUME': np.nan,
            'GLD_OPEN': np.nan, 'GLD': np.nan, 'GLD_HIGH': np.nan, 'GLD_LOW': np.nan, 'GLD_VOLUME': np.nan,
            'SHY_OPEN': np.nan, 'SHY': np.nan, 'SHY_HIGH': np.nan, 'SHY_LOW': np.nan, 'SHY_VOLUME': np.nan,
            'VIX_OPEN': vix_open_today, '^VIX': np.nan, 'VIX_HIGH': np.nan, 'VIX_LOW': np.nan, 'VIX_VOLUME': np.nan,
        }
        df_today = pd.DataFrame([today_data], index=[pd.to_datetime(today)])
        df = pd.concat([df, df_today])


    # Ensure enough data for SMAs
    if len(df) < SMA_YEAR + 1: # Need SMA_YEAR days + today's open
        print("Not enough historical data to calculate signal.")
        return None

    # Calculate indicators (using shift(-1) for conditions based on tomorrow's open, which is today's open in this context)
    df["QQQ_SMA_YEAR"] = talib.MA(df["QQQ"], timeperiod=SMA_YEAR)
    df["QQQ_SMA_LONG"] = talib.MA(df["QQQ"], timeperiod=SMA_LONG)
    df["QQQ_SMA_SHORT"] = talib.MA(df["QQQ"], timeperiod=SMA_SHORT)
    df["GLD_SMA"] = df["GLD"].rolling(window=100, min_periods=1).mean()

    df["VIX_SMA_SHORT"] = talib.MA(df["^VIX"], timeperiod=SMA_SHORT2)
    df["VIX_SMA_LONG"]  = talib.MA(df["^VIX"], timeperiod=SMA_LONG2)

    df["VIX_SMA_SHORT3"] = talib.MA(df["^VIX"], timeperiod=SMA_SHORT3)
    df["VIX_SMA_LONG3"]  = talib.MA(df["^VIX"], timeperiod=SMA_LONG3)

    # Recalculate VIX_C and VIX_OPEN_CLOSE to include today's open
    df["VIX_C"]   = df['VIX_OPEN'].pct_change().fillna(0)
    df["VIX_OPEN_CLOSE"]= ( (df['VIX_OPEN']/df["^VIX"].shift(1)) -1)

    # Define conditions (using .iloc[-1] to get the conditions for the last row, which is today)
    # Note: The original code used shift(-1) on the conditions themselves, which is unusual.
    # I will interpret this as the conditions being based on today's open and yesterday's close data.
    # If the original logic truly intended to use tomorrow's open for today's signal,
    # this would require a different data structure or approach.
    # Assuming shift(-1) on conditions was meant to apply the condition logic to the *next* row's open,
    # when calculating for today (the last row), we use today's open directly.

    # Re-evaluating the original code's shift(-1) usage:
    # qqq_open_close_b = (df['QQQ_OPEN'].shift(-1)> df['QQQ']*1.005)
    # This compares tomorrow's open to today's close. When calculating for today, this means comparing today's open to yesterday's close.
    # So, for the last row (today), we need df['QQQ_OPEN'].iloc[-1] and df['QQQ'].iloc[-2] (yesterday's close).

    qqq_open_close_b = (df['QQQ_OPEN'].iloc[-1] > df['QQQ'].iloc[-2] * 1.005) if len(df) >= 2 else False
    qqq_open_close_l = (df['QQQ_OPEN'].iloc[-1] < df['QQQ'].iloc[-2] * 0.96) if len(df) >= 2 else False

    # Similarly, for VIX conditions using shift(-1):
    # vix_up_much   =  (df["VIX_C"].shift(-1)>0.2 )     | (df["VIX_OPEN_CLOSE"].shift(-1) >0.2)
    # This compares tomorrow's VIX change/open-close to today's data. For today's signal, it's today's VIX change/open-close vs yesterday's data.
    # So, for the last row (today), we need df["VIX_C"].iloc[-1] and df["VIX_OPEN_CLOSE"].iloc[-1].

    vix_up_much   =  (df["VIX_C"].iloc[-1] > 0.2) or (df["VIX_OPEN_CLOSE"].iloc[-1] > 0.2)
    # vix_sell = (df["VIX_SMA_SHORT"] > 1.2*df["VIX_SMA_LONG"]) |(df["VIX_OPEN"].shift(-1) > 1.2*df["VIX_SMA_LONG"])
    # This compares today's SMAs or tomorrow's open to today's SMAs. For today's signal, it's today's SMAs or today's open to yesterday's SMAs.
    # Let's use today's SMAs and today's open vs today's SMAs.
    vix_sell = (df["VIX_SMA_SHORT"].iloc[-1] > 1.2 * df["VIX_SMA_LONG"].iloc[-1]) or \
               (df["VIX_OPEN"].iloc[-1] > 1.2 * df["VIX_SMA_LONG"].iloc[-1])

    # vix_down_today = (df["VIX_OPEN_CLOSE"].shift(-1) <-0.03)
    vix_down_today = (df["VIX_OPEN_CLOSE"].iloc[-1] < -0.03)
    # vix_down_today2 = (df["VIX_OPEN_CLOSE"].shift(-1) < 0.05)
    vix_down_today2 = (df["VIX_OPEN_CLOSE"].iloc[-1] < 0.05)
    # vix_down_smooth =  (df["VIX_SMA_SHORT3"] < 0.97*df["VIX_SMA_LONG3"])
    vix_down_smooth =  (df["VIX_SMA_SHORT3"].iloc[-1] < 0.97 * df["VIX_SMA_LONG3"].iloc[-1])
    # vix_down_smooth2 =  (df["VIX_SMA_SHORT3"] < 0.95*df["VIX_SMA_LONG3"]) & ( (df["^VIX"]>50)| (df['VIX_OPEN'].shift(-1)>50))
    vix_down_smooth2 =  (df["VIX_SMA_SHORT3"].iloc[-1] < 0.95 * df["VIX_SMA_LONG3"].iloc[-1]) and \
                        ((df["^VIX"].iloc[-2] > 50) or (df['VIX_OPEN'].iloc[-1] > 50)) # Use yesterday's VIX close and today's VIX open

    vix_no_need_safe  = vix_down_today2 and vix_down_smooth2
    # qqq_up_trend   = (df["QQQ_SMA_SHORT"]>0.99*df["QQQ_SMA_LONG"])
    qqq_up_trend   = (df["QQQ_SMA_SHORT"].iloc[-1] > 0.99 * df["QQQ_SMA_LONG"].iloc[-1])
    # qqq_down_trend = (df["QQQ_SMA_SHORT"]<0.95*df["QQQ_SMA_LONG"])
    qqq_down_trend = (df["QQQ_SMA_SHORT"].iloc[-1] < 0.95 * df["QQQ_SMA_LONG"].iloc[-1])

    # qqq_year_up   = (df["QQQ"] >  (1.03 * df["QQQ_SMA_YEAR"])) & (df['QQQ_OPEN'].shift(-1) > (1.03 * df["QQQ_SMA_YEAR"]))
    qqq_year_up   = (df["QQQ"].iloc[-2] > (1.03 * df["QQQ_SMA_YEAR"].iloc[-2])) and \
                    (df['QQQ_OPEN'].iloc[-1] > (1.03 * df["QQQ_SMA_YEAR"].iloc[-2])) # Use yesterday's QQQ close and SMA, and today's QQQ open
    # qqq_year_down = (df["QQQ"] <  (0.99* df["QQQ_SMA_YEAR"]) ) | (df['QQQ_OPEN'].shift(-1) < (0.99 * df["QQQ_SMA_YEAR"]) )
    qqq_year_down = (df["QQQ"].iloc[-2] < (0.99 * df["QQQ_SMA_YEAR"].iloc[-2])) or \
                    (df['QQQ_OPEN'].iloc[-1] < (0.99 * df["QQQ_SMA_YEAR"].iloc[-2])) # Use yesterday's QQQ close and SMA, and today's QQQ open

    # VIX_b_66 = (df["^VIX"]>66) & (df["VIX_OPEN"].shift(-1)>66)
    VIX_b_66 = (df["^VIX"].iloc[-2] > 66) and (df["VIX_OPEN"].iloc[-1] > 66) # Use yesterday's VIX close and today's VIX open
    # VIX_l_60 = (df["^VIX"]<60) & (df["VIX_OPEN"].shift(-1)<60)
    VIX_l_60 = (df["^VIX"].iloc[-2] < 60) and (df["VIX_OPEN"].iloc[-1] < 60) # Use yesterday's VIX close and today's VIX open

    # VIX_l_21 = (df['VIX_OPEN'].shift(-1)<21)  & qqq_open_close_b
    VIX_l_21 = (df['VIX_OPEN'].iloc[-1] < 21) and qqq_open_close_b # Use today's VIX open and the qqq_open_close_b condition (based on today's QQQ open and yesterday's close)
    # VIX_b_23 = (df['VIX_OPEN'].shift(-1)>23)  | qqq_open_close_l
    VIX_b_23 = (df['VIX_OPEN'].iloc[-1] > 23) or qqq_open_close_l # Use today's VIX open and the qqq_open_close_l condition (based on today's QQQ open and yesterday's close)
    # VIX_b_32 = (df['VIX_OPEN'].shift(-1)>32) | (df["^VIX"]>32)
    VIX_b_32 = (df['VIX_OPEN'].iloc[-1] > 32) or (df["^VIX"].iloc[-2] > 32) # Use today's VIX open and yesterday's VIX close

    # vix_mid_range      =  (df['VIX_OPEN'].shift(-1)<33) & (df['VIX_OPEN'].shift(-1)>26)
    vix_mid_range      =  (df['VIX_OPEN'].iloc[-1] < 33) and (df['VIX_OPEN'].iloc[-1] > 26) # Use today's VIX open
    # qqq_up_trend_mid  =  (df["QQQ"]>1.02*df["QQQ_SMA_LONG"])
    qqq_up_trend_mid  =  (df["QQQ"].iloc[-2] > 1.02 * df["QQQ_SMA_LONG"].iloc[-2]) # Use yesterday's QQQ close and SMA
    # qqq_down_trend_mid  =  (df["QQQ_OPEN"].shift(-1)<0.95*df["QQQ_SMA_LONG"])
    qqq_down_trend_mid  =  (df["QQQ_OPEN"].iloc[-1] < 0.95 * df["QQQ_SMA_LONG"].iloc[-2]) # Use today's QQQ open and yesterday's QQQ SMA

    # cond_up    =   (VIX_b_32 & (vix_down_today&vix_down_smooth)) #| (vix_mid_range&(vix_down_today&qqq_up_trend ))
    cond_up    =   (VIX_b_32 and (vix_down_today and vix_down_smooth)) #| (vix_mid_range&(vix_down_today&qqq_up_trend ))

    # cond_down  =   ((VIX_b_32)&(vix_up_much|vix_sell)) #|(vix_mid_range&(qqq_down_trend_mid))
    cond_down  =   ((VIX_b_32) and (vix_up_much or vix_sell)) #|(vix_mid_range&(qqq_down_trend_mid))

    # lev_cond =    qqq_year_up   | (qqq_up_trend & VIX_l_21)  |   (VIX_b_66)
    lev_cond =    qqq_year_up   or (qqq_up_trend and VIX_l_21)  or   (VIX_b_66)
    # safe_cond =   qqq_year_down & (qqq_down_trend|VIX_b_23)  & ((VIX_l_60))# 条件2：低于1%阈值
    safe_cond =   qqq_year_down and (qqq_down_trend or VIX_b_23)  and ((VIX_l_60))# 条件2：低于1%阈值


    # Determine Raw Signal for today (last row)
    raw_signal_today = np.nan
    if (safe_cond and not cond_up and not vix_no_need_safe) or cond_down:
        raw_signal_today = -1
    elif (lev_cond and not cond_down) or cond_up:
        raw_signal_today = 1

    # Get yesterday's signal for ffill logic
    # Need to calculate signal for previous days to get yesterday's signal
    # This requires running the signal calculation on the historical data first
    # Let's re-structure to calculate signals for all historical data + today's open
    # and then take the last signal.

    # Calculate signals for all data up to today's open
    df["Raw_Signal"] = np.select(
        condlist=[
            (df["QQQ"].shift(-1) <  (0.99* df["QQQ_SMA_YEAR"].shift(-1)) ) | (df['QQQ_OPEN'].shift(-2) < (0.99 * df["QQQ_SMA_YEAR"].shift(-1)) ) & # qqq_year_down
            ( (df["QQQ_SMA_SHORT"].shift(-1)<0.95*df["QQQ_SMA_LONG"].shift(-1)) | (df['VIX_OPEN'].shift(-2)>23)  | (df['QQQ_OPEN'].shift(-2)< df['QQQ'].shift(-1)*0.96) ) & # qqq_down_trend | VIX_b_23
            ((df["^VIX"].shift(-1)<60) & (df["VIX_OPEN"].shift(-2)<60)) & # VIX_l_60
            ~((df['VIX_OPEN'].shift(-2)>32) | (df["^VIX"].shift(-1)>32) & ((df["VIX_OPEN_CLOSE"].shift(-2) <-0.03)&(df["VIX_SMA_SHORT3"].shift(-1) < 0.97*df["VIX_SMA_LONG3"].shift(-1)))) & # ~cond_up
            ~((df["VIX_OPEN_CLOSE"].shift(-2) < 0.05) & (df["VIX_SMA_SHORT3"].shift(-1) < 0.95*df["VIX_SMA_LONG3"].shift(-1)) & ( (df["^VIX"].shift(-1)>50)| (df['VIX_OPEN'].shift(-2)>50)) ) # ~vix_no_need_safe
            |
            ((df['VIX_OPEN'].shift(-2)>32) | (df["^VIX"].shift(-1)>32) & ((df["VIX_C"].shift(-2)>0.2 )     | (df["VIX_OPEN_CLOSE"].shift(-2) >0.2)|(df["VIX_SMA_SHORT"].shift(-1) > 1.2*df["VIX_SMA_LONG"].shift(-1)) |(df["VIX_OPEN"].shift(-2) > 1.2*df["VIX_SMA_LONG"].shift(-1)))), # cond_down

            (df["QQQ"].shift(-1) >  (1.03 * df["QQQ_SMA_YEAR"].shift(-1))) & (df['QQQ_OPEN'].shift(-2) > (1.03 * df["QQQ_SMA_YEAR"].shift(-1))) # qqq_year_up
            |
            ( (df["QQQ_SMA_SHORT"].shift(-1)>0.99*df["QQQ_SMA_LONG"].shift(-1)) & ((df['VIX_OPEN'].shift(-2)<21)  & (df['QQQ_OPEN'].shift(-2)> df['QQQ'].shift(-1)*1.005)) ) # qqq_up_trend & VIX_l_21
            |
            ((df["^VIX"].shift(-1)>66) & (df["VIX_OPEN"].shift(-2)>66)) # VIX_b_66
            |
            ((df['VIX_OPEN'].shift(-2)>32) | (df["^VIX"].shift(-1)>32) & ((df["VIX_OPEN_CLOSE"].shift(-2) <-0.03)&(df["VIX_SMA_SHORT3"].shift(-1) < 0.97*df["VIX_SMA_LONG3"].shift(-1)))) # cond_up
            &
            ~((df['VIX_OPEN'].shift(-2)>32) | (df["^VIX"].shift(-1)>32) & ((df["VIX_C"].shift(-2)>0.2 )     | (df["VIX_OPEN_CLOSE"].shift(-2) >0.2)|(df["VIX_SMA_SHORT"].shift(-1) > 1.2*df["VIX_SMA_LONG"].shift(-1)) |(df["VIX_OPEN"].shift(-2) > 1.2*df["VIX_SMA_LONG"].shift(-1)))) # ~cond_down

        ],
        choicelist=[
            -1,   # 满足条件1时信号为-1（safe）
            1   # 满足条件2时信号为-1（leverage）
        ],
        default=np.nan  # 中间区域不改变信号
    )

    df["Signal"] = df["Raw_Signal"].ffill().fillna(-1)  # Forward fill and fill initial NaNs with -1

    # The signal for today is the last calculated signal
    signal_today = df["Signal"].iloc[-1]

    return int(signal_today)


def backtest_strategy(df):

    df["QQQ_SMA_YEAR"] = talib.MA(df["QQQ"], timeperiod=SMA_YEAR)  # matype=1 表示SMMA
    df["QQQ_SMA_LONG"] = talib.MA(df["QQQ"], timeperiod=SMA_LONG)  # matype=1 表示SMMA
    df["QQQ_SMA_SHORT"] =talib.MA(df["QQQ"], timeperiod=SMA_SHORT)  # matype=1 表示SMMA
    df["GLD_SMA"] = df["GLD"].rolling(window=100, min_periods=1).mean()
    
    df["VIX_SMA_SHORT"] = talib.MA(df["^VIX"], timeperiod=SMA_SHORT2) 
    df["VIX_SMA_LONG"]  = talib.MA(df["^VIX"], timeperiod=SMA_LONG2) 
    
    df["VIX_SMA_SHORT3"] = talib.MA(df["^VIX"], timeperiod=SMA_SHORT3) 
    df["VIX_SMA_LONG3"]  = talib.MA(df["^VIX"], timeperiod=SMA_LONG3)
    
    df["VIX_C"]   = df['VIX_OPEN'].pct_change().fillna(0)
    df["VIX_OPEN_CLOSE"]= ( (df['VIX_OPEN']/df["^VIX"].shift(1)) -1)
    
    qqq_open_close_b = (df['QQQ_OPEN'].shift(-1)> df['QQQ']*1.005) 
    qqq_open_close_l = (df['QQQ_OPEN'].shift(-1)< df['QQQ']*0.96) 
    
    vix_up_much   =  (df["VIX_C"].shift(-1)>0.2 )     | (df["VIX_OPEN_CLOSE"].shift(-1) >0.2)
    vix_sell = (df["VIX_SMA_SHORT"] > 1.2*df["VIX_SMA_LONG"]) |(df["VIX_OPEN"].shift(-1) > 1.2*df["VIX_SMA_LONG"])
    
    vix_down_today = (df["VIX_OPEN_CLOSE"].shift(-1) <-0.03)
    vix_down_today2 = (df["VIX_OPEN_CLOSE"].shift(-1) < 0.05)
    vix_down_smooth =  (df["VIX_SMA_SHORT3"] < 0.97*df["VIX_SMA_LONG3"])
    vix_down_smooth2 =  (df["VIX_SMA_SHORT3"] < 0.95*df["VIX_SMA_LONG3"]) & ( (df["^VIX"]>50)| (df['VIX_OPEN'].shift(-1)>50))
    
    vix_no_need_safe  = vix_down_today2 & vix_down_smooth2
    qqq_up_trend   = (df["QQQ_SMA_SHORT"]>0.99*df["QQQ_SMA_LONG"])
    qqq_down_trend = (df["QQQ_SMA_SHORT"]<0.95*df["QQQ_SMA_LONG"])
    
    qqq_year_up   = (df["QQQ"] >  (1.03 * df["QQQ_SMA_YEAR"])) & (df['QQQ_OPEN'].shift(-1) > (1.03 * df["QQQ_SMA_YEAR"])) 
    qqq_year_down = (df["QQQ"] <  (0.99* df["QQQ_SMA_YEAR"]) ) | (df['QQQ_OPEN'].shift(-1) < (0.99 * df["QQQ_SMA_YEAR"]) )
    
    
    VIX_b_66 = (df["^VIX"]>66) & (df["VIX_OPEN"].shift(-1)>66)
    VIX_l_60 = (df["^VIX"]<60) & (df["VIX_OPEN"].shift(-1)<60)
    
    
    VIX_l_21 = (df['VIX_OPEN'].shift(-1)<21)  & qqq_open_close_b
    VIX_b_23 = (df['VIX_OPEN'].shift(-1)>23)  | qqq_open_close_l
    VIX_b_32 = (df['VIX_OPEN'].shift(-1)>32) | (df["^VIX"]>32)
    
    vix_mid_range      =  (df['VIX_OPEN'].shift(-1)<33) & (df['VIX_OPEN'].shift(-1)>26)
    qqq_up_trend_mid  =  (df["QQQ"]>1.02*df["QQQ_SMA_LONG"])
    qqq_down_trend_mid  =  (df["QQQ_OPEN"].shift(-1)<0.95*df["QQQ_SMA_LONG"])
    cond_up    =   (VIX_b_32 & (vix_down_today&vix_down_smooth)) #| (vix_mid_range&(vix_down_today&qqq_up_trend ))
    
    cond_down  =   ((VIX_b_32)&(vix_up_much|vix_sell)) #|(vix_mid_range&(qqq_down_trend_mid))
    
    lev_cond =    qqq_year_up   | (qqq_up_trend & VIX_l_21)  |   (VIX_b_66)
    safe_cond =   qqq_year_down & (qqq_down_trend|VIX_b_23)  & ((VIX_l_60))# 条件2：低于1%阈值 
    
    df["Raw_Signal"] = np.select(
        condlist=[
            (safe_cond & ~cond_up & ~vix_no_need_safe )    | cond_down, # & ~cond_up)   | cond_down, # 条件2：低于1%阈值 
            (lev_cond  & ~cond_down)  | cond_up  #&  ~cond_down)  | cond_up # 条件1：高于1%阈值  
    
                #safe_cond| cond_down,
                #lev_cond
            
        ],
        choicelist=[
            -1,   # 满足条件1时信号为-1（safe）
            1   # 满足条件2时信号为-1（leverage）
        ],
        default=np.nan  # 中间区域不改变信号
    )
    df["Signal"] = df["Raw_Signal"].ffill().fillna(-1)  # 前向填充中间区域

    # 計算各資產收益率
    df["GLD_Return"] =  df["GLD"].pct_change().fillna(0)  # 避險資產
    df["SHY_Return"] =  df["SHY"].pct_change().fillna(0)
    df["QQQ_Return"] =  df["QQQ"].pct_change().fillna(0)
    
    # 動態組合收益率（加權槓桿或避險）
    df["Leveraged_Return"] = TARGET_LEVERAGE * df["QQQ"].pct_change() - daily_fee
    
    df["Open_Return"] =  (df["QQQ"] / df["QQQ_OPEN"] - 1) * TARGET_LEVERAGE - daily_fee
    df["Open_Close_R"] = ( (df["QQQ_OPEN"] / (df["QQQ"].shift(1))) - 1) * TARGET_LEVERAGE - daily_fee
    df["Signal_Change"] = df["Signal"].diff().abs()
    df["Trade_Day"] = df["Signal_Change"].shift(1).eq(2).astype(int)
    
    
    df["SAFE_ASSET_Return"] = np.select(
        condlist=[
          df["GLD"]> df["GLD_SMA"] ,  # 条件1：高于1%阈值
          df["VIX_OPEN"]>30
        ],
        choicelist=[
            df["GLD_Return"] ,   # gld is better
            df["SHY_Return"]   # shy i2 better
        ],
        default= df["SHY_Return"]  # 中间区域不改变信号
    )
    
    PORT= (1-SAFE_RATIO)
    df["Strategy_Return"] = np.where(
        df["Signal"].shift(1) == 1,  # If we were in QQQ yesterday
        np.where(
            df["Trade_Day"] == 1,    # If trading today  
            df["Open_Return"]*(PORT),       # Use open-to-close return
            df["Leveraged_Return"]*PORT   # Else use normal close-to-close
        ),
        
        np.where(
            df["Trade_Day"] == 1,    # If trading today  
            (df["Open_Close_R"]+0.0001)*(PORT),       # Use open-to-close return
           (df["SAFE_ASSET_Return"])*PORT   # Else use normal close-to-close
        )
    )
    df["Strategy_Return"] -= df["Trade_Day"] * COMMISSION *2* (1-SAFE_RATIO) 
    df["Strategy_Return"] +=  df["SAFE_ASSET_Return"]*SAFE_RATIO
    
    # 計算淨值曲線
    SMA_half = int(SMA_YEAR/2)
    df["Strategy_NAV"] = (1 + df["Strategy_Return"]).cumprod() * INITIAL_CAPITAL
    df = df.iloc[SMA_YEAR:]
    
    
    
    return df

def strategy_today( vix_open_today, qqq_open_today):
    
    today_str = datetime.now().strftime("%Y-%m-%d")
    today = datetime.today()

    # Calculate date 3 years ago
    three_years_ago = today - relativedelta(years=2)
    three_years_ago_str = three_years_ago.strftime("%Y-%m-%d")
    
    df = download_data(three_years_ago_str, today_str)
    
    df["QQQ_SMA_YEAR"] = talib.MA(df["QQQ"], timeperiod=SMA_YEAR)  # matype=1 表示SMMA
    df["QQQ_SMA_LONG"] = talib.MA(df["QQQ"], timeperiod=SMA_LONG)  # matype=1 表示SMMA
    df["QQQ_SMA_SHORT"] =talib.MA(df["QQQ"], timeperiod=SMA_SHORT)  # matype=1 表示SMMA
    df["GLD_SMA"] = df["GLD"].rolling(window=100, min_periods=1).mean()
    
    df["VIX_SMA_SHORT"] = talib.MA(df["^VIX"], timeperiod=SMA_SHORT2) 
    df["VIX_SMA_LONG"]  = talib.MA(df["^VIX"], timeperiod=SMA_LONG2) 
    
    df["VIX_SMA_SHORT3"] = talib.MA(df["^VIX"], timeperiod=SMA_SHORT3) 
    df["VIX_SMA_LONG3"]  = talib.MA(df["^VIX"], timeperiod=SMA_LONG3)
    
    df=add_today_row(df)
    df["VIX_OPEN"].iloc[-1]= vix_open_today
    df['QQQ_OPEN'].iloc[-1]= qqq_open_today
    
    df["VIX_C"]   = df['VIX_OPEN'].pct_change().fillna(0)
    df["VIX_OPEN_CLOSE"]= ( (df['VIX_OPEN']/df["^VIX"].shift(1)) -1)
    
    

    qqq_open_close_b = (df['QQQ_OPEN'].shift(-1)> df['QQQ']*1.005) 
    qqq_open_close_l = (df['QQQ_OPEN'].shift(-1)< df['QQQ']*0.96) 
    
    vix_up_much   =  (df["VIX_C"].shift(-1)>0.2 )     | (df["VIX_OPEN_CLOSE"].shift(-1) >0.2)
    vix_sell = (df["VIX_SMA_SHORT"] > 1.2*df["VIX_SMA_LONG"]) |(df["VIX_OPEN"].shift(-1) > 1.2*df["VIX_SMA_LONG"])
    
    vix_down_today = (df["VIX_OPEN_CLOSE"].shift(-1) <-0.03)
    vix_down_today2 = (df["VIX_OPEN_CLOSE"].shift(-1) < 0.05)
    vix_down_smooth =  (df["VIX_SMA_SHORT3"] < 0.97*df["VIX_SMA_LONG3"])
    vix_down_smooth2 =  (df["VIX_SMA_SHORT3"] < 0.95*df["VIX_SMA_LONG3"]) & ( (df["^VIX"]>50)| (df['VIX_OPEN'].shift(-1)>50))
    
    vix_no_need_safe  = vix_down_today2 & vix_down_smooth2
    qqq_up_trend   = (df["QQQ_SMA_SHORT"]>0.99*df["QQQ_SMA_LONG"])
    qqq_down_trend = (df["QQQ_SMA_SHORT"]<0.95*df["QQQ_SMA_LONG"])
    
    qqq_year_up   = (df["QQQ"] >  (1.03 * df["QQQ_SMA_YEAR"])) & (df['QQQ_OPEN'].shift(-1) > (1.03 * df["QQQ_SMA_YEAR"])) 
    qqq_year_down = (df["QQQ"] <  (0.99* df["QQQ_SMA_YEAR"]) ) | (df['QQQ_OPEN'].shift(-1) < (0.99 * df["QQQ_SMA_YEAR"]) )
    
    
    VIX_b_66 = (df["^VIX"]>66) & (df["VIX_OPEN"].shift(-1)>66)
    VIX_l_60 = (df["^VIX"]<60) & (df["VIX_OPEN"].shift(-1)<60)
    
    
    VIX_l_21 = (df['VIX_OPEN'].shift(-1)<21)  & qqq_open_close_b
    VIX_b_23 = (df['VIX_OPEN'].shift(-1)>23)  | qqq_open_close_l
    VIX_b_32 = (df['VIX_OPEN'].shift(-1)>32) | (df["^VIX"]>32)
    
    vix_mid_range      =  (df['VIX_OPEN'].shift(-1)<33) & (df['VIX_OPEN'].shift(-1)>26)
    qqq_up_trend_mid  =  (df["QQQ"]>1.02*df["QQQ_SMA_LONG"])
    qqq_down_trend_mid  =  (df["QQQ_OPEN"].shift(-1)<0.95*df["QQQ_SMA_LONG"])
    cond_up    =   (VIX_b_32 & (vix_down_today&vix_down_smooth)) #| (vix_mid_range&(vix_down_today&qqq_up_trend ))
    
    cond_down  =   ((VIX_b_32)&(vix_up_much|vix_sell)) #|(vix_mid_range&(qqq_down_trend_mid))
    
    lev_cond =    qqq_year_up   | (qqq_up_trend & VIX_l_21)  |   (VIX_b_66)
    safe_cond =   qqq_year_down & (qqq_down_trend|VIX_b_23)  & ((VIX_l_60))# 条件2：低于1%阈值 
    
    df["Raw_Signal"] = np.select(
        condlist=[
            #(safe_cond & ~cond_up & ~vix_no_need_safe )    | cond_down, # & ~cond_up)   | cond_down, # 条件2：低于1%阈值 
            #(lev_cond  & ~cond_down)  | cond_up  #&  ~cond_down)  | cond_up # 条件1：高于1%阈值  
    
                safe_cond| cond_down,
                lev_cond
            
        ],
        choicelist=[
            -1,   # 满足条件1时信号为-1（safe）
            1   # 满足条件2时信号为-1（leverage）
        ],
        default=np.nan  # 中间区域不改变信号
    )
    df["Signal"] = df["Raw_Signal"].ffill().fillna(-1)  # 前向填充中间区域

    
    
    print(f"vix_close:{df['^VIX'].iloc[-2]} \n, year_sma:{df['QQQ_SMA_YEAR'].iloc[-2]} \n, sma_short:{df['QQQ_SMA_SHORT'].iloc[-2]} \n, sma_long:{df['QQQ_SMA_LONG'].iloc[-2]} \n, last qqq close:{df['QQQ'].iloc[-2]} ")
    print(f"GLD:{df['GLD'].iloc[-2]}, GLD_SMA:{df['GLD_SMA'].iloc[-2]}")
    print(f"for sell signal: vix_sma_short_1:{df['VIX_SMA_SHORT'].iloc[-2]}, vix_sma_long_3:{df['VIX_SMA_LONG'].iloc[-2]}")
    print(f"for buy signal: vix_sma_short_3:{df['VIX_SMA_SHORT3'].iloc[-2]}, vix_sma_long_10:{df['VIX_SMA_LONG3'].iloc[-2]}")
    
    print(f"vix_yesterday_close or vix_open>32 :{VIX_b_32[-2]},vix_down_smooth:{vix_down_smooth[-2]},vix_down_today:{vix_down_today[-2]}")
    print(f"vix_up_much(vix open/open or open/close>0.2):{vix_up_much[-2]},vix_sell (vix_sma_short_1>1.2*vix_sma_long_3):{vix_sell[-2]}")
    print(f"vix_no_need_safe(vix>50, vix up less than 5%, no need to switch to safe asset):{vix_no_need_safe[-2]}")
    print(f"save_cond:{safe_cond[-2]}, cond_down:{cond_down[-2]}, qqq_year_down:{qqq_year_down[-2]}, qqq_down_trend:{qqq_down_trend[-2]}, VIX>23:{VIX_b_23[-2]}, VIX<60:{VIX_l_60[-2]}")
    print(f"lev_cond:{lev_cond[-2]},  cond_up:{cond_up[-2]},   qqq_year_up:{qqq_year_up[-2]}, qqq_up_trend:{qqq_up_trend[-2]}, VIX<20:{VIX_l_21[-2]}, VIX>66:{VIX_b_66[-2]}")
    print("check qqq/nasdaq100 forward pe <35 before buying leverage")
    
    print(f"\n\nlast day signal:{df['Signal'].iloc[-2]}, need trade today:{df['Signal'].iloc[-2] != df['Signal'].iloc[-3]}")
    print(f"last day raw signal:{df['Raw_Signal'].iloc[-2]}")
    if(df['Signal'].iloc[-2]==1 and df['Signal'].iloc[-2] != df['Signal'].iloc[-3]):
        print("!!!Need to trade today, Hold 0.8aum of 3x qqq")
    
    else:
        if(df['Signal'].iloc[-2]==-1 and df['Signal'].iloc[-2] != df['Signal'].iloc[-3]):
            if df['GLD'].iloc[-2]>df['GLD_SMA'].iloc[-2] :
                print("!!!!Need to trade today, Hold all aum of GLD")
            else:
                print("!!!!Need to trade today, Hold all aum of SHY")
        elif (df['Signal'].iloc[-2] == df['Signal'].iloc[-3]):
            if df['Signal'].iloc[-2]==1:
                print ("!!!!!Keep, 80% aum 3x qqq")
            if df['Signal'].iloc[-2] == -1:
                print("!!!!!Keep safe asset")
    
    return df