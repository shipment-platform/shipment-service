package com.danijelsudimac.shipmentservice.controller;

import com.danijelsudimac.shipmentservice.mapper.ShipmentMapper;
import com.danijelsudimac.shipmentservice.model.dto.*;
import com.danijelsudimac.shipmentservice.model.event.ShipmentCreatedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentDeletedEvent;
import com.danijelsudimac.shipmentservice.model.event.ShipmentUpdatedEvent;
import com.danijelsudimac.shipmentservice.security.ApiKeyAuthenticationToken;
import com.danijelsudimac.shipmentservice.service.IdempotencyService;
import com.danijelsudimac.shipmentservice.service.ShipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IdempotencyService idempotencyService;

    @MockitoBean
    private ShipmentService shipmentService;

    @MockitoBean
    private ShipmentMapper shipmentMapper;

    @Autowired
    private ObjectMapper objectMapper;
    private final ApiKeyAuthenticationToken authenticationPrincipal = new ApiKeyAuthenticationToken("key-3",1L);

    @BeforeEach
    void setup() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationPrincipal);
        SecurityContextHolder.setContext(context);
    }

    // ---------------- CREATE ----------------

    @Test
    @WithMockUser(username = "1")
    void shouldCreateShipment() throws Exception {

        ShipmentCreatedDto dto = mockShipmentCreatedDto();

        when(idempotencyService.shouldDenyRequest("key-2", 1L)).thenReturn(false);

        ShipmentCreatedEvent event = mock(ShipmentCreatedEvent.class);
        when(shipmentMapper.toShipmentCreatedEvent(any(), eq(1L))).thenReturn(event);

        doNothing().when(shipmentService).processEvent(event);

        mockMvc.perform(post("/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldReturnConflictOnDuplicateCreate() throws Exception {

        ShipmentCreatedDto dto = mockShipmentCreatedDto();
        when(idempotencyService.shouldDenyRequest("key-2", 1L)).thenReturn(true);

        mockMvc.perform(post("/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());

        verifyNoInteractions(shipmentService);
    }

    // ---------------- UPDATE ----------------

    @Test
    @WithMockUser(username = "1")
    void shouldUpdateShipment() throws Exception {

        ShipmentUpdatedDto dto = mockShipmentUpdatedDto();

        when(idempotencyService.shouldDenyRequest("key-2", 1L)).thenReturn(false);

        ShipmentUpdatedEvent event = mock(ShipmentUpdatedEvent.class);
        when(shipmentMapper.toShipmentUpdatedEvent(any(), eq(1L))).thenReturn(event);

        mockMvc.perform(patch("/shipments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted());

        verify(shipmentService).processEvent(event);
    }

    // ---------------- DELETE ----------------

    @Test
    void shouldDeleteShipment() throws Exception {

        ApiKeyAuthenticationToken auth = mock(ApiKeyAuthenticationToken.class);
        when(auth.getClientId()).thenReturn(1L);
        when(auth.getCredentials()).thenReturn("key-2");

        when(idempotencyService.shouldDenyRequest("key-2", 1L)).thenReturn(false);

        mockMvc.perform(delete("/shipments/EXT-10001")
                        .requestAttr("authentication", auth))
                .andExpect(status().isAccepted());

        verify(shipmentService).processEvent(any(ShipmentDeletedEvent.class));
    }

     private static ShipmentCreatedDto mockShipmentCreatedDto() {
        return new ShipmentCreatedDto(
                "key-2",                 // idempotencyKey
                 "EXT-10001",                // externalId
                 "TRK-998877",               // trackingNumber
                 "ORD-445566",               // orderId
                 null,                       // status (nullable)
                 "DHL",                      // carrier
                 ShippingMethod.EXPRESS,    // shippingMethod

                 "John Doe",                // recipientName
                 new Address(
                         "Main Street 10",
                         "Novi Sad",
                         "Serbia",
                         "21000",
                         ""
                 ),
                 "+38164111222",            // recipientPhoneNumber
                 "john.doe@example.com",    // recipientEmail

                 "Warehouse BG",            // originName
                 new Address(
                         "Industrial Zone 5",
                         "Belgrade",
                         "Serbia",
                         "11000",
                         ""
                 ),
                 "+38160123456",            // originPhoneNumber
                 "warehouse@example.com",   // originEmail

                 List.of(
                         new Item("ITEM-1", 2, "BOX",2d),
                         new Item("ITEM-2", 1, "BOX",2d)
                 ),

                 null, // estimatedPickup
                 null, // estimatedDelivery
                 null  // createdAt
         );
    }

    private static ShipmentUpdatedDto mockShipmentUpdatedDto() {
        return new ShipmentUpdatedDto(
                "key-2",        // idempotencyKey
                "EXT-10001",       // externalId
                "TRK-998877",      // trackingNumber
                "ORD-445566",      // orderId
                ShipmentStatus.IN_TRANSIT, // status )

                null, // estimatedPickup
                null, // actualPickup
                null, // estimatedDelivery
                null, // actualDelivery
                null  // updatedAt
        );
    }
}