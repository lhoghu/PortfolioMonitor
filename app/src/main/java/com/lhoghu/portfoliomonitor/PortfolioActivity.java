package com.lhoghu.portfoliomonitor;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class PortfolioActivity extends Activity {

    private SimpleCursorAdapter portfolioAdapter;
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
                PortfolioDbContract.Trade.COLUMN_NAME_POSITION,
                PortfolioDbContract.Trade.COLUMN_NAME_PRICE
        };

        int[] layoutCols = new int[]{
                R.id.portfolio_symbol,
                R.id.portfolio_name,
                R.id.portfolio_position,
                R.id.portfolio_price
        };

        ListView listView = (ListView) findViewById(R.id.portfolio);

        View header = getLayoutInflater().inflate(R.layout.portfolio_header, null);
        listView.addHeaderView(header);

        portfolioAdapter = new SimpleCursorAdapter(this, R.layout.portfolio, c, dbCols, layoutCols, 0);
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
                // pass info.id to a method that edits the item
                Toast.makeText(this, "Pretend to edit item", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.portfolio_item_details:
                Toast.makeText(this, "Pretend to show item detail", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.portfolio_item_delete:
                Cursor c = portfolioAdapter.getCursor();
                c.moveToPosition(info.position - 1);
                long tradeId = c.getLong(c.getColumnIndex(PortfolioDbContract.Trade._ID));
                dbAdapter.deleteEntry(tradeId);
                invalidateCursor(c);
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
                dbAdapter.updateTradeDynamic(stock.symbol, stock.price);
            }
        } catch (Exception e) {
            Toast.makeText(PortfolioActivity.this, "Failed to update stock data", Toast.LENGTH_SHORT).show();
        }
        invalidateCursor(cursor);
    }
}
