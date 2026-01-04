package io.github.axpx.algotrading.marketdata;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.github.axpx.algotrading.marketdata.events.EventType;
import io.github.axpx.algotrading.marketdata.events.MarketDataEvent;
import io.github.axpx.algotrading.model.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DisruptorEngine {

    private static final Logger logger = LoggerFactory.getLogger(DisruptorEngine.class);


    private int bufferSize;
    private WaitStrategy waitStrategy;
    private ProducerType producerType;
    private EventHandler<MarketDataEvent>[] marketDataHandlers;
    private Disruptor<MarketDataEvent> disruptor;
    private RingBuffer<MarketDataEvent> ringBuffer;

    @SafeVarargs
    public DisruptorEngine(
            int bufferSize,
            WaitStrategy waitStrategy,
            ProducerType producerType,
            EventHandler<MarketDataEvent> ...handlers
    ) {
        if (!validateBufferSize(bufferSize)) {
            throw new IllegalArgumentException(
                    "Buffer size must be a power of 2, got: " + bufferSize
            );
        }
        this.bufferSize = bufferSize;
        this.waitStrategy = waitStrategy;
        this.producerType = producerType;
        this.marketDataHandlers = handlers;
    }

    public void start() {
        this.disruptor = new Disruptor<>(
                MarketDataEvent::new,
                bufferSize,
                DaemonThreadFactory.INSTANCE,
                producerType,
                waitStrategy
        );
        if (marketDataHandlers != null && marketDataHandlers.length > 0) {
            disruptor.handleEventsWith(marketDataHandlers);
        }

        disruptor.setDefaultExceptionHandler(new ExceptionHandler<MarketDataEvent>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, MarketDataEvent event) {
                logger.error("Error processing event " + event + " at sequence " + sequence + ": " + ex);
                // Don't rethrow - keep Disruptor running
            }

            @Override
            public void handleOnStartException(Throwable ex) {
                logger.error("Error starting handler: " + ex);
            }

            @Override
            public void handleOnShutdownException(Throwable ex) {
                logger.error("Error shutting down handler: " + ex);
            }
        });

        this.ringBuffer = disruptor.start();
    }

    public void shutdown() {
        logger.info("Shutting down DisruptorEngine...");
        this.disruptor.shutdown();
        logger.info("DisruptorEngine shut down");
    }

    public void publishQuote(String venue, String symbol, double bid, long bidSize, double ask, long askSize) {
        long sequence = ringBuffer.next();
        try {
            MarketDataEvent event = ringBuffer.get(sequence);
            event.clear();
            event.setEventType(EventType.QUOTE);
            event.setVenue(venue);
            event.setSymbol(symbol);
            event.setBid(bid);
            event.setAsk(ask);
            event.setBidSize(bidSize);
            event.setAskSize(askSize);
            event.setTimestamp(System.nanoTime());
        } finally {
            ringBuffer.publish(sequence);
        }
    }
    public void publishTrade(String venue, String symbol, double price, long size, Side side, long tradeID) {
        long sequence = ringBuffer.next();
        try {
            MarketDataEvent event = ringBuffer.get(sequence);
            event.clear();
            event.setEventType(EventType.TRADE);
            event.setVenue(venue);
            event.setSymbol(symbol);
            event.setPrice(price);
            event.setSize(size);
            event.setSide(side);
            event.setTradeId(tradeID);
            event.setTimestamp(System.nanoTime());
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    private boolean validateBufferSize(int bufferSize) {
        return (bufferSize != 0) && ((bufferSize & (bufferSize -1)) == 0);
    }

    // Utility methods
    public long getRemainingCapacity() {
        return ringBuffer.remainingCapacity();
    }

    public long getBufferSize() {
        return ringBuffer.getBufferSize();
    }

}
