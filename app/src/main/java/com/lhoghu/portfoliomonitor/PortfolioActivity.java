package com.lhoghu.portfoliomonitor;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class PortfolioActivity extends Activity {

    private PortfolioAdapter portfolioAdapter;
    private PortfolioDbAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        // Draw the list view
        dbAdapter = new PortfolioDbAdapter(this);
        dbAdapter.open();
        Cursor c = dbAdapter.fetchAllTrades();

        String[] dbCols = new String[]{
                PortfolioDbContract.Trade.COLUMN_NAME_SYMBOL,
                PortfolioDbContract.Trade.COLUMN_NAME_NAME,
                PortfolioDbContract.Trade.COLUMN_NAME_CURRENCY,
                PortfolioDbContract.Trade.COLUMN_NAME_POSITION,
                PortfolioDbContract.Trade.COLUMN_NAME_BOUGHTAT,
                PortfolioDbContract.Trade.COLUMN_NAME_PRICE,
                PortfolioDbContract.Trade.COLUMN_NAME_CHANGE,
                PortfolioDbContract.Trade.COLUMN_NAME_PCTCHANGE,
                PortfolioDbContract.Trade.COLUMN_NAME_VOLUME
        };

        int[] layoutCols = new int[]{
                R.id.portfolio_symbol,
                R.id.portfolio_name,
                R.id.portfolio_currency,
                R.id.portfolio_position,
                R.id.portfolio_boughtat,
                R.id.portfolio_price,
                R.id.portfolio_change,
                R.id.portfolio_pctchange,
                R.id.portfolio_volume
        };

        ListView listView = (ListView) findViewById(R.id.portfolio);

        View header = getLayoutInflater().inflate(R.layout.portfolio_header, null);
        listView.addHeaderView(header);

        portfolioAdapter = new PortfolioAdapter(this, R.layout.portfolio, c, dbCols, layoutCols, 0);
        listView.setAdapter(portfolioAdapter);
        registerForContextMenu(listView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.portfolio) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.portfolio_context, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.portfolio_item_edit:
                editPortfolioItem(info.position);
                return true;

            case R.id.portfolio_item_details:
                Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.portfolio_item_delete:
                deletePortfolioItem(info.position);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.portfolio, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_search:
                openSearchActivity();
                return true;

            case R.id.action_refresh:
                refreshSymbols();
                return true;

            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Trigger a refresh of the portfolio view by setting a new cursor on the portfolio adapter
     * Closes the old cursor after a new cursor has been set on the portfolio adapter
     *
     * @param c Old cursor on the portfolio adapter that will be closed
     */
    private void invalidateCursor(Cursor c){
        Cursor newCursor = dbAdapter.fetchAllTrades();
        portfolioAdapter.changeCursor(newCursor);
        c.close();
    }

    private void openSearchActivity() {
        Intent intent = new Intent(this, SearchSymbolActivity.class);
        startActivity(intent);
    }

    private void refreshSymbols() {
        Cursor cursor = dbAdapter.fetchAllSymbols();
        String[] symbols = new String[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()) {
            symbols[i++] = cursor.getString(cursor.getColumnIndex(PortfolioDbContract.Trade.COLUMN_NAME_SYMBOL));
        }

        try {
            YahooParser parser = new YahooParser(PortfolioActivity.this);
            parser.execute(symbols);
            Stock[] stocks = parser.get();
            for (Stock stock : stocks) {
                dbAdapter.updateTradeDynamic(
                        stock.symbol, stock.price, stock.change, stock.pctChange, stock.volume);
            }
        } catch (Exception e) {
            Toast.makeText(PortfolioActivity.this, "Failed to update stock data", Toast.LENGTH_SHORT).show();
        }
        invalidateCursor(cursor);
    }

    private long getIdFromPosition(int pos) {
        // Get the id of the db row that corresponds to the user-clicked position in the portfolio view
        Cursor c = portfolioAdapter.getCursor();
        c.moveToPosition(pos - 1);
        return c.getLong(c.getColumnIndex(PortfolioDbContract.Trade._ID));
    }

    private void deletePortfolioItem(int pos) {
        long tradeId = getIdFromPosition(pos);

        // Delete the trade id from the db
        dbAdapter.deleteEntry(tradeId);

        // Reset the cursor on the view adapter to trigger a redraw
        invalidateCursor(portfolioAdapter.getCursor());
    }

    private void editPortfolioItem(int pos) {
        long tradeId = getIdFromPosition(pos);

        // Open the dialog that allows the user to set trade static data
        UpdateSymbol updater = new ExistingSymbol(tradeId);
        TradeInputDialog dialog = new TradeInputDialog(PortfolioActivity.this, updater);
        dialog.create();

        // Use onCompleted method on ExistingSymbol to trigger the redraw after the db's
        // been updated
    }

    private class PortfolioAdapter extends CursorAdapter {

        //LayoutInflater inflater;
        int layout;
        String[] dbColumns;
        int[] layoutIndices;

        DecimalFormat percent = new DecimalFormat("#.##%");
        DecimalFormat decimal = new DecimalFormat("#.##");
        DecimalFormat volume = new DecimalFormat("#,###");

        public PortfolioAdapter(
                Context context,
                int layout,
                Cursor c,
                String[] from,
                int[] to,
                int flags) {
            super(context, c, flags);

            this.layout = layout;
            this.dbColumns = from;
            this.layoutIndices = to;

            percent.setMinimumFractionDigits(2);
            decimal.setMinimumFractionDigits(2);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return getLayoutInflater().inflate(layout, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
//              Alternate bg colour from row to row
//            if(cursor.getPosition()%2==1) {
//                view.setBackgroundColor(context.getResources().getColor(R.color.background_odd));
//            }
//            else {
//                view.setBackgroundColor(context.getResources().getColor(R.color.background_even));
//            }

            int nbCols = dbColumns.length;
            for (int i = 0; i < nbCols; ++i) {
                TextView content = (TextView) view.findViewById(layoutIndices[i]);
                String val = cursor.getString(cursor.getColumnIndex(dbColumns[i]));

                val = applyFormats(layoutIndices[i], val, content);

                content.setText(val);
            }
        }

        private String applyFormats(int layoutIndex, String val, TextView content) {
            switch (layoutIndex) {
                case R.id.portfolio_price: {
                    double dVal = Double.valueOf(val);
                    return decimal.format(dVal);
                }
                case R.id.portfolio_change: {
                    double dVal = Double.valueOf(val);
                    colorDecimalFormats(dVal, content);
                    return decimal.format(dVal);
                }
                case R.id.portfolio_pctchange: {
                    double dVal = Double.valueOf(val);
                    colorDecimalFormats(dVal, content);
                    return percent.format(dVal / 100.0);
                }
                case R.id.portfolio_volume: {
                    double dVal = Double.valueOf(val);
                    return volume.format(dVal);
                }
                default: return val;
            }
        }

        private void colorDecimalFormats(double val, TextView content) {
            if (val < 0.0) {
                content.setTextColor(getResources().getColor(R.color.red));
            } else {
                content.setTextColor(getResources().getColor(R.color.green));
            }
        }
    }

    // Extend the UpdateSymbol class so we can use the TradeInputDialog to edit the
    // user defined trade data on the existing symbol
    private class ExistingSymbol extends UpdateSymbol {

        private long id;

        public ExistingSymbol(long id) {
            this.id = id;
        }

        public long update(String currency, int position, Double boughtAt) {

            boolean success = dbAdapter.updateTradeStatic(id, currency, position, boughtAt);

            if (!success)
                return -1;

            return id;
        }

        @Override
        public void overrideHintText(
                EditText currencyView,
                EditText positionView,
                EditText boughtAtView) {
            Cursor c = dbAdapter.fetchTrade(id);
            String currency = c.getString(c.getColumnIndex(PortfolioDbContract.Trade.COLUMN_NAME_CURRENCY));
            String position = c.getString(c.getColumnIndex(PortfolioDbContract.Trade.COLUMN_NAME_POSITION));
            String boughtAt = c.getString(c.getColumnIndex(PortfolioDbContract.Trade.COLUMN_NAME_BOUGHTAT));
            c.close();

            if (currency != null && !currency.isEmpty()) currencyView.setText(currency);
            if (position != null && !position.isEmpty()) positionView.setText(position);
            if (boughtAt != null && !boughtAt.isEmpty()) boughtAtView.setText(boughtAt);
        }

        @Override
        public void onCompleted(long success) {
            if (success == -1)
                Toast.makeText(
                        PortfolioActivity.this,
                        "Failed to update " + getSymbol() + " in portfolio",
                        Toast.LENGTH_SHORT).show();

            // Reset the cursor on the view adapter to trigger a redraw
            invalidateCursor(portfolioAdapter.getCursor());
        }

        private String getSymbol() {
            Cursor c = dbAdapter.fetchTrade(id);
            String symbol = c.getString(c.getColumnIndex(PortfolioDbContract.Trade.COLUMN_NAME_SYMBOL));
            c.close();

            return symbol;
        }
    }
}
