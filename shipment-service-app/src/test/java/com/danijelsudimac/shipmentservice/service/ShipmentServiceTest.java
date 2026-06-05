package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.outbox.OutboxEventType;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ShipmentServiceTest {

    private final OutboxEventRepository repository =  mock(OutboxEventRepository.class);
    private final ShipmentMetrics metrics = mock(ShipmentMetrics.class);

    private final ShipmentService service = new ShipmentService(repository, metrics, "shipment-ingest");

    @Test
    void shouldProcessCreateEvent() throws Exception {

        ShipmentCreatedEvent event = ShipmentCreatedEvent.newBuilder().setExternalId("ext-1").build();

        service.processEvent(event);
        verify(repository).save(argThat(outbox ->
                outbox.getEventType() == OutboxEventType.CREATE_SHIPMENT &&
                        outbox.getAggregateId().equals("ext-1") &&
                        outbox.getTopic().equals("shipment-ingest") &&
                        !outbox.getPublished()
        ));
        verify(metrics).incrementOutbox();
    }
}
