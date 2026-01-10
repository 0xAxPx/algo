package io.github.axpx.algotrading.metrics;

import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

public class MetricsRegistry {

    private static final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);


    static {

        // JVM Metrics
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new JvmHeapPressureMetrics().bindTo(registry);

        // System Metrics
        new ProcessorMetrics().bindTo(registry);
        new FileDescriptorMetrics().bindTo(registry);
        new UptimeMetrics().bindTo(registry);

        // Virtual Threads
        //new VirtualThreadMetrics().bindTo(registry);

    }

    public static MeterRegistry getRegistry() {
        return registry;
    }

    public static String scrape() {
        return ((PrometheusMeterRegistry) registry).scrape();
    }


}
