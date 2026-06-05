package com.danijelsudimac.shipmentservice.mapper;

import com.danijelsudimac.shipmentservice.model.dto.ShipmentStatus;
import com.danijelsudimac.shipmentservice.model.dto.ShipmentUpdatedDto;
import com.danijelsudimac.shipmentservice.model.dto.ShippingMethod;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.dto.ShipmentCreatedDto;
import com.danijelsudimac.shipmentservice.model.event.ShipmentUpdatedEvent;
import org.mapstruct.*;

import java.time.Instant;

@Mapper(componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE)
public interface ShipmentMapper {

    default Long map(Instant instant) {
        return instant != null ? instant.toEpochMilli() : null;
    }

    @ValueMappings({
            @ValueMapping(source = "CREATED", target = "SHIPMENT_STATUS_CREATED"),
            @ValueMapping(source = "PENDING", target = "SHIPMENT_STATUS_PENDING"),
            @ValueMapping(source = "SHIPPED", target = "SHIPMENT_STATUS_SHIPPED"),
            @ValueMapping(source = "IN_TRANSIT", target = "SHIPMENT_STATUS_IN_TRANSIT"),
            @ValueMapping(source = "DELIVERED", target = "SHIPMENT_STATUS_DELIVERED"),
            @ValueMapping(source = "CANCELLED", target = "SHIPMENT_STATUS_CANCELLED")
    })
    com.danijelsudimac.shipmentservice.model.common.ShipmentStatus map(ShipmentStatus status);

    @ValueMappings({
            @ValueMapping(source = "STANDARD", target = "SHIPPING_METHOD_STANDARD"),
            @ValueMapping(source = "EXPRESS", target = "SHIPPING_METHOD_EXPRESS"),
            @ValueMapping(source = "OVERNIGHT", target = "SHIPPING_METHOD_OVERNIGHT")
    })
    com.danijelsudimac.shipmentservice.model.common.ShippingMethod map(ShippingMethod method);

    @Mapping(source = "shipmentCreatedDto.items", target = "itemsList")
    @Mapping(target = "eventTimestamp", expression = "java(System.currentTimeMillis())")
    ShipmentCreatedEvent toShipmentCreatedEvent(ShipmentCreatedDto shipmentCreatedDto, Long clientId);

    @Mapping(target = "eventTimestamp", expression = "java(System.currentTimeMillis())")
    ShipmentUpdatedEvent toShipmentUpdatedEvent(ShipmentUpdatedDto shipmentCreateDto, Long clientId);
}
