package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by sam_chordas on 10/5/15.
 */
@Database(version =QuoteContract.DATABASE_VERSION, fileName = QuoteContract.DATABASE_NAME)
public class QuoteDatabase {
  private QuoteDatabase(){}


  @Table(QuoteColumns.class) public static final String QUOTES = QuoteContract.TABLE_QUOTES_NAME;
}
