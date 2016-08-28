package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import javax.inject.Inject;

public class StockViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    protected static final String TAG = StockViewsFactory.class.getSimpleName();

    private Cursor mCursor;

    private final Context mContext;

    @Inject
    public StockViewsFactory(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate() {


    }

    @Override
    public void onDataSetChanged() {
        //If exist, get a fresh copy
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Move to position first
        if (!mCursor.moveToPosition(position)) {
            return null;
        }

        //Doc: https://developer.android.com/reference/android/widget/RemoteViews.html
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);

        String symbol = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));
        String percentChange = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
        String change = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE));
        String bidPrice = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE));
        int isUp = mCursor.getInt(mCursor.getColumnIndex(QuoteColumns.ISUP));

        //Set Symbol
        views.setTextViewText(R.id.stock_symbol, symbol);

        //Set bid price
        views.setTextViewText(R.id.bid_price, bidPrice);

        //Set pill bg
        if (isUp == 1) {
            views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        //Show changes based on preference
        if (Utils.showPercent) {
            views.setTextViewText(R.id.change, percentChange);
        } else {
            views.setTextViewText(R.id.change, change);
        }

        //Send the whole quotes object through
        Intent fillInIntent = new Intent();
        Uri uri = QuoteProvider.Quotes.withSymbol(symbol);
        fillInIntent.setData(uri);
        views.setOnClickFillInIntent(R.id.stock_list_item, fillInIntent); //R.id.stock_list_item is the container main container in list view item layout
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1; //We only have 1 type
    }

    @Override
    public long getItemId(int position) {
        if (mCursor != null &&
                mCursor.moveToPosition(position)) {
            return mCursor.getLong(0); //Always first column
        }

        return position; //I think
    }

    @Override
    public boolean hasStableIds() {
        return true; //Since its from db and id used is row id
    }
}
