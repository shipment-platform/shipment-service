package com.danijelsudimac.shipmentservice.service;

import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.outbox.OutboxEventType;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ShipmentServiceTest {

    private final OutboxEventRepository repository =  mock(OutboxEventRepository.class);
    private final ShipmentMetrics metrics = mock(ShipmentMetrics.class);

    private ShipmentService service;

    @Test
    void shouldProcessCreateEvent() throws Exception {
        var serializator = new AvroSerializator();
        service = new ShipmentService(repository, metrics, serializator);

        ShipmentCreatedEvent event = new ShipmentCreatedEvent();
        event.setExternalId("ext-1");

        service.processEvent(event);
        verify(repository).save(argThat(outbox ->
                outbox.getEventType() == OutboxEventType.CREATE_SHIPMENT &&
                        outbox.getAggregateId().equals("ext-1") &&
                        outbox.getTopic().equals("shipment-ingest-topic.v1") &&
                        !outbox.getPublished()
        ));
        verify(metrics).incrementOutbox();
    }

    @Test
    void shouldThrowWhenSerializationFails() throws Exception {
        var serializator = mock(PayloadSerializator.class);
        service = new ShipmentService(repository, metrics, serializator);

        ShipmentCreatedEvent event = new ShipmentCreatedEvent();
        event.setExternalId("ext-err");

        when(serializator.serialize(event)).thenThrow(new IOException("fail"));
        assertThrows(IOException.class, () -> service.processEvent(event));

        verify(repository, never()).save(any());
        verify(metrics, never()).incrementOutbox();
    }
}
