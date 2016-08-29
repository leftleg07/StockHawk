package com.sam_chordas.android.stockhawk.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sam_chordas.android.stockhawk.data.generated.QuoteDatabase;

import java.util.Map;
import java.util.Set;

import static com.google.common.truth.Truth.assertWithMessage;

/**
 * db test util class
 */
public abstract class TestUtil {
    public static void deleteDatabase(Context context) {
        context.deleteDatabase(QuoteContract.DATABASE_NAME);
    }

    public static void deleteAllRecord(Context context) {

        context.getContentResolver().delete(
                QuoteProvider.Quotes.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = context.getContentResolver().query(
                QuoteProvider.Quotes.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertWithMessage("Error: Records not deleted from Quotes table during delete").that(cursor.getCount()).isEqualTo(0);
        cursor.close();

        context.getContentResolver().delete(
                QuoteProvider.HistoricalQuoteData.CONTENT_URI,
                null,
                null
        );

        cursor = context.getContentResolver().query(
                QuoteProvider.HistoricalQuoteData.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertWithMessage("Error: Records not deleted from History quote table during delete").that(cursor.getCount()).isEqualTo(0);
        cursor.close();
    }

    static ContentValues createQuotesEntryValues() {
        ContentValues entryValues = new ContentValues();
        entryValues.put(QuoteColumns._ID, 1);
        entryValues.put(QuoteColumns.SYMBOL, "YHOO");
        entryValues.put(QuoteColumns.PERCENT_CHANGE, "+0.49%");
        entryValues.put(QuoteColumns.CHANGE, "+0.21");
        entryValues.put(QuoteColumns.BIDPRICE, "41.87");
        entryValues.put(QuoteColumns.ISUP, 1);
        entryValues.put(QuoteColumns.ISCURRENT, 0);
        return entryValues;
    }

    static long insertQuotesEntryValues(Context context) {
        SQLiteDatabase db = QuoteDatabase.getInstance(context).getWritableDatabase();
        ContentValues testValues = TestUtil.createQuotesEntryValues();

        long entryRowId = db.insert(QuoteContract.TABLE_NAME_QUOTES, null, testValues);

        // Verify we got a row back.
        assertWithMessage("Error: Failure to insert Quotes Entry Values").that(entryRowId).isNotEqualTo(-1);

        db.close();
        return entryRowId;
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);

            assertWithMessage("Column '%s' not found. %s", columnName, error).that(idx).isNotEqualTo(-1);
            String expectedValue = entry.getValue().toString();
            String currentValue = valueCursor.getString(idx);
            assertWithMessage("Value '%s' did not match the expected value '%s'. %s", currentValue, expectedValue, error).that(currentValue).isEqualTo(expectedValue);
        }
    }

    public static ContentValues createHistoricalQuoteEntryValues() {
        ContentValues entryValues = new ContentValues();
        entryValues.put(HistoricalQuoteColumns._ID, 1);
        entryValues.put(HistoricalQuoteColumns.SYMBOL, "YHOO");
        entryValues.put(HistoricalQuoteColumns.DATE, "2016-08-19");
        entryValues.put(HistoricalQuoteColumns.OPEN, "42.799999");
        entryValues.put(HistoricalQuoteColumns.HIGH, "43.119999");
        entryValues.put(HistoricalQuoteColumns.LOW, "42.650002");
        entryValues.put(HistoricalQuoteColumns.CLOSE, "43.02");
        return entryValues;
    }

    public static long insertHistoricalQuoteEntryValues(Context context) {
        SQLiteDatabase db = QuoteDatabase.getInstance(context).getWritableDatabase();
        ContentValues testValues = TestUtil.createHistoricalQuoteEntryValues();

        long entryRowId = db.insert(QuoteContract.TABLE_NAME_HISTORICAL_QUOTE, null, testValues);

        // Verify we got a row back.
        assertWithMessage("Error: Failure to insert Historical quotes Entry Values").that(entryRowId).isNotEqualTo(-1);

        db.close();
        return entryRowId;

    }
}
