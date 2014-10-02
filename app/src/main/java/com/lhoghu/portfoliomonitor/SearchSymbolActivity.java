package com.lhoghu.portfoliomonitor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchSymbolActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_symbol);

        final Button searchButton = (Button) findViewById(R.id.symbol_search_button);
        searchButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                final EditText input = (EditText) findViewById(R.id.symbol_search);
                String symbol = input.getText().toString();
                try {
                    YahooParser parser = new YahooParser(SearchSymbolActivity.this);
                    parser.execute(symbol);
                    Stock[] stocks = parser.get();

                    StockAdapter adapter =
                            new StockAdapter(SearchSymbolActivity.this, R.layout.stock, stocks);

                    ListView listView = (ListView) findViewById(R.id.symbol_list);
                    listView.setAdapter(adapter);
                } catch (Exception e) {
                    Toast.makeText(SearchSymbolActivity.this,
                            "Failed to load stock " + symbol + " from Yahoo",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_symbol, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handler for a button click event within each listview element.
     * On click, a dialog window should appear that requests user information
     * (position, currency, ...) before the symbol is saved to the portfolio db
     */
    public void buttonAddSymbolHandler(View view)
    {
        UpdateSymbol update = new NewSymbol(SearchSymbolActivity.this);
        TradeInputDialog dialog = new TradeInputDialog(SearchSymbolActivity.this, update);
        dialog.create();
    }

    /**
     * Class to populate symbol search results in a list view
     */
    public class StockAdapter extends ArrayAdapter<Stock> {

        private Stock[] stocks;

        public StockAdapter(Context context, int textViewResourceId, Stock[] stocks) {
            super(context, textViewResourceId, stocks);
            this.stocks = stocks;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){

            // Check to see if the view is null. If so, we have to inflate it.
            // To inflate it means to render, or show, the view.
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.stock, null);
            }

            Stock stock = stocks[position];

            if (stock != null) {
                TextView symbol = (TextView) view.findViewById(R.id.symbol_search_result);
                TextView name   = (TextView) view.findViewById(R.id.symbol_search_name);
                TextView price  = (TextView) view.findViewById(R.id.symbol_search_price);
                TextView change  = (TextView) view.findViewById(R.id.symbol_search_change);
                TextView volume = (TextView) view.findViewById(R.id.symbol_search_volume);

                setIfNotNull(symbol, stock.symbol);
                setIfNotNull(name, stock.name);
                setIfNotNull(price, String.valueOf(stock.price));
                setIfNotNull(change, String.valueOf(stock.change));
                setIfNotNull(volume, String.valueOf(stock.volume));
            }

            return view;
        }

        private void setIfNotNull(TextView view, String text) {
            if (view != null) {
                view.setText(text);
            }
        }
    }

    /**
     * Extend UpdateSymbol for cases where we want to add a new symbol to the db with
     * an associated currency and position
     */
    public class NewSymbol extends UpdateSymbol {

        private String symbol;
        private String name;
        private Context context;

        /**
         * Use a view to read the symbol/name information of the trade we're adding to the db
         *
         * @param context The context used to instantiate the db
         */
        public NewSymbol(Context context) {
            this.context = context;
            setMembersFromView();
        }

        public long update(String currency, int position, Double boughtAt) {
            // Create a new entry in the db for the symbol, along with the supplied
            // currency position info
            PortfolioDbAdapter dbAdapter = new PortfolioDbAdapter(context);
            dbAdapter.open();
            long success = dbAdapter.addSymbol(symbol, name, currency, position, boughtAt);
            dbAdapter.close();

            return success;
        }

        @Override
        public void onCompleted(long success) {
            if (success == -1)
                Toast.makeText(
                        context,
                        "Failed to update " + symbol + " in portfolio",
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(
                        context,
                        "Updated " + symbol + " in portfolio",
                        Toast.LENGTH_SHORT).show();
        }

        private void setMembersFromView() {
            TextView symbolTextView = (TextView) findViewById(R.id.symbol_search_result);
            TextView nameTextView   = (TextView) findViewById(R.id.symbol_search_name);

            symbol = symbolTextView.getText().toString();
            name = nameTextView.getText().toString();
        }
    }
}
