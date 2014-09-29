package com.lhoghu.portfoliomonitor;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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

public class YahooParser extends AsyncTask<String, Integer, Stock[]> {

    private static final String baseQuoteUrl = "http://finance.yahoo.com/d/quotes.csv?f=sb2n&s=";
    private ProgressDialog pd;
    Context context;

    public YahooParser(Context context) {
        this.context = context;
    }

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
        pd = ProgressDialog.show(context, "Please wait", "Downloading from Yahoo...", true);
    }

    @Override
    protected void onPostExecute(Stock[] stocks) {
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

    private static String stripQuotes (String str) {
        return str.replaceAll("^\"|\"$", "");
    }
}
