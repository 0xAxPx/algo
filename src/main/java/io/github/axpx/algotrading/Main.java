package io.github.axpx.algotrading;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import io.github.axpx.algotrading.marketdata.DisruptorEngine;
import io.github.axpx.algotrading.marketdata.handlers.LoggingMarketDataHandler;
import io.github.axpx.algotrading.metrics.MetricsServer;
import io.github.axpx.algotrading.model.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {

        logger.info("Starting Algo Trading Platform...");
        MetricsServer.start();

        LoggingMarketDataHandler handler = new LoggingMarketDataHandler();
        DisruptorEngine disruptorEngine = new DisruptorEngine(
                1024,
                new YieldingWaitStrategy(),
                ProducerType.MULTI,
                handler
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

        for (int i = 0; i < 100_000; i++) {
            disruptorEngine.publishQuote("LMAX", "EURUSD",
                    1.1000 + (i * 0.0001), 1_000_000,
                    1.1005 + (i * 0.0001), 500_000);
        }

        long elapsed = System.nanoTime() - startTime;
        double throughput = 100_000.0 / (elapsed / 1_000_000_000.0);

        logger.info("Published 100,000 events in {} ms", elapsed / 1_000_000);
        logger.info("Throughput: {} events/second", throughput);

        Thread.sleep(2000);  // Let processing complete

        logger.info("Ring buffer remaining capacity: {}",
                disruptorEngine.getRemainingCapacity());

        disruptorEngine.shutdown();
        logger.info("Platform shutdown complete");
    }}