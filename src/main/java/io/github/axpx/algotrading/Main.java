package io.github.axpx.algotrading;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import io.github.axpx.algotrading.marketdata.DisruptorEngine;
import io.github.axpx.algotrading.marketdata.handlers.ConsolidatedQuote;
import io.github.axpx.algotrading.marketdata.handlers.LoggingMarketDataHandler;
import io.github.axpx.algotrading.marketdata.handlers.OrderBookHandler;
import io.github.axpx.algotrading.marketdata.handlers.SpreadArbitrageStrategy;
import io.github.axpx.algotrading.metrics.MetricsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting Algo Trading Platform...");
        MetricsServer.start();

        // Handlers
        LoggingMarketDataHandler loggingHandler = new LoggingMarketDataHandler();
        OrderBookHandler orderBookHandler = new OrderBookHandler();

        // Strategy
        SpreadArbitrageStrategy arbitrageStrategy = new SpreadArbitrageStrategy(
                orderBookHandler,
                "EURUSD",
                0.00005,
                0.0001,
                100_000
        );

        // Create Disruptor with all handlers
        DisruptorEngine disruptorEngine = new DisruptorEngine(
                1024,
                new YieldingWaitStrategy(),
                ProducerType.MULTI,
                loggingHandler,
                orderBookHandler,
                arbitrageStrategy
        );

        disruptorEngine.start();

        // Test Case 1: Create arbitrage opportunity
        logger.info("=== TEST CASE 1: Arbitrage Opportunity ===");

        disruptorEngine.publishQuote("LMAX", "EURUSD",
                1.1000, 1_000_000,
                1.1005, 500_000);

        disruptorEngine.publishQuote("IEX", "EURUSD",
                1.1010, 800_000,
                1.1015, 600_000);

        disruptorEngine.publishQuote("NASDAQ", "EURUSD",
                1.1003, 500_000,
                1.1008, 400_000);

        Thread.sleep(1000);

        // Test Case 2: No arbitrage (normal market)
        logger.info("=== TEST CASE 2: No Arbitrage (Normal Market) ===");

        disruptorEngine.publishQuote("LMAX", "EURUSD",
                1.1000, 1_000_000,
                1.1005, 500_000);

        disruptorEngine.publishQuote("IEX", "EURUSD",
                1.1001, 800_000,
                1.1006, 600_000);

        Thread.sleep(1000);

        // Test Case 3: Insufficient liquidity
        logger.info("=== TEST CASE 3: Insufficient Liquidity ===");

        disruptorEngine.publishQuote("LMAX", "EURUSD",
                1.1000, 50_000,
                1.1005, 30_000);

        disruptorEngine.publishQuote("IEX", "EURUSD",
                1.1010, 40_000,
                1.1015, 20_000);

        Thread.sleep(1000);

        disruptorEngine.shutdown();
        logger.info("Platform shutdown complete");
    }
}