package com.danijelsudimac.shipmentservice.repository.mock;

import com.danijelsudimac.shipmentservice.model.entity.OutboxEvent;
import com.danijelsudimac.shipmentservice.repository.OutboxEventRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class DummyOutboxEventRepository implements OutboxEventRepository {
    @Override
    public List<OutboxEvent> lockNextBatch(int limit) {
        return Collections.emptyList();
    }

    @Override
    public void markPublished(Long id, Instant publishedAt) {}

    @Override
    public void markFailed(Long id) {}

    @Override
    public void markRetired(Long id, int retryCount) {}

    @Override
    public int deletePublishedOlderThan(Instant moment) {
        return 0;
    }
}
