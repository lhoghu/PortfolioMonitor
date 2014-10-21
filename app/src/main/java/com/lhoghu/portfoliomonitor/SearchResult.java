package com.lhoghu.portfoliomonitor;

public class SearchResult {
    public final String symbol;
    public final String name;
    public final String exchange;
    public final String type;

    public SearchResult(
            String symbol,
            String name,
            String exchange,
            String type) {
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
        this.type = type;
    }

    @Override
    public String toString(){
        return name + " (" + symbol + "): " + type + " - " + exchange;
    }
}
