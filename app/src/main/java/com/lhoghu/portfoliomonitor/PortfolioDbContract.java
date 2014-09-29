package com.lhoghu.portfoliomonitor;

import android.provider.BaseColumns;

public final class PortfolioDbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public PortfolioDbContract() {}

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Portfolio.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";

    /* Inner class that defines the table contents */
    public static abstract class Trade implements BaseColumns {

        public static final String TABLE_NAME = "Portfolio";

        public static final String COLUMN_NAME_SYMBOL = "symbol";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_CURRENCY = "currency";
        public static final String COLUMN_NAME_PRICE = "price";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        Trade._ID + " INTEGER PRIMARY KEY," +
                        Trade.COLUMN_NAME_SYMBOL + TEXT_TYPE + COMMA_SEP +
                        Trade.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                        Trade.COLUMN_NAME_CURRENCY + TEXT_TYPE + COMMA_SEP +
                        Trade.COLUMN_NAME_POSITION + REAL_TYPE + COMMA_SEP +
                        Trade.COLUMN_NAME_PRICE + REAL_TYPE +
                        " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}