package com.lhoghu.portfoliomonitor;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lhoghu.yahoointerface.Stock;

public class PortfolioActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        // Draw the list view
        PortfolioDbAdapter dbAdapter = new PortfolioDbAdapter(this);
        dbAdapter.open();
        Cursor c = dbAdapter.fetchAllTrades();
        Stock[] stocks = PortfolioDbAdapter.cursorToStockArray(c);
        c.close();

        PortfolioAdapter adapter =
                new PortfolioAdapter(this, R.layout.portfolio, stocks);

        ListView listView = (ListView) findViewById(R.id.portfolio);
        listView.setAdapter(adapter);
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
     * Class to populate symbol search results in a list view
     */
    public class PortfolioAdapter extends ArrayAdapter<Stock> {

        private Stock[] stocks;

        public PortfolioAdapter(Context context, int textViewResourceId, Stock[] stocks) {
            super(context, textViewResourceId, stocks);
            this.stocks = stocks;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){

            // Check to see if the view is null. If so, we have to inflate it.
            // To inflate it means to render, or show, the view.
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.portfolio, null);
            }

            Stock stock = stocks[position];

            if (stock != null) {
                TextView symbol     = (TextView) view.findViewById(R.id.portfolio_symbol);
                TextView name       = (TextView) view.findViewById(R.id.portfolio_name);
                TextView holding   = (TextView) view.findViewById(R.id.portfolio_position);

                if (symbol != null) {
                    symbol.setText(stock.symbol);
                }
                if (name != null) {
                    name.setText(stock.name);
                }
                if (holding != null) {
                    holding.setText(String.valueOf(stock.position));
                }
            }

            return view;
        }
    }

}
