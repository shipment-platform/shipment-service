package com.danijelsudimac.shipmentservice.controller;

import com.danijelsudimac.shipmentservice.mapper.ShipmentMapper;
import com.danijelsudimac.shipmentservice.model.dto.ShipmentCreatedDto;
import com.danijelsudimac.shipmentservice.model.dto.ShipmentUpdatedDto;
import com.danijelsudimac.shipmentservice.model.event.ShipmentDeletedEvent;
import com.danijelsudimac.shipmentservice.security.ApiKeyAuthenticationToken;
import com.danijelsudimac.shipmentservice.service.IdempotencyService;
import com.danijelsudimac.shipmentservice.service.ShipmentEventPublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final IdempotencyService idempotencyService;
    private final ShipmentEventPublisher shipmentEventPublisher;
    private final ShipmentMapper shipmentMapper;
    private static final String SHIPMENT_INGEST_TOPIC = "shipment-ingest-topic";

    public ShipmentController(IdempotencyService idempotencyService, ShipmentEventPublisher shipmentEventPublisher,
                              ShipmentMapper shipmentMapper) {
        this.idempotencyService = idempotencyService;
        this.shipmentEventPublisher = shipmentEventPublisher;
        this.shipmentMapper = shipmentMapper;
    }

    @PostMapping
    public ResponseEntity<ShipmentCreatedDto> createShipment(@Valid @RequestBody ShipmentCreatedDto shipmentCreatedDto,
                                                             Authentication authentication) {
        var clientId = ((ApiKeyAuthenticationToken)authentication).getClientId();
        if (idempotencyService.shouldDenyRequest(shipmentCreatedDto.idempotencyKey(), clientId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        var shipmentCreateEvent = shipmentMapper.toShipmentCreatedEvent(shipmentCreatedDto, clientId);
        shipmentEventPublisher.publishShipmentCreateEvent(SHIPMENT_INGEST_TOPIC, shipmentCreateEvent);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(shipmentCreatedDto);
    }

    @PatchMapping("/{externalId}")
    public ResponseEntity<ShipmentCreatedDto> updateShipment(@Valid @RequestBody ShipmentUpdatedDto updateShipmentDto,
                                                             Authentication authentication) {
        var clientId = ((ApiKeyAuthenticationToken)authentication).getClientId();
        if (idempotencyService.shouldDenyRequest(updateShipmentDto.idempotencyKey(),clientId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        var shipmentUpdateEvent = shipmentMapper.toShipmentUpdatedEvent(updateShipmentDto, clientId);
        shipmentEventPublisher.publishShipmentUpdateEvent(SHIPMENT_INGEST_TOPIC, shipmentUpdateEvent);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{externalId}")
    public ResponseEntity<ShipmentCreatedDto> updateShipment(@PathVariable String externalId,
                                                             Authentication authentication) {
        var clientId = ((ApiKeyAuthenticationToken)authentication).getClientId();
        if (idempotencyService.shouldDenyRequest((String)authentication.getCredentials(),clientId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        shipmentEventPublisher.publishShipmentDeleteEvent(SHIPMENT_INGEST_TOPIC, new ShipmentDeletedEvent(clientId,
                externalId, Instant.now()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
