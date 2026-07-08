package com.danijelsudimac.shipmentservice.repository.mock;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.repository.OutboxEventJPARepository;

public class DummyOutboxEventJPARepository implements OutboxEventJPARepository {
    @Override
    public OutboxEvent save(OutboxEvent outboxEvent) {
        return outboxEvent;
    }
}
