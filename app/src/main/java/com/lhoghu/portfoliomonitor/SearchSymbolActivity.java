package com.lhoghu.portfoliomonitor;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

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
        }

        @Override
        protected void onPostExecute(Stock[] stocks) {
            ArrayAdapter<Stock> adapter =
                    new ArrayAdapter<Stock>(SearchSymbolActivity.this, R.layout.stock, stocks);

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
