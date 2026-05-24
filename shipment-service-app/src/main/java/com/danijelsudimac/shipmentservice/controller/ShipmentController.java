package com.danijelsudimac.shipmentservice.controller;

import com.danijelsudimac.shipmentservice.mapper.ShipmentMapper;
import com.danijelsudimac.shipmentservice.model.dto.ShipmentCreatedDto;
import com.danijelsudimac.shipmentservice.model.dto.ShipmentUpdatedDto;
import com.danijelsudimac.shipmentservice.model.event.ShipmentDeletedEvent;
import com.danijelsudimac.shipmentservice.security.ApiKeyAuthenticationToken;
import com.danijelsudimac.shipmentservice.service.IdempotencyService;
import com.danijelsudimac.shipmentservice.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;

@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final IdempotencyService idempotencyService;
    private final ShipmentService shipmentService;
    private final ShipmentMapper shipmentMapper;

    @PostMapping
    public ResponseEntity<ShipmentCreatedDto> createShipment(@Valid @RequestBody ShipmentCreatedDto shipmentCreatedDto,
                                                             Authentication authentication) throws IOException {
        var clientId = ((ApiKeyAuthenticationToken)authentication).getClientId();
        if (idempotencyService.shouldDenyRequest(shipmentCreatedDto.idempotencyKey(), clientId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        var shipmentCreateEvent = shipmentMapper.toShipmentCreatedEvent(shipmentCreatedDto, clientId);
        shipmentService.processEvent
                (shipmentCreateEvent);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(shipmentCreatedDto);
    }

    @PatchMapping("/{externalId}")
    public ResponseEntity<ShipmentCreatedDto> updateShipment(@Valid @RequestBody ShipmentUpdatedDto updateShipmentDto,
                                                             Authentication authentication) throws IOException {
        var clientId = ((ApiKeyAuthenticationToken)authentication).getClientId();
        if (idempotencyService.shouldDenyRequest(updateShipmentDto.idempotencyKey(),clientId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        var shipmentUpdateEvent = shipmentMapper.toShipmentUpdatedEvent(updateShipmentDto, clientId);
        shipmentService.processEvent(shipmentUpdateEvent);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping("/{externalId}")
    public ResponseEntity<ShipmentCreatedDto> deleteShipment(@PathVariable String externalId,
                                                             Authentication authentication) throws IOException {
        var clientId = ((ApiKeyAuthenticationToken)authentication).getClientId();
        if (idempotencyService.shouldDenyRequest((String)authentication.getCredentials(),clientId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        shipmentService.processEvent(new ShipmentDeletedEvent(clientId,
                externalId, Instant.now()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
