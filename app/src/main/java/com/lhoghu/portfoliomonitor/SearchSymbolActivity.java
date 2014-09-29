package com.lhoghu.portfoliomonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
                new YahooParser().execute(symbol);
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
                            public void onClick(DialogInterface dialog,int id) {
                                // Get the row the button is clicked in
                                LinearLayout parentRow = (LinearLayout) v.getParent();

                                TextView symbol = (TextView) parentRow.getChildAt(0);
                                TextView name = (TextView) parentRow.getChildAt(1);

                                int position = Long.valueOf(userInput.getText().toString()).intValue();

                                PortfolioDbAdapter dbAdapter = new PortfolioDbAdapter(context);
                                dbAdapter.open();
                                long success = dbAdapter.addSymbol(
                                        symbol.getText().toString(),
                                        name.getText().toString(),
                                        position);
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
                            public void onClick(DialogInterface dialog,int id) {
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

    private static String stripQuotes (String str) {
        return str.replaceAll("^\"|\"$", "");
    }

    private class YahooParser extends AsyncTask<String, Integer, Stock[]> {

        private static final String baseQuoteUrl = "http://finance.yahoo.com/d/quotes.csv?f=sb2n&s=";
        private ProgressDialog pd;

        public YahooParser() {}

        @Override
        protected Stock[] doInBackground(String... symbols) {
            List<Stock> stocks = new ArrayList<Stock>();
            try{
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(getData(symbols)));
                String line = reader.readLine();
                while (line != null){
                    String[] values = line.split(",");
                    Stock stock = new Stock(
                            stripQuotes(values[0]),
                            stripQuotes(values[2]),
                            Double.parseDouble(values[1]));
                    stocks.add(stock);
                    line = reader.readLine();
                }

            } catch (Exception e){
                Log.e("Portfolio Monitor", "Exception getting JSON data", e);
            }

            Stock[] stockArr = new Stock[stocks.size()];
            stockArr = stocks.toArray(stockArr);
            return stockArr;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }

        @Override
        protected  void onPreExecute() {
            pd = ProgressDialog.show(SearchSymbolActivity.this, "Please wait", "Downloading from Yahoo...", true);
        }

        @Override
        protected void onPostExecute(Stock[] stocks) {
            StockAdapter adapter =
                    new StockAdapter(SearchSymbolActivity.this, R.layout.stock, stocks);

            ListView listView = (ListView) findViewById(R.id.symbol_list);
            listView.setAdapter(adapter);

            if (pd != null)
                pd.dismiss();
        }

        private String makeUrlString(String... symbols) {
            StringBuilder sb = new StringBuilder(baseQuoteUrl);
            for (int i=0;i<symbols.length;i++) {
                if (i > 0) sb.append("+");
                sb.append(symbols[i]);
            }
            return sb.toString();
        }

        private InputStream getData(String[] symbols) throws Exception {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(new URI(makeUrlString(symbols)));

            HttpResponse response = client.execute(request);
            return response.getEntity().getContent();
        }
    }
}
