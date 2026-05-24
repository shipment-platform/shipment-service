package com.danijelsudimac.shipmentservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String topic;
    @Column(name = "payload")
    private byte[] payload;
    private String schemaName;
    private Boolean published;
    private Instant createdAt;
    private Instant publishedAt;
}
