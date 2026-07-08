package com.danijelsudimac.shipmentservice.model.entity;

import com.danijelsudimac.shipmentservice.model.outbox.OutboxEventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    @NotBlank
    private String aggregateType;

    @NotBlank
    private String aggregateId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OutboxEventType eventType;

    @NotBlank
    private String topic;

    @NotNull
    @Column(name = "payload")
    private byte[] payload;
    private String schemaName;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OutboxEventStatus status = OutboxEventStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;
    private Instant publishedAt;
    private Integer retryCount = 0;
}
