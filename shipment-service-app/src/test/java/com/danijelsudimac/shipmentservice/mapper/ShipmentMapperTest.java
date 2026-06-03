package com.danijelsudimac.shipmentservice.mapper;

import com.danijelsudimac.shipmentservice.model.dto.Address;
import com.danijelsudimac.shipmentservice.model.dto.Item;
import com.danijelsudimac.shipmentservice.model.dto.ShipmentStatus;
import com.danijelsudimac.shipmentservice.model.dto.ShippingMethod;
import com.danijelsudimac.shipmentservice.model.dto.ShipmentCreatedDto;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentMapperTest {

    private final ShipmentMapper mapper = Mappers.getMapper(ShipmentMapper.class);

    @Test
    void shouldMapShipmentCreatedDtoToShipmentCreatedEvent() {
        Long clientId = 123L;
        var recipientAddress = new Address("Main Street 1", "Novi Sad", "Serbia", "21000", "");
        var originAddress = new Address("Warehouse Street 10", "Belgrade", "Serbia", "11000", "");

        Item item = new Item("Laptop", 1, "piece", 2.5);

        Instant createdAt = Instant.now();
        Instant estimatedPickup = Instant.now().plusSeconds(3600);
        Instant estimatedDelivery = Instant.now().plusSeconds(86400);

        ShipmentCreatedDto dto = new ShipmentCreatedDto(
                "idem-123",
                "ext-123",
                "track-123",
                "order-123",
                ShipmentStatus.CREATED,
                "DHL",
                ShippingMethod.EXPRESS,
                "John Doe",
                recipientAddress,
                "+381641234567",
                "john@example.com",
                "Warehouse",
                originAddress,
                "+381641111111",
                "warehouse@example.com",
                List.of(item),
                estimatedPickup,
                estimatedDelivery,
                createdAt
        );

        Instant beforeMapping = Instant.now();

        ShipmentCreatedEvent event =
                mapper.toShipmentCreatedEvent(dto, clientId);

        Instant afterMapping = Instant.now();

        assertNotNull(event);

        assertEquals(dto.idempotencyKey(), event.getIdempotencyKey());
        assertEquals(dto.externalId(), event.getExternalId());
        assertEquals(dto.trackingNumber(), event.getTrackingNumber());
        assertEquals(dto.orderId(), event.getOrderId());
        assertEquals(dto.status().name(), event.getStatus().name());
        assertEquals(dto.carrier(), event.getCarrier());
        assertEquals(dto.shippingMethod().name(), event.getShippingMethod().name());

        assertEquals(dto.recipientName(), event.getRecipientName());
        assertTrue(addressesAreEqual(dto.recipientAddress(), event.getRecipientAddress()));
        assertEquals(dto.recipientPhoneNumber(), event.getRecipientPhoneNumber());
        assertEquals(dto.recipientEmail(), event.getRecipientEmail());

        assertEquals(dto.originName(), event.getOriginName());
        assertTrue(addressesAreEqual(dto.originAddress(), event.getOriginAddress()));
        assertEquals(dto.originPhoneNumber(), event.getOriginPhoneNumber());
        assertEquals(dto.originEmail(), event.getOriginEmail());

        assertTrue(itemsAreEqual(dto.items(), event.getItems()));

        assertEquals(dto.estimatedPickup(), event.getEstimatedPickup());
        assertEquals(dto.estimatedDelivery(), event.getEstimatedDelivery());
        assertEquals(dto.createdAt(), event.getCreatedAt());

        assertEquals(clientId, event.getClientId());

        // eventTimestamp generated with Instant.now()
        assertNotNull(event.getEventTimestamp());
        assertFalse(event.getEventTimestamp().isBefore(beforeMapping));
        assertFalse(event.getEventTimestamp().isAfter(afterMapping));
    }

    private boolean itemsAreEqual(List<Item> items, List<com.danijelsudimac.shipmentservice.model.common.Item> items1) {
        if (items.size() != items1.size()) {
            return false;
        }
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            com.danijelsudimac.shipmentservice.model.common.Item item1 = items1.get(i);
            if (!item.name().equals(item1.getName()) ||
                    !Objects.equals(item.quantity(), item1.getQuantity()) ||
                    !item.unit().equals(item1.getUnit()) ||
                    !Objects.equals(item.weight(), item1.getWeight())) {
                return false;
            }
        }
        return true;
    }

    private boolean addressesAreEqual(Address address1, com.danijelsudimac.shipmentservice.model.common.Address address2) {
        return address1.addressLine().equals(address2.getAddressLine()) &&
                address1.city().equals(address2.getCity()) &&
                address1.country().equals(address2.getCountry()) &&
                address1.postalCode().equals(address2.getPostalCode()) &&
                ((address1.state() == null && address2.getState() == null) ||
                        (address1.state() != null && address1.state().equals(address2.getState())));
    }
}
