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
 * Created by heim on 8/20/16.
 */
public abstract class TestUtil {
    public static void deleteDatabase(Context context) {
        context.deleteDatabase(QuoteContract.DATABASE_NAME);
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

    static long insertQuotesEntryValues(Context mContext) {
        SQLiteDatabase db = QuoteDatabase.getInstance(mContext).getWritableDatabase();
        ContentValues testValues = TestUtil.createQuotesEntryValues();

        long entryRowId = db.insert(QuoteContract.TABLE_QUOTES_NAME, null, testValues);

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
}
