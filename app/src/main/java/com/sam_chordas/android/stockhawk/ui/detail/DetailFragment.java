package com.sam_chordas.android.stockhawk.ui.detail;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.HistoricalQuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_CONTENT_URI = "_arg_content_uri";
    private static final int QOUTE_LOADER = 10;
    private static final int HISTORICAL_DATA_LOADER = 11;

    // TODO: Rename and change types of parameters
    private Uri mContentUri;
    private String mSymbol;

    @BindView(R.id.textView_detail_name)
    TextView mNameText;
    @BindView(R.id.textView_detail_symbol)
    TextView mSymbolText;
    @BindView(R.id.textView_detail_bid_price)
    TextView mPriceText;
    @BindView(R.id.textView_detail_change)
    TextView mChangeText;

    @BindView(R.id.linechart)
    LineChart mChart;

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uri Parameter 1.
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(Uri uri) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CONTENT_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mContentUri = getArguments().getParcelable(ARG_CONTENT_URI);
            mSymbol = QuoteProvider.Quotes.getSymbolFromUri(mContentUri);

            if (savedInstanceState == null) {
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


                Intent serviceIntent = new Intent(getActivity(), StockIntentService.class);
                serviceIntent.putExtra("tag", "hist");
                serviceIntent.putExtra("symbol", mSymbol);
                serviceIntent.putExtra("start", startDate);
                serviceIntent.putExtra("end", endDate);
                getActivity().startService(serviceIntent);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription(getString(R.string.empty_chart));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(QOUTE_LOADER, null, this);
        getLoaderManager().initLoader(HISTORICAL_DATA_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case QOUTE_LOADER:
                return new CursorLoader(getContext(), mContentUri, null, null, null, null);
            case HISTORICAL_DATA_LOADER:
                Uri uri = QuoteProvider.HistoricalQuoteData.withSymbol(mSymbol);
                String sortOrder = HistoricalQuoteColumns.DATE + " DESC LIMIT 5";
                return new CursorLoader(getContext(), uri, null, null, null, sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "count: " + data.getCount());
        if(data != null && data.moveToFirst()) {
            int id = loader.getId();
            switch (id) {
                case QOUTE_LOADER:
                    updateQuote(data);
                    break;
                case HISTORICAL_DATA_LOADER:
                    updateChart(data);
                    break;
            }
        }

    }

    /**
     * update quote info
     *
     * @param data
     */
    public void updateQuote(Cursor data) {
        String name = data.getString(data.getColumnIndex(QuoteColumns.NAME));
        String price = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
        String change = data.getString(data.getColumnIndex(QuoteColumns.CHANGE));
        String percent = data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
        getActivity().setTitle(mSymbol + " - " + name);
        mNameText.setText(name);
        mSymbolText.setText("NASDAQ: " + mSymbol);
        mPriceText.setText(price);
        mChangeText.setText(change + " (" + percent + ")");
    }

    /**
     * update chart
     *
     * @param data
     */
    public void updateChart(Cursor data) {
        int count = data.getCount();
        if (count > 0) {
            List<Entry> entries = new ArrayList<>();
            String[] xvalues = new String[count];
            for (int i = count - 1; i >= 0; i--) {
                xvalues[i] = data.getString(data.getColumnIndex(HistoricalQuoteColumns.DATE));
                String value = data.getString(data.getColumnIndex(HistoricalQuoteColumns.OPEN));
                entries.add(new Entry(Float.parseFloat(value), i));
                data.moveToNext();
            }

            XAxis xAxis = mChart.getXAxis();
            xAxis.setValueFormatter(new XAxisValueFormatter() {
                @Override
                public String getXValue(String original, int index, ViewPortHandler viewPortHandler) {
                    if (original.length() < 6) {
                        return original;
                    } else {
                        return original.substring(5);
                    }
                }
            });
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextSize(8f);
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);

            YAxis left = mChart.getAxisLeft();
            left.setTextSize(8f);
            left.setValueFormatter(new YAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, YAxis yAxis) {
                    return String.format("%.2f", value);
                }
            });
            left.setEnabled(true);
            left.setLabelCount(8, true);

            mChart.getAxisRight().setEnabled(false);
            mChart.getLegend().setTextSize(16f);

            LineDataSet dataSet = new LineDataSet(entries, mSymbol);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(8f);
            LineData lineData = new LineData(xvalues, dataSet);
            mChart.setBackgroundColor(Color.WHITE);
            mChart.setData(lineData);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
