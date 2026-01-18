package io.github.axpx.algotrading.strategies;

import io.github.axpx.algotrading.marketdata.handlers.ConsolidatedQuote;

public interface TradingStrategy {

    Signal evaluate(ConsolidatedQuote quote);
    //void onFill(Order order);

}
