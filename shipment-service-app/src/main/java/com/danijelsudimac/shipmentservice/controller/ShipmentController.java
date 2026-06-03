package com.danijelsudimac.shipmentservice.controller;

import com.danijelsudimac.shipmentservice.mapper.ShipmentMapper;
import com.danijelsudimac.shipmentservice.model.dto.ShipmentCreatedDto;
import com.danijelsudimac.shipmentservice.model.dto.ShipmentUpdatedDto;
import com.danijelsudimac.shipmentservice.model.event.ShipmentDeletedEvent;
import com.danijelsudimac.shipmentservice.service.IdempotencyService;
import com.danijelsudimac.shipmentservice.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;

@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
@Slf4j
public class ShipmentController {

    private static final String DUPLICATE_REQUEST_MESSAGE = "Duplicate request detected for clientId: {} with idempotencyKey: {}";
    private static final String PROCESSING_REQUEST_MESSAGE = "Processing shipment creation request for clientId: {} with idempotencyKey: {}";

    private final IdempotencyService idempotencyService;
    private final ShipmentService shipmentService;
    private final ShipmentMapper shipmentMapper;

    @PostMapping
    public ResponseEntity<ShipmentCreatedDto> createShipment(@Valid @RequestBody ShipmentCreatedDto shipmentCreatedDto,
                                                             @AuthenticationPrincipal Long clientId) throws IOException {
        if (idempotencyService.shouldDenyRequest(shipmentCreatedDto.idempotencyKey(), clientId)) {
            log.warn(DUPLICATE_REQUEST_MESSAGE, clientId, shipmentCreatedDto.idempotencyKey());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        log.info(PROCESSING_REQUEST_MESSAGE, clientId, shipmentCreatedDto.idempotencyKey());
        var shipmentCreateEvent = shipmentMapper.toShipmentCreatedEvent(shipmentCreatedDto, clientId);
        shipmentService.processEvent
                (shipmentCreateEvent);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(shipmentCreatedDto);
    }

    @PatchMapping
    public ResponseEntity<ShipmentCreatedDto> updateShipment(@Valid @RequestBody ShipmentUpdatedDto updateShipmentDto,
                                                             @AuthenticationPrincipal Long clientId) throws IOException {
        var shipmentUpdateEvent = shipmentMapper.toShipmentUpdatedEvent(updateShipmentDto, clientId);
        shipmentService.processEvent(shipmentUpdateEvent);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{externalId}")
    public ResponseEntity<ShipmentCreatedDto> deleteShipment(@PathVariable String externalId,
                                                             @AuthenticationPrincipal Long clientId) throws IOException {
        shipmentService.processEvent(new ShipmentDeletedEvent(clientId,
                externalId, Instant.now()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
