package com.sam_chordas.android.stockhawk.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.sam_chordas.android.stockhawk.data.generated.QuoteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

/**
 * Tests for database
 */
@RunWith(AndroidJUnit4.class)
public class TestDb {
    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getTargetContext();
        TestUtil.deleteDatabase(mContext);
    }

    @Test
    public void testCreateDb() throws Exception {
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(QuoteContract.TABLE_QUOTES_NAME);

        SQLiteDatabase db = QuoteDatabase.getInstance(mContext).getWritableDatabase();
        assertThat(db.isOpen()).isTrue();

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertWithMessage("Error: This means that the database has not been created correctly").that(c.moveToFirst()).isTrue();

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while (c.moveToNext());

        assertWithMessage("Error: Your database was created without the quotes tables").that(tableNameHashSet.isEmpty()).isTrue();
        c.close();

        /**
         * quotes table
         */

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + QuoteContract.TABLE_QUOTES_NAME + ")",
                null);

        assertWithMessage("Error: This means that we were unable to query the database for table information.").that(c.moveToFirst()).isTrue();


        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> entryColumnHashSet = new HashSet<String>();
        entryColumnHashSet.add(QuoteColumns._ID);
        entryColumnHashSet.add(QuoteColumns.SYMBOL);
        entryColumnHashSet.add(QuoteColumns.PERCENT_CHANGE);
        entryColumnHashSet.add(QuoteColumns.CHANGE);
        entryColumnHashSet.add(QuoteColumns.BIDPRICE);
        entryColumnHashSet.add(QuoteColumns.CREATED);
        entryColumnHashSet.add(QuoteColumns.ISUP);
        entryColumnHashSet.add(QuoteColumns.ISCURRENT);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            entryColumnHashSet.remove(columnName);
        } while (c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertWithMessage("Error: The database doesn't contain all of the required quotes entry columns").that(entryColumnHashSet.isEmpty()).isTrue();

        db.close();
    }

    @Test
    public void testQuotesTable() throws Exception {
        ContentValues testValues = TestUtil.createQuotesEntryValues();

        // insert data
        TestUtil.insertQuotesEntryValues(mContext);

        SQLiteDatabase db = QuoteDatabase.getInstance(mContext).getWritableDatabase();
        assertThat(db.isOpen()).isTrue();

        Cursor cursor = db.query(
                QuoteContract.TABLE_QUOTES_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        assertWithMessage("Error: No Records returned from quotes query").that(cursor.moveToFirst()).isTrue();

        TestUtil.validateCurrentRecord("Error: Quotes Query Validation Failed", cursor, testValues);

        cursor.close();
        db.close();

    }
}
