package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sam_chordas.android.stockhawk.data.HistoricalQuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.network.HistoricalQuote;
import com.sam_chordas.android.stockhawk.network.Quote;
import com.sam_chordas.android.stockhawk.network.QuoteDeserializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import dagger.internal.Preconditions;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON, ContentResolver contentResolver, boolean isUpdate) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Quote.class, new QuoteDeserializer());
        Gson gson = gsonBuilder.create();
        try {

            JSONObject jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                Quote[] quotes = null;
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    Quote quote = gson.fromJson(jsonObject.toString(), Quote.class);
                    if (quote != null) {
                        quotes = new Quote[]{quote};
                    }
                } else {
                    JSONArray resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");
                    quotes = gson.fromJson(resultsArray.toString(), Quote[].class);
                }

                if (quotes != null && quotes.length > 0) {
                    return buildQuoteBatchOperation(quotes, contentResolver, isUpdate);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return null;
    }


    public static ArrayList buildQuoteBatchOperation(Quote[] quotes, ContentResolver contentResolver, boolean isUpdate) {

        final ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();

        // Build hash table of incoming entries
        final HashMap<String, Quote> entryMap = new HashMap<>();
        for (Quote e : quotes) {
            entryMap.put(e.mSymbol, e);
        }

        Cursor cursor = contentResolver.query(QuoteProvider.Quotes.CONTENT_URI, null, null, null, null);
        Preconditions.checkNotNull(cursor);

        Log.i(LOG_TAG, "Found " + cursor.getCount() + " local entries. Computing merge solution...");


        while (cursor.moveToNext()) {
            String symbol = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
            Quote match = entryMap.get(symbol);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                entryMap.remove(symbol);
                // Check to see if the entry needs to be updated
                String name = cursor.getString(cursor.getColumnIndex(QuoteColumns.NAME));
                String percent = cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
                String change = cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE));
                String price = cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE));
                int up = cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP));
                Uri existingUri = QuoteProvider.Quotes.withSymbol(symbol);
                if (!match.mName.equals(name) ||
                        !match.mPercentChange.equals(percent) ||
                        !match.mChange.equals(change) ||
                        !match.mBidPrice.equals(price) ||
                        match.mIsUp != up) {
                    // Update existing record
                    Log.i(LOG_TAG, "Scheduling update: " + existingUri);
                    batchOperations.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(QuoteColumns.NAME, match.mName)
                            .withValue(QuoteColumns.PERCENT_CHANGE, match.mPercentChange)
                            .withValue(QuoteColumns.CHANGE, match.mChange)
                            .withValue(QuoteColumns.BIDPRICE, match.mBidPrice)
                            .withValue(QuoteColumns.ISUP, match.mIsUp)
                            .build());
                } else {
                    Log.i(LOG_TAG, "No action: " + existingUri);
                }

            } else if(isUpdate){
                // Entry doesn't exist. Remove it from the database.
                Uri deleteUri = QuoteProvider.Quotes.withSymbol(symbol);
                Log.i(LOG_TAG, "Scheduling delete: " + deleteUri);
                batchOperations.add(ContentProviderOperation.newDelete(deleteUri).build());
            }
        }
        cursor.close();

        for (Quote entry : entryMap.values()) {
            Log.i(LOG_TAG, "Scheduling insert: entry_id=" + entry.mSymbol);
            batchOperations.add(ContentProviderOperation.newInsert(QuoteProvider.Quotes.CONTENT_URI)
                    .withValue(QuoteColumns.SYMBOL, entry.mSymbol)
                    .withValue(QuoteColumns.NAME, entry.mName)
                    .withValue(QuoteColumns.PERCENT_CHANGE, entry.mPercentChange)
                    .withValue(QuoteColumns.CHANGE, entry.mChange)
                    .withValue(QuoteColumns.BIDPRICE, entry.mBidPrice)
                    .withValue(QuoteColumns.ISUP, entry.mIsUp)
                    .withValue(QuoteColumns.ISCURRENT, entry.mIsCurrent)
                    .build());
        }

        Log.i(LOG_TAG, "Merge solution ready. Applying batch update");

        return batchOperations;

    }

    public static ArrayList historicalQuoteJsonToContentVals(String JSON, ContentResolver contentResolver) {
        Gson gson = new Gson();
        try {

            JSONObject jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                HistoricalQuote[] quotes = null;
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    HistoricalQuote quote = gson.fromJson(jsonObject.toString(), HistoricalQuote.class);
                    if (quote != null) {
                        quotes = new HistoricalQuote[]{quote};
                    }
                } else {
                    JSONArray resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");
                    quotes = gson.fromJson(resultsArray.toString(), HistoricalQuote[].class);
                }

                if (quotes != null && quotes.length > 0) {
                    return buildHistoricalQuoteBatchOperation(quotes, contentResolver);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return null;
    }


    public static ArrayList buildHistoricalQuoteBatchOperation(HistoricalQuote[] data, ContentResolver contentResolver) {

        final ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        final String symbol = data[0].mSymbol;

        // Build hash table of incoming entries
        final HashMap<String, HistoricalQuote> entryMap = new HashMap<>();
        for (HistoricalQuote e : data) {
            entryMap.put(e.mDate, e);
        }

        Uri contentUri = QuoteProvider.HistoricalQuoteData.withSymbol(symbol);
        Cursor cursor = contentResolver.query(contentUri, null, null, null, null);
        Preconditions.checkNotNull(cursor);

        Log.i(LOG_TAG, "Found " + cursor.getCount() + " local entries. Computing merge solution...");

        while (cursor.moveToNext()) {
            String date = cursor.getString(cursor.getColumnIndex(HistoricalQuoteColumns.DATE));
            HistoricalQuote match = entryMap.get(date);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                entryMap.remove(date);
                // Check to see if the entry needs to be updated
                String low = cursor.getString(cursor.getColumnIndex(HistoricalQuoteColumns.LOW));
                String high = cursor.getString(cursor.getColumnIndex(HistoricalQuoteColumns.HIGH));
                String open = cursor.getString(cursor.getColumnIndex(HistoricalQuoteColumns.OPEN));
                String close = cursor.getString(cursor.getColumnIndex(HistoricalQuoteColumns.CLOSE));
                Uri existingUri = QuoteProvider.HistoricalQuoteData.withSymbolAndDate(symbol, date);
                if (!match.mLow.equals(low) || !match.mHigh.equals(high) || !match.mOpen.equals(open) || !match.mClose.equals(close)) {
                    // Update existing record
                    Log.i(LOG_TAG, "Scheduling update: " + existingUri);
                    batchOperations.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(HistoricalQuoteColumns.LOW, match.mLow)
                            .withValue(HistoricalQuoteColumns.HIGH, match.mHigh)
                            .withValue(HistoricalQuoteColumns.OPEN, match.mOpen)
                            .withValue(HistoricalQuoteColumns.CLOSE, match.mClose)
                            .build());
                } else {
                    Log.i(LOG_TAG, "No action: " + existingUri);
                }
            }
        }
        cursor.close();

        for (HistoricalQuote entry : entryMap.values()) {
            Log.i(LOG_TAG, "Scheduling insert: entry_id=" + entry.mSymbol + ", " + entry.mDate);
            batchOperations.add(ContentProviderOperation.newInsert(QuoteProvider.HistoricalQuoteData.CONTENT_URI)
                    .withValue(HistoricalQuoteColumns.SYMBOL, entry.mSymbol)
                    .withValue(HistoricalQuoteColumns.DATE, entry.mDate)
                    .withValue(HistoricalQuoteColumns.LOW, entry.mLow)
                    .withValue(HistoricalQuoteColumns.HIGH, entry.mHigh)
                    .withValue(HistoricalQuoteColumns.OPEN, entry.mOpen)
                    .withValue(HistoricalQuoteColumns.CLOSE, entry.mClose)
                    .build());
        }

        Log.i(LOG_TAG, "Merge solution ready. Applying batch update");

        return batchOperations;
    }


}
