package io.github.axpx.algotrading;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import io.github.axpx.algotrading.marketdata.DisruptorEngine;
import io.github.axpx.algotrading.marketdata.handlers.ConsolidatedQuote;
import io.github.axpx.algotrading.marketdata.handlers.LoggingMarketDataHandler;
import io.github.axpx.algotrading.marketdata.handlers.OrderBookHandler;
import io.github.axpx.algotrading.metrics.MetricsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        logger.info("Starting Algo Trading Platform...");
        MetricsServer.start();

        LoggingMarketDataHandler loggingMarketDataHandler = new LoggingMarketDataHandler();
        OrderBookHandler orderBookHandler = new OrderBookHandler();
        DisruptorEngine disruptorEngine = new DisruptorEngine(
                1024,
                new YieldingWaitStrategy(),
                ProducerType.MULTI,
                loggingMarketDataHandler, orderBookHandler
        );

        disruptorEngine.start();

        // Warm-up phase
        logger.info("Warming up JVM...");
        for (int i = 0; i < 1000; i++) {
            disruptorEngine.publishQuote("LMAX", "EURUSD",
                    1.1000, 1_000_000, 1.1005, 500_000);
        }
        Thread.sleep(500);

        // Actual test
        logger.info("Starting performance test...");
        long startTime = System.nanoTime();

        int events = 100_000;

        logger.info("Starting multi-venue test...");

        String[] venues = {"LMAX", "IEX", "NASDAQ"};
        String[] symbols = {"EURUSD", "GBPUSD", "AAPL"};

        for (int i = 0; i < 30; i++) {
            String venue = venues[i % venues.length];
            String symbol = symbols[i % symbols.length];

            double basePrice = symbol.equals("AAPL") ? 150.0 : 1.1000;
            double venueOffset = (i % 3) * 0.0001;

            disruptorEngine.publishQuote(
                    venue,
                    symbol,
                    basePrice + venueOffset,
                    1_000_000,
                    basePrice + venueOffset + 0.0005,
                    500_000
            );
        }

        for (String symbol : symbols) {
            ConsolidatedQuote best = orderBookHandler.getBestQuote(symbol);
            if (best != null) {
                logger.info("=== {} ===", symbol);
                logger.info("  Best Bid: {} @ {} ({})",
                        best.bestBid(), best.bestBidVenue(), best.bestBidSize());
                logger.info("  Best Ask: {} @ {} ({})",
                        best.bestAsk(), best.bestAskVenue(), best.bestAskSize());
                logger.info("  Spread: {}, Mid: {}", best.spread(), best.midPrice());
            }
        }

        long elapsed = System.nanoTime() - startTime;
        double throughput = events / (elapsed / 1_000_000_000.0);

        logger.info("Published {} events in {} ms", events, elapsed / 1_000_000);
        logger.info("Throughput: {} events/second", throughput);

        Thread.sleep(2000);  // Let processing complete

        logger.info("Ring buffer remaining capacity: {}",
                disruptorEngine.getRemainingCapacity());

        disruptorEngine.shutdown();
        logger.info("Platform shutdown complete");
    }}