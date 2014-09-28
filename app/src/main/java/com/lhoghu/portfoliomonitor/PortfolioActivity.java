package com.lhoghu.portfoliomonitor;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class PortfolioActivity extends Activity {

    private SimpleCursorAdapter portfolioAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        // Draw the list view
        PortfolioDbAdapter dbAdapter = new PortfolioDbAdapter(this);
        dbAdapter.open();
        Cursor c = dbAdapter.fetchAllTrades();

        String[] dbCols = new String[] {
                PortfolioDbContract.Trade._ID,
                PortfolioDbContract.Trade.COLUMN_NAME_SYMBOL,
                PortfolioDbContract.Trade.COLUMN_NAME_NAME,
                PortfolioDbContract.Trade.COLUMN_NAME_POSITION
        };

        int[] layoutCols = new int[] {
                R.id.portfolio_id,
                R.id.portfolio_symbol,
                R.id.portfolio_name,
                R.id.portfolio_position
        };

        ListView listView = (ListView) findViewById(R.id.portfolio);

        View header = getLayoutInflater().inflate(R.layout.portfolio_header, null);
        listView.addHeaderView(header);

        portfolioAdapter = new SimpleCursorAdapter(this, R.layout.portfolio, c, dbCols, layoutCols, 0);
        listView.setAdapter(portfolioAdapter);

        // Might be better way of handling deletes/updates/detailed view...
//        listView.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> listView, View view,
//                                    int position, long id) {
//                // Get the cursor, positioned to the corresponding row in the result set
//                Cursor cursor = (Cursor) listView.getItemAtPosition(position);
//
//                // Get the state's capital from this row in the database.
//                String countryCode =
//                        cursor.getString(cursor.getColumnIndexOrThrow("code"));
//                Toast.makeText(getApplicationContext(),
//                        countryCode, Toast.LENGTH_SHORT).show();
//
//            }
//        });
        //dbAdapter.close();
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

            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void openSearchActivity() {
        Intent intent = new Intent(this, SearchSymbolActivity.class);
        startActivity(intent);
    }

    /**
     * Handler for a button click event within each listview element.
     * On click, the trade is removed from the portfolio db
     */
    public void buttonEditTradeHandler(View view)
    {
        // Get the row the button is clicked in
        LinearLayout parentRow = (LinearLayout) view.getParent();

        TextView id = (TextView) parentRow.getChildAt(0);

        PortfolioDbAdapter dbAdapter = new PortfolioDbAdapter(this);
        dbAdapter.open();
        dbAdapter.deleteEntry(Long.valueOf(id.getText().toString()));

        Cursor c = dbAdapter.fetchAllTrades();
        portfolioAdapter.changeCursor(c);
        dbAdapter.close();
    }

    /**
     * Handler to remove all trades from a portfolio
     */
    public void buttonClearAllTradesHandler(View view)
    {
        PortfolioDbAdapter dbAdapter = new PortfolioDbAdapter(this);
        dbAdapter.open();
        dbAdapter.deleteAllTrades();

        Cursor c = dbAdapter.fetchAllTrades();
        portfolioAdapter.changeCursor(c);
        dbAdapter.close();
    }
}
