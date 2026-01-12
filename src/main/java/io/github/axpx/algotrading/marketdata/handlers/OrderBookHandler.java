package io.github.axpx.algotrading.marketdata.handlers;

import com.lmax.disruptor.EventHandler;
import io.github.axpx.algotrading.marketdata.events.MarketDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class OrderBookHandler implements EventHandler<MarketDataEvent> {

    private static final Logger logger = LoggerFactory.getLogger(OrderBookHandler.class);
    
    private final Map <String, Map<String, VenueQuote>> orderBooks = new HashMap<>();

    @Override
    public void onEvent(MarketDataEvent event, long sequence, boolean endOfBatch) throws Exception {
        if (event.isQuote()) {
            updateOrderBook(event);
        }
        if (endOfBatch) {
            for (String symbol : orderBooks.keySet()) {
                ConsolidatedQuote consolidated = calculateBestQuote(symbol);
                if (consolidated != null) {
                    logger.info("Best quote for {}: bid={}@{}, ask={}@{}, spread={}",
                            symbol,
                            consolidated.bestBid(), consolidated.bestBidVenue(),
                            consolidated.bestAsk(), consolidated.bestAskVenue(),
                            consolidated.spread());
                }
            }
        }

    }

    private ConsolidatedQuote calculateBestQuote(String symbol) {
        Map<String, VenueQuote> venueQuotes = orderBooks.get(symbol);

        if (venueQuotes == null || venueQuotes.isEmpty()) {
            logger.debug("No quotes available for symbol: {}", symbol);
            return null;
        }

        VenueQuote bestBidQuote = null;
        for (VenueQuote quote : venueQuotes.values()) {
            if (bestBidQuote == null || quote.bid() > bestBidQuote.bid()) {
                bestBidQuote = quote;
            }
        }

        VenueQuote bestAskQuote = null;
        for (VenueQuote quote : venueQuotes.values()) {
            if (bestAskQuote == null || quote.ask() < bestAskQuote.ask()) {
                bestAskQuote = quote;
            }
        }

        return new ConsolidatedQuote(
                symbol,
                bestBidQuote.bid(),
                bestBidQuote.venue(),
                bestBidQuote.bidSize(),
                bestAskQuote.ask(),
                bestAskQuote.venue(),
                bestAskQuote.askSize()
        );
    }

    public ConsolidatedQuote getBestQuote(String symbol) {
        return calculateBestQuote(symbol);
    }

    public Map<String, VenueQuote> getVenueQuotes(String symbol) {
        return orderBooks.get(symbol);
    }

    private void updateOrderBook(MarketDataEvent event) {
        String symbol = event.getSymbol();
        String venue = event.getVenue();

        if (symbol == null || venue == null) {
            logger.warn("Ignoring quote with null symbol or venue");
            return;
        }

        if (event.getAsk() <= event.getBid()) {
            logger.warn("Ignoring invalid quote: ask {} <= bid {} for {}/{}",
                    event.getAsk(), event.getBid(), venue, symbol);
            return;
        }

        VenueQuote venueQuote = new VenueQuote(
                venue,
                event.getBid(),
                event.getBidSize(),
                event.getAsk(),
                event.getAskSize(),
                event.getTimestamp()
        );

        orderBooks.computeIfAbsent(
                symbol, k -> new HashMap<>()
        ).put(venue, venueQuote);
    }
}
