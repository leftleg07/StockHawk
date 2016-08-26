package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by sam_chordas on 10/5/15.
 */
@Database(version =QuoteContract.DATABASE_VERSION, fileName = QuoteContract.DATABASE_NAME)
public class QuoteDatabase {
  private QuoteDatabase(){}


  @Table(QuoteColumns.class) public static final String QUOTES = QuoteContract.TABLE_NAME_QUOTES;
  @Table(HistoricalQuoteColumns.class) public static final String HISTORICAL_QUOTE = QuoteContract.TABLE_NAME_HISTORICAL_QUOTE;
}
