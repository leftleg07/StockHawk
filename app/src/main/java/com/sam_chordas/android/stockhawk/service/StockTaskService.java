package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.MyStocksApplication;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.network.YahooApiService;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {
    private String LOG_TAG = StockTaskService.class.getSimpleName();

    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();

    @Inject
    YahooApiService mApiService;

    public StockTaskService() {
    }

    public StockTaskService(Context context) {
        mContext = context;
        ((MyStocksApplication) mContext.getApplicationContext()).getComponent().inject(this);
    }

    @Override
    public int onRunTask(TaskParams params) {
        Cursor initQueryCursor;
        if (mContext == null) {
            mContext = this;
        }
        int result = GcmNetworkManager.RESULT_FAILURE;

        StringBuilder queryStringBuilder = new StringBuilder();
        if (params.getTag().equals("hist")) { // historical data
            // Base URL for the Yahoo query
            queryStringBuilder.append("select * from yahoo.finance.historicaldata where symbol = ");
            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString("symbol");
            String startInput = params.getExtras().getString("start");
            String endInput = params.getExtras().getString("end");
            queryStringBuilder.append("\"" + stockInput + "\" and ");
            queryStringBuilder.append("startDate = \"" + startInput + "\" and ");
            queryStringBuilder.append("endDate = \"" + endInput + "\"");

            if (queryStringBuilder != null) {
                String queryString = queryStringBuilder.toString();
                String getResponse = mApiService.getYQL(queryString).toBlocking().single();
                result = GcmNetworkManager.RESULT_SUCCESS;
                try {
                    ArrayList batch = Utils.historicalQuoteJsonToContentVals(getResponse, mContext.getContentResolver());
                    if (batch != null && batch.size() > 0) {
                        mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, batch);
                        mContext.getContentResolver().notifyChange(
                                QuoteProvider.HistoricalQuoteData.CONTENT_URI, // URI where data was modified
                                null,                           // No local observer
                                false);                         // IMPORTANT: Do not sync to network
                    }

                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(LOG_TAG, "Error applying batch update", e);
                }
            }
        } else {    // yahoo quote data
            boolean isUpdate = false;
            // Base URL for the Yahoo query
            queryStringBuilder.append("select * from yahoo.finance.quotes where symbol "
                    + "in (");
            if (params.getTag().equals("init") || params.getTag().equals("periodic")) {
                isUpdate = true;
                initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{"Distinct " + QuoteColumns.SYMBOL}, null,
                        null, null);
                if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
                    // Init task. Populates DB with quotes for the symbols seen below
                    queryStringBuilder.append(
                            "\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")");
                } else if (initQueryCursor != null) {
                    DatabaseUtils.dumpCursor(initQueryCursor);
                    initQueryCursor.moveToFirst();
                    for (int i = 0; i < initQueryCursor.getCount(); i++) {
                        mStoredSymbols.append("\"" +
                                initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")) + "\",");
                        initQueryCursor.moveToNext();
                    }
                    mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                    queryStringBuilder.append(mStoredSymbols.toString());
                }
            } else if (params.getTag().equals("add")) {
                isUpdate = false;
                // get symbol from params.getExtra and build query
                String stockInput = params.getExtras().getString("symbol");
                queryStringBuilder.append("\"" + stockInput + "\")");
            }

            if (queryStringBuilder != null) {
                String queryString = queryStringBuilder.toString();
                String getResponse = mApiService.getYQL(queryString).toBlocking().single();
                result = GcmNetworkManager.RESULT_SUCCESS;
                try {
                    ArrayList batch = Utils.quoteJsonToContentVals(getResponse, mContext.getContentResolver(), isUpdate);
                    if (batch != null && batch.size() > 0) {
                        mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, batch);
                        mContext.getContentResolver().notifyChange(
                                QuoteProvider.Quotes.CONTENT_URI, // URI where data was modified
                                null,                           // No local observer
                                false);                         // IMPORTANT: Do not sync to network
                    }
                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(LOG_TAG, "Error applying batch insert", e);
                }
            }
        }

        return result;
    }

}
