package com.sam_chordas.android.stockhawk.service;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.TestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by heim on 8/21/16.
 */
@RunWith(AndroidJUnit4.class)
public class TestStockIntentService {
    private Context mContext;
    private CountDownLatch signal;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();
    private ContentResolver mContentResolver;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getTargetContext();
        mContentResolver = mContext.getContentResolver();
        TestUtil.deleteAllRecord(mContext);
        signal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        signal.countDown();
    }

    @Test
    public void testInitStartedService() throws Exception {
        final ContentObserver observer = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                signal.countDown();
            }
        };
        mContentResolver.registerContentObserver(QuoteProvider.Quotes.CONTENT_URI, false, observer);
        Intent serviceIntent = new Intent(mContext, StockIntentService.class);
        // Run the initialize task service so that some stocks appear upon an empty database
        serviceIntent.putExtra("tag", "init");

        mServiceRule.startService(serviceIntent);
        signal.await();

        Cursor cursor = mContentResolver.query(QuoteProvider.Quotes.CONTENT_URI, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(3);

        mContentResolver.unregisterContentObserver(observer);

    }

    @Test
    public void testAddStartedService() throws Exception {
        final String SYMBOL = "FB";
        Uri uri = QuoteProvider.Quotes.withSymbol(SYMBOL);
        final ContentObserver observer = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange);
                signal.countDown();
            }
        };
        mContentResolver.registerContentObserver(uri, false, observer);
        Intent serviceIntent = new Intent(mContext, StockIntentService.class);
        // Run the initialize task service so that some stocks appear upon an empty database
        serviceIntent.putExtra("tag", "add");
        serviceIntent.putExtra("symbol", SYMBOL);

        mServiceRule.startService(serviceIntent);
        signal.await();

        Cursor cursor = mContentResolver.query(uri, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);

        mContentResolver.unregisterContentObserver(observer);

    }

    @Test
    public void testHistoricalStartedService() throws Exception {
        final String SYMBOL = "FB";
        final Uri HISTORICAL_URI = QuoteProvider.HistoricalQuoteData.withSymbol(SYMBOL);
        final ContentObserver observer = new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange);
                signal.countDown();
            }
        };
        mContentResolver.registerContentObserver(HISTORICAL_URI, false, observer);

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

        Intent serviceIntent = new Intent(mContext, StockIntentService.class);
        // Run the initialize task service so that some stocks appear upon an empty database
        serviceIntent.putExtra("tag", "hist");
        serviceIntent.putExtra("symbol", SYMBOL);
        serviceIntent.putExtra("start", startDate);
        serviceIntent.putExtra("end", endDate);

        mServiceRule.startService(serviceIntent);
        signal.await();

        Cursor cursor = mContentResolver.query(HISTORICAL_URI, null, null, null, null);
        assertThat(cursor.getCount()).isGreaterThan(0);

        mContentResolver.unregisterContentObserver(observer);

    }
}
