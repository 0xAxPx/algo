package io.github.axpx.algotrading.marketdata.handlers;

public record VenueQuote(
        String venue,
        double bid,
        long bidSize,
        double ask,
        long askSize,
        long timestamp
) {

    public VenueQuote {
        if (venue == null || venue.isBlank()) {
            throw new IllegalArgumentException("Venue cannot be null");
        }
        if (ask <= bid) {
            throw new IllegalArgumentException("Ask must be greater than bid");
        }
    }

}
