package com.lhoghu.portfoliomonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lhoghu.yahoointerface.Stock;

public class PortfolioDbAdapter {

    private PortfolioDbHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;

    /**
     * Helper class to maintain and return instances of the db, or create one
     * if none exist
     */
    public class PortfolioDbHelper extends SQLiteOpenHelper {

        public PortfolioDbHelper(Context context) {
            super(context, PortfolioDbContract.DATABASE_NAME, null, PortfolioDbContract.DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(PortfolioDbContract.Trade.SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database stores user data on their holdings and positions
            // therefore we need to copy across anything stored in the previous version
            // of the db
            // TODO: implement copy of existing data
            db.execSQL(PortfolioDbContract.Trade.SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public PortfolioDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the portfolio database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public PortfolioDbAdapter open() throws SQLException {
        mDbHelper = new PortfolioDbHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Add a new symbol to the portfolio db. If the trade is
     * successfully created return the new rowId for that trade, otherwise return
     * a -1 to indicate failure.
     *
     * @param symbol the yahoo symbol for the trade
     * @param name the yahoo name associated with the symbol
     * @return rowId or -1 if failed
     */
    public long addSymbol(String symbol, String name, int position) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(PortfolioDbContract.Trade.COLUMN_NAME_SYMBOL, symbol);
        initialValues.put(PortfolioDbContract.Trade.COLUMN_NAME_NAME, name);
        initialValues.put(PortfolioDbContract.Trade.COLUMN_NAME_POSITION, position);

        return mDb.insert(PortfolioDbContract.Trade.TABLE_NAME, null, initialValues);
    }

    /**
     * Delete the trade entry with the given rowId
     *
     * @param rowId id of trade to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteEntry(long rowId) {
        return mDb.delete(
                PortfolioDbContract.Trade.TABLE_NAME,
                PortfolioDbContract.Trade._ID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all trades in the portfolio
     *
     * @return Cursor over all trades
     */
    public Cursor fetchAllTrades() {
        return mDb.query(
                PortfolioDbContract.Trade.TABLE_NAME,
                new String[] {
                        PortfolioDbContract.Trade.COLUMN_NAME_SYMBOL,
                        PortfolioDbContract.Trade.COLUMN_NAME_NAME,
                        PortfolioDbContract.Trade.COLUMN_NAME_POSITION,
                        PortfolioDbContract.Trade._ID
                },
                null, null, null, null, null);
    }

//    private static String getStringFromCursor(Cursor c, String columnName) {
//        return c.getString(c.getColumnIndex(columnName));
//    }

    public static Stock[] cursorToStockArray (Cursor cursor)
    {
        Stock[] stocks = new Stock[cursor.getCount()];

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long id         = cursor.getLong(cursor.getColumnIndex(PortfolioDbContract.Trade._ID));
            String symbol   = cursor.getString(cursor.getColumnIndex(PortfolioDbContract.Trade.COLUMN_NAME_SYMBOL));
            String name     = cursor.getString(cursor.getColumnIndex(PortfolioDbContract.Trade.COLUMN_NAME_NAME));
            double position = cursor.getDouble(cursor.getColumnIndex(PortfolioDbContract.Trade.COLUMN_NAME_POSITION));
            stocks[cursor.getPosition()] = new Stock(symbol, name, 0.0, position, id);
            cursor.moveToNext();
        }

        return stocks;
    }

    /**
     * Return a Cursor positioned at the trade that matches the given rowId
     *
     * @param rowId id of trade to retrieve
     * @return Cursor positioned to matching trade, if found
     * @throws SQLException if trade could not be found/retrieved
     */
    public Cursor fetchTrade(long rowId) throws SQLException {

        Cursor mCursor = mDb.query(
                true,
                PortfolioDbContract.Trade.TABLE_NAME,
                new String[] {
                        PortfolioDbContract.Trade.COLUMN_NAME_SYMBOL,
                        PortfolioDbContract.Trade.COLUMN_NAME_NAME,
                        PortfolioDbContract.Trade.COLUMN_NAME_POSITION,
                        PortfolioDbContract.Trade._ID},
                PortfolioDbContract.Trade._ID + "=" + rowId,
                null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    /**
     * Update the trade using the details provided. The trade to be updated is
     * specified using the rowId, and it is altered to use the position
     * value passed in
     *
     * @param rowId id of trade to update
     * @param position position to set on trade
     * @return true if the trade was successfully updated, false otherwise
     */
    public boolean updateTrade(long rowId, int position) {
        ContentValues args = new ContentValues();
        args.put(PortfolioDbContract.Trade.COLUMN_NAME_POSITION, position);

        return mDb.update(
                PortfolioDbContract.Trade.TABLE_NAME,
                args,
                PortfolioDbContract.Trade._ID + "=" + rowId,
                null) > 0;
    }

    /**
     * Remove all trades from a portfolio
     *
     * @return true if the trade was successfully updated, false otherwise
     */
    public boolean deleteAllTrades() {
        return mDb.delete(PortfolioDbContract.Trade.TABLE_NAME, null, null) > 0;
    }
}