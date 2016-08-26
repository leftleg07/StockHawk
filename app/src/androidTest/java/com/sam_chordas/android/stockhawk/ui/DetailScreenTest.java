package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.detail.DetailActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Created by gsshop on 2016. 8. 24..
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DetailScreenTest {

    @Rule
    public IntentsTestRule<DetailActivity> mActivityTestRule =
            new IntentsTestRule<>(DetailActivity.class, false, false);


    @Test
    public void testActivity() throws Exception {

        final String symbol = "MSFT";

        Intent intent = new Intent();
        Uri uri = QuoteProvider.Quotes.withSymbol(symbol);
        intent.setData(uri);

        mActivityTestRule.launchActivity(intent);

        // wait for activity finished
        while (!mActivityTestRule.getActivity().isFinishing()) {
            TimeUnit.SECONDS.sleep(1);
        }


    }
}
