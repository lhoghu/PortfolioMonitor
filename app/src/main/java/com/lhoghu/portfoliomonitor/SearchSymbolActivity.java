package com.lhoghu.portfoliomonitor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchSymbolActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_symbol);

        // Auto completion on the search box
        final EditText textBox = (EditText) findViewById(R.id.symbol_search);
        textBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                String userInput = textBox.getText().toString();

                try {
                    YahooLookup lookup = new YahooLookup();
                    lookup.execute(userInput);
                    SearchResult[] searchResults = lookup.get();

                    SearchResultAdapter adapter = new SearchResultAdapter(
                            SearchSymbolActivity.this,
                            R.layout.search_result,
                            searchResults);

                    ListView listView = (ListView) findViewById(R.id.symbol_list);
                    listView.setAdapter(adapter);

                } catch (Exception e) {
                    Toast.makeText(SearchSymbolActivity.this,
                            "Failed to lookup symbol " + userInput + " from Yahoo",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Listener on the search results that adds symbols to the portfolio
        final ListView searchResults = (ListView) findViewById(R.id.symbol_list);
        searchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long row) {
                SearchResultAdapter adapter = (SearchResultAdapter) adapterView.getAdapter();
                SearchResult result = adapter.searchResults[position];

                UpdateSymbol update = new NewSymbol(SearchSymbolActivity.this, result.symbol, result.name);
                TradeInputDialog dialog = new TradeInputDialog(SearchSymbolActivity.this, update);
                dialog.create();
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
     * Class to populate symbol search results in a list view
     */
    public class SearchResultAdapter extends ArrayAdapter<SearchResult> {

        private SearchResult[] searchResults;

        public SearchResultAdapter(Context context, int textViewResourceId, SearchResult[] searchResults) {
            super(context, textViewResourceId, searchResults);
            this.searchResults = searchResults;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){

            // Check to see if the view is null. If so, we have to inflate it.
            // To inflate it means to render, or show, the view.
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.search_result, null);
            }

            SearchResult searchResult = searchResults[position];

            if (searchResult != null) {
                TextView symbol = (TextView) view.findViewById(R.id.symbol_search_result);
                TextView name   = (TextView) view.findViewById(R.id.symbol_search_name);
                TextView type  = (TextView) view.findViewById(R.id.symbol_search_type);
                TextView exchange  = (TextView) view.findViewById(R.id.symbol_search_exchange);


                setIfNotNull(symbol, searchResult.symbol);
                setIfNotNull(name, searchResult.name);
                setIfNotNull(type, String.valueOf(searchResult.type));
                setIfNotNull(exchange, String.valueOf(searchResult.exchange));
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
         * @param symbol The new symbol to be added to the db
         * @param name The new name to be added to the db
         */
        public NewSymbol(Context context, String symbol, String name) {
            this.context = context;
            this.symbol = symbol;
            this.name = name;
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
    }
}
