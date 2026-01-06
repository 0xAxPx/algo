package io.github.axpx.algotrading;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import io.github.axpx.algotrading.marketdata.DisruptorEngine;
import io.github.axpx.algotrading.marketdata.handlers.LoggingMarketDataHandler;
import io.github.axpx.algotrading.model.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {

        logger.info("Starting Algo Trading Platform...");

        // Create handler
        LoggingMarketDataHandler handler = new LoggingMarketDataHandler();

        // Create DisruptorEngine with handler
        DisruptorEngine disruptorEngine = new DisruptorEngine(
                1024,
                new YieldingWaitStrategy(),  // Kinder to your CPU
                ProducerType.MULTI,
                handler  // ← PASS THE HANDLER!
        );

        disruptorEngine.start();

        logger.info("Publishing test market data...");

        // Publish some quotes
        disruptorEngine.publishQuote("LMAX", "EURUSD", 1.1000, 1_000_000, 1.1005, 500_000);
        disruptorEngine.publishQuote("IEX", "AAPL", 150.00, 1000, 150.05, 500);
        disruptorEngine.publishQuote("NASDAQ", "AAPL", 150.01, 800, 150.04, 600);

        // Publish some trades
        disruptorEngine.publishTrade("LMAX", "EURUSD", 1.1005, 500_000, Side.BUY, 1001);
        disruptorEngine.publishTrade("IEX", "AAPL", 150.05, 200, Side.BUY, 1002);
        disruptorEngine.publishTrade("NASDAQ", "AAPL", 150.04, 300, Side.SELL, 1003);

        // More quotes
        disruptorEngine.publishQuote("LMAX", "EURUSD", 1.1005, 900_000, 1.1010, 400_000);
        disruptorEngine.publishQuote("IEX", "AAPL", 150.02, 900, 150.06, 450);

        logger.info("Finished publishing. Waiting for processing...");

        // Give handlers time to process
        Thread.sleep(1000);

        // Shutdown cleanly
        disruptorEngine.shutdown();

        logger.info("Platform shutdown complete");
    }
}