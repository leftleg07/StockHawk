package com.sam_chordas.android.stockhawk.network;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.sam_chordas.android.stockhawk.MockApplication;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.TestUtil;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for database
 */
@RunWith(AndroidJUnit4.class)
public class TestApiService {

    private Context mContext;
    private ContentResolver mContentResolver;

    @Inject
    YahooApiService mApiService;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getTargetContext();
        mContentResolver = mContext.getContentResolver();
        MockApplication application = (MockApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        application.getMockComponent().inject(this);
        TestUtil.deleteAllRecord(mContext);

    }

    @Test
    public void testQuotes() throws Exception {

        String query = "select * from yahoo.finance.quotes where symbol in (\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")";

        String jsonStockStr = mApiService.getYQL(query).toBlocking().single();
        ArrayList batch = Utils.quoteJsonToContentVals(jsonStockStr, mContentResolver);
        if(batch != null && batch.size() > 0) {
            mContentResolver.applyBatch(QuoteProvider.AUTHORITY, batch);
        }

        Cursor cursor = mContentResolver.query(QuoteProvider.Quotes.CONTENT_URI, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(3);
    }

    @Test
    public void testQuote() throws Exception {

        final String SYMBOL = "YHOO";

        String query = "select * from yahoo.finance.quotes where symbol in (\"" + SYMBOL + "\")";

        String jsonStr = mApiService.getYQL(query).toBlocking().single();
        ArrayList batch = Utils.quoteJsonToContentVals(jsonStr, mContentResolver);
        if (batch != null && batch.size() > 0) {
            mContentResolver.applyBatch(QuoteProvider.AUTHORITY, batch);
        }

        Cursor cursor = mContentResolver.query(QuoteProvider.Quotes.CONTENT_URI, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);
    }

    @Test
    public void testUnKownQuote() throws Exception {

        final String SYMBOL = "XXXX";

        String query = "select * from yahoo.finance.quotes where symbol in (\"" + SYMBOL + "\")";

        String jsonStr = mApiService.getYQL(query).toBlocking().single();
        ArrayList batch = Utils.quoteJsonToContentVals(jsonStr, mContentResolver);
        if (batch != null && batch.size() > 0) {
            mContentResolver.applyBatch(QuoteProvider.AUTHORITY, batch);
        }

        Cursor cursor = mContentResolver.query(QuoteProvider.Quotes.CONTENT_URI, null, null, null, null);
        assertThat(cursor.getCount()).isEqualTo(0);
    }

    @Test
    public void testQuoteHistoricalData() throws Exception {
        final String SYMBOL = "YHOO";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date currentDate = new Date();

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(currentDate);
        calEnd.add(Calendar.DATE, 0);

        Calendar calStart = Calendar.getInstance();
        calStart.setTime(currentDate);
        calStart.add(Calendar.MONTH, -1);

        String startDate = dateFormat.format(calStart.getTime());
        String endDate = dateFormat.format(calEnd.getTime());



        String query = "select * from yahoo.finance.historicaldata where symbol = \"" + SYMBOL + "\" and startDate = \"" + startDate + "\" and endDate = \"" + endDate + "\"";

        String jsonStr = mApiService.getYQL(query).toBlocking().single();

        mContentResolver.applyBatch(QuoteProvider.AUTHORITY,
                Utils.historicalQuoteJsonToContentVals(jsonStr, mContentResolver));

        Uri uri = QuoteProvider.HistoricalQuoteData.withSymbol(SYMBOL);
        Cursor cursor = mContentResolver.query(uri, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);
    }
}
