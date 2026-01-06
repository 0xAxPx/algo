package io.github.axpx.algotrading.marketdata.handlers;

import com.lmax.disruptor.EventHandler;
import io.github.axpx.algotrading.marketdata.events.MarketDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingMarketDataHandler implements EventHandler<MarketDataEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingMarketDataHandler.class);

    private int batchSize = 0;
    private int quoteCount = 0;
    private int tradeCount = 0;

    @Override
    public void onEvent(MarketDataEvent event, long sequence, boolean endOfBatch) {
        batchSize++;

        if (event.isQuote()) {
            quoteCount++;
        } else if (event.isTrade()) {
            tradeCount++;
        }

        if (endOfBatch) {
            logger.info("Batch complete [seq={}]: {} events ({} quotes, {} trades)",
                    sequence, batchSize, quoteCount, tradeCount);

            // Reset counters
            batchSize = 0;
            quoteCount = 0;
            tradeCount = 0;
        }
    }
}
