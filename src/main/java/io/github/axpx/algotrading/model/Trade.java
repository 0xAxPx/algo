package io.github.axpx.algotrading.model;

/**
 * Immutable snapshot of an executed market trade
 *
 * @param symbol    Trading symbol (e.g., "AAPL", "EURUSD")
 * @param price     Trading price
 * @param size      Trading size
 * @param side      Side of trade (buy, sell)
 * @param tradeId   Unique ID of trade
 * @param timestamp Event timestamp in nanoseconds (System.nanoTime())
        */
public record Trade(
        String symbol,
        double price,
        long size,
        Side side,
        long tradeId,
        long timestamp
){
    public Trade {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (side == null) {
            throw new IllegalArgumentException("Side cannot be null");
        }
        if (price < 0 || price == 0.0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (size < 0 || size == 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
        if (tradeId <= 0) {
            throw new IllegalArgumentException("TradeId must be positive");
        }
    }

    @Override
    public String toString() {
        return String.format("%s: %s %d@%.2f [id=%d]",
                symbol, side, size, price, tradeId);
    }


}
