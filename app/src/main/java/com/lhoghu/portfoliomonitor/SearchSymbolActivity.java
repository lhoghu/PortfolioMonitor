package com.lhoghu.portfoliomonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
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
        LayoutInflater li = LayoutInflater.from(this);
        View dialogView = li.inflate(R.layout.add_symbol_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(dialogView);

        final EditText userInput = (EditText) dialogView.findViewById(R.id.position_user_input);
        final View v = view;
        final Context context = this;

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Get the row the button is clicked in
                                LinearLayout parentRow = (LinearLayout) v.getParent();

                                TextView symbol = (TextView) parentRow.getChildAt(0);
                                TextView name = (TextView) parentRow.getChildAt(1);
                                TextView price = (TextView) parentRow.getChildAt(2);

                                int position = Long.valueOf(userInput.getText().toString()).intValue();

                                PortfolioDbAdapter dbAdapter = new PortfolioDbAdapter(context);
                                dbAdapter.open();
                                long success = dbAdapter.addSymbol(
                                        symbol.getText().toString(),
                                        name.getText().toString(),
                                        position);
                                if (success != -1) {
                                    dbAdapter.updateTradeDynamic(
                                            symbol.getText().toString(),
                                            Double.valueOf(price.getText().toString()));
                                }
                                dbAdapter.close();

                                if (success == -1)
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Failed to add " + symbol.getText().toString() + " to portfolio",
                                            Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(
                                            getApplicationContext(),
                                            "Added " + symbol.getText().toString() + " to portfolio",
                                            Toast.LENGTH_SHORT).show();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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

                if (symbol != null) {
                    symbol.setText(stock.symbol);
                }
                if (name != null) {
                    name.setText(stock.name);
                }
                if (price != null) {
                    price.setText(String.valueOf(stock.price));
                }
            }

            return view;
        }
    }
}
