package io.github.axpx.algotrading.marketdata.events;

import io.github.axpx.algotrading.model.Side;

public class MarketDataEvent {

    private EventType eventType;
    private String venue;
    private String symbol;
    private long timestamp;

    // Quote specific
    private double bid;
    private double ask;
    private long bidSize;
    private long askSize;

    // Trade specific
    private double price;
    private long size;
    private Side side;
    private long tradeId;

    // Disruptor sequence number
    private long sequenceId;

    public MarketDataEvent(){
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    public void setBidSize(long bidSize) {
        this.bidSize = bidSize;
    }

    public void setAskSize(long askSize) {
        this.askSize = askSize;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getVenue() {
        return venue;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getBid() {
        return bid;
    }

    public double getAsk() {
        return ask;
    }

    public long getBidSize() {
        return bidSize;
    }

    public long getAskSize() {
        return askSize;
    }

    public double getPrice() {
        return price;
    }

    public long getSize() {
        return size;
    }

    public Side getSide() {
        return side;
    }

    public long getTradeId() {
        return tradeId;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public void clear() {
        this.eventType = null;
        this.venue = null;
        this.symbol = null;
        this.timestamp = 0;
        // Quote fields
        this.bid = 0.0;
        this.ask = 0.0;
        this.bidSize = 0;
        this.askSize = 0;
        // Trade fields
        this.price = 0.0;
        this.size = 0;
        this.side = null;
        this.tradeId = 0;
        // Note: sequenceId is NOT cleared - managed by Disruptor
    }

    public boolean isQuote() {
        return eventType == EventType.QUOTE;
    }

    public boolean isTrade() {
        return eventType == EventType.TRADE;
    }

    @Override
    public String toString() {
        if (eventType == EventType.QUOTE) {
            return String.format("Quote[%s@%s: bid=%.2f@%d, ask=%.2f@%d, seq=%d]",
                    symbol, venue, bid, bidSize, ask, askSize, sequenceId);
        } else if (eventType == EventType.TRADE) {
            return String.format("Trade[%s@%s: %s %d@%.2f, id=%d, seq=%d]",
                    symbol, venue, side, size, price, tradeId, sequenceId);
        } else {
            return "MarketDataEvent[empty]";
        }
    }
}
