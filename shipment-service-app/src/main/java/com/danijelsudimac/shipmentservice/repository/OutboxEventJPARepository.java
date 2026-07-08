package com.danijelsudimac.shipmentservice.repository;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;

public interface OutboxEventJPARepository {
    OutboxEvent save(OutboxEvent outboxEvent);
}
