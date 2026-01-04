package io.github.axpx.algotrading.model;

/**
 * Immutable snapshot of best bid/offer (BBO) market data
 *
 * @param symbol    Trading symbol (e.g., "AAPL", "EURUSD")
 * @param bid       Best bid price
 * @param bidSize   Size available at bid
 * @param ask       Best ask price
 * @param askSize   Size available at ask
 * @param timestamp Event timestamp in nanoseconds (System.nanoTime())
 */

public record Quote(
        String symbol,
        double bid,
        long bidSize,
        double ask,
        long askSize,
        long timestamp
) {

    public Quote {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (bid <= 0 || ask <= 0) {
            throw new IllegalArgumentException("Prices must be positive");
        }
        if (ask <= bid) {
            throw new IllegalArgumentException("Ask (" + ask + ") cannot be less than bid (" + bid + ")");
        }
        if (bidSize < 0 || askSize < 0) {
            throw new IllegalArgumentException("Sizes cannot be negative");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
    }

    public double spread() {
        return ask - bid;
    }

    public double midPrice() {
        return (bid + ask) / 2.0;
    }

    @Override
    public String toString() {
        return String.format("%s: bid=%.2f@%d, ask=%.2f@%d, spread=%.4f",
                symbol, bid, bidSize, ask, askSize, spread());
    }

}
