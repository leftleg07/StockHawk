package com.sam_chordas.android.stockhawk.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by heim on 8/20/16.
 */

public class QuoteDeserializer implements JsonDeserializer<Quote> {
    @Override
    public Quote deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            Quote quote = new Quote();
            JsonObject jsonObject = json.getAsJsonObject();
            String change = jsonObject.get("Change").getAsString();
            quote.mSymbol = jsonObject.get("symbol").getAsString();
            quote.mName = jsonObject.get("Name").getAsString();
            quote.mBidPrice = truncateBidPrice(jsonObject.get("Bid").getAsString());
            quote.mPercentChange = truncateChange(jsonObject.get("ChangeinPercent").getAsString(), true);
            quote.mChange = truncateChange(change, false);
            quote.mIsCurrent = 1;
            if (change.charAt(0) == '-') {
                quote.mIsUp = 0;
            } else {
                quote.mIsUp = 1;
            }

            return quote;
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }

    private String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    private String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

}
