package com.sam_chordas.android.stockhawk.data;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for provider
 */
@RunWith(AndroidJUnit4.class)
public class TestProvider {
    private ContentResolver mContentResolver;
    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getTargetContext();
        mContentResolver = mContext.getContentResolver();
        TestUtil.deleteAllRecord(mContext);
    }

    @Test
    public void testProviderRegistry() throws Exception {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // MovieProvider class.
        String pkg = mContext.getPackageName();
        String cls = com.sam_chordas.android.stockhawk.data.generated.QuoteProvider.class.getName();
        ComponentName componentName = new ComponentName(pkg, cls);
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertWithMessage("Error: QuoteProvider registered with authority: %s instead of authority: %s", providerInfo.authority, QuoteProvider.AUTHORITY).that(providerInfo.authority).isEqualTo(QuoteProvider.AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.

            assertWithMessage("Error: QuoteProvider not registered at " + mContext.getPackageName()).that(false).isTrue();
        }

    }

    @Test
    public void testQuotesTable() throws Exception {
        // insert

        ContentValues testValues = TestUtil.createQuotesEntryValues();
        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(QuoteColumns.ISUP, 4);


        Uri uri = mContentResolver.insert(QuoteProvider.Quotes.CONTENT_URI, testValues);
        long quoteId = ContentUris.parseId(uri);

        assertWithMessage("Error: Quotes Query Validation Failed").that(testValues.getAsLong(QuoteColumns._ID)).isEqualTo(quoteId);

        // update
        uri = QuoteProvider.Quotes.withSymbol(updateValues.getAsString(QuoteColumns.SYMBOL));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isEqualTo(1);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from Quotes query").that(cursor.moveToFirst()).isTrue();
        TestUtil.validateCurrentRecord("Error: Quotes Query Validation Failed", cursor, updateValues);
        cursor.close();

    }

    @Test
    public void testHistoricalQuoteTable() throws Exception {
        // insert

        ContentValues testValues = TestUtil.createHistoricalQuoteEntryValues();
        ContentValues updateValues = new ContentValues(testValues);
        updateValues.put(HistoricalQuoteColumns.DATE, "2222-22-22");


        Uri uri = mContentResolver.insert(QuoteProvider.HistoricalQuoteData.CONTENT_URI, testValues);
        long quoteId = ContentUris.parseId(uri);

        assertWithMessage("Error: Historical quotes Query Validation Failed").that(testValues.getAsLong(HistoricalQuoteColumns._ID)).isEqualTo(quoteId);

        // update
        uri = QuoteProvider.HistoricalQuoteData.withSymbol(updateValues.getAsString(HistoricalQuoteColumns.SYMBOL));
        int count = mContentResolver.update(uri, updateValues, null, null);
        assertThat(count).isEqualTo(1);

        Cursor cursor = mContentResolver.query(uri, null, null, null, null, null);

        assertWithMessage("Error: No Records returned from Historical quote query").that(cursor.moveToFirst()).isTrue();
        TestUtil.validateCurrentRecord("Error: Historical quote Query Validation Failed", cursor, updateValues);
        cursor.close();

        uri = QuoteProvider.HistoricalQuoteData.withSymbolAndDate(updateValues.getAsString(HistoricalQuoteColumns.SYMBOL), updateValues.getAsString(HistoricalQuoteColumns.DATE));
        cursor = mContentResolver.query(uri, null, null, null, null, null);
        assertWithMessage("Error: No Records returned from Historical quote query").that(cursor.moveToFirst()).isTrue();
    }
}
