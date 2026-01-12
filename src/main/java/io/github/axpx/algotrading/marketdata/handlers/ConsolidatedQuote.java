package io.github.axpx.algotrading.marketdata.handlers;


public record ConsolidatedQuote(
        String symbol,
        double bestBid,
        String bestBidVenue,
        long bestBidSize,
        double bestAsk,
        String bestAskVenue,
        long bestAskSize
) {

    public double spread() {
        return bestAsk -  bestBid;
    }

    public double midPrice() {
        return (bestAsk + bestBid) / 2.0;
    }


}
