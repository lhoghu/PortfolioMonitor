package com.lhoghu.portfoliomonitor;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YahooLookup extends AsyncTask<String, Integer, SearchResult[]> {

    private static final String token = "USERINPUT";
    private static final String baseQuoteUrl =
            "http://d.yimg.com/autoc.finance.yahoo.com/autoc?query=" +
                    token + "&callback=YAHOO.Finance.SymbolSuggest.ssCallback";

    @Override
    protected SearchResult[] doInBackground(String... userInput) {
        List<SearchResult> searchResults = new ArrayList<SearchResult>();
        if (!userInput[0].isEmpty()) {
            try {
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(getData(userInput[0])));
                String line = reader.readLine();

                // Get json back from yahoo as an argument to a class method
                // Parse that here
                String jsonStr = stripClassname(line);
                JSONObject json = new JSONObject(jsonStr);
                JSONObject jsonResult = json.getJSONObject("ResultSet");
                JSONArray results = jsonResult.getJSONArray("Result");
                for (int i = 0; i < results.length(); ++i) {
                    JSONObject result = results.getJSONObject(i);

                    SearchResult searchResult = new SearchResult(
                            result.getString("symbol"),
                            result.getString("name"),
                            result.getString("exchDisp"),
                            result.getString("typeDisp"));
                    searchResults.add(searchResult);
                }

            } catch (Exception e) {
                Log.e("Portfolio Monitor", "Exception getting JSON data", e);
            }
        }

        SearchResult[] searchResultArr = new SearchResult[searchResults.size()];
        searchResultArr = searchResults.toArray(searchResultArr);
        return searchResultArr;
    }

    private String makeUrlString(String symbol) {

        return baseQuoteUrl.replace(token, symbol);
    }

    private InputStream getData(String symbol) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(new URI(makeUrlString(symbol)));

        HttpResponse response = client.execute(request);
        return response.getEntity().getContent();
    }

    private static String stripClassname (String str) {
        Pattern regex = Pattern.compile("YAHOO\\.Finance\\.SymbolSuggest\\.ssCallback\\((.*?)\\)");
        Matcher match = regex.matcher(str);
        if (!match.find()) {
            Log.e("Portfolio Monitor", "Failed to parse Yahoo json lookup result: " + str);
            return "";
        }
        return match.group(1);
    }
}
