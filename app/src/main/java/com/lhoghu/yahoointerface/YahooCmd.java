package com.lhoghu.yahoointerface;

import java.io.IOException;
import java.util.List;

public class YahooCmd {

        public static void main(String[] args) {
            if (args.length > 0) {
                String arg = args[0];
                String[] symbols = arg.split(";");
                try {
                    List<Stock> stocks = YahooServlet.getStocks(symbols);
                    for (Stock s : stocks) {
                        String output = new StringBuilder()
                                .append("Symbol: ").append(s.symbol)
                                .append("\tName: ").append(s.name)
                                .append("\tPrice: ").append(String.valueOf(s.price))
                                .toString();
                        System.out.println(output);
                    }
                }
                catch (IOException ex)
                {

                }

            }
            else {
                // TODO: Output usage...
            }
        }
}
