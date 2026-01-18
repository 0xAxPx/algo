package io.github.axpx.algotrading.marketdata.handlers;

import com.lmax.disruptor.EventHandler;
import io.github.axpx.algotrading.marketdata.events.MarketDataEvent;
import io.github.axpx.algotrading.metrics.MetricsRegistry;
import io.github.axpx.algotrading.strategies.Signal;
import io.github.axpx.algotrading.strategies.SignalType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SpreadArbitrageStrategy implements EventHandler<MarketDataEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SpreadArbitrageStrategy.class);

    // Configuration
    private final double minProfitThreshold;
    private final double feePerSide;


    // Dependency
    private final OrderBookHandler orderBookHandler;
    private final String symbol;
    private final long tradeSize;


    // Metrics
    private Counter opportunitiesDetected;
    private  Counter signalsGenerated;


    public SpreadArbitrageStrategy(
            OrderBookHandler orderBook,
            String symbol,
            double minProfitThreshold,
            double feePerSide,
            long tradeSize
    ) {
        this.orderBookHandler = orderBook;
        this.symbol = symbol;
        this.minProfitThreshold = minProfitThreshold;
        this.feePerSide = feePerSide;
        this.tradeSize = tradeSize;

        MeterRegistry registry = MetricsRegistry.getRegistry();
        this.opportunitiesDetected = registry.counter(
                "strategy.arbitrage.opportunities.detected",
                "symbol", symbol
        );
        this.signalsGenerated = registry.counter(
                "strategy.arbitrage.signals.generated",
                "symbol", symbol
        );
    }

    @Override
    public void onEvent(MarketDataEvent event, long sequence, boolean endOfBatch) throws Exception {
        ConsolidatedQuote bestQuote;
          if (endOfBatch) {
              bestQuote = orderBookHandler.getBestQuote(this.symbol);
              if (isArbitrageOpportunity(bestQuote)) {
                  generateArbitrageSignal(bestQuote);
              }

          }


    }

    private boolean isArbitrageOpportunity(ConsolidatedQuote quote) {
        if (quote == null) return false;
        double grossProfit = quote.bestBid() - quote.bestAsk();
        double netProfit = grossProfit - (feePerSide * 2);
        return netProfit > this.minProfitThreshold;

    }

    private void generateArbitrageSignal(ConsolidatedQuote quote) {
        long availableSize = Math.min(quote.bestBidSize(), quote.bestAskSize());

        if (availableSize < tradeSize) {
            logger.warn("⚠️  Insufficient liquidity: wanted {}, available {}",
                    tradeSize, availableSize);
            return;
        }


        double grossProfit = quote.bestBid() - quote.bestAsk();
        double netProfit = grossProfit - (feePerSide * 2);
        logger.info("🎯 ARBITRAGE OPPORTUNITY DETECTED!");
        logger.info("   Symbol: {}", quote.symbol());
        logger.info("   Buy  @ {} : {}", quote.bestAskVenue(), quote.bestAsk());
        logger.info("   Sell @ {} : {}", quote.bestBidVenue(), quote.bestBid());
        logger.info("   Gross Profit: {}", grossProfit);
        logger.info("   Fees: {}", feePerSide * 2);
        logger.info("   Net Profit: {}", netProfit);

        // Generate BUY signal - buy at lowest ask price
        Signal buySignal = new Signal(
                SignalType.BUY,
                quote.symbol(),
                quote.bestAskVenue(),
                quote.bestAsk(),
                this.tradeSize,
                String.format("Arbitrage buy: net profit %.5f", netProfit),
                System.nanoTime()
        );

        Signal sellSignal = new Signal(
                SignalType.SELL,
                quote.symbol(),
                quote.bestBidVenue(),
                quote.bestBid(),
                this.tradeSize,
                String.format("Arbitrage sell: net profit %.5f", netProfit),
                System.nanoTime()
        );

        logger.info("📊 SIGNAL GENERATED: {}", buySignal);
        logger.info("📊 SIGNAL GENERATED: {}", sellSignal);

        // Update metrics
        if (opportunitiesDetected != null) {
            opportunitiesDetected.increment();
        }
        if (signalsGenerated != null) {
            signalsGenerated.increment(2);
        }

        // In the future  - we will send signal to executing systems
    }
}
