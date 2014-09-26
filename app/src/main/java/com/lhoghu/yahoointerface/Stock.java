package com.lhoghu.yahoointerface;

import java.util.List;

public class Stock {
    public final String symbol;
    public final String name;
    public final double price;

    public Stock(String symbol, String name, double price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
    }

    public String toXml(){
        return "<stock><symbol>" + symbol +
                "</symbol><name><![CDATA[" +
                name + "]]></name><price>" + price +
                "</price></stock>";
    }

    public String toJson(){
        return "{ 'stock' : { 'symbol' : " +symbol +", 'name':" + name +
                ", 'price': '" + price + "'}}";
    }

    public static String toXml(List<Stock> stocks){
        StringBuilder xml = new StringBuilder("<stocks>");
        for (Stock s : stocks){
            xml.append(s.toXml());
        }
        xml.append("</stocks>");
        return xml.toString();
    }

    public static String toJson(List<Stock> stocks){
        StringBuilder json = new StringBuilder("{'stocks' : [");
        for (Stock s : stocks){
            json.append(s.toJson());
            json.append(',');
        }
        json.deleteCharAt(json.length() - 1);
        json.append("]}");
        return json.toString();
    }
}