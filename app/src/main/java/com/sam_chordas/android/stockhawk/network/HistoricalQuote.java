package com.sam_chordas.android.stockhawk.network;

import com.google.gson.annotations.SerializedName;

/**
 * historical data
 */

public class HistoricalQuote {
    @SerializedName("Symbol")
    public String mSymbol;

    @SerializedName("Date")
    public String mDate;

    @SerializedName("Low")
    public String mLow;

    @SerializedName("High")
    public String mHigh;

    @SerializedName("Open")
    public String mOpen;

    @SerializedName("Close")
    public String mClose;
}
