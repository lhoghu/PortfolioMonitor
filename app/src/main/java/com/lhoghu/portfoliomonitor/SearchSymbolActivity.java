package com.lhoghu.portfoliomonitor;

import android.app.Activity;
import android.content.Context;
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

import com.lhoghu.yahoointerface.Stock;

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
     * On click, the symbol should be saved to the portfolio db
     */
    public void buttonAddSymbolHandler(View view)
    {
        // Get the row the button is clicked in
        LinearLayout parentRow = (LinearLayout) view.getParent();

        TextView symbol = (TextView) parentRow.getChildAt(0);
        TextView name = (TextView) parentRow.getChildAt(1);

        // TODO: create a popup for the user to enter the trade position
        PortfolioDbAdapter dbAdapter = new PortfolioDbAdapter(this);
        dbAdapter.open();
        dbAdapter.addSymbol(
                symbol.getText().toString(),
                name.getText().toString(),
                0);
        dbAdapter.close();
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

    private class YahooParser extends AsyncTask<String, Integer, Stock[]> {

        private static final String baseQuoteUrl = "http://finance.yahoo.com/d/quotes.csv?f=sb2n&s=";

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
                            values[0],
                            values[2],
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
            //setProgressPercent(progress[0]);
            // TODO: Toast update popup while user waits for http request
        }

        @Override
        protected void onPostExecute(Stock[] stocks) {
            StockAdapter adapter =
                    new StockAdapter(SearchSymbolActivity.this, R.layout.stock, stocks);

            ListView listView = (ListView) findViewById(R.id.symbol_list);
            listView.setAdapter(adapter);
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
