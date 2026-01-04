package io.github.axpx.algotrading.marketdata.handlers;

import com.lmax.disruptor.EventHandler;
import io.github.axpx.algotrading.marketdata.events.MarketDataEvent;

public abstract class MarketDataEventHandler implements EventHandler<MarketDataEvent> {

    @Override
    public void onEvent(MarketDataEvent event, long sequence, boolean endOfBatch) throws Exception {

    }
}
