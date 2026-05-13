package com.danijelsudimac.shipmentservice.mapper;

import com.danijelsudimac.shipmentservice.model.dto.ShipmentUpdatedDto;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.dto.ShipmentCreatedDto;
import com.danijelsudimac.shipmentservice.model.event.ShipmentUpdatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = { java.time.Instant.class })
public interface ShipmentMapper {
    @Mapping(target = "clientId", source = "clientId")
    @Mapping(target = "eventTimestamp", expression = "java(Instant.now())")
    ShipmentCreatedEvent toShipmentCreatedEvent(ShipmentCreatedDto shipmentCreatedDto, Long clientId);
    @Mapping(target = "clientId", source = "clientId")
    @Mapping(target = "eventTimestamp", expression = "java(Instant.now())")
    ShipmentUpdatedEvent toShipmentUpdatedEvent(ShipmentUpdatedDto shipmentCreateDto, Long clientId);
}
