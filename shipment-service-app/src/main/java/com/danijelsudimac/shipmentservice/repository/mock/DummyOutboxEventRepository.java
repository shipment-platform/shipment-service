package com.danijelsudimac.shipmentservice.repository.mock;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;

import java.util.Collections;
import java.util.List;

public class DummyOutboxEventRepository implements OutboxEventRepository {
    @Override
    public List<OutboxEvent> lockNextBatch(int limit) {
        return Collections.emptyList();
    }

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return event;
    }
}
