package com.sam_chordas.android.stockhawk.network;

import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.sam_chordas.android.stockhawk.MockApplication;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.TestUtil;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for database
 */
@RunWith(AndroidJUnit4.class)
public class TestApiService {
    private static final String LOG_TAG = TestApiService.class.getSimpleName();

    private Context mContext;
    private ContentResolver mContentResolver;

    @Inject
    YahooApiService mApiService;

    CountDownLatch signal = null;

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getTargetContext();
        mContentResolver = mContext.getContentResolver();
        MockApplication application = (MockApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
        application.getMockComponent().inject(this);
        TestUtil.deleteDatabase(mContext);
        signal = new CountDownLatch(1);

    }

    @After
    public void tearDown() throws Exception {
        signal.countDown();
    }

    @Test
    public void testStocks() throws Exception {
        mContentResolver.registerContentObserver(QuoteProvider.Quotes.CONTENT_URI, false, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                Cursor cursor = mContentResolver.query(uri, null, null, null, null);
                assertThat(cursor.getCount()).isGreaterThan(0);
                signal.countDown();
            }
        });

        String query = "select * from yahoo.finance.quotes where symbol in (\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")";
//        query = "select * from yahoo.finance.quotes where symbol in (\"YHOO\")";

        String jsonStockStr = mApiService.getStocks(query).toBlocking().single();
        try {
            // update ISCURRENT to 0 (false) so new data is current
            mContentResolver.applyBatch(QuoteProvider.AUTHORITY,
                    Utils.quoteJsonToContentVals(jsonStockStr));
            signal.await();
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(LOG_TAG, "Error applying batch insert", e);
        }
        int j = 19;
        j = 20;
    }
}
