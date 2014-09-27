package com.lhoghu.yahoointerface;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class YahooServlet {

    private static final String baseQuoteUrl = "http://finance.yahoo.com/d/quotes.csv?f=sb2n&s=";

//    public void doGet(HttpServletRequest request,
//                      HttpServletResponse response) throws IOException {
//        String[] symbols = request.getParameterValues("stock");
//        List<Stock> stocks = getStocks(symbols);
//        String format = request.getParameter("format");
//        String data = "";
//        if (format == null || format.equalsIgnoreCase("xml")){
//            data = Stock.toXml(stocks);
//            response.setContentType("text/xml");
//        } else if (format.equalsIgnoreCase("json")){
//            data = Stock.toJson(stocks);
//            response.setContentType("application/json");
//        } else if (format.equalsIgnoreCase("protobuf")){
//            Portfolio p = Stock.toProtoBuf(stocks);
//            response.setContentType("application/octet-stream");
//            response.setContentLength(p.getSerializedSize());
//            p.writeTo(response.getOutputStream());
//            response.flushBuffer();
//            return;
//        }
//        response.setContentLength(data.length());
//        response.getWriter().print(data);
//        response.flushBuffer();
//        response.getWriter().close();
//    }

    public static List<Stock> getStocks(String... symbols) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String symbol : symbols){
            sb.append(symbol);
            sb.append('+');
        }
        sb.deleteCharAt(sb.length() - 1);
        // The f=sb2n part defines set the info we'd like back from yahoo
        // Check http://www.jarloo.com/yahoo_finance/ for a list of alternatives
        // TODO: work the list of alternatives into the request
        // TODO: successfully retrieve indices (^FTSE)
        String urlStr = baseQuoteUrl + sb.toString();
        URL url = new URL(urlStr);
        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String quote = reader.readLine();
        List<Stock> stocks = new ArrayList<Stock>(symbols.length);
        while (quote != null){
            String[] values = quote.split(",");
            Stock s =
                    new Stock(values[0], values[2],
                            Double.parseDouble(values[1]));
            stocks.add(s);
            quote = reader.readLine();
        }
        return stocks;
    }
}
