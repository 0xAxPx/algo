package io.github.axpx.algotrading.strategies;

public record Signal(
        SignalType type,
        String symbol,
        String venue,
        double price,
        long size,
        String reason,
        long timestamp
) {
    public Signal {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be null");
        }
        if (venue == null || venue.isBlank()) {
            throw new IllegalArgumentException("Venue cannot be null");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s @ %s: price=%.5f, size=%d [%s]",
                type, symbol, venue, price, size, reason);
    }
}
