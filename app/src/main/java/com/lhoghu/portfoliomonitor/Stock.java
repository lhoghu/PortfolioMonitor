package com.lhoghu.portfoliomonitor;

import java.util.List;

public class Stock {
    public final String symbol;
    public final String name;
    public final Double price;
    public final Double change;
    public final Double pctChange;
    public final Double volume;
    public final Double position;
    public final long id;

    public Stock(
            String symbol,
            String name,
            Double last,
            Double change,
            Double volume) {
        this.symbol = symbol;
        this.name = name;
        this.price = last;
        this.change = change;
        this.pctChange = 100.0 * change / price;
        this.volume = volume;
        this.position = 0.0;
        this.id = 0;
    }

    @Override
    public String toString(){
        return name + " (" + symbol + "): " + price;
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