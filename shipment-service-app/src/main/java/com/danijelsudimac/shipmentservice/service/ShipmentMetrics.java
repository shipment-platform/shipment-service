package com.danijelsudimac.shipmentservice.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMetrics {

    private static final String SHIPMENT_OUTBOX_COUNTER_NAME = "shipment_in_outbox_total";
    private static final String SHIPMENT_OUTBOX_COUNTER_DESCRIPTION = "Total outbox shipments";
    private static final String SHIPMENT_PUBLISHED_COUNTER_NAME = "shipment_published_total";
    private static final String SHIPMENT_PUBLISHED_COUNTER_DESCRIPTION = "Total published shipments";
    private final Counter shipmentOutboxCounter;
    private final Counter shipmentPublishedCounter;

    public ShipmentMetrics(MeterRegistry registry) {
        this.shipmentOutboxCounter =
                Counter.builder(SHIPMENT_OUTBOX_COUNTER_NAME)
                        .description(SHIPMENT_OUTBOX_COUNTER_DESCRIPTION)
                        .register(registry);
        this.shipmentPublishedCounter =
                Counter.builder(SHIPMENT_PUBLISHED_COUNTER_NAME)
                        .description(SHIPMENT_PUBLISHED_COUNTER_DESCRIPTION)
                        .register(registry);
    }

    public void incrementOutbox() {
        shipmentOutboxCounter.increment();
    }

    public void incrementPublished() {
        shipmentPublishedCounter.increment();
    }
}
