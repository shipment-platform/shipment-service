package com.danijelsudimac.shipmentservice.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMetrics {

    private static final String SHIPMENT_OUTBOX_COUNTER_NAME = "shipment_added_in_outbox";
    private static final String SHIPMENT_OUTBOX_COUNTER_DESCRIPTION = "Added outbox shipments";
    private static final String SHIPMENT_PUBLISHED_COUNTER_NAME = "shipment_published";
    private static final String SHIPMENT_PUBLISHED_COUNTER_DESCRIPTION = "Published shipments";
    private static final String SHIPMENT_PUBLISHING_FAILED_COUNTER_NAME = "shipment_publishing_failed";
    private static final String SHIPMENT_PUBLISHING_FAILED_COUNTER_DESCRIPTION = "Shipments failed publishing";
    private final Counter shipmentOutboxCounter;
    private final Counter shipmentPublishedCounter;
    private final Counter shipmentPublishingFailedCounter;

    public ShipmentMetrics(MeterRegistry registry) {
        this.shipmentOutboxCounter =
                Counter.builder(SHIPMENT_OUTBOX_COUNTER_NAME)
                        .description(SHIPMENT_OUTBOX_COUNTER_DESCRIPTION)
                        .register(registry);
        this.shipmentPublishedCounter =
                Counter.builder(SHIPMENT_PUBLISHED_COUNTER_NAME)
                        .description(SHIPMENT_PUBLISHED_COUNTER_DESCRIPTION)
                        .register(registry);
        this.shipmentPublishingFailedCounter =
                Counter.builder(SHIPMENT_PUBLISHING_FAILED_COUNTER_NAME)
                        .description(SHIPMENT_PUBLISHING_FAILED_COUNTER_DESCRIPTION)
                        .register(registry);
    }

    public void incrementOutbox() {
        shipmentOutboxCounter.increment();
    }

    public void incrementPublished() {
        shipmentPublishedCounter.increment();
    }

    public void incrementFailed(){shipmentPublishingFailedCounter.increment(); }
}
