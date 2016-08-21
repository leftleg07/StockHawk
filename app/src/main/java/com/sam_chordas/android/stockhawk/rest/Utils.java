package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.network.Quote;
import com.sam_chordas.android.stockhawk.network.QuoteDeserializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Quote.class, new QuoteDeserializer());
        Gson gson = gsonBuilder.create();
        try {

            JSONObject jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                Quote[] quotes;
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    Quote quote = gson.fromJson(jsonObject.toString(), Quote.class);
                    quotes = new Quote[]{quote};
                } else {
                    JSONArray resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");
                    quotes = gson.fromJson(resultsArray.toString(), Quote[].class);
                }

                for(Quote quote : quotes) {
                    if(quote != null) {
                        batchOperations.add(buildBatchOperation(quote));
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
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


    public static ContentProviderOperation buildBatchOperation(Quote quote) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        builder.withValue(QuoteColumns.SYMBOL, quote.mSymbol);
        builder.withValue(QuoteColumns.BIDPRICE, quote.mBidPrice);
        builder.withValue(QuoteColumns.PERCENT_CHANGE, quote.mPercentChange);
        builder.withValue(QuoteColumns.CHANGE, quote.mChange);
        builder.withValue(QuoteColumns.ISCURRENT, quote.mIsCurrent);
        builder.withValue(QuoteColumns.ISUP, quote.mIsUp);
        return builder.build();
    }
}
