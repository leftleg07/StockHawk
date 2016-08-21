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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by heim on 8/21/16.
 */
@RunWith(AndroidJUnit4.class)
public class TestStockIntentService {
    private Context mContext;
    private CountDownLatch signal;

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule().withTimeout(60L, TimeUnit.SECONDS);
    private ContentResolver mContentResolver;

    @Before
    public void setUp() throws Exception {
        mContext = InstrumentationRegistry.getTargetContext();
        mContentResolver = mContext.getContentResolver();
        signal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        signal.countDown();
    }

    @Test
    public void testInitStartedService() throws Exception {
        mContentResolver.registerContentObserver(QuoteProvider.Quotes.CONTENT_URI, false, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                Cursor cursor = mContentResolver.query(uri, null, null, null, null);
                assertThat(cursor.getCount()).isGreaterThan(0);
                signal.countDown();
            }
        });
        Intent serviceIntent = new Intent(mContext, StockIntentService.class);
        // Run the initialize task service so that some stocks appear upon an empty database
        serviceIntent.putExtra("tag", "init");

        mServiceRule.startService(serviceIntent);
        signal.await();

    }

    @Test
    public void testAddStartedService() throws Exception {
        mContentResolver.registerContentObserver(QuoteProvider.Quotes.CONTENT_URI, false, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                Cursor cursor = mContentResolver.query(uri, null, null, null, null);
                assertThat(cursor.getCount()).isGreaterThan(0);
                signal.countDown();
            }
        });
        Intent serviceIntent = new Intent(mContext, StockIntentService.class);
        // Run the initialize task service so that some stocks appear upon an empty database
        serviceIntent.putExtra("tag", "add");
        serviceIntent.putExtra("symbol", "XXXXX");

        mServiceRule.startService(serviceIntent);
        signal.await();

    }
}
