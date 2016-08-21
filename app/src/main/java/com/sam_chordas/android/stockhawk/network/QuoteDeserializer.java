package com.sam_chordas.android.stockhawk.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import static com.sam_chordas.android.stockhawk.rest.Utils.truncateBidPrice;
import static com.sam_chordas.android.stockhawk.rest.Utils.truncateChange;

/**
 * Created by heim on 8/20/16.
 */

public class QuoteDeserializer implements JsonDeserializer<Quote> {
    @Override
    public Quote deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Quote quote = null;
        try {
            JsonObject jsonObject = json.getAsJsonObject();
            String change = jsonObject.get("Change").getAsString();
            quote = new Quote();
            quote.mSymbol = jsonObject.get("symbol").getAsString();
            quote.mBidPrice = truncateBidPrice(jsonObject.get("Bid").getAsString());
            quote.mPercentChange = truncateChange(jsonObject.get("ChangeinPercent").getAsString(), true);
            quote.mChange = truncateChange(change, false);
            quote.mIsCurrent = 1;
            if (change.charAt(0) == '-') {
                quote.mIsUp = 0;
            } else {
                quote.mIsUp = 1;
            }
        } catch (UnsupportedOperationException e) {
        }

        return quote;
    }
}
